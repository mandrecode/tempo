# Design

## Width classes

`windowWidthClass(widthDp)` is a pure function over WindowManager's constants (600 / 840 / 1200 / 1600). `currentWindowWidthClass()` feeds it `currentWindowAdaptiveInfoV2().windowSizeClass.minWidthDp`, the lower bound of the bucket the window is in. Both paths therefore use one rule, and fractional-dp truncation is WindowManager's own.

- `isFloatingNavigationRailLayout()` = `≥ MEDIUM`
- `sheetPlacement(...)` = `DockedPane` when `≥ LARGE`; `rememberSheetPlacement()` reads the current class
- `isExpandedFloatingRailLayout()` still follows docked placement, so the expanded rail and docked editors switch together.

`sheetPlacement(windowWidthDp: Int)` is kept, delegating to the class, so existing tests and callers keep working.

## Animation

`animatedRailClearance(target)` is `animateDpAsState` on `floatingControlsMotionSpec()`, the no-bounce, medium-low-stiffness spring the floating bar already moves on. The rail and the content therefore settle together. On the first composition the animated value equals the target, so screenshots and first frames are unchanged. It is applied wherever a screen computes its clearance: `floatingRailContentClearance()` (Focus, Settings), and Tasks and Routines, which pick the expanded clearance while an editor is docked.

## Alternatives considered

- **Animating the whole navigation transition (bar ↔ rail) with shared elements.** This was rejected as too large and risky for this change. The clearance slide removes the visible jump of the content, which was the most noticeable part.
