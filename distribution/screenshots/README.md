# Play Store Screenshots

Source of truth for how the screenshots under this directory were produced.
Not wired into any CI workflow — screenshots are uploaded to the Play
Console listing manually, per locale. Re-run the pipeline below whenever
the app's UI changes enough to make these stale.

## Layout

```
distribution/screenshots/
  phone/     phone_{en,es}_{light,dark}_{1_focus_today,2_focus_session,3_routines_today,4_tasks_personal}.png
  medium/    medium_...
  expanded/  expanded_...
  desktop/   desktop_...
  xr/        xr_...
```

4 views × light/dark × 2 locales × 5 form factors = 80 images.

The four views are one story told in order: the day as Focus sees it, a session
started on it, and then Routines and Tasks with that session still running.
Capturing them in that order is what puts the running session into the last two,
so the sequence in `capture-screenshot-set.py` is not arbitrary.

How the session shows there depends on how much room the navigation has:

| Form factor | Navigation | With a session running |
|---|---|---|
| Phone | bottom bar | Focus tab widens into a pill with the countdown |
| Medium tablet | compact icon-only rail | Focus tab tinted; no room for digits |
| Expanded tablet, Desktop | labeled rail | countdown replaces the "Focus" label |
| XR | compact icon-only rail | Focus tab tinted; no room for digits |

Two things about the Tasks shot are load-bearing:

- It opens the **Personal** category, the one seeded category with nothing
  completed in it. A category with completed tasks gains a clear-completed
  action, and the floating bar drops the countdown to make room
  (`hasContextualActions` in `PersistentFloatingBar.kt`) — taking the running
  session out of a shot that exists to show it running.
- `generate-seed-backup.py` orders Personal first among the user-made
  categories so its chip is on screen without scrolling on a phone, where the
  row only fits about three.

## Prerequisites

