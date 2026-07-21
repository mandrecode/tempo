# Tempo Database Encryption

Tempo's local Room database (`tempo_database`) is encrypted at rest using
[SQLCipher for Android](https://github.com/sqlcipher/sqlcipher-android) (`net.zetetic:sqlcipher-android`).
This is fully transparent to the user — there is no app password or lock screen. It only protects
the database file on disk (e.g. against a rooted device, an ADB backup extraction, or shared
device storage); it does not protect against an attacker who can already run code as the app.

## Key lifecycle

The database passphrase is not a human password — it's 32 random bytes generated once per
install and used directly as SQLCipher's raw encryption key (no additional KDF pass, since it's
already high-entropy key material rather than something requiring stretching).

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
invalidated (an unusual OS/device condition), `KeystoreDbPassphraseProvider` throws
`UnrecoverableDatabaseKeyException` rather than silently minting a new passphrase. Generating a
new passphrase in that state would permanently orphan the existing encrypted database — the old
data would still be on disk, encrypted with a key nothing can reproduce. This exception must
propagate as a loud, distinct failure at the `TempoDatabase.getDatabase()` call site, never be
swallowed into a generic crash or a silent data reset.

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

Migration runs on `Dispatchers.IO`, never the main thread. There is no dedicated "migrating…" UI
for this — see `TempoDatabase.kt`/`core/di/DatabaseModule.kt` for how it's sequenced before the
Room builder runs, and `MainActivity.kt`'s existing loading state for how the app already handles
brief startup delays.

Encryption does not change the Room schema (entities, columns, indices, or `@Database(version = ...)`)
— only how the file is stored on disk — so no schema migration or `app/schemas/` regeneration is
needed alongside this change.
