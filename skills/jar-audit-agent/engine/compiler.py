from __future__ import annotations

import json
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, List, Optional

try:
    from jinja2 import Environment, FileSystemLoader, StrictUndefined
except Exception:  # pragma: no cover
    Environment = None
    FileSystemLoader = None
    StrictUndefined = None

from .session import RunPaths


@dataclass
class CoverageRow:
    vector: str
    emitted: int
    reachable: int
    verified: int
    vuln: int
    safe: int
    needs_deeper: int
    coverage_pct: str


def _coverage_map(rows: List[CoverageRow]) -> Dict[str, Dict[str, Any]]:
    return {r.vector: r.__dict__ for r in rows}


def _read_json(path: Path) -> Dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def _read_jsonl(path: Path) -> List[Dict[str, Any]]:
    if not path.exists():
        return []
    out: List[Dict[str, Any]] = []
    for line in path.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        try:
            out.append(json.loads(line))
        except Exception:
            continue
    return out


def _latest_status_map(records: List[Dict[str, Any]]) -> Dict[str, str]:
    out: Dict[str, str] = {}
    for r in records:
        cid = r.get("candidate_id")
        st = (r.get("status") or "").upper()
        if cid and st in ("VULN", "SAFE", "NEEDS_DEEPER"):
            out[cid] = st
    return out


def _latest_snippet_by_candidate(run: RunPaths) -> Dict[str, Dict[str, Any]]:
    """Return latest snippet_index record per candidate_id."""
    out: Dict[str, Dict[str, Any]] = {}
    if not run.snippet_index.exists():
        return out
    for line in run.snippet_index.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        try:
            obj = json.loads(line)
        except Exception:
            continue
        cid = obj.get("candidate_id")
        if cid:
            out[cid] = obj
    return out


def _remaining_plan(run: RunPaths, vector: str, finalized: set[str], needs_deeper: set[str], limit: int = 5) -> List[Dict[str, Any]]:
    cand_path = run.candidates_dir / f"candidates_{vector}.jsonl"
    if not cand_path.exists():
        return []
    candidates = [json.loads(line) for line in cand_path.read_text(encoding="utf-8").splitlines() if line.strip()]
    sn_by_cid = _latest_snippet_by_candidate(run)

    pool = [c for c in candidates if c.get("candidate_id") and c.get("candidate_id") not in finalized]

    def _rank(c: Dict[str, Any]):
        cid = c.get("candidate_id")
        nd = 0 if (cid in needs_deeper) else 1
        return (nd, c.get("priority", 9999), c.get("reach_depth") or 9999)

    pool.sort(key=_rank)

    out: List[Dict[str, Any]] = []
    for c in pool[:limit]:
        cid = c.get("candidate_id")
        sink_m = c.get("callee_method_name")
        sn = sn_by_cid.get(cid) or {}
        quality = sn.get("quality")
        suggested = sn.get("suggested_next")
        # default recommendation
        rec = {
            "candidate_id": cid,
            "resume": bool(cid in needs_deeper),
            "entry_mapping": c.get("entry_mapping"),
            "entry_auth_hints": c.get("entry_auth_hints") or [],
            "reach_depth": c.get("reach_depth"),
            "sink": {"class": c.get("callee_class_name"), "method": c.get("callee_method_name"), "desc": c.get("callee_method_desc")},
            "evidence_recommend": {
                "anchor": sink_m,
                "kind": "window",
                "auto_fallback": True,
            },
            "last_snippet": {
                "sha256": sn.get("sha256"),
                "quality": quality,
                "warning": sn.get("warning"),
                "suggested_next": suggested,
            } if sn else None,
        }
        if quality and quality != "GOOD":
            rec["evidence_recommend"]["kind"] = "expand"
            rec["evidence_recommend"]["expand_before"] = 120
            rec["evidence_recommend"]["expand_after"] = 120
        out.append(rec)
    return out


def _count_candidates(cand_path: Path) -> tuple[int, int]:
    """Returns (emitted, reachable)."""
    emitted = 0
    reachable = 0
    if not cand_path.exists():
        return 0, 0
    for line in cand_path.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        emitted += 1
        try:
            obj = json.loads(line)
        except Exception:
            continue
        if obj.get("reachable"):
            reachable += 1
    return emitted, reachable


