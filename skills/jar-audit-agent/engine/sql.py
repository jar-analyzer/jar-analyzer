from __future__ import annotations

import os
import sqlite3
from dataclasses import dataclass
from typing import Any, Iterable

from .db import DB


@dataclass
class SchemaLock:
    ok: bool
    missing: list[str]


class SQL:
    """SQLite helpers used for freeze/coverage proof. Read-only by convention."""

    def __init__(self, db_path: str):
        self.db_path = db_path
        self.db = DB(db_path)

    def schema_lock(self) -> SchemaLock:
        required_tables = [
            "class_table",
            "method_table",
            "method_call_table",
            "spring_method_table",
        ]
        with sqlite3.connect(self.db_path) as conn:
            cur = conn.execute("SELECT name FROM sqlite_master WHERE type='table'")
            present = {r[0] for r in cur.fetchall()}
        missing = [t for t in required_tables if t not in present]
        return SchemaLock(ok=(len(missing) == 0), missing=missing)

    def db_fingerprint(self) -> dict[str, Any]:
        st = os.stat(self.db_path)
        return {"path": self.db_path, "size": int(st.st_size), "mtime": int(st.st_mtime)}

    def q(self, sql: str, params: Iterable[Any] = ()) -> list[dict[str, Any]]:
        return self.db.q(sql, params)

    def q1(self, sql: str, params: Iterable[Any] = ()) -> dict[str, Any] | None:
        return self.db.q1(sql, params)


