from __future__ import annotations

import json
import time
from dataclasses import dataclass
from typing import Any, Dict, Optional

from .session import RunPaths


ALLOWED_STATUS = {"VULN", "SAFE", "NEEDS_DEEPER"}
FINAL_STATUS = {"VULN", "SAFE"}


def _lookup_snippet_quality(run: RunPaths, sha256: str) -> Optional[str]:
    """Lookup slice quality from snippet_index.jsonl."""
    if not run.snippet_index.exists():
        return None
    q = None
    for line in run.snippet_index.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        try:
            obj = json.loads(line)
        except Exception:
            continue
        if obj.get("sha256") == sha256:
            q = obj.get("quality") or q
    return q


@dataclass
class VerifyResult:
    ok: bool
    message: str
    wrote_path: Optional[str] = None


def _has_evidence(e: Dict[str, Any]) -> bool:
    if not isinstance(e, dict):
        return False
    return any(k in e for k in ("snippet_ref", "chain_trace", "sql_proof"))


def append_verify_record(
    run: RunPaths,
    vector: str,
    record: Dict[str, Any],
    *,
    strict: bool = True,
) -> VerifyResult:
    """Append a single verification record to verify_<vector>.jsonl.

    Evidence Contract (fail-closed): each record must contain at least one evidence pointer.
    Strict mode: VULN requires snippet_ref OR a chain_trace with entry_mapping.
    Additionally, in strict mode VULN with snippet_ref requires slice quality=GOOD.
    """
    # basic fields
    cid = record.get("candidate_id")
    status = record.get("status")
    batch_id = record.get("batch_id")
    evidence = record.get("evidence")

    if not cid or not isinstance(cid, str):
        return VerifyResult(False, "missing candidate_id")
    if status not in ALLOWED_STATUS:
        return VerifyResult(False, f"invalid status (allowed={sorted(ALLOWED_STATUS)})")
    if not batch_id or not isinstance(batch_id, str):
        return VerifyResult(False, "missing batch_id")
    if not _has_evidence(evidence):
        return VerifyResult(False, "missing evidence pointer: require snippet_ref or chain_trace or sql_proof")

    # strict checks
    if strict and status == "VULN":
        if "snippet_ref" not in evidence:
            ct = evidence.get("chain_trace")
            if not (isinstance(ct, dict) and ct.get("entry_mapping")):
                return VerifyResult(False, "VULN requires snippet_ref OR chain_trace with entry_mapping")

    # validate snippet ref points to stored file
    if "snippet_ref" in evidence:
        sr = evidence.get("snippet_ref")
        if not isinstance(sr, dict) or not sr.get("sha256"):
            return VerifyResult(False, "snippet_ref missing sha256")
        sha = sr["sha256"]
        p = run.snippets_dir / f"sha256_{sha}.txt"
        if not p.exists():
            return VerifyResult(False, f"snippet file not found for sha256={sha}")

        # quality gate (fail-closed)
        if strict and status == "VULN":
            q = None
            # prefer embedded quality from snippet_ref, fallback to snippet_index lookup
            if isinstance(sr, dict):
                q = sr.get("quality")
            if not q:
                q = _lookup_snippet_quality(run, sha)
            if q != "GOOD":
                return VerifyResult(
                    False,
                    f"VULN requires GOOD slice quality; got {q}. Re-run evidence with --kind method or --kind expand.",
                )

    record = {**record}
    record.setdefault("ts", int(time.time()))

    out = run.verify_dir / f"verify_{vector}.jsonl"
    with out.open("a", encoding="utf-8") as f:
        f.write(json.dumps(record, ensure_ascii=False) + "\n")
    return VerifyResult(True, "ok", out.as_posix())


def load_verified_ids(run: RunPaths, vector: str) -> set[str]:
    # Backward-compatible: returns all candidate_ids that ever appeared in verify_<vector>.jsonl
    return set(load_latest_status_map(run, vector).keys())


def load_latest_status_map(run: RunPaths, vector: str) -> dict[str, str]:
    """Return latest status per candidate_id, derived from verify_<vector>.jsonl order."""
    out: dict[str, str] = {}
    path = run.verify_dir / f"verify_{vector}.jsonl"
    if not path.exists():
        return out
    for line in path.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        try:
            obj = json.loads(line)
        except Exception:
            continue
        cid = obj.get("candidate_id")
        st = (obj.get("status") or "").upper()
        if cid and st in ALLOWED_STATUS:
            out[cid] = st
    return out


def load_finalized_ids(run: RunPaths, vector: str) -> set[str]:
    """Return candidate_ids whose latest status is final (VULN/SAFE)."""
    m = load_latest_status_map(run, vector)
    return {cid for cid, st in m.items() if st in FINAL_STATUS}


def load_needs_deeper_ids(run: RunPaths, vector: str) -> set[str]:
    """Return candidate_ids whose latest status is NEEDS_DEEPER."""
    m = load_latest_status_map(run, vector)
    return {cid for cid, st in m.items() if st == "NEEDS_DEEPER"}