- `android` CLI, `adb`, `avdmanager` (Android SDK)
- `python3` with [cryptography](https://pypi.org/project/cryptography/) (used to
  encrypt the seed backup); for XR captures also
  [Pillow](https://pypi.org/project/Pillow/). Install into a venv if the system
  blocks a global `pip install` — `python3 -m venv /tmp/venv &&
  /tmp/venv/bin/pip install cryptography Pillow`
- A debug build: `./gradlew assembleDebug`

**Before building, temporarily drop the "(Debug)" suffix.** Two different
things put `R.string.app_name` on screen, and both would otherwise read
"Tempo (Debug)":

- **Desktop** draws real OS window chrome — a caption bar carrying the
  app's launcher label — around the app.
- **Expanded/big tablet** has no window chrome, but its labeled navigation
  rail prints the app name as its own header (`PersistentFloatingBar.kt`
  renders `app_name` when `isExpandedRail`).

Phone and Medium tablet get the icon-only rail and no chrome, so they are
unaffected; XR's chrome pill has no text label either way.

```bash
# In app/build.gradle.kts, buildTypes { debug { ... } }:
#   resValue("string", "app_name", "Tempo (Debug)")
# temporarily becomes:
#   resValue("string", "app_name", "Tempo")
./gradlew assembleDebug
# ...capture desktop and expanded sets (both locales)...
# then revert the resValue change and rebuild before committing, so
# normal debug builds still show "(Debug)" as usual.
```

## How seeding works (and why it's not SQL any more)

Until GitHub issue #212 the pipeline seeded data by pulling the app's
`databases/tempo_database`, applying INSERT statements with the **host's**
`sqlite3`, and pushing the file back. That is no longer possible: the Room DB
is now SQLCipher-encrypted at rest, and its passphrase is generated and
Keystore-wrapped on-device (`KeystoreDbPassphraseProvider`), so it never
leaves the app process. A pulled DB file is opaque ciphertext.

The **backup export/import** feature is the way in. Its files are encrypted with
a *separate*, caller-chosen passphrase (PBKDF2WithHmacSHA256 + AES-256-GCM,
specified in [`docs/BACKUP_FORMAT.md`](../../docs/BACKUP_FORMAT.md)), unrelated
to the DB's own key — so a script can build a backup file and let the app import
it exactly as a user would, writing through the app's own encrypted DB layer.

Note the seed file is named **`.json`, not `.tempo`**, even though `.tempo` is
what the app suggests when *exporting*. The import picker is launched with the
`application/json` MIME filter, so a `.tempo` file is not selectable in
DocumentsUI and the pipeline would stall waiting for a file it can never tap.

## The pipeline

Four scripts, composed by a fifth:

- **`scripts/generate-seed-backup.py --locale en|es --theme light|dark
  --passphrase P -o FILE`** — writes an encrypted backup (as `.json`, see above)
  containing a curated demo dataset (14 tasks across 4 categories with a mix of
  priorities/due dates/completion states, 7 habits split Build/Quit, one
  3-member habit chain), with category names, task titles/descriptions, and
  habit titles translated per locale. Every date is computed relative to
  `--today` (defaults to the real current date), so re-running later doesn't
  produce stale "Today" tasks or broken streaks. It also emits a `settings`
  block carrying `--theme`, since Replace-mode imports apply that section and
  would otherwise overwrite the theme seeded into `theme_prefs.xml`.
- **`scripts/import-seed-backup.py <serial> <file-name> <passphrase> <en|es>`**
  — drives the real UI to import that file: Settings → Import data → system
  file picker (searched by name) → passphrase dialog → Merge/Replace
  (Replace) → success dialog. Retries the whole flow up to 3 times, since
  driving real UI is inherently flaky and Replace-mode import is idempotent.
- **`scripts/seed-screenshot-data.sh <serial> [light|dark] [en|es]`** —
  wipes the target device's app data, grants the notification/exact-alarm
  permissions the app would otherwise prompt for (screenshot capture
  skips onboarding), sets the theme, sets the **app's per-app language**
  (see below) to match, marks the current what's-new entry as seen so its
  bottom sheet doesn't cover the first screenshot, then generates and
  imports the backup via the two scripts above.
- **`scripts/capture-screenshot-set.py <serial> <out-dir> <prefix> <en|es>`**
  — navigates the seeded app (Focus/Today → start a session from the "Up
  next" card → Routines/Today → Tasks/Personal-category) and screenshots
  each via `adb screencap`. Navigation finds UI elements by `android
  layout`'s content-desc, falling back to plain text (desktop's nav rail
  only sets text, not content-desc); the target text/desc strings are
  looked up per locale in the script's `NAV` dict. The start button is
  matched on the *verb* of `focus_session_start` plus a digit, because the
  rest of its label is the session length from Settings.

  The Focus tab only answers to the content-desc "Focus" while nothing is
  running — once a session starts it relabels itself to the time left — so
  the first step relies on the `pm clear` that `seed-screenshot-data.sh`
  does. Running this script twice against the same device without
  re-seeding fails on step 1 rather than producing a wrong image.

  Every shot **waits for a piece of content only the finished screen has**
  (the start button, the "Focusing" status, the seeded chain title, the
  first seeded Personal task) before the shutter goes. A fixed settle alone
  was not enough on XR: Focus was still drawing its loading indicator, and
  the resulting blank panel with a spinner in the middle reads as a
  legitimately empty day rather than as a failed capture. If you add a
  view, give it a marker too — a fixed sleep will pass on a fast emulator
  and quietly produce a bad image on a slow one.
- **`scripts/generate-screenshot-set.sh <serial> <out-dir> <prefix> <en|es>`**
  — runs the above twice (light, then dark) for one form factor and
  locale, and crops XR output to 16:9 when `prefix` is `xr` (see below).

### Localization

Two independent things need to be in the target language, and the
pipeline handles both:

1. **The app's own UI strings** (nav labels, "Today"/"Yesterday", Settings
   screen, etc.) — controlled by the app's **per-app language** (Android
   13+ `LocaleManager`; the app declares `en-US`/`es-ES` support in
   `res/xml/locales_config.xml`). `seed-screenshot-data.sh` sets this via:
   ```bash
   adb shell cmd locale set-app-locales <pkg> --user 0 --locales es
   ```
   This only changes the *app's* language, not the whole emulator/system
   locale — simpler and doesn't require a reboot. Verify with
   `adb shell cmd locale get-app-locales <pkg> --user 0`.
