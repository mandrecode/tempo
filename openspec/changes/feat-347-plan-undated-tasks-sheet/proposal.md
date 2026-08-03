## Why

Focus ends its day with a footer reading "N tasks without a date", and tapping it throws the user
out of Focus into the Tasks tab — where the undated work is scattered through a list that is sorted
and grouped for something else entirely. The one moment the app knows the user is willing to plan,
it answers by losing their place and making them find the tasks again.

Closes [#347](https://github.com/sherrerapiqueras/tempo/issues/347).

## What Changes

- The Focus footer and empty-state link **stop navigating to the Tasks tab**. They open a
  **"Plan your tasks"** modal sheet over Focus instead.
- The sheet lists every undated, incomplete, top-level task using the **existing `TaskItem`
  component** — not a lookalike — so a task reads the same here as it does in Tasks.
- `TaskItem` gains an **opt-in category badge** (icon + name) that leads its metadata row. Only the
  plan sheet turns it on; the Tasks list, grouped by category already, is unchanged.
- Each unplanned row carries **quick-plan chips — Today · Tomorrow · Pick a date**. One tap sets the
  task's reminder; there is no per-task save step.
- Planning a task moves it, animated, from an **"Unplanned"** section to a **"Planned"** one, under
  headers in the Tasks list's own style (`WavyDivider` + `groupLabel`). The headers appear only once
  the split is real — a sheet where nothing is planned yet is one plain list.
- The sheet footer carries two end-aligned actions: **Close** always, and **Done** enabled once at
  least one task has been planned. Done closes the sheet and raises a **snackbar offering Undo for
  the whole batch**, restoring every reminder the sheet set.
- Setting a reminder from the sheet goes through the **same notification/exact-alarm permission
  check** the task editor uses, so the sheet cannot promise a reminder the system will not deliver.
- Tapping a card body still opens the **full task editor** stacked over the sheet, for anything the
  chips do not cover (a specific time, priority, recurrence).

## Capabilities

### New Capabilities
- `undated-task-planning`: opening, populating, planning within, and closing the Focus plan sheet —
  including the planned/unplanned split, permission gating, batch undo, and adaptive placement.

### Modified Capabilities
<!-- None. No existing spec in openspec/specs/ states requirements about the Focus undated footer,
     TaskItem's metadata row, or task reminder assignment from Focus. -->

## Impact

- **UI (Focus)**: `FocusContent.kt` (footer + empty state), `FocusContract.kt` (new state, events;
  `UiEffect.OpenTasksTab` removed), `FocusViewModel.kt`, `FocusScreen.kt` (gains a snackbar host it
  does not have today, plus sheet hosting), new `components/PlanTasksSheet.kt`.
- **UI (Tasks, shared)**: `cards/TaskCard.kt` / `cards/TaskCardMetadata.kt` gain the opt-in category
  badge; callers keep today's behaviour by default.
- **Domain**: a new use case supplying the undated task list with its categories, and a pure
  date/time helper choosing the reminder instant a quick chip implies. No model or schema changes —
  planning only writes `Task.reminderDate`, which already exists.
- **Reminders**: every plan and every undo goes through `UpdateTaskUseCase`, so `TaskReminderScheduler`
  stays the single path for scheduling and cancelling alarms. No new scheduling code.
- **Navigation**: Focus loses its only hand-off to another tab; `onNavigateToTasks` becomes unused
  at the Focus call site.
- **Strings**: new entries in `values/strings.xml` and `values-es/strings.xml`.
- **Tests**: unit tests for the new use case, helper and view-model logic; Compose tests for the
  sheet at 360dp and at expanded width.
- **What's New**: `WhatsNewRegistry.latest` replaced with a `347` entry.
- **No data-layer change**: no new entity, DAO, migration or Room schema.
