from __future__ import annotations

from dataclasses import dataclass
from typing import Any


@dataclass
class JAuditError(Exception):
    code: str
    message: str
    details: dict[str, Any] | None = None

    def to_json(self) -> dict[str, Any]:
        out: dict[str, Any] = {"ok": False, "code": self.code, "message": self.message}
        if self.details:
            out["details"] = self.details
        return out


def fail(code: str, message: str, details: dict[str, Any] | None = None) -> JAuditError:
    return JAuditError(code=code, message=message, details=details)


