## 1. Domain Rollover Behavior

- [x] 1.1 Add a domain use case or focused domain function that rolls an overdue incomplete periodic task into a linked next occurrence.
- [x] 1.2 Ensure the rollover path reuses an existing valid `nextInstanceId` instead of creating duplicate tasks.
- [x] 1.3 Ensure a stale `nextInstanceId` is cleared and replaced when the linked task no longer exists.
- [x] 1.4 Keep scheduler side effects outside repository transactions.

## 2. Reminder Integration

- [x] 2.1 Update `TaskReminderReceiver` to use rollover behavior for overdue periodic tasks instead of updating the original reminder date.
- [x] 2.2 Update `RescheduleRemindersWorker` to schedule linked next occurrences and avoid scheduling stale overdue originals.
- [x] 2.3 Preserve existing create/update and periodic completion behavior unless required for duplicate prevention.

## 3. Verification

- [x] 3.1 Add unit tests for new rollover, idempotency, and stale-link replacement behavior.
- [x] 3.2 Add regression coverage that periodic completion does not create duplicate next occurrences.
- [x] 3.3 Run `openspec validate fix-679-preserve-overdue-periodic-tasks`.
- [x] 3.4 Run `./gradlew testDebugUnitTest`.
