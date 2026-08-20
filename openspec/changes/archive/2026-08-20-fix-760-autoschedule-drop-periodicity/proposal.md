## Why

GitHub issue [#760](https://github.com/mandrecode/tempo/issues/760) reports duplicate future periodic tasks after an overdue occurrence has already been auto-rolled by reminder delivery/resync and the user later checks the original overdue task. The original overdue task currently keeps recurrence metadata, so it can still enter periodic-completion logic.

## What Changes

- When overdue rollover creates or reuses a linked next instance, strip recurrence metadata from the original overdue task (`periodicity`, interval, repeat-days, month-day option).
- Keep the original overdue task incomplete so the missed occurrence remains visible/history-safe.
- Preserve idempotent rollover behavior (`nextInstanceId` reuse and stale-link replacement).
- Keep periodic completion behavior unchanged for genuinely periodic tasks that have not been auto-rolled.

Non-goals:

- Do not change habit or habit-chain rollover behavior.
- Do not change periodic completion rollback semantics.
- Do not redesign reminder notification UX.

## Capabilities

### Modified Capabilities

- `task-reminder-rollover`: tightens overdue-rollover invariants so only the spawned future instance owns recurrence metadata after auto-rollover.

## Impact

- `RollOverduePeriodicTaskUseCase` transaction logic for persisted overdue originals.
- Rollover unit tests asserting persisted state after create/reuse/stale-link paths.
- `task-reminder-rollover` specification text for overdue rollover post-conditions.
