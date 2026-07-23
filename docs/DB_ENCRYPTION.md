# Tempo Database Encryption

Tempo's local Room database (`tempo_database`) is encrypted at rest using
[SQLCipher for Android](https://github.com/sqlcipher/sqlcipher-android) (`net.zetetic:sqlcipher-android`).
This is fully transparent to the user — there is no app password or lock screen. It only protects
the database file on disk (e.g. against a rooted device, an ADB backup extraction, or shared
device storage); it does not protect against an attacker who can already run code as the app.

## Key lifecycle

The database passphrase is not a human password — it's 32 random bytes generated once per
install. It is still passed to SQLCipher as a bound byte[] parameter (never as the `x'<hex>'`
raw-key literal), so SQLCipher runs it through its own internal PBKDF2 derivation just like any
other passphrase; see [Migrating existing installs](#migrating-existing-installs) below for why
that distinction matters, and [Startup performance: kdf_iter](#startup-performance-kdf_iter) for
why that derivation isn't run at SQLCipher's default cost.

1. On first run, `KeystoreDbPassphraseProvider` (`core/data/local/security/`) generates an
   AES-256 key in the Android Keystore (alias `tempo_db_passphrase_key`, `AndroidKeyStore`
   provider, GCM, **no** `setUserAuthenticationRequired` — it must be usable with zero user
   interaction).
2. That Keystore key encrypts the random passphrase; the encrypted blob (IV + ciphertext,
   base64) is stored in a private `SharedPreferences` file, `tempo_secure_prefs`.
3. On every subsequent launch, the same blob is decrypted via the same Keystore key to recover
   the passphrase, which is handed to SQLCipher's `SupportOpenHelperFactory` to open the
   database through Room.

### Unrecoverable key failure

If the encrypted-passphrase blob exists but the Keystore key needed to decrypt it is gone or
invalidated, `KeystoreDbPassphraseProvider` throws `UnrecoverableDatabaseKeyException` rather
than silently minting a new passphrase. Generating a new passphrase in that state would
permanently orphan the existing encrypted database — the old data would still be on disk,
encrypted with a key nothing can reproduce. This exception must propagate as a loud, distinct
failure at the `TempoDatabase.getDatabase()` call site, never be swallowed into a generic crash
or a silent data reset.

This is not just a rare OS/device edge case: Android Keystore keys are hardware-bound and are
**never** included in Auto Backup or device-to-device transfer, by design. Without an explicit
exclusion, a stock Android backup would restore `tempo_database` and the `tempo_secure_prefs`
blob to a new device while leaving the Keystore key behind — hitting this exact failure on every
launch of the restored app. `app/src/main/res/xml/data_extraction_rules.xml` (API 31+) and
`backup_rules.xml` (API 23-30) both exclude the database file (plus its `-wal`/`-shm` sidecars)
and the secure prefs file for exactly this reason, so a restore/transfer is indistinguishable
from a fresh install instead of reproducing this failure. Users who want their data on a new
device should use **Settings → Backup** export/import, which carries its own passphrase and
isn't tied to Keystore state at all.

## Migrating existing installs

Users who installed Tempo before this feature shipped have a plaintext `tempo_database` file.
`DatabaseEncryptionMigrator` (`core/data/local/security/`) converts it to an encrypted database
exactly once, automatically, before Room ever opens the file:

1. Detect a plaintext file by peeking its first 16 bytes for the standard SQLite magic header
   (`"SQLite format 3"` + a NUL byte). Its absence means the file is already encrypted (or is a
   fresh install with no file at all) — nothing to do.
2. Use SQLCipher's `sqlcipher_export()` SQL function to copy the entire plaintext database into
   a new encrypted file at a temp path (`tempo_database.new`): open the plaintext file with
   `OPEN_READWRITE | CREATE_IF_NECESSARY` (a read-only connection, or one opened without the
   create flag even though the source file already exists, fails to `ATTACH` a new writable
   database with `SQLITE_CANTOPEN`/`ENOENT` — the "may create files" capability appears to be a
   property of the whole connection, not just the attached file), `ATTACH DATABASE <temp path> AS
   encrypted KEY <passphrase>` with the passphrase **bound as a parameter**, `SELECT
   sqlcipher_export('encrypted')`, `DETACH DATABASE encrypted`. The passphrase must be bound, not
   embedded as SQLCipher's `x'<hex>'` raw-key literal — that literal form skips PBKDF2 entirely
   (SQLCipher treats that exact text pattern as an already-derived key), which silently produces a
   *different* encryption key than `SupportOpenHelperFactory`/`openDatabase(byte[])` derive from
   the same bytes everywhere else the database is opened, making the migrated file unopenable by
   the rest of the app.
3. Verify the new encrypted file opens correctly under the passphrase (a cheap
   `SELECT count(*) FROM sqlite_master` query) before touching the original file.
4. Atomically swap: rename the original to `tempo_database.plaintext.bak`, then rename the temp
   file into the original's place, back-to-back with no I/O in between. Only then delete the
   `.bak` file and the old plaintext database's stale `-wal`/`-shm` sidecar files.

**Crash safety**: until step 4's first rename, the original plaintext file is never modified, so
a process kill at any point before that leaves it untouched — the next launch just retries from
scratch (discarding any partial `.new` file). If the process is killed between the two renames in
step 4, the next launch detects an orphaned `.plaintext.bak` file with no file at the primary
path, restores it, and retries. This logic lives at the top of
`DatabaseEncryptionMigrator.migrateIfNeeded()`.

> **Fixed bug (found while building the kdf_iter work below):** step 1's magic-header constant
> was `"SQLite format 3 ".toByteArray(...)` — ending in a *space* (`0x20`), not the real SQLite
> header's terminating NUL byte (`0x00`) — since this feature's original PR. `isPlaintextSqlite()`
> therefore always returned `false`, so this entire migration path had never actually run for any
> upgrading install; pre-encryption databases stayed silently plaintext. Caught by an instrumented
> test that seeds a real plaintext file and asserts the migration ran, which failed even before any
> `kdf_iter` change was involved. Fixed by correcting the literal's trailing byte.

Migration runs on `Dispatchers.IO`, never the main thread. There is no dedicated "migrating…" UI
for this — see `TempoDatabase.kt`/`core/di/DatabaseModule.kt` for how it's sequenced before the
Room builder runs, and `MainActivity.kt`'s existing loading state for how the app already handles
brief startup delays.

Encryption does not change the Room schema (entities, columns, indices, or `@Database(version = ...)`)
— only how the file is stored on disk — so no schema migration or `app/schemas/` regeneration is
needed alongside this change.

## Startup performance: kdf_iter

SQLCipher derives the actual encryption key from the passphrase via PBKDF2, controlled by the
`kdf_iter` pragma. This isn't a one-time cost paid at first-encryption — it's paid again on
**every single database open**, i.e. on every cold start. SQLCipher 4's compiled-in default is
256,000 PBKDF2-HMAC-SHA512 iterations, which exists to slow down brute-forcing a *guessable*
human password. Tempo's passphrase (see [Key lifecycle](#key-lifecycle)) is not that — it's 32
bytes from `SecureRandom()`, already wrapped by a hardware-backed Keystore key — so that stretching
buys no real security margin here, only latency. Measured directly with
`net.zetetic:sqlcipher-android` 4.17.0 on this repo's dev emulator (x86_64; a real device's
numbers will differ, but the scaling is what matters), open time scales almost linearly with
`kdf_iter`:

| kdf_iter | ~open time |
|---|---|
| 256,000 (SQLCipher default) | ~406ms |
| 64,000 | ~103ms |
| 16,000 (Tempo's `SqlCipherKdfIter.CURRENT`) | ~26ms |
| 4,000 | ~7.5ms |

`SqlCipherKdfIter.CURRENT = 16_000` was chosen as a deliberately conservative cut — 1/16th of the
default, not zero — trading a 16x reduction in per-launch cost for keeping a substantial,
non-trivial PBKDF2 pass as defense-in-depth, rather than dropping to SQLCipher's raw-key literal
(which skips PBKDF2 entirely). `SqlCipherKdfIter.LEGACY = 256_000` documents the value every
Tempo install created before this change used, un-overridden.

SQLCipher does not self-describe `kdf_iter` inside the file — whichever value keyed a database
must be supplied identically on every later open, or the derived key is silently wrong. That
means lowering the constant can't just apply going forward: every install that already has a
database keyed at `LEGACY` needs it re-derived once, in place, before it can be opened at
`CURRENT`. `KdfIterRekeyer` (`core/data/local/security/`), driven from the same
`DatabaseEncryptionMigrator.migrateIfNeeded()` entry point as the plaintext migration above,
handles this:

1. `DbKdfIterMarker` records which `kdf_iter` the on-disk file is currently keyed with, so this
   check is a cheap SharedPreferences read on the steady-state path (already re-keyed, or a fresh
   install created directly at `CURRENT` — see `TempoDatabase`'s `inboxCallback`) instead of an
   extra database open on every single launch. An absent marker is always safe — it falls through
   to actually probing the file below. A marker that already says `CURRENT` is trusted outright,
   by design, with no re-verification (re-verifying on every read would reintroduce the very
   per-launch open this marker exists to avoid). That's safe under normal operation because every
   writer of this marker only ever writes it immediately after verifying the file at that exact
   moment; it could only go stale in the "says `CURRENT` but isn't" direction via out-of-band
   tampering with the database file afterwards, bypassing every one of this app's own write paths
   (and Android's own backup mechanisms already exclude this file — see
   [Unrecoverable key failure](#unrecoverable-key-failure) above). If that ever happened, the
   subsequent Room open at `CURRENT` would fail loudly there rather than silently using a wrong
   key.
2. If the marker is missing, probe whether the file already opens at `CURRENT` (common case: an
   install that predates the marker itself, or a restored/partial backup) — if so, just persist
   the marker.
3. Otherwise probe whether it opens at `LEGACY`. If neither `CURRENT` nor `LEGACY` opens it (wrong
   passphrase, corruption, or anything else), leave the file alone — `TempoDatabase.getDatabase()`'s
   normal Room open surfaces the real failure rather than this logic guessing and potentially
   masking it.
4. If it opens at `LEGACY`, re-key via the same `sqlcipher_export()` mechanism the plaintext
   migration uses (`ATTACH ... KEY ?`, `PRAGMA <schema>.kdf_iter = CURRENT`,
   `SELECT sqlcipher_export(...)`), verify the result opens at `CURRENT`, then swap it into place
   with the same crash-safe rename sequence as the plaintext migration (own suffixes,
   `.kdf_rekey.new` / `.kdf_rekey.bak`, so the two conversions' recovery logic can never collide).

`MainActivity` additionally holds the system splash screen on-screen (via
`SplashScreen.setKeepOnScreenCondition`, capped at 1.5s) until `TempoApp`'s startup database
warm-up finishes, so whatever `kdf_iter` cost remains is more likely to land behind a screen users
already expect to sit on briefly, rather than surfacing as the in-app loading indicator on
whichever screen needs data first.
