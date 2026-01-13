#!/usr/bin/env python3
from __future__ import annotations
import os, json, argparse, time
import sys
import shutil
from pathlib import Path

_ROOT = Path(__file__).resolve().parents[1]
if str(_ROOT) not in sys.path:
    sys.path.insert(0, str(_ROOT))

_SKILL_ROOT = _ROOT
_VECTORS_DIR = _SKILL_ROOT / "vectors"
_ASSETS_DIR = _SKILL_ROOT / "assets"
_DEFAULT_RUNS_DIR = _SKILL_ROOT / "runs"

from engine.db import DB
from engine.session import init_run, load_run
from engine.vectors import load_vector
from engine.rulesource import snapshot_rules, load_snapshot_json, sinks_from_dfs, sinks_from_vulnerability
from engine.ids import node_id, node_key
from engine.graph_cache import get_or_build_graph
from engine.reachability import compute_reachability
from engine.evidence import make_slice, store_snippet, latest_snippet_for_candidate, snippet_meta_by_sha
from engine.verify import append_verify_record, load_finalized_ids, load_needs_deeper_ids, load_latest_status_map
from engine.compiler import compile_report
from engine.sql import SQL
from types import SimpleNamespace

def _extract_full_class_code_from_mcp_tool_result(path: str) -> str:
    """
    Claude MCP tool-result files vary by wrapper shape. We try a few common forms:
      1) JSON dict with {"text": "<json-string-or-code>"}
      2) JSON list with first element {"text": "..."}
      3) "text" itself is a JSON string containing {"fullClassCode": "..."}
      4) "text" is a JSON string containing {"methodDesc": "...", "fullClassCode": "..."}
    Returns fullClassCode (string) or raises SystemExit.
    """
    raw = Path(path).read_text(encoding="utf-8", errors="ignore").strip()
    if not raw:
        raise SystemExit("empty mcp tool-result file")
    try:
        outer = json.loads(raw)
    except Exception:
        # not JSON; treat as plain code
        return raw

    text = None
    if isinstance(outer, dict):
        text = outer.get("text")
    elif isinstance(outer, list) and outer:
        if isinstance(outer[0], dict):
            text = outer[0].get("text")
        else:
            text = outer[0]

    if text is None:
        # if outer is already the code payload
        if isinstance(outer, dict) and ("fullClassCode" in outer):
            return str(outer.get("fullClassCode") or "")
        raise SystemExit("unrecognized mcp tool-result format: missing text/fullClassCode")

    if isinstance(text, dict) and ("fullClassCode" in text):
        return str(text.get("fullClassCode") or "")

    if not isinstance(text, str):
        text = str(text)

    # text may itself be JSON string
    s = text.strip()
    if s.startswith("{") or s.startswith("["):
        try:
            inner = json.loads(s)
            if isinstance(inner, dict) and ("fullClassCode" in inner):
                return str(inner.get("fullClassCode") or "")
            # some wrappers: {"text":"{\"methodDesc\":\"\",\"fullClassCode\":\"...\"}"}
            if isinstance(inner, list) and inner and isinstance(inner[0], dict) and ("fullClassCode" in inner[0]):
                return str(inner[0].get("fullClassCode") or "")
        except Exception:
            pass

    # last resort: treat text as full class code
    return s

def _env_or(arg: str | None, env_key: str) -> str:
    if arg:
        return arg
    v = os.environ.get(env_key)
    if not v:
        raise SystemExit(f"missing {env_key} (or pass flag).")
    return v

def _detect_python_cmd() -> str:
    """
    自动检测可用的 Python 命令。
    优先尝试 python，如果不存在则尝试 python3。
    返回第一个可用的命令名。
    """
    for cmd in ["python", "python3"]:
        if shutil.which(cmd):
            return cmd
    # 如果都找不到，默认返回 python3（脚本本身用 python3 运行）
    return "python3"

def cmd_init(args):
    p = init_run(args.out)
    run_id = p.root.name
    python_cmd = _detect_python_cmd()
    print(p.root.as_posix())
    print(f"[*] Session initialized successfully.")
    print(f"[*] Run ID: {run_id}")
    print("=" * 60)
    print("[!] IMPORTANT: Copy the FULL command below (including --run parameter)")
    print("=" * 60)
    # [核心优化] 使用更显眼的格式，确保 AI 能看到完整命令
    next_cmd = f"{python_cmd} scripts/cli.py freeze --vector rce --run {run_id}"
    print(f"\n{next_cmd}\n")
    print("=" * 60)
    print(f"[*] Python command detected: {python_cmd}")
    print(f"[*] Use this Python command for ALL subsequent commands")
    print("=" * 60)

def _locate_db_by_cwd(cwd: str | None) -> str | None:
    base = Path(cwd or os.getcwd()).resolve()
    # [优化] 1. 优先检查当前目录 (适配 Windows/start.bat)
    c1 = base / "jar-analyzer.db"
    if c1.exists():
        return c1.as_posix()
    # [优化] 2. 其次才检查 lib 目录
    c2 = base / "lib" / "jar-analyzer.db"
    if c2.exists():
        return c2.as_posix()
    return None

def cmd_preflight(args):
    """
    Minimal fail-closed environment verification for skills:
    1) Locate DB by cwd (jar-analyzer.db or lib/jar-analyzer.db, in that order)
    2) Schema lock (key tables exist)
    Note: evidence channel is MCP-only; HTTP is not used.
    """
    db_path = args.db or os.environ.get("JA_DB") or _locate_db_by_cwd(args.cwd)
    if not db_path or not Path(db_path).exists():
        out = {
            "ok": False,
            "stage": "db",
            "cwd": args.cwd,
            "checked": [
                "jar-analyzer.db",
                "lib/jar-analyzer.db",
            ],
            "message": "DB not found. Please analyze target in GUI first (click Start), then re-run.",
        }
        print(json.dumps(out, ensure_ascii=False, indent=2))
        raise SystemExit(2)

    lock = SQL(db_path).schema_lock()
    if not lock.ok:
        out = {
            "ok": False,
            "stage": "schema",
            "db": db_path,
            "missing_tables": lock.missing,
            "message": "DB schema not ready. Please run analysis in GUI (click Start) then re-run.",
        }
        print(json.dumps(out, ensure_ascii=False, indent=2))
        raise SystemExit(2)

    code_api = {
        "ok": None,
        "mode": "mcp_only",
        "tools": ["get_code_cfr", "get_code_fernflower"],
        "note": "evidence requires --code-json-file (MCP output) or --code-text-file",
    }

    out = {
        "ok": True,
        "db": db_path,
        "schema_ok": True,
        "code_api": code_api,
        "next": [
            "profile --cwd <jar-analyzer>",
            "freeze --vector <v> --cwd <jar-analyzer>",
            "graph -> reach -> next -> evidence -> submit -> report",
        ],
    }
    print(json.dumps(out, ensure_ascii=False, indent=2))