_HTTP_VERBS = {"GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"}


def _extract_http_params_from_snippet(snippet: str) -> List[str]:
    """Best-effort extract HTTP parameter names from an already stored snippet (non-evidence helper)."""
    if not snippet or not isinstance(snippet, str):
        return []
    names: List[str] = []
    seen: set[str] = set()

    # Spring annotations: @RequestParam("x"), @RequestParam(value="x"), @PathVariable("x")
    annos = [
        r"@RequestParam\s*\(\s*\"([^\"]+)\"",
        r"@RequestParam\s*\(\s*name\s*=\s*\"([^\"]+)\"",
        r"@RequestParam\s*\(\s*value\s*=\s*\"([^\"]+)\"",
        r"@PathVariable\s*\(\s*\"([^\"]+)\"",
        r"@PathVariable\s*\(\s*name\s*=\s*\"([^\"]+)\"",
        r"@PathVariable\s*\(\s*value\s*=\s*\"([^\"]+)\"",
    ]
    for pat in annos:
        for m in re.finditer(pat, snippet):
            n = (m.group(1) or "").strip()
            if n and n not in seen:
                seen.add(n)
                names.append(n)

    # Servlet API: request.getParameter("x") / getParameterValues("x")
    for m in re.finditer(r"\bgetParameter(?:Values)?\s*\(\s*\"([^\"]+)\"\s*\)", snippet):
        n = (m.group(1) or "").strip()
        if n and n not in seen:
            seen.add(n)
            names.append(n)

    # Method signature param names (decompiler often keeps them):
    # e.g. `public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request)`
    first_line = snippet.splitlines()[0] if snippet.splitlines() else ""
    m = re.search(r"\((.*)\)", first_line)
    if m:
        seg = (m.group(1) or "").strip()
        if seg:
            for part in seg.split(","):
                p = part.strip()
                if not p:
                    continue
                # last token is usually the var name
                toks = [t for t in re.split(r"\s+", p) if t]
                if len(toks) < 2:
                    continue
                var = toks[-1].strip().strip(")")
                # drop obvious non-user params
                low = var.lower()
                if low in {"request", "response", "session", "model", "map"}:
                    continue
                # drop common flags that are unlikely to be the exploit-carrying input
                if low in {"delete", "remove", "flag", "enabled", "enable", "disabled"}:
                    continue
                # only keep "likely HTTP input" names as a best-effort fallback
                if not re.search(r"(file|path|name|id|url|uri|host|dir|folder|key|param|query)", low):
                    continue
                if var and var not in seen:
                    seen.add(var)
                    names.append(var)

    return names


def _payload_for_param(vector: str | None, param_name: str) -> str:
    v = (vector or "").strip().lower()
    p = (param_name or "").strip().lower()
    if v in {"lfi", "traversal", "file"}:
        # Make the simulated PoC直白：优先给文件类参数一个典型 payload
        if any(k in p for k in ("file", "path", "dir", "folder", "name")):
            return "../../etc/passwd"
    return "<PAYLOAD>"


