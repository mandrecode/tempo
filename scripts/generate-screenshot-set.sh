#!/usr/bin/env bash
# Seeds + captures light and dark screenshots for one form factor and
# locale. GitHub issue #169. Not part of the CI/app build.
#
# Usage: scripts/generate-screenshot-set.sh <device-serial> <out-dir> <form-factor-prefix> <en|es>
#   e.g. scripts/generate-screenshot-set.sh emulator-5554 distribution/screenshots/phone phone es
set -euo pipefail

USAGE="Usage: generate-screenshot-set.sh <device-serial> <out-dir> <form-factor-prefix> <en|es>"
SERIAL="${1:?$USAGE}"
OUT_DIR="${2:?$USAGE}"
PREFIX="${3:?$USAGE}"
LOCALE="${4:?$USAGE}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

for THEME in light dark; do
  bash "${SCRIPT_DIR}/seed-screenshot-data.sh" "$SERIAL" "$THEME" "$LOCALE"
  python3 "${SCRIPT_DIR}/capture-screenshot-set.py" "$SERIAL" "$OUT_DIR" "${PREFIX}_${LOCALE}_${THEME}" "$LOCALE"
done

# The XR emulator always captures a square passthrough scene regardless of
# wm size, which Play Store's 16:9/9:16 requirement rejects — crop it down.
# Requires Pillow (pip install Pillow). Safe to call once per locale run
# into the same directory — already-cropped files are skipped.
if [ "$PREFIX" = "xr" ]; then
  python3 "${SCRIPT_DIR}/crop-xr-screenshots.py" "$OUT_DIR"
fi
