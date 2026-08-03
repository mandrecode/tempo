## Context

Focus's agenda ends with `UndatedTasksFooter` (`FocusContent.kt:459`), a `TempoLinkButton` whose
only job is to emit `FocusContract.UiEffect.OpenTasksTab`. That effect is Focus's single hand-off to
another tab, and `FocusScreen`'s `onNavigateToTasks` exists solely to serve it. The count behind the
label is computed in `GetFocusAgendaUseCase` as
`tasks.count { it.parentTaskId == null && it.reminderDate == null && !it.isCompleted }` — the
predicate the sheet must now list rather than merely count.

Relevant existing machinery this design leans on rather than reinvents:

- `TaskItem` (`components/cards/TaskCard.kt`) — the task card, with `MetadataRow` inside it.
- `ActiveGroupHeader` (`TasksContent.kt:652`) — `WavyDivider` + `groupLabel`, the group-header look
  the issue asks for.
- `TempoModalBottomSheet(adaptivePlacement = true)` — bottom sheet under 1200dp, `TempoDockedSheet`
  at or above it; `SHEET_MAX_WIDTH` is 640dp, `DockedEditorWidth` is 412dp.
- `FocusTaskEditor` (`FocusEditors.kt`) — Focus already hosts the Tasks editor over itself using a
  second `TasksViewModel` instance, with `SheetPlacement.BottomSheet` forced.
- `UpdateTaskUseCase` — the only sanctioned way to change a task, because it owns the
  `TaskReminderScheduler` schedule/cancel/dismiss side effects.
- `HandleReminderPermissions` — the notification/exact-alarm education flow the task editor runs
  before opening its date picker.
- `ExpressiveSnackbarHost` — used by Tasks and Routines; Focus has none yet.

## Goals / Non-Goals

**Goals:**

- Plan several undated tasks without leaving Focus, at one tap per task.
- Reuse `TaskItem` itself, not a lookalike, so a task reads identically in both places.
- Keep every reminder write on the existing `UpdateTaskUseCase` → `TaskReminderScheduler` path.
- State the behaviour at every window size, and derive sizes from available width.

**Non-Goals:**

- Choosing a *time* from the chips. The sheet plans *which day*; the full editor remains one tap
  away for a precise time, priority or recurrence.
- Planning habits or chains. Only tasks have `reminderDate` in the sense the footer counts.
- Multi-select or "plan all of these for tomorrow". A per-row chip is enough for the counts this
  footer realistically shows; a bulk mode can follow if it proves needed.
- Any change to the Tasks list's own presentation, sorting or grouping.
- Any data-layer change: no entity, DAO, migration or Room schema is touched.

## Decisions

### D1 — The sheet lives in Focus and owns a snapshot of task ids

`FocusContract.UiState` gains `planSheet: PlanSheetState?`. When it is non-null the sheet is open.

The state holds `originalReminders: ImmutableMap<Long, LocalDateTime?>` — captured from the tasks
that matched the undated predicate **when the sheet opened**, and doubling as the id snapshot — plus
`rows: ImmutableList<UndatedTask>`, which is re-emitted live by
`GetUndatedTasksUseCase.pinnedTo(ids)` on every write.

Alternative rejected: patching the row list in the view model on each plan. The repository is what
knows a task now has a date; hand-patching a local copy would make the sheet a second source of
truth for the same task.

Why a snapshot of ids at all: planning a task removes it from the undated set, so a sheet driven by
a live "undated" query would make every row vanish the instant it was planned. The issue's
planned/unplanned split only exists because the sheet remembers what it opened with.

### D2 — Quick chips over a per-row date button or the full editor

The chips are `Today · Tomorrow · Pick a date`, in a `FlowRow` so they reflow instead of needing a
size-class branch. Chosen over a trailing calendar `IconButton` (three taps and a dialog per task)
and over opening the full editor per task (a form per task, in a sheet meant to avoid exactly that).
The editor stays reachable by tapping the card body, so nothing is lost — only the common case is
made cheap.

### D3 — The chips render in a new footer slot on `TaskItem`

