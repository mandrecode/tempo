# Proposal

## Why

Originating issue: [#438](https://github.com/mandrecode/tempo/issues/438).

The first time each screen loads (Tasks, Routines, Focus, and the Focus plan sheet), the full-size loading indicator appears and disappears within a frame or two, because local data usually arrives in well under a few hundred milliseconds. The result reads as a flash, not as progress.

## What Changes

- `TempoLoadingIndicator` keeps its space laid out but stays invisible for a short grace period (400 ms). If loading is still in progress afterwards, the indicator fades in. Loads that finish inside the grace period never show it.
- Every caller gets this behavior without changes. Debug previews pass `revealDelayMillis = 0` so the static preview still shows the indicator.

- **The whole first screen** (raised on device after the first fix): the bare background, title and navigation bar still showed for a moment before the content arrived on top.
  - On a cold start, the splash now also waits for the app's settings and for the first screen's first content (`StartupContentGate`, reported by Tasks, Routines and Focus), within a cap raised from 1.5 s to 2.5 s (cold starts measured at 1.3–1.7 s on the AVD).
  - A tab opened for the first time crossfades from its loading state to its content over 250 ms (`FirstLoadCrossfade`) instead of swapping on one frame.
  - The what's-new sheet waits until the splash has handed over. As a separate window it otherwise ended the splash over an undrawn screen, which showed as a black frame.

- **Every tab, not just the first:** the three tabs' view models are created at startup, once the database is ready. They are held by the activity so each tab's data is already loaded when it is first opened, and every tab appears finished on its first frame.

### Non-goals

- No change to how or when screens load their data, or to the error path.
- No skeleton/placeholder content.

## Capabilities

### New Capabilities
- `loading-indicator-reveal`: when the shared loading indicator becomes visible.

### Modified Capabilities

None.

## Impact

- UI: `core/ui/components/TempoLoadingIndicator.kt` (`FirstLoadCrossfade`), its debug previews; `TasksContent`, `RoutinesContent`, `FocusContent` and their screens; new `core/ui/StartupContentGate.kt`; `MainActivity` (splash condition, what's-new timing).
- Tests: new `TempoLoadingIndicatorTest`; `TasksContentTest` and `RoutinesContentTest` advance past the delay.
- No domain, data, or persistence changes.
