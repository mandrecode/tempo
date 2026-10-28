"""Shared device helpers for the screenshot pipeline (import-seed-backup.py and
capture-screenshot-set.py). Not part of the CI/app build.

Both scripts drive the real UI by reading `android layout` dumps, so the dump
parsing lives here once: when the CLI's output format changes, it is fixed in
one place rather than drifting between two copies.
"""
import json
import subprocess
import tempfile
import time
from pathlib import Path


class MissingAndroidCli(RuntimeError):
    """Not a transient UI hiccup — retrying can't conjure the CLI."""


def adb(serial, *args, check=True):
    return subprocess.run(["adb", "-s", serial, *args], capture_output=True, text=True, check=check)


def focused_windows(nodes):
    """When the dump is split per window, keep only the focused one(s). On XR
    every window in the scene is reported — the home environment, System UI,
    a Gboard settings screen left open — and their labels ("Settings") would
    otherwise match before the app's own. Dialogs and the system file picker
    take focus while they're up, so they're still what gets searched."""
    focused = [n for n in nodes if n.get("focused")]
    return focused or nodes


def flatten(nodes):
    """Newer `android layout` releases nest each window's nodes under its own
    `content` list (system bars, then the app) instead of returning one flat
    list, and spell `interactions` in upper case. Flatten either shape and
    lower-case the flags so lookups see every node the same way."""
    out = []
    for n in nodes:
        if "interactions" in n:
            n["interactions"] = [i.lower() for i in n["interactions"]]
        out.append(n)
        out.extend(flatten(n.get("content", [])))
        out.extend(flatten(n.get("children", [])))
    return out


# Generous retries: once a session is running its countdown redraws every
# second, and on a slow emulator (XR especially) `android layout` can fail
# several times in a row to find the UI idle long enough to dump it.
def dump_layout(serial, retries=20, delay=1.5):
    """The focused window's nodes as one flat list, retried until a dump parses."""
    with tempfile.NamedTemporaryFile(suffix=".json", delete=False) as f:
        path = f.name
    try:
        last_err = None
        for _ in range(retries):
            try:
                result = subprocess.run(
                    ["android", "layout", "--device", serial, "-o", path],
                    capture_output=True,
                    text=True,
                )
            except FileNotFoundError as e:
                raise MissingAndroidCli(
                    "The `android` CLI was not found on PATH. It's required to inspect the "
                    "app's UI tree for navigation — install the Android SDK's `android` CLI "
                    "(see AGENTS.md's android-cli conventions) and try again.",
                ) from e
            try:
                with open(path) as f:
                    return flatten(focused_windows(json.load(f)))
            except json.JSONDecodeError as e:
                stderr = result.stderr.strip()
                last_err = f"{e} (stderr: {stderr})" if stderr else str(e)
                time.sleep(delay)
        raise RuntimeError(f"Could not get a UI dump after {retries} retries: {last_err}")
    finally:
        Path(path).unlink(missing_ok=True)


def tap_center(serial, center):
    x, y = center.strip("[]").split(",")
    adb(serial, "shell", "input", "tap", x.strip(), y.strip())
