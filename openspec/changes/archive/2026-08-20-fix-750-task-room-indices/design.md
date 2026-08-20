## Context

`TaskEntity` maps the `tasks` table. `TaskDao` repeatedly filters by `categoryId` and `parentTaskId`, but neither column is indexed, so SQLite uses full table scans. The established pattern in this codebase is `HabitChainMemberEntity`, which indexes `habitId` via `indices = [Index("habitId")]`. Room auto-names such indices `index_<table>_<column>`, so the exported schema will contain `index_tasks_categoryId` and `index_tasks_parentTaskId`.

## Decision: indices-only (D2)

Two options were considered for #750:

1. **Indices-only** — add `@Index` on both columns. Pure performance improvement; zero behavior change; no reconciliation with existing delete flows.
2. **Indices + `@ForeignKey(onDelete = CASCADE)`** — adds referential integrity and DB-level cascade. This is a behavior change because the task repository and use cases already perform manual cascade deletion of subtasks and category reassignment; enabling DB cascade risks double-deletion semantics and requires careful reconciliation.

This change takes **option 1 (indices-only)**. Foreign-key cascade is deferred to a separate considered change so the manual cascade logic can be reconciled deliberately rather than as a side effect of a performance fix.

## Migration design

- The migration is additive and idempotent: it issues `CREATE INDEX IF NOT EXISTS` for both index names, matching the names Room generates so `runMigrationsAndValidate` succeeds against the exported `9.json`.
- No data is read or rewritten; no table rebuild is required. The migration runs outside any table-swap and does not touch foreign keys.
- Index names are pinned to Room's deterministic naming (`index_tasks_categoryId`, `index_tasks_parentTaskId`) to keep the runtime schema identity-equal to the exported schema.

## Data invariants

- Index creation does not change row contents, ordering guarantees, or query results — only access paths.
- Existing v8 installs gain the indices in place; fresh installs receive them from the v9 schema. Both paths converge to the same schema hash validated by Room.
