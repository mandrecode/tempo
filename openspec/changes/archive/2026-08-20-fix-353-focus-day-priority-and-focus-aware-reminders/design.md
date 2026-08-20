## Context

Focus shipped in 1.11.0 as a third tab showing today. `GetFocusAgendaUseCase` assembles the day from
the task, habit, chain and category repositories plus the focus session record, and hands the screen
three things: an ordered `overdue` list, an ordered `today` list, and an `upNext` shortlist drawn from
both. `FocusContent` renders them. `RecordDailyActivityUseCase` independently recomputes the same
day's scheduled/completed counts for the hero and the history heatmap, and its membership rule is a
hand-copied twin of the agenda's.

Two constraints shape the reminder half of this change. First, `FocusSessionRepositoryImpl` is backed
by `SharedPreferences` rather than Room precisely so a broadcast receiver can read it on a cold start
without opening the encrypted database — that property must survive. Second, its in-memory
`focusToday` `StateFlow` is seeded once at construction against the date at that moment; a process
alive across midnight keeps yesterday's map until the next write. Today nothing reads it in a way
that cares. A reminder gate would care very much: a stale map would silence a real reminder.

## Goals / Non-Goals

**Goals:**

- One membership rule for the day, expressed once, that both the agenda and the day's counts use.
- Today ahead of overdue in both places the user reads the day: the shortlist and the section order.
- A date-correct answer to "has this task had focus time today?" that a `BroadcastReceiver` can get
  on a cold start without touching Room.
- Keep every non-notification effect of a reminder firing intact.

**Non-Goals:**

- Re-ranking items within a section, or introducing priority into the sort (the spec's original
  "priority first" claim is already not what the code does; that discrepancy is left alone).
- Making Focus a task browser: undated work stays out of the agenda and stays a footer count.
- Persisting focus history per task. `focusToday` remains a one-day record that resets with the date.
- Any change to habit or chain reminders.

## Decisions

### One `isOnTheDay` predicate, shared by the agenda and the counts

