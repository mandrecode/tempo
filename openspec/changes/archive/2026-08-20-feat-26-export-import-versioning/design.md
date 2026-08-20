# Design: Export/Import with Schema Versioning and Conflict Reporting

## Context

Tempo stores all user data in a Room database (`tempo_database`, schema v9) with five tables: `tasks`, `categories`, `habits`, `habit_chains`, `habit_chain_members`. Reminder configuration lives on the records themselves (`reminderDate`, `periodicReminder`, repeat fields); actual alarms are scheduled through `TaskReminderScheduler` / `HabitReminderScheduler` and can be rebuilt from the database (`RescheduleRemindersWorker` already does this after boot).

There is no way to get data in or out of the app. This change adds a versioned JSON export and a validating import with Replace and Merge modes, plus explicit conflict reporting. `kotlinx.serialization` is already a mandatory library; no new dependencies are needed.

## Goals / Non-Goals

**Goals:**

- A stable, documented, versioned on-disk format (JSON, schema version 1) covering all five tables.
- Import that never corrupts local data: full validation before any write, all writes in one Room transaction.
- Replace (restore) and Merge (additive) import modes; Merge skips exact duplicates and reports conflicts instead of silently merging.
- Reminders keep firing after import.
- Format evolution rules that make forward/backward compatibility testable.

**Non-Goals:**

- Cloud sync, auto-backup scheduling, preference export, per-conflict interactive resolution, third-party formats (see proposal non-goals).
- Database schema changes — this feature reads/writes existing tables only; no Room migration.

## Decisions

### D1 — File format: versioned JSON envelope with dedicated DTOs

A single JSON document:

```json
{
  "schemaVersion": 1,
  "appVersion": "1.0.0",
  "exportedAt": "2026-07-21T10:00:00",
  "categories": [ ... ],
  "tasks": [ ... ],
  "habits": [ ... ],
  "habitChains": [ ... ],
  "habitChainMembers": [ ... ]
}
```

- Dedicated `@Serializable` DTOs in `features/backup/data/model/` (e.g. `BackupFileDto`, `TaskBackupDto`, …). DTOs are deliberately decoupled from Room entities and domain models: renaming an entity field must not silently change the file format. Alternative considered: serializing entities/domain models directly — rejected because it couples the persisted contract to refactors.
- `LocalDateTime` values as ISO-8601 strings (same representation the Room `Converters` already use); enums (`Priority`, `Periodicity`, `MonthDayOption`, habit type) as their names; `repeatDays` as an array of ISO day numbers.
- Original database ids are included in the file (needed for referential integrity and Replace mode).
- JSON configured with `ignoreUnknownKeys = true`; every field added to the format after v1 must have a default. **Evolution rule:** additive+defaulted changes keep the same `schemaVersion`; anything else (rename, removal, semantic change) bumps it. The format is documented in `docs/BACKUP_FORMAT.md`.
- `schemaVersion` is read first (a tiny envelope-only decode) so an unsupported version is reported as such even if the rest of the payload doesn't parse.

### D2 — Layering: pure-Kotlin core, thin Android edges

- `features/backup/domain/` (pure Kotlin):
  - Models: `BackupData` (lists of existing domain models + chain memberships), `ImportMode` (`REPLACE`, `MERGE`), `ImportOutcome` (sealed: `Success(summary)` / `UnsupportedVersion(fileVersion, maxSupported)` / `CorruptFile` / `ValidationFailed(reasons)`), `ImportSummary` (imported/skipped/conflict counts per entity type + `List<ImportConflict>`), `ImportConflict` (entity kind, display name, reason).
  - `BackupRepository` interface: `suspend fun exportToJson(): String`, `suspend fun importFromJson(json: String, mode: ImportMode): ImportOutcome`.
  - Use cases: `ExportBackupUseCase` (also produces the suggested file name `tempo-backup-<yyyyMMdd-HHmm>.json`), `ImportBackupUseCase`.
  - Pure logic components unit-testable on the JVM: `BackupPayloadValidator` (referential integrity, value sanity) and `MergePlanner` (duplicate/conflict classification + id remapping plan). Alternative considered: putting this logic inside the repository impl — rejected; it is the business core of the feature and must be testable without Android/Room.
- `features/backup/data/`: DTOs + mappers (DTO ↔ domain `BackupData`), `BackupRepositoryImpl` (serialization, validation invocation, transactional DB access via existing DAOs and `withTransaction`), bound in `core/di/RepositoryModule.kt`. Missing DAO bulk queries/inserts (e.g. `getAllSync`, `deleteAll`) are added where needed.
- `infrastructure/backup/`: `BackupFileDataSource` — reads/writes a `content://` URI via `ContentResolver` (SAF), bound in `InfrastructureModule.kt`. The ViewModel passes URIs from SAF launchers to it; domain never sees `android.*` types.
- Settings presentation hosts the UI (section, dialogs, SAF launchers) but contains no orchestration, per the D3 settings-scope decision in AGENTS.md.