def cmd_profile(args):
    run = load_run(args.run)
    db_path = _env_or(args.db, "JA_DB")
    db = DB(db_path)

    entry = db.q1("SELECT COUNT(*) AS spring_endpoints FROM spring_method_table") or {}
    ctrl  = db.q1("SELECT COUNT(*) AS spring_controllers FROM spring_controller_table") or {}
    javaweb = db.q1("SELECT COUNT(*) AS javaweb_entries FROM java_web_table") or {}

    # Prefer structured analysis outputs (tables) as primary signals (less false positives than class-name heuristics).
    spring_endpoints = int(entry.get("spring_endpoints", 0) or 0)
    spring_controllers = int(ctrl.get("spring_controllers", 0) or 0)
    javaweb_entries = int(javaweb.get("javaweb_entries", 0) or 0)

    # Component hints from jar_table (more stable than class_name prefixes; still heuristic).
    jar_names = [str(r.get("jar_name") or "") for r in (db.q("SELECT jar_name FROM jar_table") or [])]
    jar_names_l = [j.lower() for j in jar_names if j]

    def _jar_hint_count(substrs: list[str]) -> int:
        n = 0
        for j in jar_names_l:
            if any(s in j for s in substrs):
                n += 1
        return n

    sig = {
        # structured tables (primary)
        "spring_endpoints_table": spring_endpoints,
        "spring_controllers_table": spring_controllers,
        "javaweb_entries_table": javaweb_entries,
        # jar_table hints (secondary)
        "jar_hint_spring_boot": _jar_hint_count(["spring-boot"]),
        "jar_hint_spring_webmvc": _jar_hint_count(["spring-webmvc"]),
        "jar_hint_spring": _jar_hint_count(["spring-"]),
        "jar_hint_mybatis": _jar_hint_count(["mybatis", "ibatis"]),
        "jar_hint_shiro": _jar_hint_count(["shiro"]),
        "jar_hint_tomcat": _jar_hint_count(["tomcat"]),
        "jar_hint_jetty": _jar_hint_count(["jetty"]),
        "jar_hint_undertow": _jar_hint_count(["undertow"]),
    }

    sample = db.q("SELECT class_name FROM spring_controller_table LIMIT 20")
    prefix = None
    if sample:
        s0 = sample[0]["class_name"]
        parts = s0.split("/")
        prefix = "/".join(parts[:2]) + "/" if len(parts) >= 2 else parts[0] + "/"

    profile = {
        "db": db_path,
        "signals": sig,
        "entry_surface": {
            "spring_controllers": spring_controllers,
            "spring_endpoints": spring_endpoints,
            "javaweb_entries": javaweb_entries,
        },
        "app_prefix_guess": prefix,
        "notes": [
            "signals are hints, not proof of absence",
            "anno_table lacks method_desc; verify overloads in decompile"
        ]
    }
    run.profile.write_text(json.dumps(profile, ensure_ascii=False, indent=2), encoding="utf-8")
    print(run.profile.as_posix())

    # snapshot jar-analyzer rule files if present (vulnerability.yaml, dfs-sink.json)
    try:
        snapshot_rules(run.root, cwd=args.cwd)
    except Exception:
        pass

def cmd_freeze(args):
    run = load_run(args.run)
    db_path = _env_or(args.db, "JA_DB")
    db = DB(db_path)

    vector_path = _VECTORS_DIR / f"{args.vector}.yaml"
    cfg = load_vector(vector_path.as_posix())

    # Expand sinks to exact descs (NO LIKE).
    # resolve sinks: vector sinks + (optional) jar-analyzer rule sources (vulnerability.yaml / dfs-sink.json)
    try:
        snapshot_rules(run.root, cwd=args.cwd)
    except Exception:
        pass

    sinks_raw: list[dict] = []

    rules_dir = run.root / "rules"
    dfs_parsed = None
    vul_parsed = None
    if (rules_dir / "dfs_sink.parsed.json").exists():
        dfs_parsed = load_snapshot_json((rules_dir / "dfs_sink.parsed.json").as_posix())
    if (rules_dir / "vulnerability.parsed.json").exists():
        vul_parsed = load_snapshot_json((rules_dir / "vulnerability.parsed.json").as_posix())

    from_rules = 0
    if dfs_parsed and cfg.rule_sources.dfs_sink_box:
        out = sinks_from_dfs(dfs_parsed, cfg.rule_sources.dfs_sink_box)
        sinks_raw.extend(out)
        from_rules += len(out)
    if vul_parsed and cfg.rule_sources.vulnerability_keys:
        out = sinks_from_vulnerability(vul_parsed, cfg.rule_sources.vulnerability_keys)
        sinks_raw.extend(out)
        from_rules += len(out)

    # Allow vector-defined sinks when rules do not cover a category (e.g. view name injection).
    from_vector = 0
    if cfg.sinks:
        for s in cfg.sinks:
            sinks_raw.append({"class": s.class_name, "method": s.method_name, "desc": s.method_desc, "level": s.level, "source": "vector"})
            from_vector += 1

    if not sinks_raw:
        raise SystemExit(
            "no sinks configured. "
            "Provide discovery.rule_sources (and run profile --cwd to snapshot rules) or define discovery.sinks in the vector."
        )

    # normalize + de-dup on (class, method, desc)
    sinks_norm = []
    seen_sink = set()
    for s in sinks_raw:
        key = (s.get("class"), s.get("method"), s.get("desc"))
        if key in seen_sink:
            continue
        seen_sink.add(key)
        sinks_norm.append(s)

    # expand desc=null by querying actual descs from DB
    sinks_expanded: list[dict] = []
    sink_resolution = {"from_rules": from_rules, "from_vector": from_vector, "unique": len(sinks_norm), "expanded": 0, "misses": []}
    for s in sinks_norm:
        if s.get("desc"):
            sinks_expanded.append(s)
        else:
            rows = db.q(
                """SELECT DISTINCT callee_method_desc AS desc
                   FROM method_call_table
                   WHERE callee_class_name = ? AND callee_method_name = ?""",
                (s["class"], s["method"]),
            )
            if not rows:
                sink_resolution["misses"].append({"class": s["class"], "method": s["method"], "reason": "no_desc_in_db"})
                continue
            for r in rows:
                sinks_expanded.append({**s, "desc": r["desc"]})
                sink_resolution["expanded"] += 1

    callers = []
    # Sink hit summary (for agent): how many distinct caller-sites matched each sink signature.
    sink_hits_by_method: dict[str, int] = {}
    sink_hits_by_sig_nonzero: list[dict] = []
    sink_sig_total = 0
    sink_sig_nonzero = 0
    sink_sig_top_cap = 50
    for s in sinks_expanded:
        sink_sig_total += 1
        rows = db.q(
            """SELECT DISTINCT
                  caller_jar_id, caller_class_name, caller_method_name, caller_method_desc,
                  callee_class_name, callee_method_name, callee_method_desc, callee_jar_id
                FROM method_call_table
                WHERE callee_class_name = ? AND callee_method_name = ? AND callee_method_desc = ?""",
            (s["class"], s["method"], s["desc"]),
        )
        hit_n = len(rows or [])
        key_m = f"{s.get('class')}::{s.get('method')}"
        sink_hits_by_method[key_m] = sink_hits_by_method.get(key_m, 0) + hit_n
        if hit_n > 0:
            sink_sig_nonzero += 1
            if len(sink_hits_by_sig_nonzero) < sink_sig_top_cap:
                sink_hits_by_sig_nonzero.append({"class": s.get("class"), "method": s.get("method"), "desc": s.get("desc"), "hits": hit_n})
        callers.extend(rows)

    seen = set()
    uniq = []
    for r in callers:
        nid = node_id(int(r.get("caller_jar_id") or 0), r["caller_class_name"], r["caller_method_name"], r.get("caller_method_desc"))
        if nid in seen:
            continue
        seen.add(nid)
        uniq.append({**r, "candidate_id": nid})

    uniq.sort(key=lambda x: (x["caller_class_name"], x["caller_method_name"], x["caller_method_desc"]))
    total = len(seen)
    emitted = len(uniq)

    # reduction: exclude obvious framework/lib prefixes
    red_before = emitted
    if cfg.reduce.exclude_class_prefixes:
        def _is_excluded(cn: str) -> bool:
            return any(cn.startswith(p) for p in cfg.reduce.exclude_class_prefixes)
        uniq = [u for u in uniq if not _is_excluded(u.get("caller_class_name", ""))]
    red_after = len(uniq)

    # prefer app prefix if available
    app_prefix = None
    try:
        app_prefix = json.loads(run.profile.read_text(encoding="utf-8")).get("app_prefix_guess")
    except Exception:
        app_prefix = None

    def _rank(u: dict) -> tuple:
        in_app = 0
        if cfg.reduce.prefer_app_prefix and app_prefix:
            in_app = 0 if u.get("caller_class_name", "").startswith(app_prefix) else 1
        return (in_app, u.get("caller_class_name", ""), u.get("caller_method_name", ""))

    uniq.sort(key=_rank)
    red_final = len(uniq)
    cap_applied = False
    if red_final > cfg.reduce.max_candidates and not args.no_cap:
        uniq = uniq[: cfg.reduce.max_candidates]
        emitted = len(uniq)
        cap_applied = True

    freeze = {
        "vector": cfg.id,
        "name": cfg.name,
        "db": db_path,
        "enumeration": {
            "rule": "direct callers of exact sink signatures (class+method+desc) via method_call_table",
            "sink_priority": (["rules"] if from_rules else []) + (["vector"] if from_vector else []),
            "sinks_expanded_count": len(sinks_expanded),
            "candidates_total": total,
            "candidates_emitted": emitted,
            "cap_applied": cap_applied,
            "rule_sources": getattr(cfg.rule_sources, "__dict__", {}),
            "sink_resolution": sink_resolution,
            "sink_hits": {
                "by_method": sink_hits_by_method,
                "by_signature_top": sink_hits_by_sig_nonzero,
                "total_signatures": sink_sig_total,
                "nonzero_signatures": sink_sig_nonzero,
                "truncated": bool(sink_sig_nonzero > len(sink_hits_by_sig_nonzero)),
            },
            "reduction": {"before": red_before, "after_exclusions": red_after, "final": red_final, "app_prefix": app_prefix},
        },
        "fact_contract": "candidate_id = jar_id::class::method::sha256(desc)[0:12]",
        "next": [
            "Step2 will add reachability filter: entry -> candidate shortest path (max_depth)",
            "Step3 will add evidence fetch + slicer + verify.jsonl"
        ]
    }

    out_freeze = run.freeze_dir / f"freeze_{cfg.id}.json"
    out_cand = run.candidates_dir / f"candidates_{cfg.id}.jsonl"
    out_freeze.write_text(json.dumps(freeze, ensure_ascii=False, indent=2), encoding="utf-8")

    out_cand.unlink(missing_ok=True)
    with out_cand.open("w", encoding="utf-8") as f:
        for r in uniq:
            f.write(json.dumps(r, ensure_ascii=False) + "\n")

    print(out_freeze.as_posix())
    print(out_cand.as_posix())



