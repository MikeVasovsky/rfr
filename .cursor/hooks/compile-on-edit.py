#!/usr/bin/env python3
"""postToolUse: after Write/StrReplace, compile/lint only the affected module; inject output."""

from __future__ import annotations

import json
import subprocess
import sys
from pathlib import Path

MAX_OUTPUT_CHARS = 12_000
GRADLE_TIMEOUT_SEC = 180
FRONT_TIMEOUT_SEC = 120

JAVA_MAIN_MODULES = frozenset({"rfr-api", "rfr-auth"})
FRONTEND_EXTS = frozenset({".ts", ".tsx"})


def _emit(ctx: str | None) -> None:
    if ctx:
        json.dump({"additional_context": ctx}, sys.stdout)
    else:
        json.dump({}, sys.stdout)
    sys.stdout.write("\n")


def _tool_input(payload: dict) -> dict:
    raw = payload.get("tool_input")
    if isinstance(raw, dict):
        return raw
    if isinstance(raw, str) and raw.strip():
        try:
            parsed = json.loads(raw)
            return parsed if isinstance(parsed, dict) else {}
        except json.JSONDecodeError:
            return {}
    return {}


def _edited_path(payload: dict) -> str:
    ti = _tool_input(payload)
    for key in ("path", "file_path", "filePath"):
        val = ti.get(key)
        if isinstance(val, str) and val.strip():
            return val.strip()
    top = payload.get("file_path")
    if isinstance(top, str) and top.strip():
        return top.strip()
    return ""


def _workspace_root(payload: dict) -> Path:
    roots = payload.get("workspace_roots")
    if isinstance(roots, list) and roots:
        return Path(str(roots[0])).resolve()
    cwd = payload.get("cwd")
    if isinstance(cwd, str) and cwd.strip():
        return Path(cwd).resolve()
    return Path.cwd().resolve()


def _rel_posix(path: Path, root: Path) -> str | None:
    try:
        rel = path.resolve().relative_to(root)
    except ValueError:
        return None
    return rel.as_posix()


def _truncate(text: str) -> str:
    text = text.strip()
    if len(text) <= MAX_OUTPUT_CHARS:
        return text
    return "…(truncated)…\n" + text[-MAX_OUTPUT_CHARS:]


def _run(cmd: list[str], *, cwd: Path, timeout: int) -> tuple[int, str]:
    try:
        proc = subprocess.run(
            cmd,
            cwd=str(cwd),
            capture_output=True,
            text=True,
            timeout=timeout,
        )
    except subprocess.TimeoutExpired as exc:
        out = (exc.stdout or "") + (exc.stderr or "")
        return 124, _truncate(out + f"\n[timeout after {timeout}s]")
    except FileNotFoundError as exc:
        return 127, str(exc)

    combined = ""
    if proc.stdout:
        combined += proc.stdout
    if proc.stderr:
        if combined and not combined.endswith("\n"):
            combined += "\n"
        combined += proc.stderr
    return proc.returncode, _truncate(combined)


def _plan_checks(rel: str, root: Path) -> list[tuple[str, list[str], Path, int]]:
    """Return list of (label, cmd, cwd, timeout) for the affected module only."""
    parts = rel.split("/")
    if not parts or parts[0] in (".",):
        return []
    if "build" in parts or "node_modules" in parts or "dist" in parts:
        return []

    top = parts[0]

    if top in JAVA_MAIN_MODULES:
        if not rel.endswith(".java"):
            return []
        module = top
        joined = "/" + rel
        task = (
            f":{module}:compileTestJava"
            if "/src/test/" in joined
            else f":{module}:compileJava"
        )
        return [
            (
                f"Java compile ({task}) after edit of {rel}",
                ["./gradlew", task, "--console=plain"],
                root,
                GRADLE_TIMEOUT_SEC,
            )
        ]

    if top == "rfr-e2e":
        if not rel.endswith(".java"):
            return []
        task = ":rfr-e2e:compileTestJava"
        return [
            (
                f"Java compile ({task}) after edit of {rel}",
                ["./gradlew", task, "--console=plain"],
                root,
                GRADLE_TIMEOUT_SEC,
            )
        ]

    if top == "rfr-gql-client":
        suffix = Path(rel).suffix.lower()
        if suffix not in FRONTEND_EXTS:
            return []
        client_root = root / "rfr-gql-client"
        # Flat ESLint config: run from rfr-gql-client with path relative to that folder.
        rel_in_client = Path(rel).relative_to("rfr-gql-client").as_posix()
        return [
            (
                f"Frontend tsc --noEmit after edit of {rel}",
                ["npx", "tsc", "--noEmit"],
                client_root,
                FRONT_TIMEOUT_SEC,
            ),
            (
                f"Frontend eslint after edit of {rel} (cwd rfr-gql-client, file {rel_in_client})",
                [
                    "npx",
                    "eslint",
                    rel_in_client,
                    "--report-unused-disable-directives",
                    "--max-warnings",
                    "0",
                ],
                client_root,
                FRONT_TIMEOUT_SEC,
            ),
        ]

    return []


def main() -> int:
    try:
        payload = json.load(sys.stdin)
    except json.JSONDecodeError:
        _emit("compile-on-edit hook: invalid JSON on stdin")
        return 0

    if not isinstance(payload, dict):
        _emit(None)
        return 0

    tool = str(payload.get("tool_name") or "")
    if tool not in ("Write", "StrReplace"):
        _emit(None)
        return 0

    edited = _edited_path(payload)
    if not edited:
        _emit(None)
        return 0

    root = _workspace_root(payload)
    path = Path(edited)
    if not path.is_absolute():
        path = root / path

    rel = _rel_posix(path, root)
    if rel is None:
        _emit(None)
        return 0

    checks = _plan_checks(rel, root)
    if not checks:
        _emit(None)
        return 0

    sections: list[str] = []
    worst = 0
    for label, cmd, cwd, timeout in checks:
        if not cwd.is_dir():
            sections.append(f"{label}\n[skip] cwd missing: {cwd}")
            worst = max(worst, 1)
            continue
        code, output = _run(cmd, cwd=cwd, timeout=timeout)
        worst = max(worst, 0 if code == 0 else code)
        status = "OK" if code == 0 else "FAILED"
        block = f"[compile-on-edit] {status} (exit {code})\n{label}\n$ {' '.join(cmd)}\n"
        block += output if output else "(no output)"
        sections.append(block)

    header = "[compile-on-edit] ALL OK" if worst == 0 else f"[compile-on-edit] FAILED (worst exit {worst})"
    _emit(header + "\n\n" + "\n\n".join(sections))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
