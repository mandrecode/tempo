# Tasks

## 1. Separator row

- [x] 1.1 Add a "Clear" text action (error color, new `clear` string in `values` and `values-es`) at the trailing end of `CompletedTasksSeparator`, with a weighted spacer standing in for the divider in manual sort; wire it to `UiEvent.RequestDeleteCompletedTasks` from `TasksContent`. Verify with a `TasksContentTest` case that tapping "Clear" emits `RequestDeleteCompletedTasks` in both collapsed and expanded states.
- [x] 1.2 Drop `firstGroupLabel` from the separator and show a `CompletedGroupHeader` for every completed group, including the first. Update the separator previews, and verify that the existing BY_TITLE `TasksContentTest` still finds "A → Z".

## 2. Floating bar

- [x] 2.1 Remove `hasCompletedTasks`/`onClearCompleted` from `TasksFloatingBarState`, the clear button from `TaskActionButtons` and `VerticalTaskActionButtons`, `TASK_ACTIONS_WIDTH`, the screen-side wiring in `TasksScreen`, and `ClearCompletedButton` with its now-unused strings in every locale. Verify with `./gradlew assembleDebug`.
- [x] 2.2 Remove `hasContextualActions` from `TempoBottomNavigation`/`isSessionChipCompact`, and update `SessionChipCompactnessTest`. Verify with `./gradlew testDebugUnitTest`.
- [x] 2.3 Update `PortraitFloatingBarMotionTest`: drop the clear-specific cases and keep the sort/pill/add gap check. Verify that it compiles with `./gradlew compileDebugAndroidTestKotlin`.

## 3. What's New and verification

- [x] 3.1 Replace `WhatsNewRegistry.latest` with id `351` and new `whats_new_title`/`whats_new_description` in every locale. Verify with `./gradlew lintDebug` (no missing or extra translations).
- [x] 3.2 Run `./gradlew ktlintFormat ktlintCheck :app:detekt testDebugUnitTest koverVerifyDebug validateDebugScreenshotTest` and `openspec validate feat-351-clear-completed-in-separator`; all pass except four `FocusContentMidDay` screenshots, which fail identically on `main` and are unrelated to this change.
- [x] 3.3 Run `TasksContentTest` and `PortraitFloatingBarMotionTest` on the Pixel 10 AVD and confirm they pass.

## Workflow follow-up

- Archive the change in the same PR before the final CI run.
