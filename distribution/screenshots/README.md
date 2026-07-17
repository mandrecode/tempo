# Play Store Screenshots

Source of truth for how the screenshots under this directory were produced.
Not wired into any CI workflow — screenshots are uploaded to the Play
Console listing manually, per locale. Re-run the pipeline below whenever
the app's UI changes enough to make these stale.

## Layout

```
distribution/screenshots/
  phone/     phone_{en,es}_{light,dark}_{1_routines_today,2_tasks_work,3_tasks_shopping,4_settings}.png
  medium/    medium_...
  expanded/  expanded_...
  desktop/   desktop_...
  xr/        xr_...
```

4 views × light/dark × 2 locales × 5 form factors = 80 images.

## Prerequisites

- `android` CLI, `adb`, `avdmanager` (Android SDK)
- Host `sqlite3` (macOS ships one at `/usr/bin/sqlite3`)
- `python3`; for XR captures only, [Pillow](https://pypi.org/project/Pillow/)
  (`pip install Pillow`, ideally in a venv — `python3 -m venv /tmp/venv &&
  /tmp/venv/bin/pip install Pillow` — since some systems block system-wide
  `pip install`)
- A debug build: `./gradlew assembleDebug`

**Before building, temporarily drop the "(Debug)" suffix.** Desktop and
Expanded/big-tablet form factors render OS window chrome (a caption bar
with the app's label) around the app, which would otherwise leak
"Tempo (Debug)" into those screenshots. Phone and Medium tablet run
without window chrome and are unaffected; XR's chrome pill has no text
label either way.

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

## The pipeline

Three scripts, composed by a fourth:

- **`scripts/generate-seed-sql.py --locale en|es`** — prints INSERT
  statements for a curated demo dataset (12 tasks across 4 categories with
  a mix of priorities/due dates/completion states, 4 habits split
  Build/Quit, one 3-member habit chain), with category names, task
  titles/descriptions, and habit titles translated per locale. Every date
  is computed relative to `--today` (defaults to the real current date),
  so re-running later doesn't produce stale "Today" tasks or broken
  streaks.
- **`scripts/seed-screenshot-data.sh <serial> [light|dark] [en|es]`** —
  wipes the target device's app data, grants the notification/exact-alarm
  permissions the app would otherwise prompt for (screenshot capture
  skips onboarding), sets the theme, sets the **app's per-app language**
  (see below) to match, and applies the generated SQL. Applies SQL via
  the **host's own `sqlite3`** (pull DB → checkpoint WAL → apply SQL →
  push back) rather than a device-side `sqlite3` binary, since not every
  system image ships one (the `medium_tablet` Google Play tablet image
  doesn't).
- **`scripts/capture-screenshot-set.py <serial> <out-dir> <prefix> <en|es>`**
  — navigates the seeded app (Routines/Today → Tasks/Work-category →
  Tasks/Shopping-category → Settings) and screenshots each via
  `adb screencap`. Navigation finds UI elements by `android layout`'s
  content-desc, falling back to plain text (desktop's nav rail only sets
  text, not content-desc); the target text/desc strings are looked up
  per locale in the script's `NAV` dict.
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
   plain data inserted by `generate-seed-sql.py --locale`, translated by
   hand in that script's `LOCALES` dict. Add a new locale by adding an
   entry there (matching the app's actual `res/xml/locales_config.xml`
   support) and to `NAV` in `capture-screenshot-set.py` with the
   corresponding translated nav-rail strings (look them up in
   `app/src/main/res/values-<locale>/strings.xml` — e.g. `routines`,
   `tasks`, `settings` — don't guess; a mismatched string means
   `capture-screenshot-set.py` can't find the element to tap).

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
| Medium tablet | `medium_tablet` | **portrait** (forced) | 1600×2560, 800dp width — below the 1200dp breakpoint, compact icon-only rail |
| Expanded/big tablet | `PixelTablet` | landscape (native) | 2560×1600, 1280dp width — above the breakpoint, full labeled rail with app title |
| Desktop | `large_desktop` | landscape (native) | 1920×1080, exact 16:9 |
| Android XR | `XR_Headset` | n/a (square passthrough scene) | 2558×2558 → cropped to 2558×1439 |

Create any that don't already exist:

```bash
android emulator list                     # check what's already there
android emulator create medium_tablet     # profile exists in --list-profiles
android emulator create large_desktop     # profile exists in --list-profiles
# PixelTablet isn't in the simplified `android` CLI's profile list —
# create it directly via avdmanager, reusing whatever system image an
# existing AVD (e.g. Pixel_10) uses:
avdmanager create avd -n PixelTablet \
  -d pixel_tablet \
  -k "system-images;android-37.1;google_apis_ps16k;arm64-v8a"
# Pixel_10 and XR_Headset should already exist per AGENTS.md /
# android-cli conventions; if not, `android emulator create pixel_10`
# and check `android emulator create --list-profiles` for an XR profile.
```

`medium_tablet` and `PixelTablet` end up with the **same** physical spec
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
android emulator start medium_tablet
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
android emulator start PixelTablet
adb -s emulator-5554 install -r "$APK"
adb -s emulator-5554 shell cmd overlay enable com.android.internal.systemui.navbar.gestural
for LOCALE in en es; do
  bash scripts/generate-screenshot-set.sh emulator-5554 distribution/screenshots/expanded expanded "$LOCALE"
done
adb -s emulator-5554 emu kill

# --- Desktop ---
android emulator start large_desktop
adb -s emulator-5554 install -r "$APK"
adb -s emulator-5554 shell cmd overlay enable com.android.internal.systemui.navbar.gestural
# First launch after boot may land in a small floating window rather
# than maximized — force-stop and relaunch once if so; a fresh launch
# on a rebooted AVD reliably starts full-screen.
for LOCALE in en es; do
  bash scripts/generate-screenshot-set.sh emulator-5554 distribution/screenshots/desktop desktop "$LOCALE"
done
adb -s emulator-5554 emu kill

# --- Android XR ---
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
replace sample data as needed by editing `generate-seed-sql.py`, and pick
the best up to 8 per Play Console's per-listing limit (currently
splitting evenly between light and dark).

## Play Store technical requirements (as of writing)

- **Phone / tablet**: no fixed aspect-ratio requirement observed to fail
  here; captured at each device's native resolution.
- **Desktop (Chromebook) listing**: 4–8 PNG/JPEG, ≤8MB each, 16:9 or 9:16,
  each side 1080–7680px. `Small_Desktop` (1366×768) fails on the
  768px height; `large_desktop` (1920×1080) is exact 16:9 and clears the
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

The crop window (`y=620` to `y=620+1439=2059` at full width, giving an
exact 2558×1439 16:9 frame) was derived by pixel-sampling a vertical
line through the floating app window across all four canonical views to
find its true on-screen bounds — the window-chrome pill top sits at
`y≈658` and the tallest panel (Settings, which has the most content)
bottoms out at `y≈2022` — then centering the crop on that combined range
with roughly even margins. A naive default *center-of-canvas* crop
looked unbalanced (lots of empty sky above, content nearly touching the
bottom edge) because the floating window itself isn't centered in the
square scene. If the app's UI changes enough to shift where the longest
screen's content ends, re-derive `CROP_TOP` in `crop-xr-screenshots.py`
the same way:

```bash
python3 -c "
from PIL import Image
im = Image.open('distribution/screenshots/xr/xr_en_light_4_settings.png').convert('RGB')
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