def cmd_graph(args):
    run = load_run(args.run)
    db_path = _env_or(args.db, "JA_DB")
    cache_dir = run.graph_cache_dir
    g, hit = get_or_build_graph(db_path, cache_dir)
    print(json.dumps({
        "cache_hit": hit,
        "nodes": len(g.nodes),
        "edges": sum(len(v) for v in g.adj),
        "cache_dir": cache_dir.as_posix(),
    }, ensure_ascii=False, indent=2))


def cmd_reach(args):
    run = load_run(args.run)
    db_path = _env_or(args.db, "JA_DB")
    db = DB(db_path)

    vector_path = _VECTORS_DIR / f"{args.vector}.yaml"
    cfg = load_vector(vector_path.as_posix())

    cache_dir = run.graph_cache_dir
    graph, cache_hit = get_or_build_graph(db_path, cache_dir)

    # entries: method-level endpoints (Spring) + Servlet entries (best-effort)
    entry_rows = db.q("""SELECT jar_id, class_name, method_name, method_desc, restful_type, path FROM spring_method_table""")

    # auth hints (best-effort): anno_table lacks method_desc, so we only attach hints by (jar_id, class_name, method_name)
    anno_rows = db.q("""SELECT jar_id, class_name, method_name, anno_name FROM anno_table""")
    auth_hints: dict[tuple[int, str, str], list[str]] = {}
    def _is_auth_anno(a: str) -> bool:
        x = (a or "").lower()
        return any(k in x for k in [
            "preauthorize", "postauthorize", "rolesallowed", "permitall", "denyall",
            "requiresroles", "requirespermissions", "requiresauthentication",
            "secured", "authentication", "authorization", "anonymous",
        ])
    for ar in anno_rows:
        a = ar.get("anno_name")
        if not a or not _is_auth_anno(str(a)):
            continue
        key = (int(ar["jar_id"]), ar["class_name"], ar["method_name"])
        auth_hints.setdefault(key, [])
        if str(a) not in auth_hints[key]:
            auth_hints[key].append(str(a))

    entries: dict[str, str] = {}
    entry_hint_by_node: dict[str, list[str]] = {}

    # Spring entries
    for r in entry_rows:
        jid = int(r["jar_id"])
        cn = r["class_name"]
        mn = r["method_name"]
        md = r["method_desc"]
        k = node_key(jid, cn, mn, md)
        hints = auth_hints.get((jid, cn, mn), [])
        # optional filter: only keep entries without auth-related annotation hints
        if getattr(args, "entry_no_auth_hints", False):
            if hints:
                continue
        entries[k] = f"{r['restful_type']} {r['path']}"
        entry_hint_by_node[k] = hints

    # JavaWeb entries (servlet/filter).
    #
    # NOTE: Some DBs may have java_web_table empty (0 rows) even when @WebServlet exists.
    # We therefore treat @WebServlet / extends HttpServlet as a fallback source of servlet classes,
    # then map those classes to common entry methods (service/doGet/doPost/...).
    servlet_rows = db.q("""SELECT jar_id, class_name, type_name FROM java_web_table WHERE lower(type_name) = 'servlet'""")
    servlet_methods = ("service", "doGet", "doPost", "doPut", "doDelete", "doHead", "doOptions")

    servlet_classes: list[tuple[int, str]] = []
    seen_servlet_classes: set[tuple[int, str]] = set()

    for sr in servlet_rows:
        try:
            jid = int(sr.get("jar_id") or 0)
        except Exception:
            jid = 0
        cn = sr.get("class_name")
        if not cn:
            continue
        key = (jid, cn)
        if key not in seen_servlet_classes:
            seen_servlet_classes.add(key)
            servlet_classes.append(key)

    # Fallback 1: @WebServlet annotated classes (anno_table may store dotted or slashed names).
    webservlet_rows = db.q(
        """SELECT DISTINCT jar_id, class_name
           FROM anno_table
           WHERE lower(anno_name) LIKE '%webservlet%'"""
    )
    for wr in webservlet_rows:
        try:
            jid = int(wr.get("jar_id") or 0)
        except Exception:
            jid = 0
        cn = wr.get("class_name")
        if not cn:
            continue
        key = (jid, cn)
        if key not in seen_servlet_classes:
            seen_servlet_classes.add(key)
            servlet_classes.append(key)

    # Fallback 2: classes extending HttpServlet (covers servlet stacks without @WebServlet and without java_web_table).
    httpservlet_rows = db.q(
        """SELECT DISTINCT jar_id, class_name
           FROM class_table
           WHERE super_class_name IN (
             'javax/servlet/http/HttpServlet',
             'jakarta/servlet/http/HttpServlet',
             'javax.servlet.http.HttpServlet',
             'jakarta.servlet.http.HttpServlet',
             'org/apache/jasper/runtime/HttpJspBase',
             'org.apache.jasper.runtime.HttpJspBase'
           )
           OR lower(super_class_name) LIKE '%httpjspbase%'"""
    )
    for hr in httpservlet_rows:
        try:
            jid = int(hr.get("jar_id") or 0)
        except Exception:
            jid = 0
        cn = hr.get("class_name")
        if not cn:
            continue
        key = (jid, cn)
        if key not in seen_servlet_classes:
            seen_servlet_classes.add(key)
            servlet_classes.append(key)

    for (jid, cn) in servlet_classes:

        # Best-effort mapping: try to find URL pattern-like strings inside the servlet class.
        # For @WebServlet({"/x"}), the "/x" often appears in string_table.
        paths = []
        for rr in db.q(
            """SELECT DISTINCT value
               FROM string_table
               WHERE jar_id = ? AND class_name = ?
                 AND value LIKE '/%' AND length(value) <= 120""",
            (jid, cn),
        ):
            v = (rr.get("value") or "").strip()
            if v and " " not in v and v not in paths:
                paths.append(v)
            if len(paths) >= 3:
                break
        mapping = f"SERVLET {paths[0]}" if paths else f"SERVLET {cn}"

        mrows = db.q(
            f"""SELECT method_name, method_desc
                FROM method_table
                WHERE jar_id = ? AND class_name = ?
                  AND method_name IN ({",".join(["?"] * len(servlet_methods))})""",
            (jid, cn, *servlet_methods),
        )
        for mr in mrows:
            mn = mr.get("method_name")
            md = mr.get("method_desc")
            if not mn:
                continue
            k = node_key(jid, cn, str(mn), (md or ""))
            hints = auth_hints.get((jid, cn, str(mn)), [])
            if getattr(args, "entry_no_auth_hints", False):
                if hints:
                    continue
            # avoid overwriting Spring mapping if same node_key exists
            entries.setdefault(k, mapping)
            entry_hint_by_node.setdefault(k, hints)

    # Filter entries: map to doFilter methods (best-effort).
    #
    # NOTE: Some DBs may have java_web_table empty (0 rows) even when @WebFilter or Filter impl exists.
    # We therefore treat @WebFilter / implements Filter as fallback sources of filter classes.
    filter_rows = db.q("""SELECT jar_id, class_name, type_name FROM java_web_table WHERE lower(type_name) = 'filter'""")

    filter_classes: list[tuple[int, str]] = []
    seen_filter_classes: set[tuple[int, str]] = set()

    for fr0 in filter_rows:
        try:
            jid = int(fr0.get("jar_id") or 0)
        except Exception:
            jid = 0
        cn = fr0.get("class_name")
        if not cn:
            continue
        key = (jid, cn)
        if key not in seen_filter_classes:
            seen_filter_classes.add(key)
            filter_classes.append(key)

    # Fallback 1: @WebFilter annotated classes.
    webfilter_rows = db.q(
        """SELECT DISTINCT jar_id, class_name
           FROM anno_table
           WHERE lower(anno_name) LIKE '%webfilter%'"""
    )
    for wr in webfilter_rows:
        try:
            jid = int(wr.get("jar_id") or 0)
        except Exception:
            jid = 0
        cn = wr.get("class_name")
        if not cn:
            continue
        key = (jid, cn)
        if key not in seen_filter_classes:
            seen_filter_classes.add(key)
            filter_classes.append(key)

    # Fallback 2: implements Filter (interface_table stores (interface_name, class_name)).
    filter_impl_rows = db.q(
        """SELECT DISTINCT jar_id, class_name
           FROM interface_table
           WHERE lower(interface_name) IN (
             'javax/servlet/filter',
             'jakarta/servlet/filter',
             'javax.servlet.filter',
             'jakarta.servlet.filter'
           )
           OR lower(interface_name) LIKE '%servlet%filter%'"""
    )
    for ir in filter_impl_rows:
        try:
            jid = int(ir.get("jar_id") or 0)
        except Exception:
            jid = 0
        cn = ir.get("class_name")
        if not cn:
            continue
        key = (jid, cn)
        if key not in seen_filter_classes:
            seen_filter_classes.add(key)
            filter_classes.append(key)

    for (jid, cn) in filter_classes:
        paths = []
        for rr in db.q(
            """SELECT DISTINCT value
               FROM string_table
               WHERE jar_id = ? AND class_name = ?
                 AND value LIKE '/%' AND length(value) <= 120""",
            (jid, cn),
        ):
            v = (rr.get("value") or "").strip()
            if v and " " not in v and v not in paths:
                paths.append(v)
            if len(paths) >= 3:
                break
        mapping = f"FILTER {paths[0]}" if paths else f"FILTER {cn}"

        mrows = db.q(
            """SELECT method_name, method_desc
                FROM method_table
                WHERE jar_id = ? AND class_name = ? AND method_name = 'doFilter'""",
            (jid, cn),
        )
        for mr in mrows:
            mn = mr.get("method_name")
            md = mr.get("method_desc")
            if not mn:
                continue
            k = node_key(jid, cn, str(mn), (md or ""))
            hints = auth_hints.get((jid, cn, str(mn)), [])
            if getattr(args, "entry_no_auth_hints", False):
                if hints:
                    continue
            entries.setdefault(k, mapping)
            entry_hint_by_node.setdefault(k, hints)

    # Listener entries (best-effort): java_web_table + @WebListener + implements *Listener.
    # These are container lifecycle/event callbacks, not necessarily HTTP endpoints, but can be useful for reachability.
    listener_rows = db.q("""SELECT jar_id, class_name, type_name FROM java_web_table WHERE lower(type_name) = 'listener'""")
    listener_classes: list[tuple[int, str]] = []
    seen_listener_classes: set[tuple[int, str]] = set()

    for lr0 in listener_rows:
        try:
            jid = int(lr0.get("jar_id") or 0)
        except Exception:
            jid = 0
        cn = lr0.get("class_name")
        if not cn:
            continue
        key = (jid, cn)
        if key not in seen_listener_classes:
            seen_listener_classes.add(key)
            listener_classes.append(key)

    # Fallback 1: @WebListener annotated classes.
    weblistener_rows = db.q(
        """SELECT DISTINCT jar_id, class_name
           FROM anno_table
           WHERE lower(anno_name) LIKE '%weblistener%'"""
    )
    for wr in weblistener_rows:
        try:
            jid = int(wr.get("jar_id") or 0)
        except Exception:
            jid = 0
        cn = wr.get("class_name")
        if not cn:
            continue
        key = (jid, cn)
        if key not in seen_listener_classes:
            seen_listener_classes.add(key)
            listener_classes.append(key)

    # Fallback 2: implements common servlet listeners.
    # interface_table may store dotted or slashed names; we use a broad but servlet-scoped matcher.
    listener_impl_rows = db.q(
        """SELECT DISTINCT jar_id, class_name
           FROM interface_table
           WHERE lower(interface_name) LIKE '%servlet%listener%'
              OR lower(interface_name) LIKE '%servletcontextlistener%'
              OR lower(interface_name) LIKE '%servletrequestlistener%'
              OR lower(interface_name) LIKE '%httpsessionlistener%'"""
    )
    for ir in listener_impl_rows:
        try:
            jid = int(ir.get("jar_id") or 0)
        except Exception:
            jid = 0
        cn = ir.get("class_name")
        if not cn:
            continue
        key = (jid, cn)
        if key not in seen_listener_classes:
            seen_listener_classes.add(key)
            listener_classes.append(key)

    listener_methods = (
        "contextInitialized", "contextDestroyed",
        "requestInitialized", "requestDestroyed",
        "sessionCreated", "sessionDestroyed",
    )
    for (jid, cn) in listener_classes:
        mapping = f"LISTENER {cn}"
        mrows = db.q(
            f"""SELECT method_name, method_desc
                FROM method_table
                WHERE jar_id = ? AND class_name = ?
                  AND method_name IN ({",".join(["?"] * len(listener_methods))})""",
            (jid, cn, *listener_methods),
        )
        for mr in mrows:
            mn = mr.get("method_name")
            md = mr.get("method_desc")
            if not mn:
                continue
            k = node_key(jid, cn, str(mn), (md or ""))
            hints = auth_hints.get((jid, cn, str(mn)), [])
            if getattr(args, "entry_no_auth_hints", False):
                if hints:
                    continue
            entries.setdefault(k, mapping)
            entry_hint_by_node.setdefault(k, hints)

    # JAX-RS entries (best-effort, no resource file parsing).
    # anno_table does not include annotation values (e.g., @Path("/x")), so entry_mapping may degrade to class/method.
    # We still add these as entries so reachability can be computed for non-Spring REST stacks.
    jaxrs_method_verb_map = {
        "javax.ws.rs.GET": "GET",
        "javax.ws.rs.POST": "POST",
        "javax.ws.rs.PUT": "PUT",
        "javax.ws.rs.DELETE": "DELETE",
        "javax.ws.rs.HEAD": "HEAD",
        "javax.ws.rs.OPTIONS": "OPTIONS",
        "javax.ws.rs.PATCH": "PATCH",
        "jakarta.ws.rs.GET": "GET",
        "jakarta.ws.rs.POST": "POST",
        "jakarta.ws.rs.PUT": "PUT",
        "jakarta.ws.rs.DELETE": "DELETE",
        "jakarta.ws.rs.HEAD": "HEAD",
        "jakarta.ws.rs.OPTIONS": "OPTIONS",
        "jakarta.ws.rs.PATCH": "PATCH",
    }
    # Normalize anno_name to a comparable dotted form (handles both slash and dot).
    def _anno_norm(a: str) -> str:
        return (a or "").replace("/", ".").strip()

    # Find methods annotated with JAX-RS HTTP method annotations.
    jaxrs_anno_rows = db.q(
        """SELECT jar_id, class_name, method_name, anno_name
           FROM anno_table
           WHERE lower(anno_name) LIKE '%ws%rs%get%'
              OR lower(anno_name) LIKE '%ws%rs%post%'
              OR lower(anno_name) LIKE '%ws%rs%put%'
              OR lower(anno_name) LIKE '%ws%rs%delete%'
              OR lower(anno_name) LIKE '%ws%rs%head%'
              OR lower(anno_name) LIKE '%ws%rs%options%'
              OR lower(anno_name) LIKE '%ws%rs%patch%'"""
    )
    jaxrs_methods: dict[tuple[int, str, str], str] = {}
    for ar0 in jaxrs_anno_rows:
        a = _anno_norm(str(ar0.get("anno_name") or ""))
        verb = jaxrs_method_verb_map.get(a)
        if not verb:
            continue
        try:
            jid = int(ar0.get("jar_id") or 0)
        except Exception:
            jid = 0
        cn = ar0.get("class_name")
        mn = ar0.get("method_name")
        if not cn or not mn:
            continue
        jaxrs_methods[(jid, cn, mn)] = verb

    for (jid, cn, mn), verb in jaxrs_methods.items():
        # Best-effort path: try to find a single "/xxx" string inside this method or class.
        paths = []
        for rr in db.q(
            """SELECT DISTINCT value
               FROM string_table
               WHERE jar_id = ? AND class_name = ? AND method_name = ?
                 AND value LIKE '/%' AND length(value) <= 120""",
            (jid, cn, mn),
        ):
            v = (rr.get("value") or "").strip()
            if v and " " not in v and v not in paths:
                paths.append(v)
            if len(paths) >= 1:
                break
        if not paths:
            for rr in db.q(
                """SELECT DISTINCT value
                   FROM string_table
                   WHERE jar_id = ? AND class_name = ?
                     AND value LIKE '/%' AND length(value) <= 120""",
                (jid, cn),
            ):
                v = (rr.get("value") or "").strip()
                if v and " " not in v and v not in paths:
                    paths.append(v)
                if len(paths) >= 1:
                    break
        mapping = f"{verb} {paths[0]}" if paths else f"JAXRS {cn}::{mn}"

        # anno_table lacks method_desc → we enumerate all overloads with same method_name.
        for mr in db.q(
            """SELECT method_desc
               FROM method_table
               WHERE jar_id = ? AND class_name = ? AND method_name = ?""",
            (jid, cn, mn),
        ):
            md = mr.get("method_desc") or ""
            k = node_key(jid, cn, mn, md)
            hints = auth_hints.get((jid, cn, mn), [])
            if getattr(args, "entry_no_auth_hints", False):
                if hints:
                    continue
            entries.setdefault(k, mapping)
            entry_hint_by_node.setdefault(k, hints)

    # Struts2 entries (best-effort): Action interface implementations → execute/doDefault.
    # Mapping is typically in struts.xml (resource), which we do not read; we still add entry nodes for reachability.
    struts_classes = db.q(
        """SELECT DISTINCT jar_id, class_name
           FROM interface_table
           WHERE lower(interface_name) LIKE '%xwork2%action%'"""
    )
    struts_entry_methods = ("execute", "doDefault")
    for sc in struts_classes:
        try:
            jid = int(sc.get("jar_id") or 0)
        except Exception:
            jid = 0
        cn = sc.get("class_name")
        if not cn:
            continue
        for mr in db.q(
            f"""SELECT method_name, method_desc
                FROM method_table
                WHERE jar_id = ? AND class_name = ?
                  AND method_name IN ({",".join(["?"] * len(struts_entry_methods))})""",
            (jid, cn, *struts_entry_methods),
        ):
            mn = mr.get("method_name")
            md = mr.get("method_desc") or ""
            if not mn:
                continue
            k = node_key(jid, cn, str(mn), md)
            hints = auth_hints.get((jid, cn, str(mn)), [])
            if getattr(args, "entry_no_auth_hints", False):
                if hints:
                    continue
            entries.setdefault(k, f"STRUTS2 {cn}::{mn}")
            entry_hint_by_node.setdefault(k, hints)

    cand_path = run.candidates_dir / f"candidates_{cfg.id}.jsonl"
    if not cand_path.exists():
        raise SystemExit(f"missing candidates file: {cand_path}")

    records = [json.loads(line) for line in cand_path.read_text(encoding="utf-8").splitlines() if line.strip()]
    cand_keys = [node_key(int(r.get("caller_jar_id") or 0), r["caller_class_name"], r["caller_method_name"], r.get("caller_method_desc")) for r in records]

    reach = compute_reachability(graph, entries, cand_keys, max_depth=cfg.max_depth)

    reachable_count = 0
    for r in records:
        ck = node_key(int(r.get("caller_jar_id") or 0), r["caller_class_name"], r["caller_method_name"], r.get("caller_method_desc"))
        rr = reach.get(ck)
        if rr and rr.reachable:
            reachable_count += 1
            r["reachable"] = True
            r["reach_depth"] = rr.depth
            r["entry_mapping"] = rr.entry_mapping
            r["entry_node_key"] = rr.entry_node_id
            r["chain_node_keys"] = rr.chain_node_ids
            r["entry_auth_hints"] = entry_hint_by_node.get(rr.entry_node_id) or []
            # simple priority: smaller depth is higher priority
            # NOTE: depth=0 is valid (entry method == candidate caller). Do not treat 0 as "missing".
            r["priority"] = 9999 if rr.depth is None else int(rr.depth)
        else:
            r["reachable"] = False
            r["reach_depth"] = None
            r["entry_mapping"] = None
            r["entry_node_key"] = None
            r["chain_node_keys"] = None
            r["entry_auth_hints"] = []
            r["priority"] = 9999

    # Write back candidates (overwrite)
    with cand_path.open("w", encoding="utf-8") as f:
        for r in records:
            f.write(json.dumps(r, ensure_ascii=False) + "\n")

    # Patch freeze with reach summary
    freeze_path = run.freeze_dir / f"freeze_{cfg.id}.json"
    if freeze_path.exists():
        fr = json.loads(freeze_path.read_text(encoding="utf-8"))
    else:
        fr = {"vector": cfg.id}
    fr["reachability"] = {
        "entries_total": len(entries),
        "candidates_total": len(records),
        "reachable_candidates": reachable_count,
        "max_depth": cfg.max_depth,
        "graph_cache_hit": cache_hit,
        "graph_nodes": len(graph.nodes),
        "graph_edges": sum(len(v) for v in graph.adj),
    }
    freeze_path.write_text(json.dumps(fr, ensure_ascii=False, indent=2), encoding="utf-8")

    print(json.dumps(fr["reachability"], ensure_ascii=False, indent=2))


