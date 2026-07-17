#!/usr/bin/env bash
# Seeds a debug build with curated demo data for Google Play Store
# screenshots (GitHub issue #169). Wipes existing app data on the target
# device/emulator, so only run against a disposable AVD.
#
# Usage: scripts/seed-screenshot-data.sh <device-serial> [light|dark] [en|es]
set -euo pipefail

SERIAL="${1:?Usage: seed-screenshot-data.sh <device-serial> [light|dark] [en|es]}"
THEME="${2:-light}"
LOCALE="${3:-en}"
PKG="com.mandrecode.tempo.debug"
ACTIVITY="com.mandrecode.tempo.MainActivity"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

LOCAL_SQL="$(mktemp)"
LOCAL_DB="$(mktemp)"
trap 'rm -f "$LOCAL_SQL" "$LOCAL_DB"' EXIT
python3 "${SCRIPT_DIR}/generate-seed-sql.py" --locale "$LOCALE" > "$LOCAL_SQL"

case "$THEME" in
  light) THEME_MODE="LIGHT" ;;
  dark) THEME_MODE="DARK" ;;
  *) echo "Unknown theme '$THEME' (expected light|dark)" >&2; exit 1 ;;
esac

case "$LOCALE" in
  en|es) ;;
  *) echo "Unknown locale '$LOCALE' (expected en|es)" >&2; exit 1 ;;
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

# Set the app's per-app language (Android 13+ LocaleManager) so its own UI
# strings (nav labels, Settings, "Today", etc.) render in $LOCALE. This is
# separate from the seeded content's language, controlled above via
# generate-seed-sql.py --locale. The app declares support for en-US/es-ES
# in res/xml/locales_config.xml.
adb -s "$SERIAL" shell cmd locale set-app-locales "$PKG" --user 0 --locales "$LOCALE" >/dev/null

# First launch creates the Room DB (and seeds the default Inbox category via
# TempoDatabase.inboxCallback) and the shared_prefs directory.
adb -s "$SERIAL" shell am start -n "${PKG}/${ACTIVITY}" >/dev/null
db_ready=0
for _ in $(seq 1 90); do
  if adb -s "$SERIAL" shell run-as "$PKG" test -f databases/tempo_database 2>/dev/null; then
    db_ready=1
    break
  fi
  sleep 1
done
if [ "$db_ready" -ne 1 ]; then
  echo "Timed out waiting for ${PKG}'s database to be created on ${SERIAL}." \
    "Check that the serial is correct, the app is installed, and it isn't crashing on launch." >&2
  exit 1
fi
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

# Apply the seed SQL with the host's own sqlite3 rather than depending on a
# device-side sqlite3 binary, which not every system image ships (e.g. the
# medium_tablet Google Play tablet image). Pull the DB, checkpoint any WAL
# into it, apply the SQL locally, then push the single resulting file back.
adb -s "$SERIAL" exec-out run-as "$PKG" cat databases/tempo_database > "$LOCAL_DB"
adb -s "$SERIAL" shell run-as "$PKG" test -f databases/tempo_database-wal 2>/dev/null \
  && adb -s "$SERIAL" exec-out run-as "$PKG" cat databases/tempo_database-wal > "${LOCAL_DB}-wal" || true
adb -s "$SERIAL" shell run-as "$PKG" test -f databases/tempo_database-shm 2>/dev/null \
  && adb -s "$SERIAL" exec-out run-as "$PKG" cat databases/tempo_database-shm > "${LOCAL_DB}-shm" || true

sqlite3 "$LOCAL_DB" "PRAGMA wal_checkpoint(TRUNCATE);" ".read ${LOCAL_SQL}"
rm -f "${LOCAL_DB}-wal" "${LOCAL_DB}-shm"

cat "$LOCAL_DB" | adb -s "$SERIAL" shell "run-as $PKG sh -c \"cat > databases/tempo_database\""
adb -s "$SERIAL" shell run-as "$PKG" rm -f databases/tempo_database-wal databases/tempo_database-shm

adb -s "$SERIAL" shell am start -n "${PKG}/${ACTIVITY}" >/dev/null
sleep 3

echo "Seeded ${PKG} on ${SERIAL} with ${THEME_MODE} theme (${LOCALE})."
