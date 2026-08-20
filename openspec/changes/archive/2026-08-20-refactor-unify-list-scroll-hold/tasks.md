## 1. Share the mechanism

- [x] 1.1 Move `rememberViewportHold` out of `PlanTasksSheet.kt` into `core/ui`
- [x] 1.2 Give it overloads for `LazyListState` and `LazyGridState` over their shared behaviour
- [x] 1.3 Point the plan sheet at the shared one, unchanged in behaviour

## 2. Adopt it in the Tasks list

- [x] 2.1 Hold the view when a task is checked off or un-checked
- [x] 2.2 Remove the `scroll_anchor` item
- [x] 2.3 Reword the `isFirstVisibleItem` comment that cites the anchor

## 3. Verify

- [x] 3.1 Cover the hold with an instrumented test on the Tasks list
- [x] 3.2 Confirm on device that the first card sits 8dp from the panel and the view holds
- [x] 3.3 ktlintCheck, detekt, unit tests, lint, kover, connected tests
