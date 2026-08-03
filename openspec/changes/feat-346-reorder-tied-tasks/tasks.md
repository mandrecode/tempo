## 1. Sort criteria model

- [x] 1.1 Add `TaskSortCriteria` in `features/tasks/presentation/model/` mapping each `SortOption` to its ordered pre-manual criteria (`BY_DATE` → date, priority; `BY_PRIORITY` → priority, date; `BY_TITLE` → title; `MANUAL` → none), exposing a `comparator()` that appends `sortOrder` then `id`, and an `areTied(a, b)` derived from the same criteria list
- [x] 1.2 Replace the four hand-written `compareBy` chains for active tasks in `TasksViewModelDataLoading.loadData()` with the derived comparator, keeping `isCompleted` as the leading criterion
- [x] 1.3 Unit-test `TaskSortCriteria`: equal priority broken by closer date, equal datetime broken by priority, nulls last for both, `MANUAL` ties everything, `areTied` agrees with the comparator returning 0 on the pre-manual criteria
- [x] 1.4 Delete `SortOptionTest`, whose three cases re-implemented the comparators inside the test instead of exercising production code; they are now covered against `TaskSortCriteria` itself

## 2. Reorderable runs

- [x] 2.1 Add `ReorderableRun` (run key, index in run, `ImmutableList<Task>`) in `features/tasks/presentation/model/`
- [x] 2.2 In `TasksViewModelDataLoading.loadData()`, chunk each active group's task list into maximal runs of mutually tied tasks and build `ImmutableMap<Long, ReorderableRun>` from runs of size ≥ 2 (active tasks only; completed groups excluded)
- [x] 2.3 Expose `reorderableRuns` on `TasksContract.UiState` with an empty default
- [x] 2.4 Unit-test run computation in `ReorderableRunTest` and `TasksViewModelTest`: three undated unprioritised tasks form one run under `BY_PRIORITY`; a task the sort distinguishes is absent from the map; runs never straddle groups; `MANUAL` yields a single run over all active tasks; completed tasks never appear

## 3. Persisted manual order

- [x] 3.1 Change `ReorderTasksUseCase` to reassign the passed tasks' own `sortOrder` values (sorted ascending) positionally to the reordered list, falling back to consecutive renumbering from the minimum when those values are not distinct
- [x] 3.2 Extend `ReorderTasksUseCaseTest`: gap-y values are permuted not renumbered, tasks outside the list are never written, contiguous lists behave exactly as before, duplicate values hit the fallback, no-op moves write nothing

## 4. Drag-and-drop in `TasksContent`

- [x] 4.1 Replace the composable-local `draggedIndex`/`targetIndex` drag state with a `TaskDragState` holding `draggedTaskId`, `draggedRunKey`, `targetIndex`, `offsetY`
- [x] 4.2 Extract the task card into one `TaskListItem` composable used by both the active and completed renderings, and attach the drag modifier only when `uiState.reorderableRuns[task.id]` is non-null, clamping the target index to the run's bounds
- [x] 4.3 Delete the `MANUAL`-only `itemsIndexed` drag branch, letting `MANUAL` flow through the run mechanism (single run over all active tasks)
- [x] 4.4 Emit `ReorderTasks` on drop with the run's task list and from/to indices within the run; keep dragged/target visual treatment (translate + alpha) as-is
- [x] 4.5 Verify `TasksScreen`, the `src/debug/` previews, and `TasksContentTest` still compile against the new `UiState` field — all build it with named arguments, so the defaulted field needs no call-site change

## 5. UI tests

- [x] 5.1 `TasksContentTest`: a long-press drag on a tied task under `BY_PRIORITY` emits `ReorderTasks` with indices scoped to its run
- [x] 5.2 `TasksContentTest`: a task with no run emits nothing on the same gesture, and the `MANUAL` list still reorders across the whole active list (regression guard for 4.3)
- [x] 5.3 Previews left unchanged: they render a static frame with no drag, so seeding `reorderableRuns` into them would show nothing new

## 6. Announce and verify

- [x] 6.1 Replace `WhatsNewRegistry.latest` with the `346` entry and rewrite `whats_new_title`/`whats_new_description` in `values/` and `values-es/`
- [x] 6.2 Run `./gradlew ktlintFormat`, then `./gradlew compileDebugKotlin compileDebugUnitTestKotlin compileDebugAndroidTestKotlin`
- [x] 6.3 Run `./gradlew testDebugUnitTest`, `./gradlew koverVerifyDebug`, `./gradlew :app:detekt`, `./gradlew ktlintCheck`, and `./gradlew lintDebug` (translations)
- [x] 6.4 Prune the three detekt baseline entries the `TasksContent` refactor resolved and lower the CI ceiling (`MAX=158` → `155`), updating the stale figures in `AGENTS.md`
- [x] 6.5 Run `:app:connectedDebugAndroidTest` for the `features.tasks` package on the Pixel 10 AVD (63 tests, including the three new drag tests)
- [x] 6.6 Run `openspec validate feat-346-reorder-tied-tasks --strict`
