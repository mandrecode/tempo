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
import sys
import time
from pathlib import Path

from android_ui import adb, dump_layout, tap_center

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
# See the session step in capture(): used only when its marker can't be read.
SESSION_FALLBACK_SETTLE_SECONDS = 8.0
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


def set_demo_mode(serial, on):
    """SystemUI demo mode, so every capture's status bar reads the same: 9:41,
    full battery and Wi-Fi, no notification icons and no work-profile badge.
    (The running session's own notification needs hide_session_notification
    on top.) Without it each image
    shows whatever the emulator's clock and icons happened to be, and the set
    reads as unpolished side by side in the Play carousel."""
    def demo(*extras):
        adb(serial, "shell", "am", "broadcast", "-a", "com.android.systemui.demo", *extras, check=False)

    if not on:
        demo("-e", "command", "exit")
        return
    adb(serial, "shell", "settings", "put", "global", "sysui_demo_allowed", "1", check=False)
    demo("-e", "command", "enter")
    demo("-e", "command", "clock", "-e", "hhmm", "0941")
    demo("-e", "command", "battery", "-e", "level", "100", "-e", "plugged", "false")
    # `fully` marks the network as validated; without it demo mode draws the
    # Wi-Fi icon with a "no internet" exclamation mark.
    demo("-e", "command", "network", "-e", "wifi", "show", "-e", "level", "4", "-e", "fully", "true")
    # Mobile is hidden rather than set to full: demo mode draws it as a half-empty
    # "3G" in white whatever the bar's tint, so it clashes on light screens.
    demo("-e", "command", "network", "-e", "mobile", "hide")
    demo("-e", "command", "notifications", "-e", "visible", "false")
    demo("-e", "command", "status", "-e", "managed_profile", "hide")


def hide_session_notification(serial):
    """Snooze the running session's notification so it leaves the status bar.
    It is an ongoing (on Android 16, promoted Live Update) notification, which
    demo mode's "hide notifications" leaves in place — every shot after the
    session starts would otherwise carry its icon by the clock. Snoozing only
    takes it off screen; the session itself keeps running."""
    listing = adb(serial, "shell", "cmd", "notification", "list", check=False).stdout
    for key in listing.split():
        if f"|{PKG}|" in key:
            # Quoted: the key is `user|pkg|id|tag|uid`, and the device shell would
            # otherwise read each `|` as a pipe.
            adb(serial, "shell", "cmd", "notification", "snooze", "--for", "3600000", f"'{key}'", check=False)


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
    set_demo_mode(serial, True)
    try:
        capture(serial, out, prefix, nav)
    finally:
        set_demo_mode(serial, False)


def capture(serial, out, prefix, nav):

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
    try:
        wait_for(serial, text=nav["focusing"])
    except RuntimeError:
        # The session sheet's progress ring never stops animating, and on XR
        # `android layout` can fail to find the UI idle for as long as it is on
        # screen. The tap that opened it already succeeded, so fall back to a
        # settle long enough for the slowest form factor rather than giving up.
        time.sleep(SESSION_FALLBACK_SETTLE_SECONDS)
    hide_session_notification(serial)
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