2. **The seeded demo content** (category names, task/habit titles) —
   plain data written by `generate-seed-backup.py --locale`, translated by
   hand in that script's `LOCALES` dict. Add a new locale by adding an
   entry there (matching the app's actual `res/xml/locales_config.xml`
   support), plus entries in `NAV` in `capture-screenshot-set.py` and
   `STRINGS` in `import-seed-backup.py` with the corresponding translated
   strings (look them up in `app/src/main/res/values-<locale>/strings.xml` —
   e.g. `focus`, `routines`, `tasks`, `focus_session_start`,
   `backup_import_title` — don't guess; a mismatched string means the
   script can't find the element to tap).

   Note that the **system file picker** follows the emulator's *system*
   locale, not the app's per-app one, so `import-seed-backup.py` addresses
   its elements by resource id rather than by label.

## Devices

Each form factor uses a dedicated AVD matching a real device category —
**not** a single resizable AVD with `resize-display` presets. That was
tried first and abandoned: the app has a single width breakpoint at
1200dp (`SheetPlacement.kt`), and both the "medium" and "expanded"
`resize-display` presets landed above it regardless of orientation
(`resize-display` applies a 240dpi density override, e.g. 1920px at
240dpi = 1280dp), so medium and expanded screenshots looked identical.

| Form factor | AVD | Orientation | Result |
|---|---|---|---|
| Phone | `Pixel_10` | portrait (native) | 1080×2424 |
| Medium tablet | `Medium_Tablet` | **portrait** (forced) | 1600×2560, 800dp width — below the 1200dp breakpoint, compact icon-only rail |
| Expanded/big tablet | `Pixel_Tablet` | landscape (native) | 2560×1600, 1280dp width — above the breakpoint, full labeled rail with app title |
| Desktop | `Large_Desktop` | landscape (native) | 1920×1080, exact 16:9 |
| Android XR | `XR_Headset` | n/a (square passthrough scene) | 2558×2558 → cropped to 2558×1439 |

Create any that don't already exist:

```bash
android emulator list                     # check what's already there
android emulator create Medium_Tablet     # profile exists in --list-profiles
android emulator create Large_Desktop     # profile exists in --list-profiles
# Pixel_Tablet isn't in the simplified `android` CLI's profile list —
# create it directly via avdmanager, reusing whatever system image an
# existing AVD (e.g. Pixel_10) uses:
avdmanager create avd -n Pixel_Tablet \
  -d pixel_tablet \
  -k "system-images;android-37.1;google_apis_ps16k;arm64-v8a"
# Pixel_10 and XR_Headset should already exist per AGENTS.md /
# android-cli conventions; if not, `android emulator create pixel_10`
# and check `android emulator create --list-profiles` for an XR profile.
```

`Medium_Tablet` and `Pixel_Tablet` end up with the **same** physical spec
(2560×1600 @ 320dpi — apparently pinned by the Google Play tablet system
image regardless of requested device profile); orientation is what makes
them look different, not the hardware.

## Recreating the full set

Repeat each form factor's block once per locale (`en`, `es`) — swap the
`LOCALE=en` line and the `_en`/`_es` output naming is handled
automatically by `generate-screenshot-set.sh`.

```bash
./gradlew assembleDebug
APK=app/build/outputs/apk/debug/app-debug.apk

# --- Phone ---
android emulator start Pixel_10
adb -s emulator-5554 install -r "$APK"
adb -s emulator-5554 shell cmd overlay enable com.android.internal.systemui.navbar.gestural
for LOCALE in en es; do
  bash scripts/generate-screenshot-set.sh emulator-5554 distribution/screenshots/phone phone "$LOCALE"
done
adb -s emulator-5554 emu kill

# --- Medium tablet (portrait) ---
android emulator start Medium_Tablet
adb -s emulator-5554 install -r "$APK"
adb -s emulator-5554 shell cmd overlay enable com.android.internal.systemui.navbar.gestural
adb -s emulator-5554 shell settings put system accelerometer_rotation 0
adb -s emulator-5554 shell settings put system user_rotation 1   # 1 = portrait for this panel's native-landscape shape
sleep 2
# Verify with dumpsys, NOT `wm size` — `wm size` always reports the
# unrotated physical panel size, not the current logical/rotated size:
adb -s emulator-5554 shell dumpsys window displays | grep -o "cur=[0-9x]*"
for LOCALE in en es; do
  bash scripts/generate-screenshot-set.sh emulator-5554 distribution/screenshots/medium medium "$LOCALE"
done
adb -s emulator-5554 emu kill

# --- Expanded/big tablet (landscape, native — no rotation needed) ---
android emulator start Pixel_Tablet
adb -s emulator-5554 install -r "$APK"
adb -s emulator-5554 shell cmd overlay enable com.android.internal.systemui.navbar.gestural
for LOCALE in en es; do
  bash scripts/generate-screenshot-set.sh emulator-5554 distribution/screenshots/expanded expanded "$LOCALE"
done
adb -s emulator-5554 emu kill

# --- Desktop ---
android emulator start Large_Desktop
adb -s emulator-5554 install -r "$APK"
adb -s emulator-5554 shell cmd overlay enable com.android.internal.systemui.navbar.gestural
# The app may launch into a small floating window rather than maximized.
# Force-stopping and relaunching does NOT reliably fix it — the desktop
# shell remembers the window size. Launch the app, tap the maximize button
# in its window chrome (the middle of the three controls, top right), and
# verify with a screencap before capturing. That state then survives the
# `pm clear` + relaunch cycles the seeding does, so it only needs doing once.
adb -s emulator-5554 shell am start -n com.mandrecode.tempo.debug/com.mandrecode.tempo.MainActivity
for LOCALE in en es; do
  bash scripts/generate-screenshot-set.sh emulator-5554 distribution/screenshots/desktop desktop "$LOCALE"
done
adb -s emulator-5554 emu kill

# --- Android XR ---
# Do NOT launch this one with `-gpu host`. It renders icon glyphs as garbage
# noise (nav rail, Settings rows, even the window-chrome close button) and can
# produce an entirely blank frame — while text still renders fine, so the
# result looks plausible enough to miss. Use the default GPU mode below. It is
# slower: expect one locale to take longer than a 10-minute timeout, so run
# each theme separately if you're driving this from a script with a deadline.
android emulator start XR_Headset
adb -s emulator-5554 install -r "$APK"
adb -s emulator-5554 shell cmd overlay enable com.android.internal.systemui.navbar.gestural
for LOCALE in en es; do
  bash scripts/generate-screenshot-set.sh emulator-5554 distribution/screenshots/xr xr "$LOCALE"   # auto-crops to 16:9
done
adb -s emulator-5554 emu kill
```

If the auto-crop step fails with `ModuleNotFoundError: No module named
'PIL'` (system `python3` instead of a Pillow-equipped venv ran it), the
capture itself already succeeded — just re-run the crop manually:
`/path/to/venv/bin/python3 scripts/crop-xr-screenshots.py distribution/screenshots/xr`.
It's safe to call repeatedly; already-cropped files (height already
1439px) are skipped.

