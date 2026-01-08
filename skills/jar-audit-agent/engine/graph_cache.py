
from __future__ import annotations
import json, os, struct
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List, Tuple, Optional
from .db import DB

@dataclass
class GraphCacheMeta:
    db_path: str
    db_size: int
    db_mtime: int
    edge_count: int
    codec_version: int = 1

@dataclass
class Graph:
    # node_id (string) <-> int_id mapping
    nodes: List[str]
    node_to_int: Dict[str, int]
    # adjacency list (caller -> callees)
    adj: List[List[int]]
    # reverse adjacency (callee -> callers)
    radj: List[List[int]]

def _db_fingerprint(db_path: str) -> Tuple[int,int]:
    st = os.stat(db_path)
    return (int(st.st_size), int(st.st_mtime))

def _load_meta(path: Path) -> Optional[GraphCacheMeta]:
    if not path.exists():
        return None
    data = json.loads(path.read_text(encoding="utf-8"))
    return GraphCacheMeta(**data)

def _save_meta(path: Path, meta: GraphCacheMeta) -> None:
    path.write_text(json.dumps(meta.__dict__, ensure_ascii=False, indent=2), encoding="utf-8")

def _write_edges_bin(path: Path, edges: List[Tuple[int,int]]) -> None:
    with path.open("wb") as f:
        for u,v in edges:
            f.write(struct.pack("<II", u, v))

def _read_edges_bin(path: Path) -> List[Tuple[int,int]]:
    edges: List[Tuple[int,int]] = []
    with path.open("rb") as f:
        data = f.read()
    if len(data) % 8 != 0:
        # corrupted cache
        raise ValueError("edges.bin corrupted")
    for i in range(0, len(data), 8):
        u,v = struct.unpack("<II", data[i:i+8])
        edges.append((u,v))
    return edges

def build_graph_cache(db_path: str, cache_dir: Path) -> Graph:
    cache_dir.mkdir(parents=True, exist_ok=True)
    meta_path = cache_dir / "graph_meta.json"
    nodes_path = cache_dir / "nodes.json"
    edges_path = cache_dir / "edges.bin"

    db = DB(db_path)

    node_to_int: Dict[str,int] = {}
    nodes: List[str] = []
    edges: List[Tuple[int,int]] = []

    def _get_id(node_id: str) -> int:
        i = node_to_int.get(node_id)
        if i is None:
            i = len(nodes)
            node_to_int[node_id] = i
            nodes.append(node_id)
        return i

        # Stream edges to avoid loading huge tables into memory.
    import sqlite3
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    cur = conn.execute(
        """SELECT
             caller_jar_id, caller_class_name, caller_method_name, caller_method_desc,
             callee_jar_id, callee_class_name, callee_method_name, callee_method_desc
           FROM method_call_table"""
    )
    while True:
        batch = cur.fetchmany(50000)
        if not batch:
            break
        for rr in batch:
            r = dict(rr)
            caller = f"{int(r.get('caller_jar_id') or 0)}::{r.get('caller_class_name')}::{r.get('caller_method_name')}::{r.get('caller_method_desc') or ''}"
            callee = f"{int(r.get('callee_jar_id') or 0)}::{r.get('callee_class_name')}::{r.get('callee_method_name')}::{r.get('callee_method_desc') or ''}"
            u = _get_id(caller)
            v = _get_id(callee)
            edges.append((u,v))
    conn.close()

# build adjacency
    n = len(nodes)
    adj = [[] for _ in range(n)]
    radj = [[] for _ in range(n)]
    for u,v in edges:
        adj[u].append(v)
        radj[v].append(u)

    nodes_path.write_text(json.dumps(nodes, ensure_ascii=False), encoding="utf-8")
    _write_edges_bin(edges_path, edges)

    size, mtime = _db_fingerprint(db_path)
    meta = GraphCacheMeta(db_path=str(db_path), db_size=size, db_mtime=mtime, edge_count=len(edges))
    _save_meta(meta_path, meta)

    return Graph(nodes=nodes, node_to_int=node_to_int, adj=adj, radj=radj)

def load_graph_cache(db_path: str, cache_dir: Path) -> Optional[Graph]:
    meta_path = cache_dir / "graph_meta.json"
    nodes_path = cache_dir / "nodes.json"
    edges_path = cache_dir / "edges.bin"
    meta = _load_meta(meta_path)
    if meta is None or not nodes_path.exists() or not edges_path.exists():
        return None
    size, mtime = _db_fingerprint(db_path)
    if meta.db_size != size or meta.db_mtime != mtime:
        return None
    nodes = json.loads(nodes_path.read_text(encoding="utf-8"))
    node_to_int = {nid:i for i,nid in enumerate(nodes)}
    edges = _read_edges_bin(edges_path)
    # build adjacency
    n = len(nodes)
    adj = [[] for _ in range(n)]
    radj = [[] for _ in range(n)]
    for u,v in edges:
        if u < n and v < n:
            adj[u].append(v)
            radj[v].append(u)
    return Graph(nodes=nodes, node_to_int=node_to_int, adj=adj, radj=radj)

def get_or_build_graph(db_path: str, cache_dir: Path) -> Tuple[Graph, bool]:
    g = load_graph_cache(db_path, cache_dir)
    if g is not None:
        return g, True
    return build_graph_cache(db_path, cache_dir), False
