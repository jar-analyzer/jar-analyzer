from __future__ import annotations
import json, time
from dataclasses import dataclass
from pathlib import Path

@dataclass
class RunPaths:
    root: Path
    session: Path
    inventory: Path
    manifest: Path
    profile: Path
    rules_dir: Path
    graph_cache_dir: Path
    freeze_dir: Path
    candidates_dir: Path
    verify_dir: Path
    batches_dir: Path
    evidence_dir: Path
    snippets_dir: Path
    snippet_index: Path

def init_run(out_dir: str) -> RunPaths:
    ts = time.strftime("%Y%m%d-%H%M%S")
    root = Path(out_dir) / ts
    root.mkdir(parents=True, exist_ok=False)
    p = RunPaths(
        root=root,
        session=root/"session.json",
        inventory=root/"inventory.json",
        manifest=root/"manifest.json",
        profile=root/"profile.json",
        rules_dir=root/"rules",
        graph_cache_dir=root/"graph_cache",
        freeze_dir=root/"freeze",
        candidates_dir=root/"candidates",
        verify_dir=root/"verify",
        batches_dir=root/"batches",
        evidence_dir=root/"evidence",
        snippets_dir=root/"evidence"/"snippets",
        snippet_index=root/"evidence"/"snippet_index.jsonl",
    )
    for d in [p.rules_dir, p.graph_cache_dir, p.freeze_dir, p.candidates_dir, p.verify_dir, p.batches_dir, p.evidence_dir, p.snippets_dir]:
        d.mkdir(parents=True, exist_ok=True)

    # create evidence index if missing
    if not p.snippet_index.exists():
        p.snippet_index.write_text("", encoding="utf-8")

    manifest = {
        "run_id": ts,
        "created_at": ts,
        "contracts": {
            "fact_node_id": "{jar_id}::{class}::{method}::{desc_hash12}",
            "evidence_required": True,
            "report_fail_closed": True
        }
    }
    p.manifest.write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")

    # session/inventory placeholders (filled by profile/freeze/report gradually)
    if not p.session.exists():
        p.session.write_text(json.dumps({"run_id": ts, "created_at": ts}, ensure_ascii=False, indent=2), encoding="utf-8")
    if not p.inventory.exists():
        p.inventory.write_text(json.dumps({}, ensure_ascii=False, indent=2), encoding="utf-8")
    return p

def load_run(run_dir: str) -> RunPaths:
    root = Path(run_dir)
    if not root.exists():
        # allow passing just run_id, auto-resolve under ./runs/<run_id>
        # (common when skill root is current working directory)
        if "/" not in run_dir and "\\" not in run_dir:
            cand = Path("runs") / run_dir
            if cand.exists():
                root = cand
    # backward-compat: old runs used cache/graph_cache
    graph_cache = root/"graph_cache"
    if not graph_cache.exists() and (root/"cache"/"graph_cache").exists():
        graph_cache = root/"cache"/"graph_cache"
    return RunPaths(
        root=root,
        session=root/"session.json",
        inventory=root/"inventory.json",
        manifest=root/"manifest.json",
        profile=root/"profile.json",
        rules_dir=root/"rules",
        graph_cache_dir=graph_cache,
        freeze_dir=root/"freeze",
        candidates_dir=root/"candidates",
        verify_dir=root/"verify",
        batches_dir=root/"batches",
        evidence_dir=root/"evidence",
        snippets_dir=root/"evidence"/"snippets",
        snippet_index=root/"evidence"/"snippet_index.jsonl",
    )
