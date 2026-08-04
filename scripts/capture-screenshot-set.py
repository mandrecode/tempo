#!/usr/bin/env python3
"""Navigates the seeded debug build and captures the four canonical Play
Store screenshot views (Focus/Today, a running Focus session, Routines/
Today, Tasks/Personal-category) for GitHub issues #169 and #318. Not part
of the CI/app build.

The order is also the story the listing tells: the day as Focus sees it,
one session started on it, then the two tabs the session keeps running
behind. Once a session is under way the navigation says so — a live
countdown where there is room for one, a tinted Focus tab where there
isn't — which is why the session is started before Routines and Tasks are
captured rather than after.

Usage: capture-screenshot-set.py <device-serial> <output-dir> <prefix> <locale>
  e.g. capture-screenshot-set.py emulator-5554 distribution/screenshots/phone phone_en_light en
"""
import json
import subprocess
import sys
import tempfile
import time
from pathlib import Path

PKG = "com.mandrecode.tempo.debug"
ACTIVITY = "com.mandrecode.tempo.MainActivity"

# Extra pause between the last tap and `adb screencap`. The per-tap settle in
# `tap()` is enough for the app to update its own view tree (which `android
# layout` reads), but on XR the screenshot comes from the compositor's render of
# the floating panel, which lags behind — without this the capture reliably grabs
# the *previous* screen, silently producing a set where every image is one
# navigation step stale.
CAPTURE_SETTLE_SECONDS = 3.0

# How long to keep re-reading the view tree waiting for a screen to finish
# rendering. A fixed sleep is not enough: XR is slow enough that Focus was still
# showing its loading indicator when the shutter went, and the result — a blank
# panel with the app's spinner in the middle — looks like a legitimately empty
# screen rather than a failure. Every capture below therefore waits for a piece
# of content only the finished screen has.
READY_TIMEOUT_SECONDS = 90.0
READY_POLL_SECONDS = 2.0

# Nav labels (Focus/Routines/Tasks) come from the app's own string resources,
# localized by the per-app locale set in seed-screenshot-data.sh. The "category"
# chip label is seeded data (see generate-seed-backup.py) and must match exactly
# what was inserted there.
#
# That category is Personal rather than Work because it is the one seeded
# category with nothing completed in it. A Tasks tab that has completed tasks to
# clear gains a clear-completed action, and the floating bar gives up the
# session countdown to make room for it (`hasContextualActions` in
# PersistentFloatingBar.kt) — which would take the running session out of the
# one shot that exists to show it still running.
#
# `start` is only the *verb* of `focus_session_start` ("Start %1$s"), because
# the rest of that label is the session length from Settings — matching the
# whole string would mean hardcoding 25 minutes here and silently failing the
# day that default changes.
#
# `focusing`, `chain` and `task` are not tapped — they are the "this screen has
# finished rendering" markers the captures wait on, each the first item its
# screen draws. `focusing` is `focus_session_focusing`; `chain` and `task` are
# seeded content and must stay in step with `LOCALES` in
# generate-seed-backup.py (`chain_title`, and the first of the Personal tasks).
NAV = {
    "en": {
        "focus": "Focus", "routines": "Routines", "tasks": "Tasks",
        "category": "Personal", "start": "Start ",
        "focusing": "Focusing", "chain": "Morning Routine", "task": "Plan weekend hike",
    },
    "es": {
        "focus": "Enfoque", "routines": "Rutinas", "tasks": "Tareas",
        "category": "Personal", "start": "Empezar ",
        "focusing": "En curso", "chain": "Rutina matutina",
        "task": "Planear la excursión del fin de semana",
    },
}


def adb(serial, *args, check=True):
    return subprocess.run(["adb", "-s", serial, *args], capture_output=True, text=True, check=check)


def dump_layout(serial, retries=8, delay=1.5):
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
                raise RuntimeError(
                    "The `android` CLI was not found on PATH. It's required to inspect the "
                    "app's UI tree for navigation — install the Android SDK's `android` CLI "
                    "(see AGENTS.md's android-cli conventions) and try again.",
                ) from e
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


def find_center(nodes, desc=None, text=None, text_prefix=None):
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
    if text_prefix is not None:
        for n in nodes:
            # The digit is what separates "Start 25 min" from "Start with", the
            # custom-length label on the session-finished sheet, which shares the
            # same verb in both locales.
            value = n.get("text") or ""
            if value.startswith(text_prefix) and any(c.isdigit() for c in value):
                return n["center"]
    raise RuntimeError(f"Element not found: desc={desc!r} text={text!r} text_prefix={text_prefix!r}")


