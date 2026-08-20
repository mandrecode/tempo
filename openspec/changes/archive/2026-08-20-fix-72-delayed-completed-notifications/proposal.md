## Why

[GitHub issue #72](https://github.com/mandrecode/tempo/issues/72) reports that Android can delay overdue reminder alarms while extreme battery saver is enabled. If the user completes the overdue habit or task in the app before Android later delivers the alarm, Tempo must discard that stale delivery instead of showing a notification for work that is already complete.

## What Changes

- Suppress delayed habit reminder notifications when the scheduled occurrence date has already been completed in habit completion history.
- Suppress delayed task reminder notifications when the referenced task has already been completed or archived by the time the alarm is delivered.
- Add targeted tests for delayed/battery-saver-style delivery paths across habit and task reminders.
- Non-goal: do not change reminder permission UX, notification channel behavior, or exact alarm policy prompts.
- Non-goal: do not change task periodic rollover semantics except where needed to avoid notifying completed occurrences.

## Capabilities

### New Capabilities
- `delayed-completed-reminder-suppression`: Defines how reminder delivery discards stale habit and task alarms for occurrences completed before Android delivers the alarm.

### Modified Capabilities
- `task-reminder-rollover`: Clarify that overdue periodic task rollover must not display a notification once the original occurrence is already completed.

## Impact

- Affected infrastructure: reminder receivers, notification scheduling/delivery helpers, and delayed alarm extras.
- Affected domain/data seams: task and habit repository reads used by reminder delivery.
- Affected tests: unit coverage for habit and task reminder receivers/schedulers, plus any rollover tests needed to lock the completed-overdue case.
- No new dependencies, schema changes, or UI changes are expected.
