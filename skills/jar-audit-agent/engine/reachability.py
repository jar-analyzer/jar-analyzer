
from __future__ import annotations
from collections import deque
from dataclasses import dataclass
from typing import Dict, List, Optional, Tuple

from .graph_cache import Graph

@dataclass
class ReachResult:
    reachable: bool
    depth: Optional[int]
    entry_node_id: Optional[str]
    entry_mapping: Optional[str]
    chain_node_ids: Optional[List[str]]  # entry -> ... -> candidate

def multi_source_bfs(graph: Graph, entry_ints: List[int], max_depth: int) -> Tuple[List[int], List[int], List[int]]:
    """
    BFS from multiple entry nodes, limited by max_depth.
    Returns:
      dist[int] = distance from nearest entry or -1
      parent[int] = predecessor int on shortest path tree or -1
      origin[int] = which entry int this node is reached from, or -1
    """
    n = len(graph.nodes)
    dist = [-1] * n
    parent = [-1] * n
    origin = [-1] * n
    q = deque()

    for e in entry_ints:
        if 0 <= e < n and dist[e] == -1:
            dist[e] = 0
            origin[e] = e
            q.append(e)

    while q:
        u = q.popleft()
        d = dist[u]
        if d >= max_depth:
            continue
        for v in graph.adj[u]:
            if dist[v] != -1:
                continue
            dist[v] = d + 1
            parent[v] = u
            origin[v] = origin[u]
            q.append(v)
    return dist, parent, origin

def reconstruct_chain(graph: Graph, parent: List[int], start: int) -> List[str]:
    path_ints = []
    cur = start
    while cur != -1:
        path_ints.append(cur)
        cur = parent[cur]
    path_ints.reverse()
    return [graph.nodes[i] for i in path_ints]

def compute_reachability(
    graph: Graph,
    entries: Dict[str, str],  # node_id -> "REST PATH"
    candidates: List[str],     # candidate node_ids
    max_depth: int
) -> Dict[str, ReachResult]:
    entry_ints = []
    for nid in entries.keys():
        i = graph.node_to_int.get(nid)
        if i is not None:
            entry_ints.append(i)

    dist, parent, origin = multi_source_bfs(graph, entry_ints, max_depth=max_depth)

    out: Dict[str, ReachResult] = {}
    for cand in candidates:
        ci = graph.node_to_int.get(cand)
        if ci is None or dist[ci] == -1:
            out[cand] = ReachResult(False, None, None, None, None)
            continue
        o = origin[ci]
        entry_nid = graph.nodes[o] if o != -1 else None
        entry_map = entries.get(entry_nid) if entry_nid else None
        chain = reconstruct_chain(graph, parent, ci)
        out[cand] = ReachResult(True, dist[ci], entry_nid, entry_map, chain)
    return out
