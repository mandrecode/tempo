## Context

Android may defer `AlarmManager` delivery while extreme battery saver is active. Tempo's reminder receivers therefore cannot assume the alarm represents current app state: by the time a delayed alarm fires, the user may have already completed the habit occurrence or task inside the app.

Habit delivery already carries `EXTRA_SCHEDULED_DATE` and checks the fired occurrence date against `completionHistory`. Task delivery re-reads the task by id and only displays a notification when the current task is incomplete. Periodic task rollover also re-reads inside a Room transaction before creating or reusing the next occurrence.

## Goals / Non-Goals

**Goals:**
- Keep notification display decisions based on current persisted state at receiver time.
- Ensure stale habit alarms use the scheduled occurrence date, not the habit's advanced reminder date, when checking completion history.
- Ensure stale task alarms for completed or deleted tasks do not display notifications and do not roll over periodic occurrences.
- Preserve existing idempotency for overdue periodic rollover and future reminder scheduling.

**Non-Goals:**
- Do not introduce a new persisted notification delivery table or schema migration.
- Do not change notification permission, channel, exact-alarm, or battery optimization UX.
- Do not change task completion or habit completion user flows outside reminder delivery.
- Do not add a dedicated Settings/domain exception or UI work.

## Decisions

- Treat reminder receivers as the stale-alarm boundary.
  - Rationale: the receiver is the first point that can compare a delayed Android delivery with current repository state.
  - Alternative considered: cancel all matching alarms during in-app completion. Cancellation remains useful but is insufficient because Android may already have an undelivered broadcast queued.

- Use occurrence-specific data for habits.
  - Rationale: `Habit.isCompleted` is a legacy/current-day flag, while `completionHistory` is date-specific and can distinguish an overdue occurrence from the advanced next reminder.
  - Alternative considered: suppress every habit notification when `isCompleted` is true. That would reintroduce missed future reminders after the habit advances.

- Use current task completeness for tasks.
  - Rationale: task completion archives the current occurrence in place, so `getTaskById(id)?.isCompleted` is the delivery-time source of truth for whether the fired task should notify.
  - Alternative considered: embed scheduled task metadata in the alarm extras. That is unnecessary for suppression because the task id remains sufficient to re-read completion/deletion state.

- Keep transaction boundaries inside rollover use cases only.
  - Rationale: Room mutations for periodic rollover must stay atomic, while Android scheduler and notification side effects should run after the transaction to avoid holding database locks.
  - Alternative considered: show notifications from inside the rollover transaction. That would mix external side effects with persistence and make rollback/retry behavior harder to reason about.

## Risks / Trade-offs

- Habit alarms scheduled before `EXTRA_SCHEDULED_DATE` existed may fall back to the current stored reminder date -> Existing fallback keeps delivery compatible; targeted tests should cover the new scheduled-date path.
- A completed task notification may already be visible before in-app completion -> Completion/update flows should continue dismissing existing notifications; this change focuses on delayed alarms that have not displayed yet.
- Rollover could create a next periodic occurrence from stale input -> The use case re-reads the current task inside the transaction and returns not applicable if it became completed or otherwise ineligible.
