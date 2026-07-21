## 1. Dependency setup

- [x] 1.1 Add `sqlcipher` version and `sqlcipher-android` (`net.zetetic:sqlcipher-android`, corrected from the originally planned but non-existent `net.zetetic:android-database-sqlite` coordinate — that legacy library is deprecated; `net.zetetic:sqlcipher-android` is its Maven Central successor) library alias to `gradle/libs.versions.toml`, plus an explicit `androidx.sqlite:sqlite` alias per its integration docs
- [x] 1.2 Add `implementation(libs.sqlcipher.android)` and `implementation(libs.androidx.sqlite)` to `app/build.gradle.kts` near the existing Room dependencies
- [x] 1.3 Ran a clean debug build (`./gradlew :app:assembleDebug`) — succeeded with no native library (`.so`) duplication/merge conflicts against Room's bundled SQLite driver; no `packaging.jniLibs.pickFirsts` needed

## 2. Keystore-backed database passphrase provider

- [x] 2.1 Create `core/data/local/security/DbPassphraseProvider.kt` interface: `suspend fun getOrCreatePassphrase(): ByteArray`
- [x] 2.2 Create `core/data/local/security/UnrecoverableDatabaseKeyException.kt` for the case where the Keystore key can't be recovered but an encrypted DB/passphrase blob already exists
- [x] 2.3 Create `core/data/local/security/KeystoreDbPassphraseProvider.kt`: generate/reuse an AES-256 `AndroidKeyStore` key (`KeyGenParameterSpec`, GCM, no `setUserAuthenticationRequired`); on first run generate a random 32-byte passphrase, encrypt it, persist `iv + ciphertext` (base64) in a new private SharedPreferences file (`tempo_secure_prefs`); on later runs decrypt and return the same passphrase; throw `UnrecoverableDatabaseKeyException` instead of silently regenerating when the Keystore key can't decrypt an existing blob (also guards against a stale leftover Keystore alias by deleting it before minting a fresh key+passphrase pair)
- [x] 2.4 Create `core/data/local/security/FakeDbPassphraseProvider.kt` under `app/src/test/...` (fixed byte array) so downstream logic is unit-testable without Keystore
- [x] 2.5 Add a Hilt `@Provides` entry for `DbPassphraseProvider` (bind `KeystoreDbPassphraseProvider` in production) in `core/di/DatabaseModule.kt`

## 3. One-time plaintext-to-encrypted database migration

