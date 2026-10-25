# Proposal

## Why

Originating issue: [#163](https://github.com/mandrecode/tempo/issues/163) asks to improve adaptive quality with smooth animations, responsiveness, and Material Design breakpoints, pointing at Jetpack WindowManager 1.5 (which adds the **Large**, 1200 dp, and **Extra-large**, 1600 dp, width classes).

The app adapts at two points today, decided by two different mechanisms:
- **Rail instead of bottom bar** from the Medium class (600 dp), via `currentWindowAdaptiveInfo()`.
- **Editors docked beside content** from a hand-coded `1200` dp, via its own `LocalWindowInfo`/density maths. A comment there warns that the two must truncate fractional dp the same way or they disagree at a boundary.

When a breakpoint is crossed (a foldable opening, a desktop window being resized, split screen), or when an editor docks, screen content jumps to its new rail clearance in one frame.

## What Changes

- **One breakpoint source:** `WindowWidthClass` (Compact, Medium, Expanded, Large, Extra-large), built on WindowManager 1.5's official `WIDTH_DP_*_LOWER_BOUND` constants and `currentWindowAdaptiveInfoV2()` (Large/Extra-large aware). The rail (`≥ Medium`) and docked editors (`≥ Large`) both read it, so they cannot disagree, and the hand-coded 1200 dp and its separate rounding logic are gone. Extra-large windows are treated as Large.
- **No recreation on size changes:** `MainActivity` handles rotation, fold, split-screen and window-resize configuration changes in place (Compose re-lays out from the new window size), so state and running animations survive them.
- **Smooth adaptation:** the content's start clearance for the floating rail animates on the same spring the floating controls use (`animatedRailClearance`). When the window crosses a breakpoint, or when an editor docks beside the list (which widens the rail), the content slides instead of jumping. This applies to Tasks, Routines, Focus and Settings.
- What's New: the entry becomes `434-380-115-335-336-440-163` (en, es).

### Non-goals

- No new layouts for Expanded/Large/Extra-large beyond what exists (the readable max width of 840 dp is unchanged).
- No change to which window sizes get the rail or docked editors. The thresholds are the same values, now from the official constants.

## Capabilities

### New Capabilities
- `adaptive-window-classes`: how window width maps to layout tiers, and how layout transitions between them.

### Modified Capabilities

None.

## Impact

- Manifest: `MainActivity` `configChanges` (orientation, screen size, screen layout, smallest screen size).
- UI: new `core/ui/adaptive/WindowWidthClass.kt`; `SheetPlacement.kt` (derived from the width class); `FloatingNavigationLayout.kt` (rail check, `animatedRailClearance`); `TasksScreen.kt` and `RoutinesScreen.kt` (animated clearance).
- Tests: new unit `WindowWidthClassTest`, new instrumented `AnimatedRailClearanceTest`. `SheetPlacementTest` and the adaptive screenshot tests pass unchanged.