def tap_center(serial, center):
    # center is a string like "[540,2227]"
    x, y = center.strip("[]").split(",")
    adb(serial, "shell", "input", "tap", x.strip(), y.strip())


def wait_for(serial, desc=None, text=None, text_prefix=None, timeout=READY_TIMEOUT_SECONDS):
    """Re-read the view tree until the described element shows up, and return its center."""
    started = time.monotonic()
    deadline = started + timeout
    while True:
        nodes = dump_layout(serial)
        try:
            return find_center(nodes, desc=desc, text=text, text_prefix=text_prefix)
        except RuntimeError as missing:
            if time.monotonic() >= deadline:
                # Say that this was a *timeout*, and for how long. The bare
                # "Element not found" reads like a typo in NAV, which is the one
                # thing it is not after 90 seconds of polling — far more likely
                # the screen never finished rendering, or the seeded content this
                # marker names has drifted from generate-seed-backup.py.
                raise TimeoutError(
                    f"Screen never finished rendering: waited "
                    f"{time.monotonic() - started:.0f}s of {timeout:.0f}s for {missing}",
                ) from missing
            time.sleep(READY_POLL_SECONDS)


def tap(serial, desc=None, text=None, text_prefix=None, settle=1.0):
    center = wait_for(serial, desc=desc, text=text or desc, text_prefix=text_prefix)
    tap_center(serial, center)
    time.sleep(settle)


def screencap(serial, out_path: Path):
    time.sleep(CAPTURE_SETTLE_SECONDS)
    adb(serial, "shell", "screencap", "-p", "/sdcard/_shot.png")
    adb(serial, "pull", "/sdcard/_shot.png", str(out_path))
    adb(serial, "shell", "rm", "-f", "/sdcard/_shot.png")


def main():
    if len(sys.argv) != 5:
        print(__doc__, file=sys.stderr)
        sys.exit(1)
    serial, out_dir, prefix, locale = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4]
    if locale not in NAV:
        print(f"Unknown locale {locale!r} (expected one of {sorted(NAV)})", file=sys.stderr)
        sys.exit(1)
    nav = NAV[locale]
    out = Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)

    adb(serial, "shell", "am", "force-stop", PKG)
    adb(serial, "shell", "am", "start", "-n", f"{PKG}/{ACTIVITY}")
    time.sleep(4)

    # 1. Focus / Today — the day before anything has been started, so the
    #    "Up next" card still offers the session the next shot then runs.
    #    The tab only answers to "Focus" while nothing is running: once a
    #    session starts it relabels itself to the time left, so this step
    #    depends on the `pm clear` seed-screenshot-data.sh does first.
    tap(serial, desc=nav["focus"], settle=1.5)
    # The start button is both this screen's readiness marker and the next
    # step's target: it only exists once the agenda has loaded and found
    # something worth starting.
    wait_for(serial, text_prefix=nav["start"])
    screencap(serial, out / f"{prefix}_1_focus_today.png")

    # 2. A running session. Starting one from the "Up next" card opens the
    #    session view over the day by itself — no second tap to expand it.
    tap(serial, text_prefix=nav["start"], settle=2.0)
    wait_for(serial, text=nav["focusing"])
    screencap(serial, out / f"{prefix}_2_focus_session.png")
    # Back to the day underneath. The session keeps running; that is the whole
    # point of the two shots that follow.
    adb(serial, "shell", "input", "keyevent", "KEYCODE_BACK")
    time.sleep(1.5)

    # 3. Routines / Today, with the session's countdown live in the nav bar.
    tap(serial, desc=nav["routines"], settle=1.5)
    wait_for(serial, text=nav["chain"])
    screencap(serial, out / f"{prefix}_3_routines_today.png")

    # 4. Tasks / Personal, same countdown still running.
    tap(serial, desc=nav["tasks"], settle=1.0)
    tap(serial, text=nav["category"], settle=1.0)
    wait_for(serial, text=nav["task"])
    screencap(serial, out / f"{prefix}_4_tasks_personal.png")

    print(f"Captured 4 screenshots into {out}")


if __name__ == "__main__":
    main()
