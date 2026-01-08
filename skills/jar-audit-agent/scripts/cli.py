#!/usr/bin/env python3
from __future__ import annotations

import sys
from pathlib import Path

_ROOT = Path(__file__).resolve().parents[1]
if str(_ROOT) not in sys.path:
    sys.path.insert(0, str(_ROOT))

# Temporary compatibility wrapper:
# This keeps `scripts/cli.py` as the single entry point while we progressively
# migrate `scripts/jaudit.py` logic into engine/session/contracts/sql/... modules.
from scripts.jaudit import main

if __name__ == "__main__":
    main()