Check `adb devices -l` for the actual serial if more than one emulator
ends up running at once — `emulator-5554` is simply the first slot, not
guaranteed.

Then curate: review the resulting 8 images per form factor per locale,
replace sample data as needed by editing `generate-seed-backup.py`, and pick
the best up to 8 per Play Console's per-listing limit (currently
splitting evenly between light and dark).

## Play Store technical requirements (as of writing)

- **Phone / tablet**: no fixed aspect-ratio requirement observed to fail
  here; captured at each device's native resolution.
- **Desktop (Chromebook) listing**: 4–8 PNG/JPEG, ≤8MB each, 16:9 or 9:16,
  each side 1080–7680px. `Small_Desktop` (1366×768) fails on the
  768px height; `Large_Desktop` (1920×1080) is exact 16:9 and clears the
  minimum comfortably.
- **Android XR listing**: 4–8 PNG/JPEG, ≤15MB each, 16:9 or 9:16, each
  side 720–7680px.

## Android XR: why the crop step exists

`XR_Headset`'s `adb screencap` always captures the full square (2558×2558)
passthrough environment scene, regardless of `adb shell wm size`
overrides (confirmed: overriding size changes nothing about the actual
captured pixels — the override only affects the app's own logical
window, not the compositor's scene render). Square fails the 16:9/9:16
requirement outright, so `generate-screenshot-set.sh` calls
`scripts/crop-xr-screenshots.py` automatically whenever the form-factor
prefix is `xr`.

The crop window (`y=620` to `y=620+1439=2059` at full width, giving a
2558×1439 frame — the closest integer-height 16:9 crop at that width,
since 2558 isn't evenly divisible by 16) was derived by pixel-sampling a vertical
line through the floating app window across all four canonical views to
find its true on-screen bounds — the window-chrome pill top sits at
`y≈658` and the tallest panel (Focus/Today, which has the most content —
it was Settings before the views changed in GitHub issue #318, and both
land in the same place) bottoms out at `y≈2022` — then centering the crop
on that combined range
with roughly even margins. A naive default *center-of-canvas* crop
looked unbalanced (lots of empty sky above, content nearly touching the
bottom edge) because the floating window itself isn't centered in the
square scene. If the app's UI changes enough to shift where the longest
screen's content ends, re-derive `CROP_TOP` in `crop-xr-screenshots.py`
the same way:

```bash
python3 -c "
from PIL import Image
im = Image.open('distribution/screenshots/xr/xr_en_light_1_focus_today.png').convert('RGB')
x = 1000  # a column safely inside the panel, left of the chrome pill
prev = None
for y in range(0, im.size[1], 4):
    px = im.getpixel((x, y))
    if prev is None or sum(abs(a-b) for a,b in zip(px, prev)) > 30:
        print(y, px)
    prev = px
"
```
Look for the panel's light/dark background color persisting until it
jumps to the reddish passthrough-terrain color — that transition point
is the panel's true bottom edge.