`TaskItem` gains two optional parameters, both defaulting to today's behaviour:

- `category: Category?` — the domain model itself, not a mirrored UI type. `TaskItem` already takes
  a `Task`, so a second domain model is consistent, and a `CategoryBadge` DTO would only restate
  `Category`'s three relevant fields. The badge resolves the icon (`TempoIcon.fromName`) and colour
  (`resolveColor`) itself, so call sites pass what they already have.
- `footer: (@Composable () -> Unit)?` — rendered inside the card, below the content row.
- `showAddSubtaskAction: Boolean = true` — off in the sheet. Reusing `TaskItem` means inheriting its
  trailing actions, and "add a step" beside a row whose whole purpose is answering *which day?* is a
  second job offered in the middle of the first. The expand control stays, because it reveals what
  the task already is rather than starting something new, and the full editor is a tap on the card
  away for anyone who does want to break the task down.

Alternative rejected: composing the chips *outside* the card, in the sheet's own `Column`. The chips
would then float between cards, belonging to neither, and the tap target for "this task's date"
would sit outside the surface representing that task.

Both parameters are opt-in with `null` defaults, so `TasksContent`, `FocusContent` and every
existing test call site are untouched — an important property given how widely `TaskItem` is used.

### D4 — All writes go through `UpdateTaskUseCase`; nothing is batched or deferred

Each chip tap is one `UpdateTaskUseCase(task.copy(reminderDate = ...))` call. That use case already
trims, validates, runs `TaskReminderDateUtil.advanceReminderIfNeeded`, persists, then schedules or
cancels the alarm — including `dismissNotification` and the defensive `cancel` when `schedule()`
skips. Duplicating any of that here would be a second reminder path to keep in step.

**Transaction boundary:** each plan is one `taskRepository.updateTask` call — a single-row write,
already atomic in Room. There is no multi-row transaction, and deliberately so: an undo that has to
roll back N rows atomically would need a repository-level batch API that does not exist, and a
partial failure mid-batch is better left as "some tasks planned" than as an all-or-nothing rollback
the user cannot see.

**Android scheduler side effects sit outside that boundary.** `TaskReminderScheduler.schedule()` and
`cancel()` are `AlarmManager` calls made *after* the row is committed, and they can fail
independently (revoked exact-alarm permission, for instance). The design accepts this: the database
stays the source of truth, and the existing `ScheduleResult` reporting already tells the caller when
scheduling was skipped.

**Idempotency:** planning is idempotent per task — the same chip tapped twice writes the same
`reminderDate` and reschedules the same alarm, because `schedule()` replaces by task id rather than
accumulating. Undo is idempotent for the same reason: restoring `null` cancels an already-cancelled
alarm harmlessly.

**Recurrence invariant:** the sheet only ever writes `reminderDate`. It never sets, clears or
advances `periodicity`, `repeatDays`, `monthDayOption` or `nextInstanceId`. An undated *periodic*
task planned from the sheet therefore gains a first occurrence and keeps whatever recurrence it
already had, which is exactly what setting a date in the editor does.

### D5 — The default planning time, and why it is a pure domain helper

Chips carry a day, not a time, so a time must be chosen. `PlanReminderTimeUtil` (pure Kotlin,
`kotlinx-datetime`, in `features/tasks/domain/util/`) resolves it:

- Preferred: the chosen day at `PLAN_DEFAULT_REMINDER_HOUR` (09:00).
- If that instant has already passed (only possible when the chosen day is today), the next whole
  hour after now, capped at 23:59 on that day.

This keeps the sheet from handing `UpdateTaskUseCase` a reminder in the past, which
`isPastReminderWithoutPeriodicity` would otherwise flag and `schedule()` would skip — a chip that
silently produces no reminder. It is a pure function of `(chosenDate, now)`, so it is unit-testable
without a clock abstraction or a robolectric shadow.

Alternative rejected: reusing the editor's date→time dialog pair for the chips. That is three
dialogs' worth of interaction for "tomorrow", and the sheet exists to avoid it.