def cmd_next(args):
    run = load_run(args.run)

    vector_path = _VECTORS_DIR / f"{args.vector}.yaml"
    cfg = load_vector(vector_path.as_posix())
    limit = int(args.limit or cfg.batch_size)

    cand_path = run.candidates_dir / f"candidates_{cfg.id}.jsonl"
    if not cand_path.exists():
        raise SystemExit(f"missing candidates file: {cand_path}")
    records = [json.loads(line) for line in cand_path.read_text(encoding="utf-8").splitlines() if line.strip()]

    # require reach() first unless user allows unreachable
    if not args.allow_unreachable and not any(r.get("reachable") for r in records):
        raise SystemExit("no reachable annotations found. Run `jaudit reach` first (or pass --allow-unreachable).")

    # Step7: NEEDS_DEEPER is not finalized; next should prefer it.
    finalized = load_finalized_ids(run, cfg.id)
    needs_deeper = load_needs_deeper_ids(run, cfg.id)
    # Fix: also exclude candidates currently in-flight (pending in recent batches),
    # to prevent "take -> fail to submit -> take again" loops.
    # Strategy: scan batch files and exclude candidate_ids whose state is not submitted/finalized.
    # We only consider "recent" batches to avoid permanent deadlock if an old batch was abandoned.
    in_flight: set[str] = set()
    now_ts = int(time.time())
    inflight_ttl_sec = 3600  # 1 hour
    for b_file in sorted(run.batches_dir.glob(f"batch_{cfg.id}_*.json")):
        try:
            b_data = json.loads(b_file.read_text(encoding="utf-8"))
        except Exception:
            continue
        created_at = int(b_data.get("created_at") or 0)
        if created_at and (now_ts - created_at) > inflight_ttl_sec:
            continue
        for c in (b_data.get("candidates") or []):
            if not isinstance(c, dict):
                continue
            cid = c.get("candidate_id")
            if not cid or not isinstance(cid, str):
                continue
            st = str(c.get("state") or "").lower()
            if st and st not in ("submitted", "finalized"):
                in_flight.add(cid)

    pool = []
    for r in records:
        cid = r.get("candidate_id")
        if cid in finalized:
            continue
        # allow NEEDS_DEEPER to be re-issued (resume), but block other in-flight items
        if cid in in_flight and cid not in needs_deeper:
            continue
        if args.allow_unreachable:
            pool.append(r)
        else:
            if r.get("reachable"):
                pool.append(r)

    def _next_rank(x: dict):
        cid = x.get("candidate_id")
        is_nd = 0 if (cid in needs_deeper) else 1
        return (is_nd, x.get("priority", 9999), x.get("caller_class_name", ""), x.get("caller_method_name", ""))

    pool.sort(key=_next_rank)

    chosen = pool[:limit]

    # batch id
    existing = sorted(run.batches_dir.glob(f"batch_{cfg.id}_*.json"))
    batch_no = len(existing) + 1
    batch_id = f"{cfg.id}-{batch_no:04d}"

    batch = {
        "batch_id": batch_id,
        "vector": cfg.id,
        "created_at": int(time.time()),
        "limit": limit,
        "allow_unreachable": bool(args.allow_unreachable),
        "candidates": [
            {
                "candidate_id": r["candidate_id"],
                "state": "pending",
                "resume": bool(r.get("candidate_id") in needs_deeper),
                "last_status": load_latest_status_map(run, cfg.id).get(r["candidate_id"]),
                "reachable": r.get("reachable"),
                "reach_depth": r.get("reach_depth"),
                "entry_mapping": r.get("entry_mapping"),
                "caller": {
                    "jar_id": r["caller_jar_id"],
                    "class": r["caller_class_name"],
                    "method": r["caller_method_name"],
                    "desc": r["caller_method_desc"],
                },
                "sink": {
                    "class": r["callee_class_name"],
                    "method": r["callee_method_name"],
                    "desc": r["callee_method_desc"],
                }
            }
            for r in chosen
        ],
    }

    out = run.batches_dir / f"batch_{cfg.id}_{batch_no:04d}.json"
    out.write_text(json.dumps(batch, ensure_ascii=False, indent=2), encoding="utf-8")

    print(out.as_posix())
    # also print a short summary for LLM
    for c in batch["candidates"]:
        print(f"- {c['candidate_id']} depth={c['reach_depth']} entry={c['entry_mapping']} sink={c['sink']['class']}::{c['sink']['method']}")


