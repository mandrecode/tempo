#!/usr/bin/env bash
# Seeds a debug build with curated demo data for Google Play Store
# screenshots (GitHub issues #169, #252). Wipes existing app data on the
# target device/emulator, so only run against a disposable AVD.
#
# Seeds through the app's own encrypted-backup import flow. The earlier
# approach — pull databases/tempo_database, edit it with the host's sqlite3,
# push it back — stopped working when GitHub issue #212 made the Room DB
# SQLCipher-encrypted at rest: the pulled file is now ciphertext, and its
# passphrase never leaves the app's Android Keystore. Backup exports are
# encrypted with a *separate*, caller-chosen passphrase (docs/BACKUP_FORMAT.md),
# so we can build a backup file here and import it as a user would.
#
# Usage: scripts/seed-screenshot-data.sh <device-serial> [light|dark] [en|es]
set -euo pipefail

SERIAL="${1:?Usage: seed-screenshot-data.sh <device-serial> [light|dark] [en|es]}"
THEME="${2:-light}"
LOCALE="${3:-en}"
PKG="com.mandrecode.tempo.debug"
ACTIVITY="com.mandrecode.tempo.MainActivity"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# Only ever encrypts a throwaway demo dataset on a disposable AVD; it is not a
# credential and deliberately doesn't need to be secret.
SEED_PASSPHRASE="tempo-screenshot-seed"
SEED_FILE_NAME="tempo_seed.json"
DEVICE_SEED_PATH="/sdcard/Download/${SEED_FILE_NAME}"

LOCAL_BACKUP="$(mktemp --suffix=.json)"
trap 'rm -f "$LOCAL_BACKUP"' EXIT

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

# Generate the seed data relative to the *device's* current date, not the
# host's — they can differ (timezone, clock drift, CI runner vs emulator),
# which would otherwise shift what counts as "Today" and desync habit
# streaks from what the app itself considers current.
DEVICE_TODAY="$(adb -s "$SERIAL" shell date +%Y-%m-%d | tr -d '\r\n')"
python3 "${SCRIPT_DIR}/generate-seed-backup.py" \
  --locale "$LOCALE" \
  --theme "$THEME" \
  --today "$DEVICE_TODAY" \
  --passphrase "$SEED_PASSPHRASE" \
  -o "$LOCAL_BACKUP" >/dev/null

adb -s "$SERIAL" shell am force-stop "$PKG" >/dev/null
adb -s "$SERIAL" shell pm clear "$PKG" >/dev/null

# Grant the notification/alarm permissions the app would otherwise prompt
# for on first run, since screenshot capture skips onboarding.
adb -s "$SERIAL" shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS >/dev/null 2>&1 || true
adb -s "$SERIAL" shell appops set "$PKG" SCHEDULE_EXACT_ALARM allow >/dev/null 2>&1 || true

# Set the app's per-app language (Android 13+ LocaleManager) so its own UI
# strings (nav labels, Settings, "Today", etc.) render in $LOCALE. This is
# separate from the seeded content's language, controlled above via
# generate-seed-backup.py --locale. The app declares support for en-US/es-ES
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

# Mark the current what's-new entry as already seen, or its bottom sheet opens
# over the first screenshot (MainViewModel shows it whenever onboarding is
# complete and the stored id differs from WhatsNewRegistry.latest, which is
# every fresh install). Read the id from the registry rather than hardcoding
# it, so shipping a new entry doesn't silently break the pipeline.
WHATS_NEW_ID="$(grep -oP '(?<=id = ")[^"]+' \
  "${REPO_ROOT}/app/src/main/java/com/mandrecode/tempo/features/whatsnew/presentation/WhatsNewRegistry.kt")"
if [ -z "$WHATS_NEW_ID" ]; then
  echo "Could not read WhatsNewRegistry.latest's id — the what's-new sheet would cover the first screenshot." >&2
  exit 1
fi
adb -s "$SERIAL" shell "run-as $PKG sh -c \"cat > shared_prefs/whats_new_preferences.xml\"" <<EOF
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="last_seen_entry_id">${WHATS_NEW_ID}</string>
</map>
EOF

adb -s "$SERIAL" push "$LOCAL_BACKUP" "$DEVICE_SEED_PATH" >/dev/null
python3 "${SCRIPT_DIR}/import-seed-backup.py" \
  "$SERIAL" "$SEED_FILE_NAME" "$SEED_PASSPHRASE" "$LOCALE"
adb -s "$SERIAL" shell rm -f "$DEVICE_SEED_PATH" >/dev/null

adb -s "$SERIAL" shell am force-stop "$PKG" >/dev/null
adb -s "$SERIAL" shell am start -n "${PKG}/${ACTIVITY}" >/dev/null
sleep 3

echo "Seeded ${PKG} on ${SERIAL} with ${THEME_MODE} theme (${LOCALE})."
