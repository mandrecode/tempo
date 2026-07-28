#!/usr/bin/env python3
"""Drives the app's Settings -> Import data flow to load a seed backup file
(GitHub issue #252). Not part of the CI/app build.

The file must be named `.json`, not `.tempo`: the app launches its import picker
with the `application/json` MIME filter, so DocumentsUI won't offer a `.tempo`
file and the search step below would never find it.

Why UI automation rather than writing the database directly: since GitHub
issue #212 the Room DB is SQLCipher-encrypted at rest with a passphrase held
only in the app's Android Keystore, so the old approach of pulling
databases/tempo_database and editing it with the host's sqlite3 no longer
works. The backup import flow uses a separate, caller-chosen passphrase (see
docs/BACKUP_FORMAT.md), which makes it the one supported way to get a curated
dataset into a real, encrypted, on-device DB.

Steps: Settings -> Import data -> system file picker (search by name) ->
passphrase dialog -> Merge/Replace dialog (Replace) -> success dialog -> OK.

Usage: import-seed-backup.py <device-serial> <file-name-on-device> <passphrase> <en|es>
  e.g. import-seed-backup.py emulator-5554 tempo_seed.json hunter2 es
"""
import json
import subprocess
import sys
import tempfile
import time
from pathlib import Path

PKG = "com.mandrecode.tempo.debug"
ACTIVITY = "com.mandrecode.tempo.MainActivity"

# App-owned strings, localized by the per-app locale set in
# seed-screenshot-data.sh. Values must match
# app/src/main/res/values[-<locale>]/strings.xml exactly — a mismatch means the
# element can't be found. Keys map to: settings (nav entry point),
# import_title (backup_import_title), passphrase_confirm
# (backup_import_passphrase_confirm), mode_replace (backup_import_mode_replace),
# success (backup_import_success_title), failure (backup_import_error_title),
# ok (ok).
STRINGS = {
    "en": {
        "settings": "Settings",
        "import_title": "Import data",
        "passphrase_confirm": "Continue",
        "mode_replace": "Replace",
        "success": "Import complete",
        "failure": "Import failed",
        "ok": "OK",
    },
    "es": {
        "settings": "Ajustes",
        "import_title": "Importar datos",
        "passphrase_confirm": "Continuar",
        "mode_replace": "Reemplazar",
        "success": "Importación completada",
        "failure": "Error al importar",
        "ok": "Aceptar",
    },
}

# The system file picker (DocumentsUI) follows the *system* locale, not the
# app's per-app locale, so its labels can't be looked up in STRINGS above.
# These resource ids are stable and locale-independent.
PICKER_SEARCH_ID = "option_menu_search"
PICKER_ITEM_TITLE_ID = "title"
# On some system images (notably XR) tapping a search result puts the picker into
# its multi-select action mode — the file is highlighted but not returned until
# this confirm button is pressed. Elsewhere the single tap returns immediately and
# this button never appears.
PICKER_CONFIRM_ID = "action_menu_select"


