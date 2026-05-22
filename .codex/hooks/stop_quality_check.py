#!/usr/bin/env python3
import json
import subprocess
from pathlib import Path


KTLINT_SCOPES = (
    "app/src/main/java/org/stepik/",
)


def run(command: list[str], cwd: Path, timeout: int = 120) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        command,
        cwd=cwd,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        timeout=timeout,
        check=False,
    )


def repo_root(cwd: Path) -> Path:
    result = run(["git", "rev-parse", "--show-toplevel"], cwd, timeout=10)
    if result.returncode == 0:
        return Path(result.stdout.strip())
    return cwd


def changed_files(root: Path) -> list[str]:
    result = run(["git", "status", "--porcelain=v1"], root, timeout=10)
    if result.returncode != 0:
        return []

    paths: list[str] = []
    for line in result.stdout.splitlines():
        raw = line[3:]
        if " -> " in raw:
            raw = raw.rsplit(" -> ", 1)[1]
        if raw:
            paths.append(raw)
    return paths


def should_run_ktlint(paths: list[str]) -> bool:
    return any(
        path.endswith(".kt") and path.startswith(KTLINT_SCOPES)
        for path in paths
    )


def main() -> int:
    try:
        payload = json.load(open(0))
    except Exception:
        payload = {}

    if payload.get("stop_hook_active"):
        return 0

    root = repo_root(Path(payload.get("cwd") or ".").resolve())
    paths = changed_files(root)

    if not should_run_ktlint(paths):
        return 0

    result = run(["./gradlew", ":app:ktlint"], root, timeout=180)
    if result.returncode == 0:
        return 0

    tail = "\n".join(result.stdout.splitlines()[-80:])
    reason = (
        "`./gradlew :app:ktlint` failed for changed Kotlin files. "
        "Inspect the output, fix relevant formatting/style issues, and rerun the check.\n\n"
        + tail
    )
    print(json.dumps({
        "decision": "block",
        "reason": reason
    }))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
