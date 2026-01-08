from __future__ import annotations

import hashlib
import re
from dataclasses import dataclass
from typing import Optional


def sha256_text(s: str) -> str:
    return hashlib.sha256(s.encode("utf-8", errors="ignore")).hexdigest()


def jvm_desc_arg_count(method_desc: str) -> Optional[int]:
    """Parse JVM method descriptor and return argument count.

    Examples:
      ()V -> 0
      (Ljava/lang/String;I)[B -> 2
      ([Ljava/lang/String;[[I)V -> 2
    """
    if not method_desc or "(" not in method_desc or ")" not in method_desc:
        return None
    try:
        args = method_desc.split("(", 1)[1].split(")", 1)[0]
    except Exception:
        return None
    i = 0
    n = 0
    while i < len(args):
        c = args[i]
        if c == "[":
            # array: skip all '[' then parse element
            while i < len(args) and args[i] == "[":
                i += 1
            continue
        if c in "ZBCSIFJD":
            n += 1
            i += 1
            continue
        if c == "L":
            # object type until ';'
            semi = args.find(";", i)
            if semi == -1:
                return None
            n += 1
            i = semi + 1
            continue
        # unknown token
        return None
    return n


def _count_params_in_header_line(line: str) -> Optional[int]:
    """Best-effort count of parameters in a decompiled method header line."""
    if "(" not in line or ")" not in line:
        return None
    seg = line.split("(", 1)[1].split(")", 1)[0].strip()
    if not seg:
        return 0
    # remove generics-ish noise inside params (very rough)
    seg = re.sub(r"<[^>]+>", "", seg)
    # count commas not inside angle brackets (already stripped mostly)
    return seg.count(",") + 1


def _quality(ok: bool, balanced: bool, truncated: bool, warning: Optional[str]) -> str:
    if not ok:
        return "BAD"
    if not balanced:
        return "BAD"
    if truncated:
        return "WARN"
    if warning:
        return "WARN"
    return "GOOD"


@dataclass
class SliceMeta:
    kind: str  # window | method | expand
    ok: bool
    balanced_braces: bool
    truncated: bool
    quality: str  # GOOD | WARN | BAD
    start_line: Optional[int]
    end_line: Optional[int]
    anchor_line: Optional[int]
    sha256: str
    warning: Optional[str] = None
    suggested_next: Optional[str] = None


@dataclass
class SliceResult:
    snippet: str
    meta: SliceMeta


def _find_method_header_line(lines: list[str], method_name: str, arg_count: Optional[int] = None) -> Optional[int]:
    """Locate a method header line.

    If arg_count is provided, we prefer matches whose parameter count equals arg_count.
    This reduces overload collisions without needing full signature parsing.
    """
    pat = re.compile(rf"\b{re.escape(method_name)}\s*\(")
    candidates: list[int] = []
    for i, l in enumerate(lines):
        if pat.search(l):
            candidates.append(i)
    if not candidates:
        return None
    if arg_count is None:
        return candidates[0]
    for i in candidates:
        pc = _count_params_in_header_line(lines[i])
        if pc is not None and pc == arg_count:
            return i
    # fallback
    return candidates[0]


def _brace_extract(lines: list[str], header_i: int, max_body_lines: int = 2000) -> tuple[Optional[int], Optional[int], bool, bool, str]:
    """Return (brace_start_i, brace_end_i, balanced, truncated, warning)."""
    # find first "{" near header
    brace_start = None
    for j in range(header_i, min(header_i + 60, len(lines))):
        if "{" in lines[j]:
            brace_start = j
            break
    if brace_start is None:
        return None, None, False, False, "no_open_brace_near_header"

    def _mask_quotes_and_comments(line: str) -> str:
        # Remove line comments, then mask string/char literals.
        # This is not a full lexer, but avoids brace drift from common literals/comments.
        line = re.sub(r"//.*", "", line)
        line = re.sub(r'"(?:\\.|[^"\\])*"', '""', line)
        line = re.sub(r"'(?:\\.|[^'\\])*'", "''", line)
        return line

    depth = 0
    end = None
    truncated = False
    for k in range(brace_start, len(lines)):
        clean_line = _mask_quotes_and_comments(lines[k])
        depth += clean_line.count("{")
        depth -= clean_line.count("}")
        if depth == 0 and k > brace_start:
            end = k
            break
        if (k - brace_start) >= max_body_lines:
            truncated = True
            break

    if end is None:
        # if truncated due to max lines, still return a window
        return brace_start, (brace_start + max_body_lines), False, True, "unterminated_or_too_large"

    return brace_start, end, True, False, ""


