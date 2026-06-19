## Context

Task reminder rescheduling currently uses `TaskReminderDateUtil.advanceReminderIfNeeded()` in receiver and WorkManager paths. For incomplete periodic tasks whose reminder is overdue, that updates the existing task's `reminderDate` to a future occurrence. That is correct when preparing a newly created or edited recurring task for scheduling, but incorrect after an occurrence was already missed because the unfinished occurrence disappears.

Periodic completion already archives the completed task and inserts a next occurrence. The overdue-rollover path needs the same preservation principle but not the same completion semantics: the overdue task remains incomplete and must not auto-complete subtasks or strip recurrence fields.

## Goals / Non-Goals

**Goals:**

- Preserve the overdue incomplete task exactly as unfinished user work.
- Insert at most one next occurrence for each overdue periodic task.
- Schedule only the latest next occurrence after rollover.
- Make receiver and WorkManager processing safe to repeat.
- Keep Android scheduler side effects outside Room transactions.

**Non-Goals:**

- Do not change habit or habit-chain recurrence behavior.
- Do not add task-chain management UI.
- Do not change how periodic completion archives and rolls back completed occurrences beyond avoiding duplicated next occurrences.

## Decisions

1. Use `nextInstanceId` as the persisted idempotency link.

   The overdue task will point to the next occurrence it spawned. If processing sees an existing `nextInstanceId`, it loads that task and schedules it instead of inserting another copy. This reuses an existing persistence field and avoids introducing schema changes.

2. Keep recurrence metadata on the overdue task.

   Completion strips recurrence from archived completed tasks because the recurrence moved to the next instance. Overdue rollover leaves the task incomplete, so stripping recurrence would hide why the task exists and would be a separate behavior change. Scheduling ownership is determined by `nextInstanceId`, not by removing `periodicity`.

3. Perform database mutation transactionally, then schedule outside the transaction.

   The transaction inserts the next occurrence and sets `nextInstanceId` on the overdue task. After commit, the caller schedules the next occurrence and does not re-schedule the overdue task. This keeps database state atomic without holding a DB lock while calling Android alarm APIs, and preserves the just-shown overdue notification for user action.

4. Derive the next occurrence from the overdue task's existing recurrence rules.

   Use `TaskReminderDateUtil.calculateNextOccurrence()` from the overdue `reminderDate` and then advance if needed to ensure the spawned instance's reminder is future. This preserves daily/weekly/monthly/hourly/yearly behavior and handles long-overdue tasks.

## Risks / Trade-offs

- Reusing `nextInstanceId` now represents both completion-spawned and overdue-spawned next tasks -> Keep completion rollback guarded by `isCompleted && nextInstanceId != null`, and document scheduling ownership separately.
- A spawned next task could be manually deleted -> On reprocessing, clear the stale link and create a replacement next occurrence.
- Scheduler side effects can fail after the DB transaction commits -> Persisted `nextInstanceId` makes retry safe; later sync can schedule the existing next occurrence.
- Older overdue tasks will retain `periodicity` -> Scheduler paths must treat `nextInstanceId != null` as "does not own future scheduling" to avoid stale future reminders.