def _find_candidate(run, vector_id: str, candidate_id: str) -> dict:
    cand_path = run.candidates_dir / f"candidates_{vector_id}.jsonl"
    if not cand_path.exists():
        raise SystemExit(f"missing candidates file: {cand_path}")
    for line in cand_path.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        r = json.loads(line)
        if r.get("candidate_id") == candidate_id:
            return r
    raise SystemExit(f"candidate_id not found: {candidate_id}")


def _find_batch_file(run, vector_id: str, candidate_id: str, batch_id: str | None = None) -> Path | None:
    if not run.batches_dir.exists():
        return None
    # explicit batch id
    if batch_id:
        for p in sorted(run.batches_dir.glob(f"batch_{vector_id}_*.json")):
            try:
                obj = json.loads(p.read_text(encoding="utf-8"))
            except Exception:
                continue
            if obj.get("batch_id") == batch_id:
                return p
    # search newest first
    for p in sorted(run.batches_dir.glob(f"batch_{vector_id}_*.json"), reverse=True):
        try:
            obj = json.loads(p.read_text(encoding="utf-8"))
        except Exception:
            continue
        for c in (obj.get("candidates") or []):
            if c.get("candidate_id") == candidate_id:
                return p
    return None


def _patch_batch_candidate(run, vector_id: str, candidate_id: str, patch: dict, batch_id: str | None = None) -> None:
    p = _find_batch_file(run, vector_id, candidate_id, batch_id=batch_id)
    if not p:
        return
    try:
        obj = json.loads(p.read_text(encoding="utf-8"))
    except Exception:
        return
    changed = False
    cand_list = obj.get("candidates") or []
    for c in cand_list:
        if c.get("candidate_id") == candidate_id:
            c.update(patch)
            c["updated_at"] = int(time.time())
            changed = True
            break
    if changed:
        p.write_text(json.dumps(obj, ensure_ascii=False, indent=2), encoding="utf-8")