class MissingAndroidCli(RuntimeError):
    """Not a transient UI hiccup — retrying the import can't conjure the CLI."""


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
                raise MissingAndroidCli(
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


def match(node, desc=None, text=None, res_id=None, password=False):
    if desc is not None and node.get("content-desc") != desc:
        return False
    if text is not None and node.get("text") != text:
        return False
    if res_id is not None and node.get("resource-id") != res_id:
        return False
    if password and "password" not in node.get("interactions", []):
        return False
    return True


def find(nodes, **kwargs):
    for n in nodes:
        if match(n, **kwargs) and "center" in n:
            return n
    return None


def find_label(nodes, label):
    """content-desc is the more reliable accessibility label, but the expanded
    tablet and desktop rails set only plain text on nav items, so fall back to it."""
    return find(nodes, desc=label) or find(nodes, text=label)


def tap_center(serial, center):
    x, y = center.strip("[]").split(",")
    adb(serial, "shell", "input", "tap", x.strip(), y.strip())


def tap(serial, settle=1.0, **kwargs):
    """Always re-dumps before tapping: dialogs shift position when the IME opens,
    so coordinates read before typing are stale by the time we act on them."""
    node = find(dump_layout(serial), **kwargs)
    if node is None:
        raise RuntimeError(f"Element not found: {kwargs}")
    tap_center(serial, node["center"])
    time.sleep(settle)


def scroll_to(serial, max_swipes=10, **kwargs):
    """Swipes up through a scrollable screen until the element appears. Checks
    once more after the final swipe, so the last swipe isn't wasted."""
    for attempt in range(max_swipes + 1):
        nodes = dump_layout(serial)
        node = find(nodes, **kwargs)
        if node is not None:
            return node
        if attempt == max_swipes:
            break
        scrollable = next(
            (n for n in nodes if "scrollable" in n.get("interactions", []) and "bounds" in n),
            None,
        )
        if scrollable is None:
            break
        top_left, bottom_right = scrollable["bounds"].split("][")
        x1, y1 = (int(v) for v in top_left.strip("[]").split(","))
        x2, y2 = (int(v) for v in bottom_right.strip("[]").split(","))
        mid_x = (x1 + x2) // 2
        adb(serial, "shell", "input", "swipe", str(mid_x), str(int(y2 * 0.85)),
            str(mid_x), str(int(y1 + (y2 - y1) * 0.2)), "300")
        time.sleep(1.0)
    raise RuntimeError(f"Element not found after scrolling: {kwargs}")


def wait_for(serial, timeout=60, **kwargs):
    deadline = time.time() + timeout
    while time.time() < deadline:
        node = find(dump_layout(serial), **kwargs)
        if node is not None:
            return node
        time.sleep(1.0)
    return None


def run_import(serial, file_name, passphrase, s):
    adb(serial, "shell", "am", "force-stop", PKG)
    adb(serial, "shell", "am", "start", "-n", f"{PKG}/{ACTIVITY}")
    time.sleep(4)

    settings = find_label(dump_layout(serial), s["settings"])
    if settings is None:
        raise RuntimeError(f"Settings entry point not found: {s['settings']!r}")
    tap_center(serial, settings["center"])
    time.sleep(1.5)

    node = scroll_to(serial, text=s["import_title"])
    tap_center(serial, node["center"])
    time.sleep(2.5)

    # System file picker: search by name rather than navigating the folder tree,
    # whose layout differs across system images and locales.
    tap(serial, res_id=PICKER_SEARCH_ID, settle=1.5)
    adb(serial, "shell", "input", "text", Path(file_name).stem)
    time.sleep(1.5)
    result = wait_for(serial, timeout=20, res_id=PICKER_ITEM_TITLE_ID, text=file_name)
    if result is None:
        raise RuntimeError(f"{file_name} not found in the file picker — was it pushed to the device?")
    tap_center(serial, result["center"])
    time.sleep(2.5)
    confirm = find(dump_layout(serial), res_id=PICKER_CONFIRM_ID)
    if confirm is not None:
        tap_center(serial, confirm["center"])
        time.sleep(2.5)

    # Passphrase dialog. The field carries no content-desc, but it's the only
    # password-masked input on screen, which is locale-independent.
    tap(serial, password=True, settle=1.0)
    adb(serial, "shell", "input", "text", passphrase)
    time.sleep(1.0)
    tap(serial, text=s["passphrase_confirm"], settle=2.5)

    # Merge/Replace dialog — Replace restores the file's ids verbatim.
    tap(serial, text=s["mode_replace"], settle=2.0)

    success = wait_for(serial, timeout=90, text=s["success"])
    if success is None:
        # Surface the failure dialog's own wording (which names the offending
        # record for validation errors) rather than a bare timeout.
        nodes = dump_layout(serial)
        detail = ""
        if find(nodes, text=s["failure"]) is not None:
            detail = " — " + "; ".join(n["text"] for n in nodes if n.get("text"))
        raise RuntimeError(f"Import did not report success{detail}")

    tap(serial, text=s["ok"], settle=1.0)
    adb(serial, "shell", "input", "keyevent", "KEYCODE_BACK")
    time.sleep(1.0)


def main():
    if len(sys.argv) != 5:
        print(__doc__, file=sys.stderr)
        sys.exit(1)
    serial, file_name, passphrase, locale = sys.argv[1:5]
    if locale not in STRINGS:
        print(f"Unknown locale {locale!r} (expected one of {sorted(STRINGS)})", file=sys.stderr)
        sys.exit(1)
    s = STRINGS[locale]

    # Driving real UI is inherently flaky — a dialog can be a frame late, the
    # picker's search box can drop the typed query. Each attempt restarts the app
    # from scratch and Replace-mode import is idempotent, so retrying is safe.
    attempts = 3
    for attempt in range(1, attempts + 1):
        try:
            run_import(serial, file_name, passphrase, s)
            print(f"Imported {file_name} on {serial} ({locale}).")
            return
        except MissingAndroidCli:
            raise
        except RuntimeError as e:
            if attempt == attempts:
                raise
            print(f"Import attempt {attempt}/{attempts} failed ({e}); retrying.", file=sys.stderr)
            time.sleep(3)


if __name__ == "__main__":
    main()
