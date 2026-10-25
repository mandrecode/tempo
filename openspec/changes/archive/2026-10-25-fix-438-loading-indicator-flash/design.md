# Design

## Approach

The fix sits in the shared `TempoLoadingIndicator`, so all four loading surfaces get it at once and no ViewModel timing changes.

- A `revealed` flag starts `false` (or `true` when `revealDelayMillis <= 0`). A `LaunchedEffect` waits `revealDelayMillis` and then sets it.
- The indicator is always measured and laid out. Only its alpha animates, from 0 to 1 over 200 ms. A caller that wraps its height around it, like the plan sheet, therefore does not grow when it appears. An `AnimatedVisibility` would not reserve the 96 dp while hidden. Until the indicator is revealed it carries no content description, so accessibility services do not announce it either.
- When loading finishes, callers drop the composable. That cancels the effect, so a quick load never shows the indicator.

## Why 400 ms

Room-backed first loads on a Pixel 7 finish in roughly 50–250 ms. 400 ms covers them with margin, and it is still short enough that a slow load (cold database, migration) shows progress quickly.

## Alternatives considered

- **Delaying `isLoading` in each ViewModel.** This repeats the same timing logic in four places and mixes a presentation concern into state.
- **Starting with `isLoading = false`.** This would flash the empty state instead, which is worse.

## The whole first screen

Hiding the indicator did not remove the flash; it moved it. The first frames after the splash were the screen's background, title and navigation bar, followed by the content.

- **Cold start:** `MainActivity`'s splash condition now also holds while `MainUiState` is `Loading` and while `StartupContentGate.isHolding`. Tasks, Routines and Focus call `HoldSplashWhileFirstLoad(uiState.isLoading)` from their screens, which reports in a `SideEffect`, so the report is in place before the splash's next pre-draw check. The gate holds only for the first screen's first load. Once any content has shown it stays open, and screens that never load (onboarding) never hold it. The gate is reached through `LocalActivity` (`StartupContentGateHost`) rather than a new `CompositionLocal`, which the project's Compose rules forbid. The existing 1.5 s cap bounds the wait.
- **Later first visits:** the three contents switch on `FirstLoadCrossfade(isLoading)`, a 250 ms `Crossfade`. Content that is ready on first composition (previews, screenshot tests, a warm tab) shows immediately; only a loading-to-loaded change fades.
- **What's new:** the sheet is a separate window, and opening it while the main window's drawing was still held ended the splash over an undrawn screen (a black frame). It now waits for `hasSplashHandedOver`, which the splash condition sets the first time it lets go.

Verified with screenshot bursts during cold starts on the Pixel 10 AVD. Before: splash, blank, bare Focus chrome, content. After: splash, then the complete Focus screen, and with a pending what's-new entry the sheet opens over it with no black frame.
