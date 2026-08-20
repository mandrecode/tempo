## Why

Tempo currently stores all task, habit, and chain data in a plaintext Room/SQLite database, and the export/import feature (#26) writes fully plaintext JSON backups. As Tempo moves toward broader data portability, this leaves user data exposed to anyone with filesystem access (rooted device, ADB backup, or a casually shared export file) with no defense-in-depth beyond the OS's own file-based encryption. [#28](https://github.com/mandrecode/tempo/issues/28) asks for encryption at rest for both the local database and exported backup payloads; its sub-issue [#5](https://github.com/mandrecode/tempo/issues/5) covers only the database half and is a strict subset of #28, so this change addresses both together and closes #5 once merged.

## What Changes

- Add SQLCipher (`net.zetetic:sqlcipher-android`) to encrypt the Room-backed SQLite database file at rest.
- Generate a random 256-bit database passphrase on first run, protected by an Android Keystore-backed AES key; fetched transparently on every app start with no user-facing secret.
- **BREAKING (internal/data-format only, not user-visible)**: existing installs' plaintext `tempo_database` file is migrated in place to an encrypted SQLCipher database, once, automatically, before the database is first opened.
- Backup exports are now mandatorily encrypted: the user supplies a passphrase (with confirmation) at export time; the payload is wrapped in a new encrypted envelope (AES-256-GCM, key derived via PBKDF2WithHmacSHA256) instead of being written as plain JSON.
- Exported file extension changes from `.json` to `.tempo` to reflect the new format.
- Backup imports detect and decrypt the new encrypted envelope, prompting for the export passphrase. Export/import (#26) shipped the same day as this change, so there's no real unencrypted `.json` backup in the wild to stay compatible with: import requires the encrypted envelope, and content that isn't one is reported as corrupt rather than imported as-is.
- New Settings UI: passphrase entry dialogs for export (with confirmation) and import, plus a distinct "wrong passphrase" error state separate from existing "corrupt file" / "unsupported version" import errors.

## Capabilities

### New Capabilities
- `database-encryption`: Local Room/SQLite database is encrypted at rest via SQLCipher, keyed by a Keystore-protected passphrase, including one-time crash-safe migration of existing plaintext databases.
- `backup-encryption`: Exported backup payloads are encrypted with a user-supplied passphrase; there is no unencrypted import format — content that isn't a decodable encrypted envelope is reported as corrupt.

### Modified Capabilities
(none — the existing backup export/import behavior from #26 is extended with an encryption layer, not changed in its data semantics; no existing `openspec/specs/` capability covers backup today, so this is captured as new capabilities above.)

## Impact

- **Dependencies**: new Gradle dependency `net.zetetic:sqlcipher-android` (SQLCipher for Android).
- **Data layer**: `core/data/local/TempoDatabase.kt`, `core/di/DatabaseModule.kt`, new `core/data/local/security/` package (passphrase provider, migrator).
- **Backup feature**: `features/backup/data/repository/BackupRepositoryImpl.kt`, `features/backup/data/model/BackupFileDto.kt`, new `infrastructure/security/BackupEncryptionService.kt`.
- **Settings UI**: `features/settings/presentation/SettingsBackupDelegate.kt`, `SettingsContract.kt`, new passphrase dialog composables, new string resources (all locales).
- **Docs**: `docs/BACKUP_FORMAT.md` gets an encryption section; a new doc covers DB encryption/key lifecycle; `docs/PRIVACY_POLICY.md` § "Data Security" is updated to describe the new encryption (local DB and backup exports) in place of its current "depends on your device's security" language.
- **Existing users**: pay a one-time, automatic, crash-safe migration cost on first launch after upgrading; no data loss path, original plaintext file is never removed until the encrypted copy is verified.
- **GitHub**: closes #5 as subsumed by this change once merged; references #28.
