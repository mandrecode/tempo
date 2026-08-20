## Why

Four defects reported against the Focus tab and task reminders
([#353](https://github.com/mandrecode/tempo/issues/353),
[#354](https://github.com/mandrecode/tempo/issues/354),
[#357](https://github.com/mandrecode/tempo/issues/357),
[#359](https://github.com/mandrecode/tempo/issues/359)). Three of them share a root cause: Focus was
built to show the day, but it keeps putting things in front of the user that are not the day —
yesterday's leftovers ahead of today's work, subtasks in an order nobody chose, and dated subtasks
nowhere at all. The fourth is Tempo talking over itself: a reminder notification for a task the user
is sitting in a focus session on.

## What Changes

- **#353 — Today outranks overdue, in the row and in the list.** Up next draws from today's work
  first and only falls back to overdue when today runs out; the agenda renders the Today section
  above the Overdue section. Today Focus does the opposite in both places: `GetFocusAgendaUseCase`
  hands `overdue + today` to `GetUpNextItemUseCase`, so a task from last week fills the shortlist
  ahead of the 09:00 meeting, and `FocusContent` emits the Overdue section first, so the day you are
  meant to be working sits below the day you already missed.
- **#354 — Subtasks read in the order they were given.** The Focus agenda lists a task's subtasks by
  `sortOrder` then `id`, the same comparator the Tasks tab already uses. Focus builds its subtask map
  by grouping `getAllTasks()`, whose only ordering is `ORDER BY id DESC`, so the same task shows its
  steps reversed in Focus and in order in Tasks.
- **#357 — No reminder for work already under way.** A task reminder is not posted when that task has
  had a focus session today (started, finished, or currently running). The reported case is a session
  ending at 09:01 and the task's own reminder firing in the same notification shade. The suppression
  covers both notification paths: the reminder alarm and the daily missed-reminder catch-up sweep.
  Periodic rollover is unaffected — suppressing a notification must not suppress the bookkeeping that
  rides with it.
- **#359 — A dated subtask can be focused on.** A subtask with a due date becomes its own agenda row,
  and therefore an Up next candidate a session can be started on, when its parent task is not itself
  in the agenda. When the parent is on the day, its subtasks stay nested inside its card exactly as
  now, so a parent and its steps never crowd the Up next row together. The day's scheduled/completed
  counts follow the same membership rule, so the hero and the heatmap keep agreeing with the list.

Non-goals:

- No change to how items are ordered *within* a section — timed before untimed, clock order,
  completed last stays as it is. #353 is about which section comes first, not about resorting one.
- No change to the Overdue section's contents, its header, or its count. Overdue work is demoted, not
  hidden.
- No change to what a task reminder says, when it is scheduled, or to habit and chain reminders. #357
  only adds a reason not to post one.
- No parent breadcrumb, indentation, or new pill on a promoted subtask row — it renders as the task
  card it already is.
- No change to the undated footer's count, which stays a count of undated *top-level* tasks.
- `WhatsNewRegistry` is untouched: this is a fix release against the Focus feature already announced,
  not a new feature.

## Capabilities

### New Capabilities

- `focus-day-priority`: how the Focus agenda ranks today's work against overdue work — in the Up next
  shortlist and in the order the sections are read down — and the order a task's subtasks appear in.
- `focus-subtask-membership`: when a subtask stands as its own row on the day rather than as a step
  inside its parent, and how the day's counts follow that.
- `focus-aware-task-reminders`: when a task reminder notification is withheld because the task has
  already had focus time today.

### Modified Capabilities

<!-- None: no spec in openspec/specs/ covers Focus or task reminder notification suppression.
     The Focus capabilities from feat-42-focus-mode and fix-343-focus-and-bar-polish are not yet
     archived, so this change states its requirements in full rather than as deltas against them. -->

## Impact

- `features/focus/domain/usecase/GetFocusAgendaUseCase.kt` — Up next draws today first; subtasks are
  sorted; dated subtasks join the agenda when their parent does not.
- `features/focus/domain/usecase/RecordDailyActivityUseCase.kt` — the day's counts mirror the new
  membership rule.
- `features/focus/presentation/FocusContent.kt` — the Today section is emitted above Overdue.
- `features/focus/domain/repository/FocusSessionRepository.kt`,
  `features/focus/data/FocusSessionRepositoryImpl.kt` — a date-checked, single-task read of today's
  focus record, so a broadcast receiver can ask the question on a cold start without inheriting a
  stale in-memory day.
- `features/focus/domain/usecase/` — a new use case answering "has this task had focus time today?",
  shared by both notification paths.
- `infrastructure/reminders/receivers/TaskReminderReceiver.kt`,
  `infrastructure/reminders/MissedReminderCatchUpRunner.kt` — the notify call is gated; rollover and
  the catch-up re-arm are not.
- Unit tests: `GetFocusAgendaUseCaseTest`, `GetUpNextItemUseCaseTest`, `RecordDailyActivityUseCaseTest`,
  `FocusSessionRepositoryImplTest`, `TaskReminderReceiverTest`, `MissedReminderCatchUpRunnerTest`, and
  a test for the new use case. Instrumented: `FocusContentTest` for section order.
- No Room entity, schema, migration, or string-resource changes. No new dependencies.