def _suggest_http_poc(entry_mapping: str | None, *, params: Optional[List[str]] = None, vector: str | None = None) -> Optional[str]:
    """Generate a non-evidence HTTP raw request skeleton from entry_mapping like 'POST /path'."""
    if not entry_mapping or not isinstance(entry_mapping, str):
        return None

    raw = entry_mapping.strip()
    if not raw:
        return None

    # Parse "METHOD /path". If method token is not a real HTTP verb, default to GET.
    parts = raw.split()
    method = ""
    path = ""
    if len(parts) >= 2:
        m0 = parts[0].upper().strip()
        p0 = parts[1].strip()
        if m0 in _HTTP_VERBS and p0:
            method, path = m0, p0
        else:
            # entry_mapping may be like "Request /path" or other labels
            method = "GET"
            # if second token looks like a path, take it; otherwise try the first token
            path = p0 if p0.startswith("/") else (parts[0].strip() if parts[0].strip().startswith("/") else "")
    elif raw.startswith("/"):
        method, path = "GET", raw
    if not method or not path:
        return None

    params = [p for p in (params or []) if isinstance(p, str) and p.strip()]
    params = list(dict.fromkeys(params))  # de-dup, keep order
    max_params = 6
    params = params[:max_params]

    def _qs() -> str:
        if not params:
            return ""
        return "?" + "&".join(f"{p}={_payload_for_param(vector, p)}" for p in params)

    headers_common = (
        "Host: <TARGET>\n"
        "User-Agent: jar-audit-agent\n"
        "Accept: */*\n"
        "Connection: close\n"
        "Authorization: <OPTIONAL>\n"
        "Cookie: <OPTIONAL>\n"
    )

    if method in {"GET", "DELETE", "HEAD", "OPTIONS"}:
        return f"{method} {path}{_qs()} HTTP/1.1\n" + headers_common + "\n"

    # Default to form-urlencoded body for write methods
    body = "<FILL_BODY>\n"
    if params:
        body = "&".join(f"{p}={_payload_for_param(vector, p)}" for p in params) + "\n"
    return (
        f"{method} {path} HTTP/1.1\n"
        + headers_common
        + "Content-Type: application/x-www-form-urlencoded\n"
        + "\n"
        + body
    )


def _status_counts(records: List[Dict[str, Any]]) -> tuple[int, int, int]:
    v = sum(1 for r in records if r.get("status") == "VULN")
    s = sum(1 for r in records if r.get("status") == "SAFE")
    n = sum(1 for r in records if r.get("status") == "NEEDS_DEEPER")
    return v, s, n


def _missing_hint(r: Dict[str, Any]) -> str:
    ev = r.get("evidence") or {}
    hints = []
    if "snippet_ref" not in ev:
        hints.append("snippet_ref")
    ct = ev.get("chain_trace") if isinstance(ev, dict) else None
    if not (isinstance(ct, dict) and ct.get("entry_mapping")):
        hints.append("entry_mapping")
    return "missing: " + ", ".join(hints) if hints else ""


