from __future__ import annotations
import sqlite3
from typing import Any, Iterable

class DB:
    def __init__(self, path: str):
        self.path = path

    def connect(self) -> sqlite3.Connection:
        conn = sqlite3.connect(self.path)
        conn.row_factory = sqlite3.Row
        return conn

    def q(self, sql: str, params: Iterable[Any] = ()) -> list[dict[str, Any]]:
        with self.connect() as conn:
            cur = conn.execute(sql, list(params))
            rows = cur.fetchall()
        return [dict(r) for r in rows]

    def q1(self, sql: str, params: Iterable[Any] = ()) -> dict[str, Any] | None:
        rows = self.q(sql, params)
        return rows[0] if rows else None
