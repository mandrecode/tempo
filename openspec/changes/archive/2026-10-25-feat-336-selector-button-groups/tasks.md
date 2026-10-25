# Tasks

- [x] 1.1 Add `ConnectedSelectorButton`, `SelectorButtonGroup`, `selectorButtonColors` and `connectedButtonShapes` in `core/ui/components/SelectorButtonGroup.kt`.
- [x] 1.2 Task editor: priority and monthly day option as `SelectorButtonGroup`; category and periodicity rows as `ConnectedSelectorButton`s in their existing `LazyRow`s; keep the test tags.
- [x] 1.3 Habit editor: Habit / Habit chain switch as `SelectorButtonGroup`.
- [x] 1.4 Instrumented `SelectorButtonGroupTest`; debug previews.
- [x] 1.5 What's New `434-380-115-335-336` (en, es).
- [x] 1.6 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest koverVerifyDebug lintDebug`, `TaskBottomSheetTest`, `HabitBottomSheetTest` and `openspec validate --strict`; check by hand on the Pixel 10 AVD.
- [x] 1.7 Update the `AdaptiveTaskSheet*` tablet screenshot references (the task sheet's category and priority rows are the intended change; the reference images are CI's own Linux renders, reviewed against the diff report).
- [x] 1.8 Fall back to a scrolling row when labels do not fit an equal share; covered by `SelectorButtonGroupTest.longLabels_scrollInsteadOfBeingCut` (Spanish monthly options, 2× font, 260 dp).