def cmd_evidence(args):
    run = load_run(args.run)

    vector_path = _VECTORS_DIR / f"{args.vector}.yaml"
    cfg = load_vector(vector_path.as_posix())

    cand = _find_candidate(run, cfg.id, args.candidate_id)
    class_name = cand["caller_class_name"]
    method_name = cand["caller_method_name"]
    method_desc = cand.get("caller_method_desc")
    sink_method = cand.get("callee_method_name")
    anchor = args.anchor or sink_method

    engine = args.prefer
    code = None
    if args.code_text_file:
        code = Path(args.code_text_file).read_text(encoding="utf-8")
    elif args.code_json_file:
        obj = json.loads(Path(args.code_json_file).read_text(encoding="utf-8"))
        if isinstance(obj, dict):
            code = obj.get("fullClassCode") or obj.get("full_class_code")
        if not code:
            raise SystemExit("code_json_file missing fullClassCode")
    elif args.mcp_tool_result_file:
        code = _extract_full_class_code_from_mcp_tool_result(args.mcp_tool_result_file)
    else:
        raise SystemExit("missing code input: pass --mcp-tool-result-file OR --code-json-file OR --code-text-file")
    assert code is not None

    attempts = []
    kinds = [args.kind]
    if args.auto_fallback and args.kind == "window":
        kinds = ["window", "expand", "method"]

    sr = None
    for k in kinds:
        sr = make_slice(
            code,
            method_name,
            method_desc,
            kind=k,
            window=int(args.window),
            expand_before=int(args.expand_before),
            expand_after=int(args.expand_after),
            anchor=anchor,
        )
        attempts.append({"kind": k, "quality": getattr(sr.meta, "quality", None), "warning": sr.meta.warning, "suggested_next": sr.meta.suggested_next})
        if getattr(sr.meta, "quality", None) == "GOOD":
            break
    assert sr is not None
    ref = store_snippet(run, args.candidate_id, class_name, method_name, sr)

    _patch_batch_candidate(
        run,
        cfg.id,
        args.candidate_id,
        {
            "state": "evidenced",
            "evidence": {
                "sha256": ref.sha256,
                "kind": ref.kind,
                "quality": getattr(sr.meta, "quality", None),
                "warning": sr.meta.warning,
                "suggested_next": sr.meta.suggested_next,
            },
        },
        batch_id=args.batch_id,
    )

    out = {
        "candidate_id": args.candidate_id,
        "vector": cfg.id,
        "decompiler": engine,
        "anchor": anchor,
        "attempts": attempts,
        "snippet_ref": {
            "sha256": ref.sha256,
            "snippet_file": ref.path,
            "start_line": ref.start_line,
            "end_line": ref.end_line,
            "anchor_line": ref.anchor_line,
            "kind": ref.kind,
        },
        "slice_meta": sr.meta.__dict__,
    }
    print(json.dumps(out, ensure_ascii=False, indent=2))


