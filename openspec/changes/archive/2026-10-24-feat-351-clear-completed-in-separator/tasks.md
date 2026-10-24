# Tasks

## 1. Completed section

- [x] 1.1 Add `ClearCompletedFooter` (error-colored text button with the remove-done icon) as a lazy item after the last completed task, only while the section is expanded, raising `RequestDeleteCompletedTasks`. Add a preview. Verified by `TasksContentTest.clearCompleted_showsOnlyWhenExpanded_andRequestsDeletion`.
- [x] 1.2 Keep the separator row as on `main`; verified by the existing priority/title separator tests.

## 2. Floating bar

- [x] 2.1 Remove `hasCompletedTasks`/`onClearCompleted` from `TasksFloatingBarState`, the clear button from both action layouts, `TASK_ACTIONS_WIDTH`, the screen-side wiring in `TasksScreen`, and `ClearCompletedButton` with its now-unused string. Verified with `./gradlew assembleDebug`.
- [x] 2.2 Pass `hasContextualActions = isTasksRoute` so the session chip stays compact on Tasks. Covered by `SessionChipCompactnessTest`.
- [x] 2.3 Update `PortraitFloatingBarMotionTest`: drop clear-specific cases, keep the sort/pill/add gap check.

## 3. What's New and verification

- [x] 3.1 Combine the What's New entry with the unreleased #379/#422 entry (id `379-422-351`) in every locale. Verified by `./gradlew lintDebug`.
- [x] 3.2 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest koverVerifyDebug lintDebug` and `openspec validate --specs --strict`.
- [x] 3.3 Run `TasksContentTest`, `PortraitFloatingBarMotionTest` and `TasksScreenTest` on the Pixel 10 AVD.
