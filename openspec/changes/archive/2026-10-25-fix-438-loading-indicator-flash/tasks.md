# Tasks

## 1. Indicator

- [x] 1.1 Add `revealDelayMillis` (default `LOADING_INDICATOR_REVEAL_DELAY_MILLIS` = 400 ms) to `TempoLoadingIndicator`, hide the indicator until the delay elapses, then fade it in.
- [x] 1.2 Pass `revealDelayMillis = 0L` in the debug previews.

## 2. Tests and verification

- [x] 2.1 Add `TempoLoadingIndicatorTest` (hidden before the delay, shown after it, immediate when the delay is zero); update `TasksContentTest` and `RoutinesContentTest` `showsLoadingIndicator_whenLoading` to advance past the delay.
- [x] 2.2 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest lintDebug` and `openspec validate --strict`.
- [x] 2.3 Run `TempoLoadingIndicatorTest`, `TasksContentTest`, `PlanTasksSheetTest` and `RoutinesContentTest` on the Pixel 10 AVD.