- [x] 3.1 Create `core/data/local/security/DatabaseEncryptionMigrator.kt`: resolve the DB file path, skip if absent (fresh install)
- [x] 3.2 Implement plaintext detection by peeking the first 16 bytes for the SQLite magic header (`"SQLite format 3\0"`)
- [x] 3.3 Implement startup recovery for interrupted prior runs: clean up orphaned temp/`.new` files, and detect/restore from a `.plaintext.bak` file left behind if the process died between the two renames
- [x] 3.4 Implement the `sqlcipher_export()` migration sequence against a temp file (`tempo_database.new`): open plaintext DB with `OPEN_READWRITE | CREATE_IF_NECESSARY` (required for `ATTACH` to create the encrypted file — a real, instrumented-test-caught bug: without `CREATE_IF_NECESSARY` ATTACH fails with `SQLITE_CANTOPEN`/`ENOENT` even though the source file already exists), `ATTACH ... KEY ?` with the passphrase **bound as a parameter** (binding is required — embedding it as SQLCipher's `x'<hex>'` raw-key literal skips PBKDF2 and silently derives a different key than every other place the database is opened, another bug caught by the instrumented migration test), `SELECT sqlcipher_export(...)`, `DETACH`
- [x] 3.4a Discovered and fixed via `DatabaseEncryptionMigratorTest` on a real emulator: `System.loadLibrary("sqlcipher")` was never called anywhere — this newer `net.zetetic:sqlcipher-android` library (unlike the deprecated `android-database-sqlcipher`) does not load its native core automatically. Added to `TempoApp.onCreate()`.
- [x] 3.5 Verify the new encrypted file opens correctly under the passphrase (a cheap `SELECT count(*) FROM sqlite_master` row-count query) before touching the original
- [x] 3.6 Perform the atomic swap: rename original → `.plaintext.bak`, rename `.new` → original (back-to-back, no I/O in between); then delete the `.bak` and old `-wal`/`-shm` sidecars
- [x] 3.7 Ensure the whole migrator runs on `Dispatchers.IO`, never on the main thread

## 4. Wire SQLCipher into Room

- [x] 4.1 Update `TempoDatabase.kt`: make `getDatabase()` suspend (guarded by a `Mutex`, replacing the old `synchronized` double-checked lock since suspend calls can't happen inside `synchronized`), fetch the passphrase via `DbPassphraseProvider`, run `DatabaseEncryptionMigrator` before building Room, then build with SQLCipher's `SupportOpenHelperFactory(passphrase)` (actual class name in the current `net.zetetic:sqlcipher-android` library, not `SupportFactory` as originally planned from the deprecated library's docs) via `.openHelperFactory(...)`; kept `version = 9` and all existing migrations unchanged (no schema bump)
- [x] 4.2 Update `core/di/DatabaseModule.kt`: dispatch the now-suspending `getDatabase()` call explicitly onto `Dispatchers.IO` inside the `@Provides` function via `runBlocking(Dispatchers.IO)`
- [x] 4.3 Confirmed nothing in `TempoApp.kt`/app startup touches the database eagerly on the main thread (only `HiltWorkerFactory` config in `onCreate()`)

## 5. Backup export/import encryption

- [x] 5.1 Create `infrastructure/security/BackupEncryptionService.kt`: `encrypt(plaintext: String, passphrase: CharArray): EncryptedEnvelope` and `decrypt(envelope: EncryptedEnvelope, passphrase: CharArray): DecryptResult` (a sealed `Success/WrongPassphrase/Corrupt` type, clearer than a bare `Result<String>` for distinguishing failure kinds), using PBKDF2WithHmacSHA256 (200k iterations, random salt) and AES-256-GCM (random IV); `AEADBadTagException` maps to `WrongPassphrase`
- [x] 5.2 Create `BackupEncryptedEnvelopeDto` in `features/backup/data/model/` (`encryptionVersion`, `kdf`, `iterations`, `salt`, `iv`, `ciphertext`) — distinct from the existing `BackupEnvelopeDto` used for schema-version peeking; base64 mapping in new `features/backup/data/mapper/BackupEncryptionMapper.kt` uses `kotlin.io.encoding.Base64` (not `android.util.Base64`/`java.util.Base64` — the former isn't mocked in this project's Robolectric-less JVM unit tests, the latter needs API 26+, above minSdk 24)
- [x] 5.3 Updated `BackupRepositoryImpl.kt`: `exportToJson()` → `exportEncrypted(passphrase)` wraps the (renamed, now-private) plaintext-JSON builder through `BackupEncryptionService`; `importFromJson` gained an `isEncryptedBackup(content)` peek and a `passphrase: CharArray?` param, decrypting first when the content is an encrypted envelope and falling back to the legacy plaintext `BackupFileDto`/`BackupEnvelopeDto` path otherwise; added `ImportOutcome.WrongPassphrase`
- [x] 5.4 Updated the export filename suggestion (`ExportBackupUseCase`) from `tempo-backup-<date>-<time>.json` to `tempo-backup-<date>-<time>.tempo`
- [x] 5.5 Updated `SettingsContract.kt`: added `BackupDialog.EnterExportPassphrase`, `BackupDialog.EnterImportPassphrase(attemptsFailed: Boolean)`, and `ExportPassphraseConfirmed`/`ImportPassphraseEntered` events (no separate `ImportError.WrongPassphrase` needed — wrong-passphrase loops back to `EnterImportPassphrase(attemptsFailed = true)` directly rather than a terminal failure dialog)
- [x] 5.6 Updated `SettingsBackupDelegate.kt`: `ExportClicked` now opens the passphrase dialog first; only a confirmed matching passphrase triggers export. Import reads the file up front (now async, so added a cancellable `importReadJob` to stop a dismissed dialog from being resurrected by a still-in-flight read), detects encryption, and remembers the chosen mode so a wrong-passphrase retry doesn't force re-choosing Merge/Replace
- [x] 5.7 Created `ExportPassphraseDialog.kt` and `ImportPassphraseDialog.kt`, reusing the shared `TempoConfirmDialog` shell (added an optional `confirmEnabled` param to it, backward compatible) rather than duplicating `ImportModeDialog.kt`'s bespoke layout, since this is a plain two-button confirm/cancel shape
- [x] 5.8 Added the new string resources to `values/strings.xml` and `values-es/strings.xml` (only translated locale in this project)

## 6. Documentation

- [x] 6.1 Updated `docs/BACKUP_FORMAT.md`: replaced the "exports are unencrypted" callout with an "Encryption" section (envelope shape, KDF params, mandatory passphrase, `.tempo` extension, continued legacy `.json` import support), plus a note in "Evolution rules" that `encryptionVersion` and `schemaVersion` are independent
- [x] 6.2 Added `docs/DB_ENCRYPTION.md` covering Keystore key lifecycle, the migration sequence (with crash-safety reasoning), and the unrecoverable-key failure contract
- [x] 6.3 Updated `docs/PRIVACY_POLICY.md` § "Data Security": describes the encrypted local database (SQLCipher, Keystore-protected key, transparent) and encrypted backup exports (user-chosen passphrase, unrecoverable if forgotten); bumped "Last Updated" to July 21, 2026

## 7. Tests

- [x] 7.1 Unit test `BackupEncryptionServiceTest`: round-trip encrypt/decrypt, wrong-passphrase failure (including tampered-ciphertext, which fails the same AES-GCM auth-tag check as a wrong passphrase), malformed IV input
- [x] 7.2 Unit test legacy-vs-encrypted detection in `BackupRepositoryImpl`: existing `v1-backup.json` fixture still imports plaintext; added a test that encrypts that same fixture through a real `BackupEncryptionService` and confirms it still imports correctly via `isEncryptedBackup`/`importFromJson`; added a wrong-passphrase-on-import test
- [x] 7.3 Instrumented test `KeystoreDbPassphraseProviderTest` on-device: passphrase round-trip via real Keystore, persists across new provider instances, 32-byte length
- [x] 7.4 Instrumented test `DatabaseEncryptionMigratorTest` on-device: seed a real plaintext DB fixture (via plain `android.database.sqlite.SQLiteDatabase`), run the migrator, assert data integrity and that SQLCipher can reopen the result; simulate a crash mid-migration (orphaned `.new` file) and between the swap renames (`.plaintext.bak`-only state) and assert cleanup/recovery; assert idempotency on a second call. **Caught two real bugs** (see 3.4/3.4a) that unit tests could never have caught — a missing `System.loadLibrary`, and a key-derivation mismatch that would have made every migrated database unopenable by the rest of the app
- [x] 7.5 Instrumented smoke test `TempoDatabaseSqlCipherSmokeTest`: SQLCipher-backed `TempoDatabase` (via the same `SupportOpenHelperFactory` wiring as production, isolated database name) opens, writes, reads, and closes correctly end-to-end
- [x] 7.6 Ran `./gradlew testDebugUnitTest`, `./gradlew ktlintFormat`, `./gradlew ktlintCheck`, `./gradlew :app:detekt`, `./gradlew koverVerifyDebug` locally — all pass (detekt required refactoring `BackupRepositoryImpl`/`KeystoreDbPassphraseProvider`/`BackupEncryptionService` to stay within `TooManyFunctions`/`ReturnCount`/`ThrowsCount`/`SwallowedException`/`TooGenericExceptionCaught` thresholds, no baseline suppressions added)
- [x] 7.7 Ran `./gradlew connectedDebugAndroidTest` on the Pixel 10 AVD for the instrumented tests above — all 9 pass after the fixes in 3.4/3.4a
- [x] 7.8 Manual smoke test on the Pixel 10 AVD: fresh-installed this branch's debug build, confirmed via logcat that `libsqlcipher.so` loads and "Database keying operation returned:0" on real first launch (no crash, WorkManager/DAO access succeeds through the real Hilt-wired `TempoDatabase`); navigated Settings → Backup → Export data and visually confirmed the new "Set a passphrase" dialog renders correctly with the Export button correctly disabled until both fields are filled. Did not separately hand-simulate upgrading from an old pre-encryption `main` build with real data — that exact migration mechanism (detect plaintext → ATTACH → export → verify → swap, crash-recovery included) is already covered end-to-end against the real production `DatabaseEncryptionMigrator` class by the instrumented tests in 7.4

## 8. Wrap-up

- [ ] 8.1 Run `openspec validate feat-28-encryption-at-rest` and resolve any issues
- [ ] 8.2 Open the PR against `main`, referencing `Closes #28`
- [ ] 8.3 Close GitHub issue #5 with a comment pointing to #28/this PR, noting it is fully covered by this change
