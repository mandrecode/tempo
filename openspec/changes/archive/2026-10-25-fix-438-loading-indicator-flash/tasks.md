# Tasks

## 1. Indicator

- [x] 1.1 Add `revealDelayMillis` (default `LOADING_INDICATOR_REVEAL_DELAY_MILLIS` = 400 ms) to `TempoLoadingIndicator`, keep the indicator laid out but transparent (and without a content description) until the delay elapses, then fade it in.
- [x] 1.2 Pass `revealDelayMillis = 0L` in the debug previews.

## 2. Tests and verification

- [x] 2.1 Add `TempoLoadingIndicatorTest` (hidden before the delay, shown after it, immediate when the delay is zero, same height hidden and shown); update `TasksContentTest` and `RoutinesContentTest` `showsLoadingIndicator_whenLoading` to advance past the delay.
- [x] 2.2 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest lintDebug` and `openspec validate --strict`.
- [x] 2.3 Run `TempoLoadingIndicatorTest`, `TasksContentTest`, `PlanTasksSheetTest` and `RoutinesContentTest` on the Pixel 10 AVD.

## 3. Whole first screen

- [x] 3.1 `StartupContentGate` plus `HoldSplashWhileFirstLoad` (via `StartupContentGateHost` on `LocalActivity`); `MainActivity` splash also holds for `MainUiState.Loading` and the gate. Unit test `StartupContentGateTest`.
- [x] 3.2 `FirstLoadCrossfade` in Tasks, Routines and Focus content. Instrumented tests in `TempoLoadingIndicatorTest` (fades rather than swaps; ready content shows at once).
- [x] 3.3 What's-new sheet waits for the splash to hand over.
- [x] 3.4 Cold-start screenshot bursts on the Pixel 10 AVD, with and without a pending what's-new entry; `TasksContentTest`, `RoutinesContentTest`, `FocusContentTest`, `TasksScreenTest` and `MainActivityNavigationTest` pass.
- [x] 3.5 Rendered-pixel tests in `TempoLoadingIndicatorTest` (Copilot): partial opacity mid-fade and full opacity after it, and full opacity in the very first frame under a `StartupContentGateHost` (a stand-in activity provided through `LocalActivity`). The latter fails when the splash branch is disabled.