def cmd_submit(args):
    run = load_run(args.run)

    vector_path = _VECTORS_DIR / f"{args.vector}.yaml"
    cfg = load_vector(vector_path.as_posix())

    cand = _find_candidate(run, cfg.id, args.candidate_id)

    reasoning = args.reasoning
    if args.reasoning_file:
        reasoning = Path(args.reasoning_file).read_text(encoding="utf-8")

    sha = args.snippet_sha256
    if not sha:
        last = latest_snippet_for_candidate(run, args.candidate_id)
        if last:
            sha = last.get("sha256")

    evidence = {}
    if sha:
        meta = snippet_meta_by_sha(run, sha) or {}
        evidence["snippet_ref"] = {
            "sha256": sha,
            "kind": meta.get("kind"),
            "start_line": meta.get("start_line"),
            "end_line": meta.get("end_line"),
            "anchor_line": meta.get("anchor_line"),
            "quality": meta.get("quality"),
            "warning": meta.get("warning"),
            "suggested_next": meta.get("suggested_next"),
            "snippet_file": meta.get("snippet_file"),
        }

    # chain evidence (from reach)
    if cand.get("entry_mapping") or cand.get("chain_node_keys"):
        evidence["chain_trace"] = {
            "entry_mapping": cand.get("entry_mapping"),
            "entry_node_key": cand.get("entry_node_key"),
            "chain_node_keys": cand.get("chain_node_keys"),
            "reach_depth": cand.get("reach_depth"),
            "entry_auth_hints": cand.get("entry_auth_hints") or [],
        }

    record = {
        "candidate_id": args.candidate_id,
        "vector": cfg.id,
        "batch_id": args.batch_id,
        "status": args.status,
        "reasoning": reasoning,
        "caller": {
            "jar_id": cand.get("caller_jar_id"),
            "class": cand.get("caller_class_name"),
            "method": cand.get("caller_method_name"),
            "desc": cand.get("caller_method_desc"),
        },
        "sink": {
            "jar_id": cand.get("callee_jar_id"),
            "class": cand.get("callee_class_name"),
            "method": cand.get("callee_method_name"),
            "desc": cand.get("callee_method_desc"),
        },
        "evidence": evidence,
    }
    # Optional: store raw HTTP PoC request (for report display)
    poc = args.poc_http
    if args.poc_http_file:
        poc = Path(args.poc_http_file).read_text(encoding="utf-8")
    if poc:
        record["poc_http"] = {"raw": poc}

    res = append_verify_record(run, cfg.id, record, strict=(not args.no_strict))
    if res.ok:
        _patch_batch_candidate(
            run,
            cfg.id,
            args.candidate_id,
            {
                "state": "submitted",
                "submitted": {
                    "status": args.status,
                    "snippet_sha256": sha,
                },
            },
            batch_id=args.batch_id,
        )
    print(json.dumps(res.__dict__, ensure_ascii=False, indent=2))


def _parse_vectors_arg(v: str | None) -> list[str]:
    if not v:
        return []
    return [x.strip() for x in v.split(",") if x.strip()]


def _load_default_queue() -> list[str]:
    q = _VECTORS_DIR / "default_queue.yaml"
    if not q.exists():
        return []
    import yaml
    data = yaml.safe_load(q.read_text(encoding="utf-8")) or {}
    vs = data.get("vectors") or []
    return [str(x) for x in vs if str(x).strip()]


def cmd_run(args):
    """One-shot orchestrator for Agent usage.

    init -> profile -> graph(cache) -> for each vector: freeze -> reach -> next
    """
    out_root = args.out
    run_paths = init_run(out_root)
    run_dir = run_paths.root.as_posix()

    # resolve vectors
    vectors = _parse_vectors_arg(args.vectors)
    if not vectors:
        vectors = _load_default_queue()
    if not vectors:
        raise SystemExit("no vectors selected (pass --vectors or edit vectors/default_queue.yaml)")

    # profile
    cmd_profile(SimpleNamespace(run=run_dir, db=args.db, cwd=args.cwd))

    # graph is optional in targeted mode; reach will build it on demand.
    if not args.skip_graph:
        cmd_graph(SimpleNamespace(run=run_dir, db=args.db))

    # per-vector pipeline
    for v in vectors:
        cmd_freeze(SimpleNamespace(run=run_dir, vector=v, db=args.db, cwd=args.cwd, no_cap=args.no_cap))
        if not args.skip_reach:
            cmd_reach(SimpleNamespace(run=run_dir, vector=v, db=args.db, entry_no_auth_hints=args.entry_no_auth_hints))
        # create first batch for LLM
        cmd_next(SimpleNamespace(run=run_dir, vector=v, limit=args.batch or None, allow_unreachable=args.allow_unreachable))

    print(run_dir)


def cmd_report(args):
    run = load_run(args.run)

    vectors = _parse_vectors_arg(args.vectors)
    if not vectors:
        # infer from freeze files
        vectors = [p.name.replace("freeze_", "").replace(".json", "") for p in sorted(run.freeze_dir.glob("freeze_*.json"))]
    if not vectors:
        raise SystemExit("no vectors found in run (missing freeze_*.json)")

    if args.template:
        template = Path(args.template)
        if not template.is_absolute():
            template = (_SKILL_ROOT / template).resolve()
    else:
        template = (_ASSETS_DIR / "report_template.md.j2")
    if not template.exists():
        raise SystemExit(f"missing template: {template}")

    out_path = Path(args.out) if args.out else (run.root / "audit_report.md")
    compile_report(
        run,
        vectors=vectors,
        template_path=template,
        out_path=out_path,
        strict=args.strict,
    )
    print(out_path.as_posix())

def cmd_status(args):
    run = load_run(args.run)
    vectors = _parse_vectors_arg(args.vectors)
    if not vectors:
        vectors = [p.name.replace("freeze_", "").replace(".json", "") for p in sorted(run.freeze_dir.glob("freeze_*.json"))]
    if not vectors:
        raise SystemExit("no vectors found (missing freeze_*.json)")

    rows = []
    for v in vectors:
        cand_path = run.candidates_dir / f"candidates_{v}.jsonl"
        candidates = []
        if cand_path.exists():
            candidates = [json.loads(line) for line in cand_path.read_text(encoding="utf-8").splitlines() if line.strip()]
        total = len(candidates)
        reachable = sum(1 for c in candidates if c.get("reachable"))
        status_map = load_latest_status_map(run, v)
        # finalized only counts as verified (VULN/SAFE). NEEDS_DEEPER remains remaining.
        finalized = {cid for cid, st in status_map.items() if st in ("VULN","SAFE")}
        status_counts = {"VULN": 0, "SAFE": 0, "NEEDS_DEEPER": 0}
        for st in status_map.values():
            if st in status_counts:
                status_counts[st] += 1

        verified = len(finalized)
        remaining = max(total - verified, 0)
        rows.append({
            "vector": v,
            "total": total,
            "reachable": reachable,
            "verified": verified,
            "remaining": remaining,
            **status_counts,
        })

    header = ["vector","total","reachable","verified","remaining","VULN","SAFE","NEEDS_DEEPER"]
    print("\t".join(header))
    for r in rows:
        print("\t".join(str(r[h]) for h in header))
    if args.json:
        print(json.dumps(rows, ensure_ascii=False, indent=2))