`GetFocusAgendaUseCase` and `RecordDailyActivityUseCase` already encode the same rule ("due today, or
overdue and still open") in two places, in slightly different shapes. #359 adds a second clause to it
("…and, for a subtask, its parent is not itself on the day"), which is exactly the kind of rule that
drifts when it lives twice.

The predicate moves into a single internal helper both use cases call, taking the task, the day, and
a lookup of tasks by id (to reach the parent). Put in `features/focus/domain/model/` beside
`FocusAgendaItem` as a small pure-Kotlin file rather than in either use case, so neither owns it.

*Alternative considered:* a third use case injected into both. Rejected — it holds no dependencies and
does no I/O, so a use case would be ceremony around a function, and Hilt wiring for something with no
collaborators.

*Alternative considered:* leaving the counts alone. Rejected — the hero would then say "3 scheduled"
over a list of five rows the moment a subtask is promoted, which is the bug fix creating a new one.

### Membership for a subtask is decided against its parent, not against its parent's date

The rule is "the parent is not itself in the agenda", not "the parent has no reminder". These differ
for a parent dated in the future or a completed overdue parent — in both cases the parent is absent
from the day, so its dated subtask should be able to stand on its own. Writing the rule as recursion
on the same predicate makes that fall out for free and keeps a single sentence describing membership.

Parent lookup is by id against the same task list the use case already has in hand, so no new query.
Recursion depth is bounded in practice by the task tree, and the predicate only ever looks one level
up per call, so a deep chain of dated ancestors resolves without a cycle guard as long as parent ids
form a tree — which `BackupPayloadValidator` and the Room foreign key already guarantee.

### Up next takes today first, by ordering its input

`GetUpNextItemUseCase` is deliberately sort-free: it takes the agenda already ordered and only picks.
That property is worth keeping — it is why the row and the list can never disagree. So the fix is at
the call site: `getUpNextItem(todayItems + overdue)` instead of `getUpNextItem(overdue + todayItems)`.
The section order in `FocusContent` flips to match, and the two changes are the same decision stated
in the two places it shows.

*Alternative considered:* teaching `GetUpNextItemUseCase` a comparator that ranks by "is it today".
Rejected — it would have to re-derive the day boundary the caller already knows, and the moment it
sorts, the row can drift from the list again.

### Subtask order comes from the same comparator the Tasks tab uses

`compareBy<Task> { it.sortOrder }.thenBy { it.id }` — copied from `TasksViewModelDataLoading`, not
invented. Applied where the agenda builds `subtasksByParent`, so every consumer of a `TaskEntry`
(agenda card, running session card, session screen) gets the fixed order without each having to sort.

*Alternative considered:* changing `TaskDao.getAllTasks()`'s `ORDER BY id DESC`. Rejected — that
query feeds many screens and widgets, and reordering it to fix Focus is a change with a blast radius
nobody asked for.

### A date-checked, per-task read on the repository, not the `StateFlow`

`FocusSessionRepository` gains `fun focusOn(taskId: Long, date: LocalDate): TaskFocusToday`. The
implementation reads `SharedPreferences` directly and returns `TaskFocusToday()` when the stored day
is not `date` — so the answer is correct regardless of how long the process has been alive, and it
still never opens the database. The existing `focusToday` `StateFlow` stays exactly as it is for the
agenda's `combine`.

*Alternative considered:* making `focusToday` itself date-aware by re-reading on access. Rejected —
it is a `StateFlow` with live collectors; a value that changes under them without an emission is
worse than the stale value it replaces.

### The question is asked by a use case, the answer is used by infrastructure

A new `HasFocusTimeTodayUseCase` in `features/focus/domain/usecase/` holds the whole rule: the day's
record for the task, plus a running session (or break) on it. It takes the `Clock` and resolves
"today" itself, because both callers are background entry points with no notion of a selected day.
`TaskReminderReceiver` and `MissedReminderCatchUpRunner` inject it and gate only the `notify` call.

A running session is checked separately from the stored record because minutes are only written when
a session ends — a reminder firing five minutes into a session would otherwise sail through.

### The gate wraps `notify`, not the receiver's early return

`TaskReminderReceiver` currently guards notification and periodic rollover with one condition. Moving
the new check into that condition would make focusing on a periodic task stop it rolling over, which
is a data bug wearing a notification bug's clothes. The existing `shouldProcessTaskReminder` gate
stays as the "is this task still real and open" check, and the focus check becomes a second, narrower
gate around `notify` alone. Same shape in the catch-up runner: `filterNot` before the `forEach`, with
the `finally` re-arm untouched.

## Risks / Trade-offs

- **A user who focuses on a task early and genuinely wants the later reminder loses it.** → Accepted,
  and it is what #357 asks for. The suppression is one day wide and one task narrow; the task stays
  in Focus, in Tasks, and in tomorrow's reminder if it is still open.
- **`focusOn` reads `SharedPreferences` on the main thread of a `BroadcastReceiver`.** → The receiver
  already does its work inside `goAsync()` on the IO dispatcher, and the prefs file is a handful of
  short strings that Android has already loaded into memory for the singleton.
- **Promoting subtasks changes what the heatmap counted for past days.** → It does not: past days
  keep whatever was last written while they were current, by `RecordDailyActivityUseCase`'s existing
  design. Only today's row is recomputed.
- **A user with many dated subtasks under undated parents sees a longer Focus list than before.** →
  Intended by #359, and the Up next row is still capped at five cards. The parent-not-on-the-day
  clause is what stops a parent and its steps arriving together.
- **The `feat-42-focus-mode` spec says the agenda is ordered "Up next, Overdue, Today" and that Up
  next holds a single ranked item.** → That change is still unarchived and already superseded on both
  counts by what shipped. This change states the ordering requirement in full in its own capability;
  reconciling the older change's text is left to whoever archives it.