### D3 — Import pipeline and transaction boundary

Order: **read file → decode envelope → version check → decode payload → validate → plan → single Room transaction → reschedule reminders**.

1. Validation (before any write): every `task.categoryId` resolves to a payload category; `parentTaskId` / `nextInstanceId` resolve to payload tasks (a dangling `nextInstanceId` is nulled, not fatal); chain members reference payload chains/habits; no duplicate ids within the payload. Failures produce `ValidationFailed` with human-readable reasons; the database is untouched.
2. All row mutations for an import happen in **one Room `withTransaction` block**. If anything throws, the transaction rolls back and the import reports failure with local data intact.
3. Scheduler side effects are **outside the transaction** (they touch AlarmManager, not the DB):
   - Before the transaction (Replace only): cancel alarms for current tasks/habits/chains with reminders.
   - After the transaction (both modes, success or failure): enqueue the existing `RescheduleRemindersWorker`, which rebuilds alarms from whatever is now in the database. Because rescheduling reads the DB, this is idempotent and also self-heals the cancel-then-rollback case.

### D4 — Replace mode

Validate, then in the transaction: delete all rows (children before parents), insert payload rows **preserving original ids** (including the seeded Inbox `id = -1`; if the payload has no default category, re-seed Inbox). Replace is the backup/recovery path: the result is exactly the file's contents. The UI requires an explicit confirmation with a destructive-action warning before running it.

### D5 — Merge mode: natural keys, duplicates vs conflicts

Ids in the file are meaningless locally, so Merge matches by natural key and remaps ids:

- **Natural keys:** Category → `name`; Task → `title` within its resolved category and parent task; Habit → `title`; HabitChain → `title`.
- **Duplicate** (natural key matches and remaining content — ignoring ids and `sortOrder` — is equal): skip, count as `skipped`, reuse the local row for reference remapping.
- **Conflict** (natural key matches but content differs): keep the local row untouched, do **not** import the incoming record, add an `ImportConflict` to the summary. References from other imported records resolve to the local row.
- **New** (no natural-key match): insert with a fresh id; all references (`categoryId`, `parentTaskId`, `nextInstanceId`, chain member ids) remapped via the plan. Tasks whose parent was skipped as a conflict are skipped too (reported, cascading).
- **Sort order:** imported new rows are offset past the current local max per scope, preserving their relative order without interleaving existing lists.
- Merging the same file twice is a no-op by construction (everything classifies as duplicate).

Alternative considered: overwrite-on-match ("last write wins") — rejected; the issue explicitly requires conflicts to be surfaced, not silently merged or lost.

### D6 — Settings UI

New "Backup" section in Settings (Contract/ViewModel/Content updates, MVI as usual):

- **Export**: row → `CreateDocument("application/json")` launcher with the suggested name → use case → write via `BackupFileDataSource` → success/error feedback.
- **Import**: row → `OpenDocument` launcher → mode chooser dialog (Merge default; Replace styled destructive with warning copy) → progress state while running → result dialog showing `ImportSummary` counts and the conflict list, or the typed error (`UnsupportedVersion` names the versions; `CorruptFile` / `ValidationFailed` show reasons).
- All strings in `values/strings.xml` + `values-es/strings.xml` (CI enforces translation parity).

## Risks / Trade-offs

- [Replace cancels alarms, then the transaction fails] → rollback keeps rows; `RescheduleRemindersWorker` is enqueued on failure too and rebuilds alarms from the untouched DB.
- [Conflicted records are dropped from the import] → acceptable for v1: nothing local is ever lost, and the summary tells the user exactly what was skipped; interactive resolution is deliberately deferred.
- [Natural-key matching may misclassify genuinely different items with identical titles] → scoped keys (task title within category+parent) reduce this; worst case is a reported conflict, never silent data loss.
- [Large datasets serialized in memory as one string] → dataset sizes for a personal task app are small (thousands of rows ≈ a few MB); streaming is not worth the complexity now.
- [Exported file readable by other apps/users] → user explicitly chooses the location via SAF; no silent writes to shared storage. Documented in the format doc that exports are unencrypted.
- [Future DB schema changes can drift from the backup format] → DTO decoupling plus `docs/BACKUP_FORMAT.md` evolution rules; compat fixture tests (checked-in v1 JSON) fail if decoding of the frozen format breaks.

## Migration Plan

No Room migration and no persisted-state changes; the feature is purely additive. Rollback = removing the UI entry points. Compatibility fixtures pin schema v1 from the first release.

## Open Questions

- None blocking. Preference export and auto-backup are follow-up candidates once the envelope exists.
