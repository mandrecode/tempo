## Why

Tracking [issue #346](https://github.com/mandrecode/tempo/issues/346).

Outside `Manual` sort, the Tasks list is fully machine-ordered: two tasks with the same
priority (or the same exact reminder datetime) land in an arbitrary, unstable order the user
cannot influence, and reordering them by hand requires switching the whole category to
`Manual`. There is also no secondary criterion today — sorting by priority ignores dates
entirely, so a task due in ten minutes can sit below one due next month.

## What Changes

- **Richer sort tie-breaking.** Each sort option gains a documented tie-break chain instead of
  falling back to arbitrary list order:
  - `Date` → reminder datetime (undated last), then priority, then manual order.
  - `Priority` → priority (unprioritised last), then reminder datetime (undated last), then manual order.
  - `Title` → title, then manual order.
  - `Manual` → manual order (unchanged).
- **Drag-and-drop inside a tie.** When two or more consecutive active tasks in a group are
  indistinguishable under every criterion the active sort applies (identical priority *and*
  identical reminder datetime, or identical title), they form a *reorderable run* and can be
  long-press dragged into any order within that run. The manual order they get is persisted and
  is what the tie-break chain reads back, so it survives sort switches and app restarts.
- **Drags stay inside the run.** A task can never be dragged out of its group or past a task the
  sort can actually distinguish it from — the sort criteria always win over manual order.
- **Manual sort is folded into the same mechanism.** `Manual` is simply the case where every
  active task is tied, so the list keeps its existing full-list drag-and-drop behaviour through
  the same code path.
- **Reordering preserves neighbouring tasks' manual order.** Persisting a reorder now permutes
  the dragged run's own `sortOrder` values rather than renumbering them consecutively, so
  reordering a subset (a tie run, or a subtask list) no longer collides with the `sortOrder` of
  tasks outside it and silently scrambles the `Manual` view.

### Non-Goals

- Completed tasks remain non-reorderable in every sort mode.
- No new drag affordance, drag handle, or accessibility reorder action — long-press drag stays
  the interaction, consistent with categories, subtasks, and today's `Manual` list.
- No change to how groups themselves are formed, labelled, or ordered.
- No new sort options.

## Capabilities

### New Capabilities

- `task-sort-tie-reordering`: how the Tasks list breaks ties between tasks under each sort
  option, which tasks may be manually reordered within a sorted group, and how that manual
  order is persisted.

### Modified Capabilities

<!-- None: no existing spec in openspec/specs/ states requirements about task sorting or reordering. -->

## Impact

- **Domain**: `ReorderTasksUseCase` changes its `sortOrder` assignment strategy (permutation of
  the passed list's own values instead of consecutive renumbering from the minimum).
- **Presentation**: `TasksViewModelDataLoading` gains the tie-break comparators and computes the
  reorderable runs; `TasksContract.UiState` exposes them; `TasksContent` replaces its
  `Manual`-only drag block with a single run-scoped drag path shared by all sort options.
- **Persistence / schema**: none — the existing `Task.sortOrder` column carries the manual order.
- **Tests**: `ReorderTasksUseCaseTest`, `TasksViewModelTest` (sorting + run computation),
  `TasksContentTest` (grouped drag rendering).
- **What's New**: `WhatsNewRegistry.latest` is replaced with the `346` entry, with its strings
  added to every `values-<locale>/strings.xml`.
