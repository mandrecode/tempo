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