### D6 — Permission gating reuses the editor's flow, checked once per sheet

The first chip tap in a sheet session runs `HandleReminderPermissions`; the pending chip is held and
applied on grant, dropped on dismiss. Subsequent taps in the same session skip it — the permission
state cannot change under us while the sheet is open, and asking per row would make the sheet
unusable.

### D7 — One button, and every exit raises the same undo

The footer is a single **Done**, always enabled, with a leading check. A Cancel beside it would be
lying: nothing is staged, because every chip has already written, so it could only do what Done
does. Two buttons where one cannot do what its name says is worse than one button — and gating the
lone way out on having planned something would leave a state whose only exit is greyed out.

Every dismissal therefore funnels to one `UiEvent.ClosePlanSheet`: the button, the drag handle,
predictive back, the scrim and Escape. Each captures `changedTaskIds`, closes, and emits
`UiEffect.PlanBatchConfirmed(count)` when that is non-empty. An earlier draft raised the undo only
from the button, which made the quickest way out the one that quietly withheld the way back.

Chips toggle for the same reason the undo exists: pressing the lit one clears the date via
`UiEvent.UnplanTask`. Planning and then unplanning leaves the task at its original reminder, so
`changedTaskIds` is empty and closing says nothing — the sheet does not offer to undo a round trip.

