# Proposal: Export/Import with Schema Versioning and Conflict Reporting

GitHub issue: [#26 — feat: export/import with schema versioning and conflict reporting](https://github.com/mandrecode/tempo/issues/26)

## Why

Users currently have no way to move their data in or out of Tempo: uninstalling the app or losing a device means losing every task, category, habit, and habit chain. Before Tempo can expand into backup, collaboration, or sync scenarios (2.0 work), it needs a dependable, versioned export/import mechanism — a documented on-disk format that future versions can read, validate, and evolve.

## What Changes

- **Export**: users can export all app data (categories, tasks, habits, habit chains, chain memberships — including reminder configuration carried on those records — plus the app settings configuration) to a single versioned JSON file, saved wherever they choose via the system file picker (Storage Access Framework).
- **Import**: users can pick a previously exported file and restore it in one of two modes:
  - **Replace** — wipe current data and restore the file's contents exactly (backup/recovery path).
  - **Merge** — add the file's contents to existing data, skipping exact duplicates and reporting conflicts.
- **Schema versioning**: the export payload embeds a `schemaVersion`. Imports validate the version and fail with a clear, user-visible error when the file's version is newer than the app understands (or the payload is corrupt). Unknown JSON fields from older exports of *newer* minor revisions are tolerated where safe (`ignoreUnknownKeys`).
- **Integrity validation**: imports validate referential integrity (task→category, subtask→parent, chain member→habit/chain) before touching the database; a payload that fails validation is rejected with a specific reason and the local database is left untouched.
- **Conflict reporting**: in Merge mode, records that match an existing local record by natural key but differ in content are *not* silently merged or overwritten — they are skipped and reported in a post-import summary (imported / skipped-duplicate / conflict counts, with per-item detail).
- **Reminder rescheduling**: after a successful import, reminders for imported tasks/habits/chains are (re)scheduled so notifications keep working.
- **Settings UI**: a new "Backup" section in Settings hosts the Export and Import entry points, the import mode chooser (with a destructive-action warning for Replace), and the result/conflict summary dialog.

Non-goals (explicitly out of scope for this change):

- Cloud sync, scheduled/automatic backups, or collaboration features (this change is their foundation only).
- Interactive per-conflict resolution UI (keep-mine/take-theirs). Conflicts are reported, not interactively resolved.
- Importing formats other than Tempo's own export (no CSV/Todoist/etc.).

## Capabilities

### New Capabilities

- `data-export`: exporting the full user dataset to a versioned, documented JSON file via the system file picker.
- `data-import`: importing a Tempo export file with schema-version validation, payload integrity validation, Replace and Merge modes, conflict detection/reporting, and post-import reminder rescheduling.

### Modified Capabilities

_None — existing specs are unaffected; this adds new behavior only._

## Impact

- **New feature package** `features/backup/` (domain models + use cases, data DTOs + repository impl, presentation additions live in Settings).
- **Settings feature** gains a Backup section (Contract/ViewModel/Screen/Content updates); per the D3 decision this orchestration lives in the new `features/backup/domain` use cases, not in Settings presentation.
- **Data layer**: new serialization DTOs and a `BackupRepository` implementation reading/writing through existing DAOs inside Room transactions; new `@Binds` in `core/di/RepositoryModule.kt`.
- **Infrastructure**: a small file reader/writer over `ContentResolver` for SAF URIs; reuse of existing reminder schedulers (`TaskReminderScheduler`, `HabitReminderScheduler`) for post-import rescheduling.
- **Dependencies**: none new — `kotlinx.serialization` is already in the tech stack.
- **Docs**: the export format (schema version 1) is documented in `docs/` as the stable contract for future backup/sync work.
- **Tests**: unit tests for serialization round-trip, version/integrity validation, merge/conflict logic, and ViewModel behavior; forward/backward-compatibility tests around the versioned envelope.
