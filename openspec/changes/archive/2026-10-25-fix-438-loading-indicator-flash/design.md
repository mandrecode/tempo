# Design

## Approach

The fix sits in the shared `TempoLoadingIndicator`, so all four loading surfaces get it at once and no ViewModel timing changes.

- A `revealed` flag starts `false` (or `true` when `revealDelayMillis <= 0`). A `LaunchedEffect` waits `revealDelayMillis` and then sets it.
- The indicator sits in an `AnimatedVisibility` with a 200 ms fade-in, inside the same `Box` as before. The box keeps its size and alignment from the first frame, so nothing around it moves when the indicator appears.
- When loading finishes, callers drop the composable. That cancels the effect, so a quick load never shows the indicator.

## Why 400 ms

Room-backed first loads on a Pixel 7 finish in roughly 50–250 ms. 400 ms covers them with margin, and it is still short enough that a slow load (cold database, migration) shows progress quickly.

## Alternatives considered

- **Delaying `isLoading` in each ViewModel.** This repeats the same timing logic in four places and mixes a presentation concern into state.
- **Starting with `isLoading = false`.** This would flash the empty state instead, which is worse.
