## Why

Habit-chain reminders are posted at their scheduled time even when the user has already started that occurrence manually, creating a redundant prompt for work already in progress. GitHub issue [#120](https://github.com/mandrecode/tempo/issues/120) requires reminder delivery to reflect the chain's current progress.

## What Changes

- Suppress a scheduled habit-chain reminder when any member habit is already completed for that reminder's occurrence date.
- Continue advancing and scheduling the chain's next recurring reminder even when the current notification is suppressed.
- Add regression coverage for unstarted, partially started, and stale/delayed chain occurrences.
- Non-goals: changing live-activity behavior, chain completion semantics, reminder cadence, or standalone habit reminders.

## Capabilities

### New Capabilities

- `habit-chain-reminder-delivery`: Defines when a scheduled habit-chain reminder is shown or suppressed based on progress for its occurrence date.

### Modified Capabilities

None.

## Impact

- Affects habit-chain delivery in `HabitReminderReceiver` and its unit tests.
- Uses existing habit completion history and repository APIs; no persistence schema, public API, dependency, UI, or localization changes are required.
