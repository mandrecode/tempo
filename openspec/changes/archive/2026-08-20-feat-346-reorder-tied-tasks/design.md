## Context

`TasksViewModelDataLoading.loadData()` sorts a category's tasks with a single-criterion
comparator per `SortOption`, then buckets the active ones into `ActiveGroupKey` groups that
`TasksContent` renders under headers. Ties inside a bucket resolve to whatever order
`getAllTasks()` happened to emit — stable within a session, arbitrary across reinstalls, and not
influenceable by the user.

Drag-and-drop exists in three places already, all long-press based: category chips
(`CategoryChipRow`), subtasks (`TaskItem` → `ReorderSubtasks`), and the `Manual` active list
(`TasksContent`). The `Manual` list is a special-cased branch inside `TasksContent`: it renders
a flat `itemsIndexed` list with drag state (`draggedIndex`, `dragOffset`, `targetIndex`) held in
the composable, while every other sort option renders a header-and-groups branch with no drag
support at all. All three drag sites persist through `ReorderTasksUseCase`, which renumbers the
passed list consecutively from its minimum `sortOrder`.

## Goals / Non-Goals

**Goals:**

- Give every sort option a total, reproducible order whose last criterion is the user's manual order.
- Let the user hand-order tasks the active sort genuinely cannot distinguish, without leaving the sort.
- Keep the tie definition and the sort comparator derived from one source of truth, so they cannot drift.
- Fold the `Manual` drag path into the general mechanism rather than adding a second one.

**Non-Goals:**

- Changing how groups are formed, labelled, or ordered.
- A drag handle, reorder affordance, or accessibility reorder action.
- Reordering completed tasks or reordering across groups.
- Live reflow during the drag — the existing translate-the-dragged-item + fade-the-target
  treatment is reused as-is.

## Decisions

### Decision 1: Model each sort option as an ordered list of criteria, not a hand-written comparator

Each `SortOption` maps to a `TaskSortCriteria` describing the ordered criteria applied *before*
manual order. The comparator used for sorting and the equality test used to detect ties are both
derived from that one description:

- `BY_DATE` → `[reminderDate (nulls last), priority (nulls last)]`
- `BY_PRIORITY` → `[priority (nulls last), reminderDate (nulls last)]`
- `BY_TITLE` → `[title lowercased]`
- `MANUAL` → `[]`

Two tasks are *tied* exactly when every criterion compares equal — which for `MANUAL` (empty
criteria list) is vacuously true, so `MANUAL` needs no special case: its whole active list is
one tie. `sortOrder` then `id` are appended as the final criteria of every comparator, making
each order total.

*Alternative considered:* keep the hand-written `compareBy` chains and write a separate
`areTied(a, b)` predicate. Rejected — two places encoding the same rule is precisely the drift
this feature cannot afford: a comparator criterion missing from the predicate would let the user
drag a task past one the sort can distinguish, and the sort would silently undo the drag on the
next emission.

### Decision 2: Compute reorderable runs in the ViewModel, expose them keyed by task id

After the active groups are built, each group's task list is chunked into maximal runs of
mutually tied tasks; runs of two or more become reorderable. The result is exposed on
`UiState` as `reorderableRuns: ImmutableMap<Long, ReorderableRun>` (task id → the run it belongs
to, its key, and the task's index inside it). Tasks absent from the map are not draggable.

Chunking happens per group rather than over the flat list so a run can never straddle a group
header, even if a future group key stops being a function of the sort criteria.

*Alternative considered:* have `TasksContent` chunk each group itself. Rejected — it would put
comparator knowledge in the UI layer (against the Content-is-dumb rule) and would be reachable
only from instrumented tests, whereas run computation in the ViewModel is covered by fast unit
tests alongside the sorting it derives from.

*Alternative considered:* expose `ImmutableList<ImmutableList<Task>>` of runs and have the UI
look up membership. Rejected — an O(runs × tasks) lookup per recomposed item, for the same
information.

### Decision 3: One drag path in `TasksContent`, scoped by run instead of by list index

The `Manual`-only `itemsIndexed` branch is removed. Both the flat (`MANUAL`/`BY_TITLE`) and
header-grouped renderings go through the same item composable, which attaches the drag modifier
only when `uiState.reorderableRuns[task.id] != null` and clamps the target index to
`0..run.tasks.lastIndex`.

Drag state moves from `(draggedIndex, targetIndex)` — ambiguous once the same index exists in
several groups — to `(draggedTaskId, draggedRunKey, targetIndexInRun, dragOffset)`. An item is
the drop target when its run key matches `draggedRunKey` and its index in the run matches
`targetIndexInRun`, which makes indices unambiguous across groups. On drop the existing
`ReorderTasks` event is emitted with the run's task list and the from/to indices *within the
run*, so the ViewModel and use case are unchanged in signature.

The drag distance estimate (`96.dp` per item) is carried over unchanged. It is a worse
approximation the further a task travels, and runs are shorter than the full list, so the
estimate is if anything more accurate here than in today's `Manual` list.

### Decision 4: `ReorderTasksUseCase` permutes the passed list's own `sortOrder` values

Today the use case assigns `min(sortOrder) + index`. Applied to a *subset* of a category — which
a tie run always is — that mints values that collide with tasks outside the subset and scrambles
their relative order in the `Manual` view. Instead, the use case takes the `sortOrder` values the
passed tasks already hold, sorts them ascending, and reassigns them positionally to the reordered
list. The multiset of values in the database is unchanged, so nothing outside the list can be
affected.

For a list whose values are already contiguous from its minimum — the `Manual` full-list and
subtask cases as they exist today — the two algorithms produce identical results, so existing
behaviour and its tests are preserved.

When the passed values are not distinct (legacy rows all sitting at `0`), a permutation cannot
express an order; the use case falls back to consecutive renumbering from the minimum, i.e.
today's behaviour.

*Alternative considered:* add a second `ReorderTiedTasksUseCase` and leave the existing one
alone. Rejected — two near-identical use cases, and the existing one's subset behaviour (subtask
reordering) has the same latent bug, so fixing it in place is the smaller change.

### Decision 5: Active tasks only

Completed tasks keep their `completedAt`-descending ordering and stay undraggable, as they are
today under `Manual`. Manual order has no meaning for a list ordered by when things were ticked
off, and the completed section is collapsed by default.

## Risks / Trade-offs

- **A drag is only honoured while the tasks stay tied.** Editing a task's priority or date moves
  it out of its run and drops its hand-placed position. → Accepted and inherent: the spec states
  sort criteria outrank manual order. Nothing is lost — `sortOrder` is still stored, so the
  position is restored if the task becomes tied again.
- **More items are now long-press draggable, so long-press could compete with other gestures on
  the card.** → `TaskItem`'s existing gestures are tap (edit) and checkbox click; long-press is
  unused outside the `Manual` list, and the modifier is attached under the same
  `detectDragGesturesAfterLongPress` used there.
- **Under `Priority`/`Date` sort, an unstructured category makes almost every task draggable**,
  which is a behaviour change for users who never long-press deliberately. → An accidental
  long-press drag lands the task back where it started unless it is dragged a full item height,
  and the change is announced through the What's New entry.
- **`sortOrder` permutation changes the `Manual` outcome for lists with gaps.** → This is the
  intended fix; the gap-free case (everything created through `CreateTaskUseCase` in one
  category) is bit-identical to today, and the new behaviour is pinned by tests.
- **Run computation runs on every emission of the tasks flow.** → It is a single linear pass over
  the already-sorted active tasks, inside the existing `combine` block on `defaultDispatcher`.
