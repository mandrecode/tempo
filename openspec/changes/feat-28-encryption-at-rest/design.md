## Context

Tempo persists all task/habit/chain data in a Room-backed SQLite database (`TempoDatabase.kt`, currently version 9, no encryption) and, since #26/PR #208, exports the same data as plain JSON via SAF file picker (`BackupRepositoryImpl.kt`, `BackupFileDataSource.kt`). There is currently zero encryption anywhere in the codebase: no Keystore usage, no SQLCipher, no `androidx.security-crypto`. Existing users already have real data in a plaintext `tempo_database` file, so any DB-encryption approach must migrate that file in place without loss.

Room itself has no encryption support — it delegates straight to Android's stock SQLite, which has none either. The only practical way to get page-level encryption under Room is to swap Room's `SupportSQLiteOpenHelper.Factory` for one backed by an encryption-capable SQLite build (SQLCipher).

## Goals / Non-Goals

**Goals:**
- Encrypt the on-disk Room database file so its contents aren't plainly readable by anything with raw filesystem access (rooted device, ADB backup extraction, shared/synced device storage).
- Keep the database transparent to use — no user-facing password, no added friction to normal app usage.
- Encrypt exported backup payloads so a shared/leaked export file isn't casually readable, while keeping backups portable/restorable (which rules out Keystore-only keys for exports — see Decisions).
- Migrate existing installs' plaintext database automatically and safely, with no data loss even if the process is killed mid-migration.
- Keep importing older plaintext `.json` backups working, indefinitely.

**Non-Goals:**
- Protecting against an attacker who has the device unlocked and the app already running (in-memory secrets, screen content) — out of scope; this is at-rest encryption only.
- Biometric/PIN-gated database access — the DB passphrase is Keystore-protected but not gated behind user authentication, since the goal is transparency, not an app lock feature (a future app-lock feature is a separate concern).
- Cloud sync/backup key escrow — out of scope; this only covers local DB and local export files.
- Changing the backup JSON's inner data schema (`BACKUP_SCHEMA_VERSION`) — encryption is an outer envelope around the existing schema, orthogonal to it.

## Decisions

### D1: SQLCipher for DB encryption, not `androidx.security-crypto`
`androidx.security-crypto` (`EncryptedFile`, `EncryptedSharedPreferences`) encrypts whole files or key-value pairs, not a live SQLite database that Room reads/writes incrementally via transactions, indices, and WAL. It has no SQLite integration point. SQLCipher (`net.zetetic:sqlcipher-android`) is a modified SQLite engine that transparently encrypts every page on read/write and implements `androidx.sqlite.db.SupportSQLiteOpenHelper.Factory`, so it plugs into Room's `RoomDatabase.Builder.openHelperFactory(...)` directly — no change to entities, DAOs, or queries. This is the standard, widely used approach for encrypting a Room database (used by Signal, Bitwarden, and others on Android). Alternative considered: rely solely on Android's OS-level file-based encryption (FBE, mandatory since API 24+) — rejected because it only protects data when the device is off/locked at the storage layer, not against a rooted or ADB-backup-extracted unlocked device, which is exactly the threat #28 calls out.

### D2: Keystore-wrapped random passphrase for the DB, not a user passphrase
The DB must stay fully transparent (Goal: no added friction). Android Keystore can generate a hardware-backed AES key that never leaves the device and requires no user interaction to use. That key encrypts a randomly generated 256-bit passphrase, which SQLCipher uses to open the DB — the user never sees or types anything. Alternative considered: user-set app passphrase for the whole DB — rejected, would require a lock-screen-like UX for a data app where that's disproportionate friction; not requested by #28 or the user.

### D3: User passphrase (not Keystore) for exports
Keystore keys are non-exportable by design and don't survive app uninstall — that's the whole point of hardware-backed keys. A backup encrypted with a Keystore key could never be restored on a new device or after reinstall, defeating the "compatible with future recovery and portability needs" goal in #28. A portable secret is unavoidable for something meant to travel outside the device: the user supplies a passphrase at export time (with a confirmation field to catch typos, since a lost passphrase means a permanently unrecoverable backup) and re-enters it at import time. Key derivation: PBKDF2WithHmacSHA256 (JDK-provided via `javax.crypto.SecretKeyFactory`, no new library), ~200k iterations, random salt per export. Payload: AES-256-GCM (authenticated encryption — detects wrong-passphrase/tampering via auth tag failure, not just garbled output).

