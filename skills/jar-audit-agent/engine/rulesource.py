from __future__ import annotations
import json, hashlib
from dataclasses import dataclass
from pathlib import Path
from typing import Any
import yaml

@dataclass
class RuleSnapshot:
    name: str
    path: str | None
    sha256: str | None
    parsed_path: str | None
    loaded: bool
    notes: list[str]

def _sha256_bytes(b: bytes) -> str:
    return hashlib.sha256(b).hexdigest()

class _IgnoreTagsLoader(yaml.SafeLoader):
    pass

def _construct_unknown(loader: yaml.Loader, node: yaml.Node):
    if isinstance(node, yaml.MappingNode):
        return loader.construct_mapping(node)
    if isinstance(node, yaml.SequenceNode):
        return loader.construct_sequence(node)
    return loader.construct_scalar(node)

# ignore any unknown tags like !!me.n1ar4...
_IgnoreTagsLoader.add_constructor(None, _construct_unknown)
_IgnoreTagsLoader.add_multi_constructor("!", lambda loader, tag_suffix, node: _construct_unknown(loader, node))

def load_vulnerability_yaml(path: str) -> dict[str, Any]:
    data = Path(path).read_bytes()
    obj = yaml.load(data, Loader=_IgnoreTagsLoader)
    if obj is None:
        return {}
    # normalize: expect {"vulnerabilities": {key: [SearchCondition...]}} or a direct map
    if isinstance(obj, dict) and "vulnerabilities" in obj and isinstance(obj["vulnerabilities"], dict):
        return obj["vulnerabilities"]
    if isinstance(obj, dict):
        return obj
    return {}

def load_dfs_sink_json(path: str) -> list[dict[str, Any]]:
    data = json.loads(Path(path).read_text(encoding="utf-8"))
    if isinstance(data, list):
        return data
    return []

def find_rule_file(filename: str, cwd: str | None = None) -> str | None:
    base = Path(cwd or ".").resolve()
    p = base / filename
    return p.as_posix() if p.exists() else None

def snapshot_rules(run_root: Path, cwd: str | None = None) -> dict[str, RuleSnapshot]:
    rules_dir = run_root / "rules"
    rules_dir.mkdir(parents=True, exist_ok=True)

    out: dict[str, RuleSnapshot] = {}

    # vulnerability.yaml
    vpath = find_rule_file("vulnerability.yaml", cwd=cwd)
    if vpath:
        b = Path(vpath).read_bytes()
        h = _sha256_bytes(b)
        try:
            parsed = load_vulnerability_yaml(vpath)
            parsed_path = (rules_dir / "vulnerability.parsed.json").as_posix()
            Path(parsed_path).write_text(json.dumps(parsed, ensure_ascii=False, indent=2), encoding="utf-8")
            out["vulnerability"] = RuleSnapshot("vulnerability", vpath, h, parsed_path, True, [])
        except Exception as e:
            out["vulnerability"] = RuleSnapshot("vulnerability", vpath, h, None, False, [f"parse_error: {e}"])
    else:
        out["vulnerability"] = RuleSnapshot("vulnerability", None, None, None, False, ["not_found"])

    # dfs-sink.json
    dpath = find_rule_file("dfs-sink.json", cwd=cwd)
    if dpath:
        b = Path(dpath).read_bytes()
        h = _sha256_bytes(b)
        try:
            parsed = load_dfs_sink_json(dpath)
            parsed_path = (rules_dir / "dfs_sink.parsed.json").as_posix()
            Path(parsed_path).write_text(json.dumps(parsed, ensure_ascii=False, indent=2), encoding="utf-8")
            out["dfs_sink"] = RuleSnapshot("dfs_sink", dpath, h, parsed_path, True, [])
        except Exception as e:
            out["dfs_sink"] = RuleSnapshot("dfs_sink", dpath, h, None, False, [f"parse_error: {e}"])
    else:
        out["dfs_sink"] = RuleSnapshot("dfs_sink", None, None, None, False, ["not_found"])

    # persist summary
    summary = {
        k: {
            "name": v.name,
            "path": v.path,
            "sha256": v.sha256,
            "parsed_path": v.parsed_path,
            "loaded": v.loaded,
            "notes": v.notes,
        }
        for k, v in out.items()
    }
    (rules_dir / "rules_manifest.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8")
    return out

def load_snapshot_json(path: str) -> Any:
    return json.loads(Path(path).read_text(encoding="utf-8"))

def sinks_from_dfs(parsed: list[dict[str, Any]], box_names: list[str]) -> list[dict[str, Any]]:
    want = set(box_names)
    out = []
    for item in parsed:
        bn = item.get("boxName")
        if bn in want:
            out.append({
                "class": item.get("className"),
                "method": item.get("methodName"),
                "desc": item.get("methodDesc"),
                "level": "high",
                "source": f"dfs_sink:{bn}",
            })
    return out

def sinks_from_vulnerability(parsed: dict[str, Any], keys: list[str]) -> list[dict[str, Any]]:
    out = []
    for k in keys:
        items = parsed.get(k)
        if not items:
            continue
        if not isinstance(items, list):
            continue
        for it in items:
            # SearchCondition fields may be className/methodName/methodDesc/level
            out.append({
                "class": it.get("className") or it.get("class") or "",
                "method": it.get("methodName") or it.get("method") or "",
                "desc": it.get("methodDesc") or it.get("desc"),
                "level": (it.get("level") or "medium").lower(),
                "source": f"vulnerability:{k}",
            })
    return out


