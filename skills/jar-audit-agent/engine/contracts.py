from __future__ import annotations

from dataclasses import dataclass
from typing import Any


@dataclass
class ContractResult:
    ok: bool
    message: str
    details: dict[str, Any] | None = None


def require_fields(obj: dict[str, Any], fields: list[str]) -> ContractResult:
    missing = [f for f in fields if f not in obj or obj.get(f) in (None, "")]
    if missing:
        return ContractResult(False, "missing required fields", {"missing": missing})
    return ContractResult(True, "ok")


def evidence_contract_verify_record(record: dict[str, Any]) -> ContractResult:
    base = require_fields(record, ["candidate_id", "vector", "batch_id", "status", "evidence"])
    if not base.ok:
        return base
    ev = record.get("evidence")
    if not isinstance(ev, dict):
        return ContractResult(False, "evidence must be object")
    if not any(k in ev for k in ("snippet_ref", "chain_trace", "sql_proof")):
        return ContractResult(False, "missing evidence pointer: snippet_ref/chain_trace/sql_proof")
    return ContractResult(True, "ok")


def batch_schema(batch: dict[str, Any]) -> ContractResult:
    base = require_fields(batch, ["batch_id", "vector", "created_at"])
    if not base.ok:
        return base
    # candidates is optional for empty batch, but if present it must be list
    if "candidates" in batch and not isinstance(batch["candidates"], list):
        return ContractResult(False, "batch.candidates must be list")
    return ContractResult(True, "ok")