### D4: New outer envelope format, not a `BACKUP_SCHEMA_VERSION` bump
Encryption wraps the existing plaintext backup JSON as an outer layer; it doesn't change any field in `BackupFileDto`. Bumping `BACKUP_SCHEMA_VERSION` would incorrectly imply the inner record schema changed, per `docs/BACKUP_FORMAT.md`'s own evolution rules ("anything [beyond additive fields] bumps schemaVersion"). Instead, a new top-level JSON shape (`BackupEncryptedEnvelopeDto`, marked by an `encryptionVersion` field) is introduced; import detects which shape a file is by attempting to decode the encrypted envelope first, falling back to the legacy plaintext shape. This keeps the two concerns (record schema vs. encryption format) independently versioned. The file extension also changes (`.json` → `.tempo`) as a user-facing signal that the format changed, while legacy `.json` imports keep working indefinitely.

### D5: One-time in-place migration via `sqlcipher_export()`, not destructive recreation
Existing users have real, valuable data in a plaintext `tempo_database`. `fallbackToDestructiveMigration` is not an option — that would delete all user data. SQLCipher's built-in `sqlcipher_export()` SQL function copies an entire plaintext database into a newly-keyed encrypted one via `ATTACH`/`DETACH`, without touching application code paths (DAOs, entities). The migration runs once, detected by peeking the DB file's first 16 bytes for the plaintext SQLite magic header (`"SQLite format 3\0"`) — its absence means the file is already encrypted (or is a fresh install with no file at all).

### D6: No new UI for the one-time DB migration
The app already shows a blank `MainUiState.Loading` surface at startup (`MainActivity.kt`) while preferences load, and all DB access happens through suspend repository calls with their own per-screen loading states. Given typical local task/habit databases are small, the one-time migration (a single-table-copy operation) should complete well under a second in practice; building dedicated "preparing your data" UI for this is disproportionate. This is flagged as something to sanity-check manually with a large seeded DB rather than solved architecturally up front.

## Risks / Trade-offs

- **[Risk]** Process death exactly between the two file renames in the migration swap (original renamed to `.bak`, new encrypted file not yet renamed into place) would leave neither file at the primary path. → **Mitigation**: on next launch, before the normal "does `tempo_database` exist" check, look for an orphaned `.bak` file with no primary file present and restore it, then retry migration from scratch.
- **[Risk]** The Keystore-protected DB passphrase becomes undecryptable (Keystore key invalidated by unusual OS/device behavior) while the encrypted DB and its encrypted-passphrase blob still exist. → **Mitigation**: never silently regenerate a new passphrase in this state (that would permanently orphan existing data behind a key nothing can produce again); surface a distinct, loud failure (`UnrecoverableDatabaseKeyException`) rather than a generic crash or silent data loss, so the user gets a clear "data unavailable" signal instead of corruption.
- **[Risk]** SQLCipher's native library increases APK size and adds a native dependency surface (JNI, ABI splits). → **Mitigation**: accepted trade-off given no first-party alternative exists for Room-level SQLite encryption; verify no `.so` conflicts with Room's bundled SQLite driver at build time.
- **[Risk]** Users who forget their export passphrase permanently lose that backup (by design — there's no recovery mechanism for a user-chosen secret). → **Mitigation**: confirmation field at export time to catch typos immediately, when the mistake is still cheap to fix; document this clearly in the export dialog copy and in `docs/BACKUP_FORMAT.md`.
- **[Trade-off]** DB encryption is fully transparent (Keystore-only) rather than user-passphrase-protected, so it does not protect against an attacker who can run code as the app itself (e.g. a rooted device with a malicious app running as/alongside Tempo) — only against raw file/backup extraction. This matches the goals stated in #28 and avoids disproportionate UX friction for a habit-tracking app.

## Migration Plan

1. Ship the new dependency, passphrase provider, migrator, and SQLCipher-backed `TempoDatabase` together — there is no intermediate rollout state; encryption is unconditional on first launch of the new version.
2. On first launch after upgrade: `DatabaseEncryptionMigrator` runs before Room opens the file (detects plaintext header → migrates via `sqlcipher_export()` → verifies → atomic swap). Fresh installs skip migration entirely (no file exists yet) and get an encrypted DB from creation.
3. No rollback path once migrated (downgrading the app to a pre-encryption build would not be able to open the SQLCipher file) — this is an accepted one-way migration, consistent with how Room's own destructive migrations are already treated in this codebase (`fallbackToDestructiveMigration(false)` — the team already does not support silent downgrades).
4. Backup export/import: no migration needed for existing exported files — legacy plaintext `.json` backups keep importing via the fallback path indefinitely; only newly created exports use the encrypted `.tempo` format.
5. Close #5 once this change is merged, referencing #28 and this change/PR.

## Open Questions

- Exact copy/wording for the "unrecoverable database key" failure screen (D2 risk) is left to implementation/UI review rather than fixed here — needs a clear, non-technical message and likely a "start fresh" action, but exact microcopy isn't a spec-level concern.
- Whether `docs/DB_ENCRYPTION.md` should be a standalone doc or folded into `docs/agents/DATA.md` is an implementation-time call, not a design decision.
