from __future__ import annotations
import hashlib

def desc_hash(method_desc: str | None) -> str:
    s = method_desc or ""
    h = hashlib.sha256(s.encode("utf-8", errors="ignore")).hexdigest()
    return h[:12]

def node_id(jar_id: int, class_name: str, method_name: str, method_desc: str | None) -> str:
    return f"{jar_id}::{class_name}::{method_name}::{desc_hash(method_desc)}"


def node_key(jar_id: int, class_name: str, method_name: str, method_desc: str | None) -> str:
    """Full signature key used for graph/reachability: includes raw method_desc."""
    return f"{jar_id}::{class_name}::{method_name}::{method_desc or ''}"


def split_node_key(key: str):
    """Return (jar_id, class_name, method_name, method_desc) from node_key."""
    jar, cls, m, desc = key.split("::", 3)
    return int(jar), cls, m, desc
