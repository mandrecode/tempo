## Context

`PersistentLandscapeFloatingBar` (`app/src/main/java/com/mandrecode/tempo/core/ui/navigation/PersistentFloatingBar.kt:127`) renders the vertical floating navigation rail used whenever `isFloatingNavigationRailLayout()` is true (window width at least the medium breakpoint). It is a single `Column(Modifier.fillMaxHeight()...)`:

```
[title (expanded rail only)]
[Add action button]
navigationContent()            // Routines/Tasks nav pill
VerticalTaskActionButtons(...) // Sort button, Clear-completed button (Tasks route only)
Spacer(Modifier.weight(1f))
SettingsRailButton(...)
```

All items except the spacer report a fixed intrinsic height (`FloatingToolbarItemSize` = 48.dp rows, or taller expanded rows with labels). Jetpack Compose's `Column` does not shrink non-weighted children to fit a fixed-height parent: it measures each non-weighted child with a loose (unbounded) height constraint, sums their actual heights, and only the weighted `Spacer` absorbs the remaining space — clamped to a minimum of `0.dp` when there isn't enough room. When the sum of fixed items (title + add + nav pill + sort + clear-completed) exceeds the column's available height, the spacer contributes nothing and the column's total content height exceeds its own bounds. The trailing `SettingsRailButton`, being last in the arrangement, is positioned past the visible/clipped bounds, which the reporter sees as the button "shrinking" (a partially clipped rounded shape reads as smaller).

This is most reachable on the Tasks route when there are completed tasks (both Sort and Clear-completed render) combined with the expanded rail tier (`isExpandedFloatingRailLayout()`, labels shown, taller rows) in a window that is wide enough to trigger the rail but not very tall (e.g., a tablet/foldable in landscape, or a resized desktop window).

## Goals / Non-Goals

**Goals:**
- The Settings button in the floating navigation rail must always render at its full, correct size and remain reachable, regardless of how many task-action buttons are showing or how tall the rail's content is relative to the available height.
- No visible change to the rail's appearance or spacing in the common case where content already fits (which is effectively all current device/window configurations in the app's supported range).

**Non-Goals:**
- Not redesigning the rail's item priority/order or introducing per-item shrink/collapse behavior.
- Not touching the portrait/bottom-bar layout (`PersistentPortraitFloatingBar`), which does not have this vertical stacking problem.
- Not changing task-action button visibility rules or the expanded/compact rail tier logic.

## Decisions

**Decision: split the rail into a scrollable, weighted content `Column` (`RailScrollableContent`) followed by a fixed `SettingsRailButton` sibling — do not put `Modifier.verticalScroll()` and `Modifier.weight()` on the same `Column`.**

The initial idea was to simply add `Modifier.verticalScroll(rememberScrollState())` to the existing rail `Column` and keep the trailing `Spacer(Modifier.weight(1f))` pinning Settings to the bottom. That does not work: Jetpack Compose does not support combining `Modifier.weight()` on a child with `Modifier.verticalScroll()` on its parent `Column` — the scroll modifier measures children with an unbounded (infinite) max height in the scroll direction, but weight resolution requires a bounded max height to divide remaining space among weighted children. Applying both fails to satisfy the intended layout.

The implemented approach avoids this by scoping `verticalScroll` to a separate inner `Column` (`RailScrollableContent`) that holds the title, add action, nav tabs, and Sort/Clear-completed buttons. That inner `Column` is given `Modifier.weight(1f)` **as a child of the outer (non-scrolling) rail `Column`** — legal, since the weight is resolved by the outer `Column`, not by the scrollable one. `SettingsRailButton` is placed as the outer `Column`'s next (and last) child, outside the scrollable region, so it always renders at its full size regardless of how much the inner content overflows.

When the inner content is shorter than the weighted space allotted to it (the common case), it renders top-aligned with empty space below, and Settings — the very next sibling — lands at the bottom of the outer `Column`, identical to the old `Spacer(weight(1f))` behavior. When content overflows, the inner `Column` scrolls within its bounded weighted space instead of overflowing/clipping, and Settings remains a normal, fully-rendered sibling immediately after it.

Alternatives considered:
- *Let content overflow/clip (status quo)*: this is the bug; rejected.
- *`verticalScroll` directly on the rail's single `Column`, keeping the `Spacer(weight(1f))`*: incompatible with Compose's weight/scroll constraint model as described above; rejected.
- *Shrink item sizes or hide labels earlier when space is tight*: would require intrinsic-height measurement/coordination across independently defined child composables (title, add button, nav pill, sort/clear-completed) and a new "compact under pressure" mode for each; substantially more invasive for a rare edge case, and changes visual identity of the rail contents. Rejected as disproportionate to the bug.
- *Give the Spacer a minimum height / reserve fixed space for Settings and let earlier items scroll or wrap*: equivalent in effect to the scroll approach but more custom code (manual overflow detection via `onSizeChanged`) instead of reusing the standard `verticalScroll` modifier. Rejected in favor of the simpler, standard solution.

## Risks / Trade-offs

- [Enabling scroll on a nav rail is an unusual interaction pattern for navigation chrome] → Mitigation: it only activates when content genuinely doesn't fit (a pre-existing bug condition); in all currently supported configurations content fits, so no behavior change is observable. Settings remaining fully visible and clickable is a strict improvement over the current clipped/broken state.
- [`verticalScroll` adds an extra layout pass] → Mitigation: negligible for a small, low-frequency-recomposition column with a handful of items.

## Migration Plan

Not applicable — this is a self-contained UI fix in a single composable with no data, API, or persisted-state changes. Verify manually on a resized/landscape tablet emulator with completed tasks present (to show both Sort and Clear-completed) alongside standard phone/tablet configurations, per `docs/agents/UI_UX.md` for `@Preview` conventions.