def slice_code(
    code_text: str,
    method_name: str,
    method_desc: Optional[str] = None,
    *,
    kind: str = "window",
    window: int = 20,
    expand_before: int = 60,
    expand_after: int = 60,
    anchor: Optional[str] = None,
    max_body_lines: int = 2000,
) -> SliceResult:
    """Slice decompiled code around a method.

    - kind="window": returns a small window around an anchor inside the method (or method header if anchor not found)
    - kind="method": returns the whole method body (brace extraction)
    - kind="expand": returns a larger window around the anchor/header (for recovery when window seems incomplete)
    """
    lines = code_text.splitlines()
    arg_count = jvm_desc_arg_count(method_desc) if method_desc else None
    header_i = _find_method_header_line(lines, method_name, arg_count=arg_count) if method_name else None
    if header_i is None:
        # anchor fallback: if we have an anchor, slice around first occurrence in whole file
        if anchor:
            for i, l in enumerate(lines):
                if anchor in l:
                    s = max(0, i - window)
                    e = min(len(lines), i + window)
                    snippet = "\n".join(lines[s:e])
                    q = _quality(True, False, False, "file_window_anchor_only")
                    m = SliceMeta(
                        kind="window",
                        ok=True,
                        balanced_braces=False,
                        truncated=False,
                        quality=q,
                        start_line=s + 1,
                        end_line=e,
                        anchor_line=i + 1,
                        sha256=sha256_text(snippet),
                        warning="method_header_not_found; used file window by anchor",
                        suggested_next="kind=expand",
                    )
                    return SliceResult(snippet, m)

        head = "\n".join(lines[: min(len(lines), max(50, window * 2))])
        q = _quality(False, False, True, "method_header_not_found")
        m = SliceMeta(
            kind=kind,
            ok=False,
            balanced_braces=False,
            truncated=True,
            quality=q,
            start_line=1,
            end_line=min(len(lines), max(50, window * 2)),
            anchor_line=None,
            sha256=sha256_text(head),
            warning="method_header_not_found; returned file head",
            suggested_next="kind=expand",
        )
        return SliceResult(head, m)

    brace_start, brace_end, balanced, truncated, warn = _brace_extract(lines, header_i, max_body_lines=max_body_lines)
    if brace_start is None:
        # fallback: window around header
        s = max(0, header_i - window)
        e = min(len(lines), header_i + window)
        snippet = "\n".join(lines[s:e])
        q = _quality(False, False, False, "no_open_brace_near_header")
        m = SliceMeta(
            kind=kind,
            ok=False,
            balanced_braces=False,
            truncated=False,
            quality=q,
            start_line=s + 1,
            end_line=e,
            anchor_line=header_i + 1,
            sha256=sha256_text(snippet),
            warning="no_open_brace_near_header; returned header window",
            suggested_next="expand(before=100,after=100) or use full class",
        )
        return SliceResult(snippet, m)

    # method region (header..brace_end)
    method_lines = lines[header_i : min(len(lines), brace_end + 1)]
    # locate anchor line inside method
    anchor_i = None
    if anchor:
        try:
            ap = re.compile(re.escape(anchor))
        except re.error:
            ap = None
        if ap:
            for i, l in enumerate(method_lines):
                if ap.search(l):
                    anchor_i = header_i + i
                    break

    # default anchor is header
    if anchor_i is None:
        anchor_i = header_i

    if kind == "method":
        snippet = "\n".join(method_lines)
        ok = bool(balanced and not truncated)
        q = _quality(ok, balanced, truncated, warn or None)
        m = SliceMeta(
            kind=kind,
            ok=ok,
            balanced_braces=balanced,
            truncated=truncated,
            quality=q,
            start_line=header_i + 1,
            end_line=min(len(lines), brace_end + 1),
            anchor_line=anchor_i + 1,
            sha256=sha256_text(snippet),
            warning=(warn or None) if (not balanced or truncated) else None,
            suggested_next=("expand(before=120,after=120)" if not balanced else None),
        )
        return SliceResult(snippet, m)

    if kind == "expand":
        s = max(0, anchor_i - expand_before)
        e = min(len(lines), anchor_i + expand_after)
        snippet = "\n".join(lines[s:e])
        ok = True
        q = _quality(ok, balanced, (e - s) < (expand_before + expand_after), None)
        m = SliceMeta(
            kind=kind,
            ok=ok,
            balanced_braces=balanced,
            truncated=(e - s) < (expand_before + expand_after),
            quality=q,
            start_line=s + 1,
            end_line=e,
            anchor_line=anchor_i + 1,
            sha256=sha256_text(snippet),
            warning=None,
            suggested_next=("method" if not balanced else None),
        )
        return SliceResult(snippet, m)

    # default: window
    s = max(0, anchor_i - window)
    e = min(len(lines), anchor_i + window)
    snippet = "\n".join(lines[s:e])
    ok = True
    q = _quality(ok, balanced, (e - s) < (window * 2), ("method_unbalanced" if not balanced else None))
    m = SliceMeta(
        kind="window",
        ok=ok,
        balanced_braces=balanced,
        truncated=(e - s) < (window * 2),
        quality=q,
        start_line=s + 1,
        end_line=e,
        anchor_line=anchor_i + 1,
        sha256=sha256_text(snippet),
        warning=("method_unbalanced; consider expand/method" if not balanced else None),
        suggested_next=("expand(before=120,after=120)" if not balanced else None),
    )
    return SliceResult(snippet, m)
