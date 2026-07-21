# Tasks: Export/Import with Schema Versioning and Conflict Reporting

## 1. Backup domain layer (pure Kotlin)

- [x] 1.1 Create `features/backup/domain/model/`: `BackupData` (categories, tasks, habits, habit chains, chain memberships using existing domain models), `ChainMembership`, `ImportMode` (REPLACE, MERGE)
- [x] 1.2 Create outcome models: `ImportOutcome` (sealed: Success / UnsupportedVersion / CorruptFile / ValidationFailed), `ImportSummary` (per-entity imported/skipped/conflict counts + conflict list), `ImportConflict` (entity kind, display name, reason)
- [x] 1.3 Create `BackupPayloadValidator` (pure Kotlin): referential integrity checks per spec (task→category, subtask→parent, chain member→chain/habit, duplicate ids; dangling `nextInstanceId` nulled, not fatal) + unit tests
- [x] 1.4 Create `MergePlanner` (pure Kotlin): natural-key matching, duplicate vs conflict classification, cascading skips for dependents of conflicts, id remapping plan, sort-order offsetting + unit tests
- [x] 1.5 Create `BackupRepository` interface (`exportToJson(): String`, `importFromJson(json, mode): ImportOutcome`) and use cases `ExportBackupUseCase` (with suggested file name `tempo-backup-<yyyyMMdd-HHmm>.json`) and `ImportBackupUseCase` + unit tests

## 2. Backup data layer (DTOs, serialization, repository)

- [x] 2.1 Create `features/backup/data/model/` serialization DTOs (`BackupFileDto` envelope with `schemaVersion`/`appVersion`/`exportedAt`, plus per-entity DTOs with stable field names and database ids); envelope-only decode for early version check; Json configured with `ignoreUnknownKeys = true`
- [x] 2.2 Create `features/backup/data/mapper/` DTO ↔ domain `BackupData` mappers (dates as ISO-8601 strings, enums as names, repeatDays as ISO day numbers) + unit tests
- [x] 2.3 Add missing DAO operations needed for bulk export/import (sync get-all, delete-all, bulk insert preserving ids) to the five DAOs
- [x] 2.4 Implement `BackupRepositoryImpl`: export (read all via DAOs → map → serialize); import pipeline (decode envelope → version check → decode payload → validate → plan → single Room `withTransaction` applying Replace or Merge per design D3–D5, re-seeding Inbox when absent in Replace) ; bind in `core/di/RepositoryModule.kt`
- [x] 2.5 Unit-test `BackupRepositoryImpl` logic: version rejection (newer schema), corrupt file, validation failure leaves DB untouched (transaction rollback path), merge no-op on re-import
- [x] 2.6 Add forward/backward-compatibility fixture tests: checked-in frozen v1 JSON fixture decodes with expected values; fixture with unknown extra fields imports; round-trip export→replace-import equality

## 3. Reminder rescheduling and file I/O infrastructure

- [x] 3.1 Create `infrastructure/backup/BackupFileDataSource` (read/write `content://` URIs via `ContentResolver`), bind in `InfrastructureModule.kt`
- [x] 3.2 Wire reminder side effects around import per design D3: cancel existing alarms before Replace transaction; enqueue `RescheduleRemindersWorker` after the transaction on both success and failure + unit tests for the orchestration

## 4. Settings UI

- [x] 4.1 Extend `SettingsContract`: Backup section state (in-progress flag, result/error to show), events (ExportRequested/ExportDestinationPicked, ImportRequested/ImportFilePicked, ImportModeChosen, ResultDismissed), effects as needed
- [x] 4.2 Extend `SettingsViewModel`: invoke use cases + `BackupFileDataSource`, map `ImportOutcome` to UI state + unit tests (MockK, Turbine)
- [x] 4.3 Add Backup section UI in Settings Content: Export and Import rows, SAF launchers (`CreateDocument("application/json")` with suggested name, `OpenDocument`) in the Screen layer
- [x] 4.4 Add import mode chooser dialog (Merge default; Replace destructive-styled with warning), progress indication, and result dialog (summary counts + conflict list, or typed error incl. file vs supported version)
- [x] 4.5 Add all string resources to `values/strings.xml` and `values-es/strings.xml`; `@Preview` composables for new components under `src/debug/`

## 5. Documentation and verification

- [x] 5.1 Write `docs/BACKUP_FORMAT.md`: schema v1 field-by-field description, evolution rules (additive+defaulted keeps version; otherwise bump), unencrypted-export note
- [x] 5.2 Run `./gradlew testDebugUnitTest` and fix failures; check coverage thresholds with `./gradlew koverVerifyDebug`
- [x] 5.3 Run `./gradlew ktlintFormat`, `./gradlew ktlintCheck`, `./gradlew :app:detekt`, and `./gradlew lintDebug` (strings.xml touched); fix findings without growing the detekt baseline
- [x] 5.4 Run `openspec validate feat-26-export-import-versioning`
- [x] 5.5 Manual smoke test on device/AVD: export → uninstall-simulating wipe (Replace import) → verify data and reminders; merge same file twice → verify no-op summary; import a conflicting edit → verify conflict report
