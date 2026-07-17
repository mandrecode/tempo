#!/usr/bin/env bash
# Seeds a debug build with curated demo data for Google Play Store
# screenshots (GitHub issue #169). Wipes existing app data on the target
# device/emulator, so only run against a disposable AVD.
#
# Usage: scripts/seed-screenshot-data.sh <device-serial> [light|dark]
set -euo pipefail

SERIAL="${1:?Usage: seed-screenshot-data.sh <device-serial> [light|dark]}"
THEME="${2:-light}"
PKG="com.mandrecode.tempo.debug"
ACTIVITY="com.mandrecode.tempo.MainActivity"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_FILE="${SCRIPT_DIR}/seed-screenshot-data.sql"
REMOTE_SQL="/data/local/tmp/tempo_seed.sql"

case "$THEME" in
  light) THEME_MODE="LIGHT" ;;
  dark) THEME_MODE="DARK" ;;
  *) echo "Unknown theme '$THEME' (expected light|dark)" >&2; exit 1 ;;
esac

adb -s "$SERIAL" shell svc power stayon true >/dev/null
adb -s "$SERIAL" shell locksettings set-disabled true >/dev/null 2>&1 || true
adb -s "$SERIAL" shell input keyevent KEYCODE_WAKEUP >/dev/null

adb -s "$SERIAL" shell am force-stop "$PKG" >/dev/null
adb -s "$SERIAL" shell pm clear "$PKG" >/dev/null

# Grant the notification/alarm permissions the app would otherwise prompt
# for on first run, since screenshot capture skips onboarding.
adb -s "$SERIAL" shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS >/dev/null 2>&1 || true
adb -s "$SERIAL" shell appops set "$PKG" SCHEDULE_EXACT_ALARM allow >/dev/null 2>&1 || true

# First launch creates the Room DB (and seeds the default Inbox category via
# TempoDatabase.inboxCallback) and the shared_prefs directory.
adb -s "$SERIAL" shell am start -n "${PKG}/${ACTIVITY}" >/dev/null
for _ in $(seq 1 90); do
  if adb -s "$SERIAL" shell run-as "$PKG" test -f databases/tempo_database 2>/dev/null; then
    break
  fi
  sleep 1
done
sleep 2
adb -s "$SERIAL" shell am force-stop "$PKG" >/dev/null

adb -s "$SERIAL" shell run-as "$PKG" mkdir -p shared_prefs >/dev/null

adb -s "$SERIAL" shell "run-as $PKG sh -c \"cat > shared_prefs/onboarding_preferences.xml\"" <<'EOF'
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <boolean name="completed" value="true" />
</map>
EOF

adb -s "$SERIAL" shell "run-as $PKG sh -c \"cat > shared_prefs/theme_prefs.xml\"" <<EOF
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="theme_mode">${THEME_MODE}</string>
    <boolean name="use_tempo_colors" value="true" />
</map>
EOF

adb -s "$SERIAL" push "$SQL_FILE" "$REMOTE_SQL" >/dev/null
adb -s "$SERIAL" shell "run-as $PKG sqlite3 databases/tempo_database \".read ${REMOTE_SQL}\""
adb -s "$SERIAL" shell rm -f "$REMOTE_SQL"

adb -s "$SERIAL" shell am start -n "${PKG}/${ACTIVITY}" >/dev/null
sleep 3

echo "Seeded ${PKG} on ${SERIAL} with ${THEME_MODE} theme."
