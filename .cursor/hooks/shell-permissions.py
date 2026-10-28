#!/usr/bin/env python3
"""beforeShellExecution: block destructive shell (delete / force-push / history wipe)."""

from __future__ import annotations

import json
import re
import sys

# --- push & push-equivalents (agent never pushes) ---
_GIT_PUSH = re.compile(
    r"(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+push\b"
)
_GIT_SEND_PACK = re.compile(
    r"(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+send-pack\b"
)

# --- file deletion tools (use .bak instead) ---
_DELETE_TOOL = re.compile(
    r"(?:^|[\s;|&])(?:rm|rmdir|unlink|shred|srm|wipe|truncate)\b"
    r"|(?:^|[\s;|&])(?:busybox|toybox)\s+(?:rm|unlink|rmdir)\b"
)

# find … -delete / -exec rm|unlink / -ok rm
_FIND_DELETE = re.compile(
    r"(?:^|[\s;|&])find\b[\s\S]*?(?:"
    r"-delete\b"
    r"|-exec(?:dir)?\s+(?:rm|unlink)\b"
    r"|-ok(?:dir)?\s+(?:rm|unlink)\b"
    r")"
)

# --- git working-tree / index destruction ---
_GIT_RM = re.compile(
    r"(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+rm\b"
)
_GIT_CLEAN = re.compile(
    r"(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+clean\b"
)
_GIT_RESET_HARD = re.compile(
    r"(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+reset\b[\s\S]*?--hard\b"
)
# discard uncommitted work: checkout -- PATH, restore (worktree), switch --discard-changes
_GIT_DISCARD = re.compile(
    # `git checkout -- PATH` discards worktree; avoid `\b` after `--` (no word char follows)
    r"(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+checkout\s+--(?:\s|$)"
    r"|(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+restore\b"
    r"|(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+switch\b[\s\S]*?--discard-changes\b"
)

# --- local ref / history destruction ---
_GIT_BRANCH_FORCE_D = re.compile(
    r"(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+branch\b[\s\S]*?(?:-D\b|--delete\s+--force\b)"
)
_GIT_TAG_D = re.compile(
    r"(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+tag\b[\s\S]*?(?:-d\b|--delete\b)"
)
_GIT_UPDATE_REF_D = re.compile(
    r"(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+update-ref\b[\s\S]*?(?:-d\b|--delete\b)"
)
_GIT_HISTORY_REWRITE = re.compile(
    r"(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+"
    r"(?:filter-branch|filter-repo)\b"
    r"|(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+reflog\s+expire\b"
    r"|(?:^|[\s;|&])git(?:\s+-[^\s]+|\s+--[^\s=]+(?:=[^\s]+)?)*\s+gc\b[\s\S]*?--prune\b"
)


def _strip_quoted(s: str) -> str:
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

    if _GIT_PUSH.search(scanned) or _GIT_SEND_PACK.search(scanned):
        _deny(
            "Blocked: git push / send-pack is disabled for the agent.",
            "Do not run git push or git send-pack (rule 00 + shell hook). Leave pushing to the user.",
        )
        return 0

    if (
        _DELETE_TOOL.search(scanned)
        or _FIND_DELETE.search(scanned)
        or _GIT_RM.search(scanned)
    ):
        _deny(
            "Blocked: file deletion is disabled. Use .bak instead.",
            "Do not delete with rm/rmdir/unlink/shred/srm/wipe/truncate, busybox/toybox rm, "
            "find -delete/-exec rm, or git rm. Rename or copy to a .bak path instead.",
        )
        return 0

    if _GIT_CLEAN.search(scanned) or _GIT_RESET_HARD.search(scanned) or _GIT_DISCARD.search(
        scanned
    ):
        _deny(
            "Blocked: discarding/cleaning the working tree is disabled for the agent.",
            "Do not run git clean, git reset --hard, git checkout --, git restore, or "
            "git switch --discard-changes. Leave destructive git cleanup to the user.",
        )
        return 0

    if (
        _GIT_BRANCH_FORCE_D.search(scanned)
        or _GIT_TAG_D.search(scanned)
        or _GIT_UPDATE_REF_D.search(scanned)
        or _GIT_HISTORY_REWRITE.search(scanned)
    ):
        _deny(
            "Blocked: destructive git ref/history command is disabled for the agent.",
            "Do not run git branch -D, tag -d, update-ref -d, filter-branch/filter-repo, "
            "reflog expire, or gc --prune. Leave history/ref destruction to the user.",
        )
        return 0

    _allow()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
