#!/usr/bin/env python3
"""Navigates the seeded debug build and captures the four canonical Play
Store screenshot views (Routines/Today, Tasks/Work, Tasks/Shopping,
Settings) for GitHub issue #169. Not part of the CI/app build.

Usage: capture-screenshot-set.py <device-serial> <output-dir> <prefix>
  e.g. capture-screenshot-set.py emulator-5554 distribution/screenshots/phone phone_light
"""
import json
import subprocess
import sys
import tempfile
import time
from pathlib import Path

PKG = "com.mandrecode.tempo.debug"
ACTIVITY = "com.mandrecode.tempo.MainActivity"


def adb(serial, *args, check=True):
    return subprocess.run(["adb", "-s", serial, *args], capture_output=True, text=True, check=check)


def dump_layout(serial, retries=8, delay=1.5):
    with tempfile.NamedTemporaryFile(suffix=".json", delete=False) as f:
        path = f.name
    try:
        last_err = None
        for _ in range(retries):
            result = subprocess.run(
                ["android", "layout", "--device", serial, "-o", path],
                capture_output=True,
                text=True,
            )
            try:
                with open(path) as f:
                    return json.load(f)
            except json.JSONDecodeError as e:
                stderr = result.stderr.strip()
                last_err = f"{e} (stderr: {stderr})" if stderr else str(e)
                time.sleep(delay)
        raise RuntimeError(f"Could not get a UI dump after {retries} retries: {last_err}")
    finally:
        Path(path).unlink(missing_ok=True)


def find_center(nodes, desc=None, text=None):
    # content-desc is the more reliable accessibility label, but some form
    # factors (e.g. desktop windowing) only set plain text on nav items, so
    # try desc across all nodes first, then fall back to text.
    if desc is not None:
        for n in nodes:
            if n.get("content-desc") == desc:
                return n["center"]
    if text is not None:
        for n in nodes:
            if n.get("text") == text:
                return n["center"]
    raise RuntimeError(f"Element not found: desc={desc!r} text={text!r}")


def tap_center(serial, center):
    # center is a string like "[540,2227]"
    x, y = center.strip("[]").split(",")
    adb(serial, "shell", "input", "tap", x.strip(), y.strip())


def tap(serial, desc=None, text=None, settle=1.0):
    nodes = dump_layout(serial)
    center = find_center(nodes, desc=desc, text=text or desc)
    tap_center(serial, center)
    time.sleep(settle)


def screencap(serial, out_path: Path):
    adb(serial, "shell", "screencap", "-p", "/sdcard/_shot.png")
    adb(serial, "pull", "/sdcard/_shot.png", str(out_path))
    adb(serial, "shell", "rm", "-f", "/sdcard/_shot.png")


def main():
    if len(sys.argv) != 4:
        print(__doc__, file=sys.stderr)
        sys.exit(1)
    serial, out_dir, prefix = sys.argv[1], sys.argv[2], sys.argv[3]
    out = Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)

    adb(serial, "shell", "am", "force-stop", PKG)
    adb(serial, "shell", "am", "start", "-n", f"{PKG}/{ACTIVITY}")
    time.sleep(4)

    # 1. Routines / Today
    tap(serial, desc="Routines", settle=1.5)
    screencap(serial, out / f"{prefix}_1_routines_today.png")

    # 2. Tasks / Work
    tap(serial, desc="Tasks", settle=1.0)
    tap(serial, text="Work", settle=1.0)
    screencap(serial, out / f"{prefix}_2_tasks_work.png")

    # 3. Tasks / Shopping
    tap(serial, text="Shopping", settle=1.0)
    screencap(serial, out / f"{prefix}_3_tasks_shopping.png")

    # 4. Settings
    tap(serial, desc="Settings", settle=1.0)
    screencap(serial, out / f"{prefix}_4_settings.png")
    adb(serial, "shell", "input", "keyevent", "KEYCODE_BACK")

    print(f"Captured 4 screenshots into {out}")


if __name__ == "__main__":
    main()
