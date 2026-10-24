#!/usr/bin/env python3
"""beforeShellExecution: deny git push and file-delete commands (rm/rmdir/unlink/git rm)."""

from __future__ import annotations

import json
import re
import sys

# git … push (allows flags between git and push: git -C x push, git push -u …)
_GIT_PUSH = re.compile(r"(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+push\b")

# File deletion: rm / rmdir / unlink as a command word; also git rm
_DELETE = re.compile(
    r"(?:^|[\s;|&])(?:rm|rmdir|unlink)\b"
    r"|(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+rm\b"
)


def _strip_quoted(s: str) -> str:
    """Remove single/double-quoted spans so words inside strings do not trigger."""
    s = re.sub(r"'(?:\\.|[^'\\])*'", " ", s)
    s = re.sub(r'"(?:\\.|[^"\\])*"', " ", s)
    return s


def _deny(user_message: str, agent_message: str) -> None:
    json.dump(
        {
            "permission": "deny",
            "user_message": user_message,
            "agent_message": agent_message,
        },
        sys.stdout,
    )
    sys.stdout.write("\n")


def _allow() -> None:
    json.dump({"permission": "allow"}, sys.stdout)
    sys.stdout.write("\n")


def main() -> int:
    try:
        payload = json.load(sys.stdin)
    except json.JSONDecodeError:
        _deny(
            "Shell permission hook received invalid JSON.",
            "Hook input was not valid JSON; command blocked (fail-closed).",
        )
        return 0

    command = payload.get("command") or ""
    if not isinstance(command, str):
        command = str(command)

    scanned = _strip_quoted(command)

    if _GIT_PUSH.search(scanned):
        _deny(
            "Blocked: git push is disabled for the agent in this repo.",
            "Do not run git push (rule 00 + shell hook). Leave pushing to the user.",
        )
        return 0

    if _DELETE.search(scanned):
        _deny(
            "Blocked: rm/rmdir/unlink (and git rm) are disabled. Use .bak instead.",
            "Do not delete files with rm/rmdir/unlink/git rm. Rename or copy to a .bak path "
            "(e.g. mv path path.bak) instead of removing.",
        )
        return 0

    # Matcher fired, but not a real deny case (e.g. word "push" in a string).
    _allow()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
