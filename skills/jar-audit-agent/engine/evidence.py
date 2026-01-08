from __future__ import annotations

import json
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Optional, Dict, Any

from .session import RunPaths
from .slicer import slice_code, SliceResult


@dataclass
class SnippetRef:
    sha256: str
    path: str
    start_line: Optional[int]
    end_line: Optional[int]
    anchor_line: Optional[int]
    kind: str


def store_snippet(run: RunPaths, candidate_id: str, class_name: str, method_name: str, slice_result: SliceResult) -> SnippetRef:
    sha = slice_result.meta.sha256
    out_path = run.snippets_dir / f"sha256_{sha}.txt"
    if not out_path.exists():
        out_path.write_text(slice_result.snippet, encoding="utf-8")

    idx = {
        "ts": int(time.time()),
        "candidate_id": candidate_id,
        "class_name": class_name,
        "method_name": method_name,
        "sha256": sha,
        "kind": slice_result.meta.kind,
        "start_line": slice_result.meta.start_line,
        "end_line": slice_result.meta.end_line,
        "anchor_line": slice_result.meta.anchor_line,
        "ok": slice_result.meta.ok,
        "balanced_braces": slice_result.meta.balanced_braces,
        "truncated": slice_result.meta.truncated,
        "quality": getattr(slice_result.meta, "quality", None),
        "warning": slice_result.meta.warning,
        "suggested_next": slice_result.meta.suggested_next,
        "snippet_file": out_path.as_posix(),
    }
    with run.snippet_index.open("a", encoding="utf-8") as f:
        f.write(json.dumps(idx, ensure_ascii=False) + "\n")

    return SnippetRef(
        sha256=sha,
        path=out_path.as_posix(),
        start_line=slice_result.meta.start_line,
        end_line=slice_result.meta.end_line,
        anchor_line=slice_result.meta.anchor_line,
        kind=slice_result.meta.kind,
    )


def latest_snippet_for_candidate(run: RunPaths, candidate_id: str) -> Optional[Dict[str, Any]]:
    if not run.snippet_index.exists():
        return None
    last = None
    for line in run.snippet_index.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        obj = json.loads(line)
        if obj.get("candidate_id") == candidate_id:
            last = obj
    return last


def snippet_meta_by_sha(run: RunPaths, sha256: str) -> Optional[Dict[str, Any]]:
    """Return the latest snippet_index record for a given sha256."""
    if not run.snippet_index.exists():
        return None
    last = None
    for line in run.snippet_index.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        obj = json.loads(line)
        if obj.get("sha256") == sha256:
            last = obj
    return last


def make_slice(
    code_text: str,
    method_name: str,
    method_desc: Optional[str] = None,
    *,
    kind: str,
    window: int,
    expand_before: int,
    expand_after: int,
    anchor: Optional[str],
) -> SliceResult:
    return slice_code(
        code_text,
        method_name,
        method_desc,
        kind=kind,
        window=window,
        expand_before=expand_before,
        expand_after=expand_after,
        anchor=anchor,
    )
