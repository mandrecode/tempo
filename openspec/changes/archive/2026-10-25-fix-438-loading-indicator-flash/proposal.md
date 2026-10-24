# Proposal

## Why

Originating issue: [#438](https://github.com/mandrecode/tempo/issues/438).

The first time each screen loads (Tasks, Routines, Focus, and the Focus plan sheet), the full-size loading indicator appears and disappears within a frame or two, because local data usually arrives in well under a few hundred milliseconds. The result reads as a flash, not as progress.

## What Changes

- `TempoLoadingIndicator` keeps its space laid out but stays invisible for a short grace period (400 ms). If loading is still in progress afterwards, the indicator fades in. Loads that finish inside the grace period never show it.
- Every caller gets this behavior without changes. Debug previews pass `revealDelayMillis = 0` so the static preview still shows the indicator.

### Non-goals

- No change to how or when screens load their data, or to the error path.
- No skeleton/placeholder content.

## Capabilities

### New Capabilities
- `loading-indicator-reveal`: when the shared loading indicator becomes visible.

### Modified Capabilities

None.

## Impact

- UI: `core/ui/components/TempoLoadingIndicator.kt`, its debug previews.
- Tests: new `TempoLoadingIndicatorTest`; `TasksContentTest` and `RoutinesContentTest` advance past the delay.
- No domain, data, or persistence changes.