def compile_report(
    run: RunPaths,
    *,
    vectors: List[str],
    template_path: Path,
    out_path: Path,
    strict: bool = False,
) -> Path:
    """Compile audit_report.md from artifacts (freeze/candidates/verify/evidence).

    Fail-closed behavior:
      - If strict=True, raises SystemExit when any vector has verified < emitted.
      - Otherwise, prints INCOMPLETE banner in the report.
    """
    if Environment is None:
        raise RuntimeError(
            "missing dependency: Jinja2 (请使用 `python3 -m pip install -r scripts/requirements.txt` 安装)"
        )

    manifest = _read_json(run.manifest) if run.manifest.exists() else {}
    profile = _read_json(run.profile) if run.profile.exists() else {
        "signals": {},
        "entry_surface": {"spring_controllers": 0, "spring_endpoints": 0, "javaweb_entries": 0},
    }

    coverage_rows: List[CoverageRow] = []
    vectors_payload: List[Dict[str, Any]] = []
    incomplete_vectors: List[str] = []

    for v in vectors:
        freeze_path = run.freeze_dir / f"freeze_{v}.json"
        cand_path = run.candidates_dir / f"candidates_{v}.jsonl"
        verify_path = run.verify_dir / f"verify_{v}.jsonl"

        freeze = _read_json(freeze_path) if freeze_path.exists() else {}
        emitted, reachable = _count_candidates(cand_path)
        records = _read_jsonl(verify_path)
        latest = _latest_status_map(records)
        finalized_ids = {cid for cid, st in latest.items() if st in ("VULN", "SAFE")}
        needs_deeper_ids = {cid for cid, st in latest.items() if st == "NEEDS_DEEPER"}
        verified = len(finalized_ids)
        vuln, safe, needs_deeper = _status_counts(records)

        cov = (verified / emitted * 100.0) if emitted else 0.0
        cov_str = f"{cov:.1f}%"
        coverage_rows.append(
            CoverageRow(
                vector=v,
                emitted=emitted,
                reachable=reachable,
                verified=verified,
                vuln=vuln,
                safe=safe,
                needs_deeper=needs_deeper,
                coverage_pct=cov_str,
            )
        )

        if verified < emitted:
            incomplete_vectors.append(v)

        # Build finding lists
        vuln_records = []
        needs_records = []
        for r in records:
            ev = r.get("evidence") or {}
            sr = ev.get("snippet_ref") if isinstance(ev, dict) else None
            sha = sr.get("sha256") if isinstance(sr, dict) else None
            q = sr.get("quality") if isinstance(sr, dict) else None
            warn = sr.get("warning") if isinstance(sr, dict) else None
            nxt = sr.get("suggested_next") if isinstance(sr, dict) else None
            poc = r.get("poc_http")
            entry_mapping = (ev.get("chain_trace") or {}).get("entry_mapping") if isinstance(ev, dict) else None
            entry_auth_hints = (ev.get("chain_trace") or {}).get("entry_auth_hints") if isinstance(ev, dict) else None
            if not isinstance(entry_auth_hints, list):
                entry_auth_hints = []

            # Best-effort: extract possible HTTP input parameter names from stored snippet (if any).
            params: list[str] = []
            if sha:
                try:
                    snippet_text = (run.snippets_dir / f"sha256_{sha}.txt").read_text(encoding="utf-8")
                    params = _extract_http_params_from_snippet(snippet_text)
                except Exception:
                    params = []

            auth_assessment = "未知"
            if entry_auth_hints:
                auth_assessment = "可能需要鉴权（存在鉴权/权限相关注解线索）"
            else:
                auth_assessment = "未发现鉴权注解线索（仍需运行时确认）"

            controllability_assessment = "未知"
            if params:
                controllability_assessment = "存在 HTTP 入参线索（可能可控）"
            else:
                controllability_assessment = "未从证据片段提取到入参线索（需进一步确认）"

            base = {
                "candidate_id": r.get("candidate_id"),
                "batch_id": r.get("batch_id"),
                "status": r.get("status"),
                "reasoning": r.get("reasoning", ""),
                "caller": r.get("caller", {}),
                "sink": r.get("sink", {}),
                "entry_mapping": entry_mapping,
                "entry_auth_hints": entry_auth_hints,
                "snippet_sha": sha,
                "snippet_quality": q,
                "snippet_warning": warn,
                "snippet_suggested_next": nxt,
                "input_params": params,
                "auth_assessment": auth_assessment,
                "controllability_assessment": controllability_assessment,
                "missing_hint": _missing_hint(r),
                "poc_http": poc,
                "poc_http_suggested": None,
            }
            if not (isinstance(poc, dict) and poc.get("raw")):
                base["poc_http_suggested"] = _suggest_http_poc(base.get("entry_mapping"), params=params, vector=v)
            if r.get("status") == "VULN":
                vuln_records.append(base)
            elif r.get("status") == "NEEDS_DEEPER":
                needs_records.append(base)

        vectors_payload.append(
            {
                "vector": v,
                "freeze": freeze,
                "vuln_records": vuln_records,
                "needs_deeper_records": needs_records,
                "remaining_plan": _remaining_plan(run, v, finalized_ids, needs_deeper_ids, limit=5),
            }
        )

    if strict and incomplete_vectors:
        raise SystemExit(f"INCOMPLETE: verified < emitted for {', '.join(incomplete_vectors)}")

    env = Environment(
        loader=FileSystemLoader(str(template_path.parent)),
        undefined=StrictUndefined,
        autoescape=False,
        trim_blocks=True,
        lstrip_blocks=True,
    )
    tpl = env.get_template(template_path.name)
    coverage_map = _coverage_map(coverage_rows)
    report = tpl.render(
        run_id=manifest.get("run_id", run.root.name),
        db_path=(profile.get("db") or ""),
        profile=profile,
        coverage_rows=coverage_rows,
        coverage_map=coverage_map,
        vectors=vectors_payload,
        incomplete_vectors=incomplete_vectors,
    )

    out_path.write_text(report, encoding="utf-8")
    return out_path
