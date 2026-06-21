#!/usr/bin/env bash
set -Eeuo pipefail

DIAGNOSTICS_DIR="app/build/reports/androidTests/diagnostics"
mkdir -p "$DIAGNOSTICS_DIR"

adb devices -l | tee "$DIAGNOSTICS_DIR/adb-devices-before.txt"
adb shell getprop > "$DIAGNOSTICS_DIR/getprop-before.txt" || true
adb shell settings list global > "$DIAGNOSTICS_DIR/settings-global-before.txt" || true
adb logcat -c || true
adb logcat -v time > "$DIAGNOSTICS_DIR/logcat.txt" &
LOGCAT_PID=$!

cleanup() {
  local exit_code=$?

  adb devices -l > "$DIAGNOSTICS_DIR/adb-devices-after.txt" || true
  adb shell dumpsys activity activities > "$DIAGNOSTICS_DIR/dumpsys-activity.txt" || true
  adb shell dumpsys window > "$DIAGNOSTICS_DIR/dumpsys-window.txt" || true
  adb shell ps -A > "$DIAGNOSTICS_DIR/ps-after.txt" || true
  kill "$LOGCAT_PID" || true

  exit "$exit_code"
}
trap cleanup EXIT

timeout --foreground --preserve-status 12m ./gradlew :app:connectedDebugAndroidTest --scan --info