**What counts as changed is asked-for, not only seen.** `PlanSheetState.writes` records the value a
chip asked for the instant it is pressed, and `changedTaskIds` takes either evidence: a row that has
moved (which covers edits made in the full editor behind the sheet's back) or a recorded write that
differs from the original (which covers a tap and a dismissal in the same breath, before the
repository flow has answered). `writes` holds the latest value asked for rather than a history, so
planning and then unplanning lands back on the original and drops out of the batch.

**The batch travels with the offer.** `PlanBatchConfirmed` carries the map itself and `UndoPlanBatch`
takes it back, rather than the view model holding "the last batch" in a field. A snackbar sits on
screen for seconds, and in those seconds another sheet can be opened and closed — against a shared
field that second session would either clear the offer (closing with no changes) or overwrite it
(closing with different ones), leaving the still-visible undo to do nothing or to restore somebody
else's work. Carrying the payload also makes undo idempotent rather than consume-once, which is
free: `RestoreTaskRemindersUseCase` skips a task whose reminder already matches.

Undo replays `originalReminders` for exactly the ids the sheet changed, through a new
`RestoreTaskRemindersUseCase` — which re-reads each task before writing, so a title or priority
edited between the planning and the undo survives it, and a deleted task is skipped rather than
resurrected. The view model does not touch `TaskRepository` directly; that use case does, and
delegates the write to `UpdateTaskUseCase` so the scheduler path stays single.
Because the map is captured at sheet-open time, a task that was re-edited in the full editor mid-
session still returns to the reminder it had when the sheet opened — undo means "put the sheet
back", not "undo the last write".

`FocusScreen` gains a `SnackbarHostState` + `ExpressiveSnackbarHost` in its `Scaffold`, matching
`TasksScreen` and `RoutinesScreen`.

`hasUnsavedChanges` on the sheet stays `false`: nothing is unsaved, so the discard-changes dialog
would be a lie.

### D8 — Adaptive behaviour, stated at every size

- **Placement:** `TempoModalBottomSheet(adaptivePlacement = true)`. Bottom sheet below 1200dp
  (capped at `SHEET_MAX_WIDTH` = 640dp), `TempoDockedSheet` at 1200dp and above. This is the app's
  existing rule; the plan sheet does not invent a second one. Unlike `FocusTaskEditor`, which forces
  `BottomSheet` because Focus has no list-detail layout for a pane to dock beside, the plan sheet
  *is* a supporting view onto the Focus agenda, so docking it is coherent.
- **Columns:** `LazyVerticalGrid(columns = GridCells.Adaptive(minSize = PlanRowMinWidth))` with
  `PlanRowMinWidth = 328.dp` — the width a task card actually gets in the Tasks list on a 360dp
  phone. Section headers span `maxLineSpan`. At today's sheet widths this resolves to one column
  everywhere; it is written as a derivation rather than a hardcoded `1` so it stays correct if the
  sheet's max width ever changes, per the "a fixed item count is a red flag" rule in `UI_UX.md`.
- **Chips:** `FlowRow`, so they wrap at 360dp and sit on one line above it, with no breakpoint.
- **Pointer and keyboard:** Escape dismisses the sheet (`onPreviewKeyEvent` on the sheet content —
  the app has no Escape handling anywhere today, so this is added locally rather than retrofitted
  into `TempoModalSheet` for every sheet in the app). Chips are `ExpressiveChip`-derived and
  therefore already carry hover and focus indication from the shared component.
- **Height:** the sheet inherits `TempoModalSheetState.maxSheetHeight`; the grid scrolls inside it.
  Drag-to-dismiss is bound to the handle only (`pointerInputForSheetDrag` is applied to
  `TempoModalSheetDragHandle`), so an inner scrollable does not fight it.

### D9 — Sections are computed in the view model, not the composable

`PlanSheetState` exposes `plannedIds` / `unplannedIds` derived from the live tasks. `UI_UX.md` bars
business logic from composables, and the split is also the thing the requirement is written against,
so it belongs somewhere unit-testable.

Headers appear as soon as `planned` is non-empty. That one rule covers all three cases the spec
asks for: nothing planned is one plain list, some planned shows both headers, everything planned
shows only "Planned" — because the "Unplanned" header is additionally gated on that section having
rows.

### D10 — Two files apiece, for detekt's ceilings rather than for their own sake

`FocusViewModel` was already at detekt's 11-function limit, and the sheet's five handlers would have
broken it. They live in `FocusViewModelPlanSheet.kt` as extension functions on the view model, which
is exactly how `TasksViewModel` is already split across `TasksViewModelTaskActions.kt` and friends —
the members they need become `internal` rather than `private`.

`PlanTaskRow` and its chips are likewise split out of `PlanTasksSheet.kt`, but for a second reason
that matters more: the sheet is a `Dialog`, so a width constraint wrapped around it in a test does
nothing. The row is where the narrow-window risk actually lives, and as its own composable it can
be measured at 328dp — the narrowest column the grid will ever hand it.

## Risks / Trade-offs

- **Alarm scheduling can fail after the row is committed** (revoked exact-alarm permission,
  OEM restrictions) → the write is still correct and the existing `ScheduleResult` path reports the
  skip; D6's permission gate makes the common cause unlikely, and the task keeps its date either way.
- **`TaskItem` is used in many places and now takes two more parameters** → both default to `null`
  and reproduce today's rendering exactly; existing call sites and `androidTest`s compile and behave
  unchanged. Verified by running the existing Tasks and Focus Compose tests untouched.
- **The category badge widens the metadata row** → this has broken constrained-width instrumented
  tests before in this repo. The badge is opt-in and only the plan sheet sets it, but the sheet's own
  Compose test runs at 360dp specifically to catch it.
- **Undo restores a reminder that may itself now be in the past** → `UpdateTaskUseCase` already
  handles past reminders via `advanceReminderIfNeeded` / `isPastReminderWithoutPeriodicity`; undo
  inherits that behaviour rather than adding a second rule.
- **A second `TasksViewModel` instance for the sheet's own writes** → the sheet reuses the Focus view
  model for planning and only borrows `FocusTaskEditor`'s existing instance for full edits, so no
  third instance appears.
- **Losing Focus's only tab hand-off** → `onNavigateToTasks` becomes unused at the Focus call site
  and `UiEffect.OpenTasksTab` is removed. Its `FocusContentTest` assertions change from "navigates"
  to "opens the sheet"; nothing else observes it.

## Open Questions

None blocking. Two deliberate calls worth revisiting after use:

- Whether "Pick a date" should also ask for a time. Currently it does not, keeping the sheet about
  days; the editor covers precise times.
- Whether 09:00 should become a user setting. The app has no default-task-reminder-time preference
  today, and adding one is a separate change.
