#!/usr/bin/env bash
# Seeds + captures light and dark screenshots for one form factor.
# GitHub issue #169. Not part of the CI/app build.
#
# Usage: scripts/generate-screenshot-set.sh <device-serial> <out-dir> <form-factor-prefix>
#   e.g. scripts/generate-screenshot-set.sh emulator-5554 distribution/screenshots/phone phone
set -euo pipefail

SERIAL="${1:?Usage: generate-screenshot-set.sh <device-serial> <out-dir> <form-factor-prefix>}"
OUT_DIR="${2:?Usage: generate-screenshot-set.sh <device-serial> <out-dir> <form-factor-prefix>}"
PREFIX="${3:?Usage: generate-screenshot-set.sh <device-serial> <out-dir> <form-factor-prefix>}"
PKG="com.mandrecode.tempo.debug"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

for THEME in light dark; do
  bash "${SCRIPT_DIR}/seed-screenshot-data.sh" "$SERIAL" "$THEME"
  python3 "${SCRIPT_DIR}/capture-screenshot-set.py" "$SERIAL" "$OUT_DIR" "${PREFIX}_${THEME}"
done
