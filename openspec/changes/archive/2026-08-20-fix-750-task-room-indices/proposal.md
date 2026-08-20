## Why

GitHub issue [#750](https://github.com/mandrecode/tempo/issues/750) (Phase 2 of [#461](https://github.com/mandrecode/tempo/issues/461)) reports that `TaskEntity` (`tasks` table) declares no indices on `categoryId` or `parentTaskId`, yet `TaskDao` filters on both columns in hot paths (`getSubtasks`, `getMaxSortOrder`, `deleteTasksByCategoryId`, `getCompletedTopLevelTaskIds`, and the bulk subtask updates/deletes). Without indices SQLite performs full table scans, degrading as the task list grows. By contrast `HabitChainMemberEntity` already indexes `habitId`, establishing the project pattern.

## What Changes

- Add `@Index("categoryId")` and `@Index("parentTaskId")` to `TaskEntity` so the columns `TaskDao` filters on are backed by indices.
- Bump the Room database `version` from 8 to 9 and add `MIGRATION_8_9` that creates the two indices on existing installs via `CREATE INDEX`.
- Register `MIGRATION_8_9` alongside the existing migrations and export the regenerated `app/schemas/9.json`.
- Add an in-memory Room migration test validating the v8→v9 upgrade.

### D2 decision: indices-only

This change adds indices only (pure performance, zero behavior change). It does NOT add `@ForeignKey(onDelete = CASCADE)` on `categoryId`/`parentTaskId`. Foreign-key cascade is deferred to a separate considered change so it can be reconciled with the existing manual cascade logic in the task repository and use cases.

Non-goals:

- Do not add foreign keys or cascade delete behavior on the `tasks` table.
- Do not change task domain models, mappers, repository behavior, or any query semantics.
- Do not alter any other table or entity.

## Capabilities

### New Capabilities

- `task-data-persistence`: Defines the persistence-layer indexing guarantees for the `tasks` table and the migration contract that keeps existing installs consistent with the exported Room schema.

### Modified Capabilities

- None.

## Impact

- `core/data/entity/TaskEntity.kt`: index declarations.
- `core/data/local/TempoDatabase.kt`: version bump and `MIGRATION_8_9` registration.
- `app/schemas/.../9.json`: newly exported schema with task indices.
- `core/data/local/MigrationTest.kt`: v8→v9 migration coverage.
