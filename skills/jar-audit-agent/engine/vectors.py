from __future__ import annotations
from dataclasses import dataclass, field
from pathlib import Path
import yaml

@dataclass
class Sink:
    class_name: str
    method_name: str
    method_desc: str | None
    level: str = "medium"
    source: str = "vector"

@dataclass
class RuleSources:
    dfs_sink_box: list[str] = field(default_factory=list)
    vulnerability_keys: list[str] = field(default_factory=list)

@dataclass
class ReduceCfg:
    max_candidates: int = 200
    exclude_class_prefixes: list[str] = field(default_factory=list)
    prefer_app_prefix: bool = True

@dataclass
class VectorCfg:
    id: str
    name: str
    sinks: list[Sink]
    max_depth: int
    evidence_engine: str
    max_chars: int
    window_lines: int
    batch_size: int
    rule_sources: RuleSources = field(default_factory=RuleSources)
    reduce: ReduceCfg = field(default_factory=ReduceCfg)

def load_vector(path: str) -> VectorCfg:
    data = yaml.safe_load(Path(path).read_text(encoding="utf-8"))
    d = data.get("discovery", {}) or {}
    sinks = []
    for s in (d.get("sinks") or []):
        sinks.append(Sink(s["class"], s["method"], s.get("desc"), (s.get("level") or "medium").lower(), "vector"))
    rs = d.get("rule_sources", {}) or {}
    rule_sources = RuleSources(
        dfs_sink_box=list(rs.get("dfs_sink_box") or []),
        vulnerability_keys=list(rs.get("vulnerability_keys") or []),
    )
    red = data.get("reduce", {}) or {}
    reduce_cfg = ReduceCfg(
        max_candidates=int(red.get("max_candidates", 200)),
        exclude_class_prefixes=list(red.get("exclude_class_prefixes") or []),
        prefer_app_prefix=bool(red.get("prefer_app_prefix", True)),
    )
    return VectorCfg(
        id=data["id"],
        name=data["name"],
        sinks=sinks,
        rule_sources=rule_sources,
        reduce=reduce_cfg,
        max_depth=int(data.get("reachability", {}).get("max_depth", 12)),
        evidence_engine=str(data.get("evidence", {}).get("engine", "fernflower")),
        max_chars=int(data.get("evidence", {}).get("max_chars", 6000)),
        window_lines=int(data.get("evidence", {}).get("window_lines", 25)),
        batch_size=int(data.get("batch", {}).get("size", 5)),
    )
