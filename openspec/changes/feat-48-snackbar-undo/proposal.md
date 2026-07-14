## Why

Tempo's snackbar feedback does not yet match the Didi-inspired visual language used by the rest of the app, and confirmed deletions are permanent even when triggered accidentally. [GitHub issue #48](https://github.com/mandrecode/tempo/issues/48) calls for cohesive snackbar styling and a short recovery path for destructive actions.

## What Changes

- Restyle the shared snackbar host with Didi-inspired shape, spacing, typography, color roles, and action treatment while preserving its placement above Tempo's floating navigation.
- Add localized Undo actions to successful deletion snackbars for individual tasks, categories and their tasks, completed tasks, habits, and habit chains with either of the chain dialog's deletion choices.
- Restore the full deleted snapshot when Undo is selected, including identifiers, hierarchy or membership, completion history, ordering, reminder data, and the category selected before deletion where applicable.
- Reconcile AlarmManager-backed reminders after restoration without making snackbar/UI code depend on Android infrastructure.
- Keep errors and non-reversible informational snackbars action-free.
- Non-goals: removing existing destructive-action confirmation dialogs, adding general undo/redo history, making reminder-clearing actions reversible, or changing unrelated cards, dialogs, and navigation styling.

## Capabilities

### New Capabilities

- `expressive-snackbar-feedback`: Shared visual and interaction requirements for Didi-inspired snackbars across task and routine screens.
- `destructive-action-undo`: Recoverable deletion behavior for task, category, completed-task, habit, and habit-chain operations.

### Modified Capabilities

None.

## Impact

- Affected UI: shared snackbar component, Tasks and Routines MVI contracts, screens, localized strings, previews, and UI tests.
- Affected domain/data: deletion and restoration use cases, repository/DAO snapshot operations, Room transactions, and unit/integration tests.
- Affected infrastructure: task and habit reminder rescheduling after a successful restore.
- No new dependency or database schema version is expected; existing entities can be restored with their original primary keys.
