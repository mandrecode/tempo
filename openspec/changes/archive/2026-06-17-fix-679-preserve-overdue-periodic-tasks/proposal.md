## Why

GitHub issue [#679](https://github.com/mandrecode/tempo/issues/679) reports that overdue periodic task reminders are advanced in-place when WorkManager or the reminder receiver reschedules them. This loses the overdue occurrence even though the task was not completed, so users can miss unfinished periodic work.

## What Changes

- Preserve an incomplete overdue periodic task when its reminder fires or reminders are resynced.
- Create and schedule a separate next-occurrence task instead of mutating the overdue task's `reminderDate`.
- Link the overdue task to the spawned next occurrence so repeated receiver/worker runs are idempotent.
- Ensure only the latest spawned occurrence owns the future reminder alarm.

Non-goals:

- Do not change habit or habit-chain reminder rollover behavior.
- Do not redesign task recurrence editing, deletion, or rollback UI.
- Do not change task completion behavior except where required to avoid duplicate periodic instances.

## Capabilities

### New Capabilities

- `task-reminder-rollover`: Defines how overdue periodic task reminders create and schedule next task occurrences without losing unfinished work.

### Modified Capabilities

- None. No existing OpenSpec specs are present.

## Impact

- Domain task recurrence/reminder use case behavior.
- Task repository transaction and `nextInstanceId` usage.
- `TaskReminderReceiver` and `RescheduleRemindersWorker` scheduling paths.
- Unit tests for task reminder rollover and periodic completion interactions.