class CustomArgumentParser(argparse.ArgumentParser):
    """自定义 ArgumentParser，提供更友好的错误提示"""
    def error(self, message):
        # 检查是否是缺少 --run 参数的错误
        if "--run" in message and "required" in message.lower():
            self.print_usage(sys.stderr)
            print("\n" + "=" * 60, file=sys.stderr)
            print("ERROR: Missing --run parameter!", file=sys.stderr)
            print("=" * 60, file=sys.stderr)
            print("\n[!] SOLUTION:", file=sys.stderr)
            print("    1. Run 'init' command first to get a Run ID", file=sys.stderr)
            print("    2. Copy the FULL command from init output (it includes --run <ID>)", file=sys.stderr)
            print("    3. Make sure you copy the ENTIRE command, not just part of it", file=sys.stderr)
            print("\nExample:", file=sys.stderr)
            print("    python scripts/cli.py freeze --vector rce --run 20240101-120000", file=sys.stderr)
            print("=" * 60 + "\n", file=sys.stderr)
            sys.exit(2)
        # 其他错误使用默认处理
        super().error(message)

def main():
    ap = CustomArgumentParser(prog="jaudit")
    sub = ap.add_subparsers(dest="cmd", required=True)

    p = sub.add_parser("init")
    p.add_argument("--out", default=str(_DEFAULT_RUNS_DIR))
    p.set_defaults(fn=cmd_init)

    p = sub.add_parser("preflight")
    p.add_argument("--cwd", help="jar-analyzer cwd (to locate DB and rule files)")
    p.add_argument("--db", help="override JA_DB")
    p.set_defaults(fn=cmd_preflight)

    # Orchestrator for Agents: init->profile->graph->freeze->reach->next
    p = sub.add_parser("run")
    p.add_argument("--out", default=str(_DEFAULT_RUNS_DIR))
    p.add_argument("--vectors", help="comma-separated, e.g. rce,sqli")
    p.add_argument("--db")
    p.add_argument("--cwd", help="jar-analyzer cwd (for vulnerability.yaml / dfs-sink.json)")
    p.add_argument("--no-cap", action="store_true")
    p.add_argument("--allow-unreachable", action="store_true")
    p.add_argument("--batch", help="override batch size for initial next()")
    p.add_argument("--skip-graph", action="store_true", help="targeted mode: do not build graph up-front")
    p.add_argument("--skip-reach", action="store_true", help="targeted mode: skip reachability (use --allow-unreachable with next)")
    p.add_argument("--entry-no-auth-hints", action="store_true", help="reach filter: only use entries without auth annotation hints (anno_table)")
    p.set_defaults(fn=cmd_run)

    p = sub.add_parser("profile")
    p.add_argument("--run", required=True, help="Run ID from 'init' command output (REQUIRED - copy from init output)")
    p.add_argument("--db")
    p.add_argument("--cwd", help="jar-analyzer cwd (for vulnerability.yaml / dfs-sink.json)")
    p.set_defaults(fn=cmd_profile)

    p = sub.add_parser("freeze")
    p.add_argument("--run", required=True, help="Run ID from 'init' command output (REQUIRED - copy from init output)")
    p.add_argument("--vector", required=True)
    p.add_argument("--db")
    p.add_argument("--cwd", help="jar-analyzer cwd (for vulnerability.yaml / dfs-sink.json)")
    p.add_argument("--no-cap", action="store_true")
    p.set_defaults(fn=cmd_freeze)

    
    p = sub.add_parser("graph")
    p.add_argument("--run", required=True, help="Run ID from 'init' command output (REQUIRED - copy from init output)")
    p.add_argument("--db")
    p.set_defaults(fn=cmd_graph)

    p = sub.add_parser("reach")
    p.add_argument("--run", required=True, help="Run ID from 'init' command output (REQUIRED - copy from init output)")
    p.add_argument("--vector", required=True)
    p.add_argument("--db")
    p.add_argument("--entry-no-auth-hints", action="store_true", help="only use entries without auth annotation hints (anno_table)")
    p.set_defaults(fn=cmd_reach)

    p = sub.add_parser("next")
    p.add_argument("--run", required=True, help="Run ID from 'init' command output (REQUIRED - copy from init output)")
    p.add_argument("--vector", required=True)
    p.add_argument("--limit")
    p.add_argument("--allow-unreachable", action="store_true")
    p.set_defaults(fn=cmd_next)

    p = sub.add_parser("evidence")
    p.add_argument("--run", required=True, help="Run ID from 'init' command output (REQUIRED - copy from init output)")
    p.add_argument("--vector", required=True)
    p.add_argument("--candidate-id", required=True)
    p.add_argument("--batch-id", help="optional: patch corresponding batch state")
    p.add_argument("--code-json-file", help="MCP output JSON file containing fullClassCode")
    p.add_argument("--code-text-file", help="file containing full class code text")
    p.add_argument("--mcp-tool-result-file", help="Claude MCP tool-result file (auto extract fullClassCode); preferred to avoid ad-hoc scripts")
    p.add_argument("--prefer", choices=["fernflower","cfr"], default="cfr", help="metadata only (MCP decompiler used)")
    p.add_argument("--kind", choices=["window","method","expand"], default="window")
    p.add_argument("--auto-fallback", action="store_true", help="if quality is WARN/BAD, auto retry expand->method")
    p.add_argument("--window", default="20")
    p.add_argument("--expand-before", default="60")
    p.add_argument("--expand-after", default="60")
    p.add_argument("--anchor")
    p.set_defaults(fn=cmd_evidence)

    p = sub.add_parser("submit")
    p.add_argument("--run", required=True, help="Run ID from 'init' command output (REQUIRED - copy from init output)")
    p.add_argument("--vector", required=True)
    p.add_argument("--candidate-id", required=True)
    p.add_argument("--batch-id", required=True)
    p.add_argument("--status", choices=["VULN","SAFE","NEEDS_DEEPER"], required=True)
    p.add_argument("--reasoning", default="")
    p.add_argument("--reasoning-file")
    p.add_argument("--snippet-sha256")
    p.add_argument("--poc-http", help="raw HTTP request PoC (will be embedded into report)")
    p.add_argument("--poc-http-file", help="file containing raw HTTP request PoC")
    p.add_argument("--no-strict", action="store_true")
    p.set_defaults(fn=cmd_submit)

    p = sub.add_parser("report")
    p.add_argument("--run", required=True, help="Run ID from 'init' command output (REQUIRED - copy from init output)")
    p.add_argument("--vectors", help="comma-separated; default=all freeze_*.json")
    p.add_argument("--template", help="default=assets/report_template.md.j2")
    p.add_argument("--out", help="default=<run>/audit_report.md")
    p.add_argument("--strict", action="store_true", help="fail (exit non-zero) if coverage incomplete")
    p.set_defaults(fn=cmd_report)

    p = sub.add_parser("status")
    p.add_argument("--run", required=True, help="Run ID from 'init' command output (REQUIRED - copy from init output)")
    p.add_argument("--vectors", help="comma-separated; default=all freeze_*.json")
    p.add_argument("--json", action="store_true")
    p.set_defaults(fn=cmd_status)

    args = ap.parse_args()
    args.fn(args)

if __name__ == "__main__":
    main()
