## Why

Habit reminders can disappear after a habit is completed because the scheduler uses the legacy `isCompleted` flag to skip scheduling future alarms. Issue [#95](https://github.com/mandrecode/tempo/issues/95) reports this in production: task reminders continue to fire, while habit reminders are frequently missed even though notification channels are enabled.

## What Changes

- Allow habit reminders to schedule whenever their `reminderDate` is in the future, even if the legacy `isCompleted` flag is true from an earlier completion.
- Suppress habit notification display using date-specific completion history for the reminder occurrence, not the legacy cross-day flag.
- Keep task reminder behavior unchanged.
- Add focused tests for completed habits with future reminders and already-completed reminder occurrences.

Non-goals:
- No changes to Android notification channel setup or user permission flows.
- No migration of the legacy `isCompleted` field.
- No redesign of habit recurrence or task recurrence semantics.

## Capabilities

### New Capabilities
- `habit-reminder-delivery`: Habit reminder alarms and notifications are delivered reliably across repeated habit completions.

### Modified Capabilities

## Impact

- Affected code: habit reminder scheduler, habit reminder receiver, and unit tests.
- Affected systems: AlarmManager scheduling and notification receiver display gating for habits.
- No database schema, dependency, permission, or API changes.
