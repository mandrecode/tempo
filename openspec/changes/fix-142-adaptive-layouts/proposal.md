## Why

[GitHub issue #142](https://github.com/mandrecode/tempo/issues/142) reports that the app is not adaptive and horizontal (landscape / wide-window) layouts are broken. Verified on a Pixel 10 emulator in landscape:

- The floating navigation rail is drawn over screen content: the day-filter chips on Routines and the category chips, section headers, and task action buttons on Tasks are partially covered. The existing `floatingRailContentPadding` (96dp) is smaller than the rail's real footprint (~120dp), so content always overlaps.
- Screen content is not inset from the display cutout or the 3-button navigation bar on the short edges, so cards and buttons render under system UI on the right edge.
- Task and habit cards stretch the full window width (~900dp on a landscape phone, more on tablets), far past a readable width.
- Modal task/habit sheets size themselves from `LocalConfiguration.screenHeightDp` and fill the full window width, producing a sparse, broken-looking full-screen sheet in landscape.
- Window width is classified with `LocalConfiguration.screenWidthDp` and a hand-rolled 600dp constant instead of the window-size-class API the Android adaptive guidance recommends, so split-screen and freeform windows are misclassified.

The Android team's adaptive-first guidance (official `adaptive` skill) is the reference for the fix.

## What Changes

- Classify window width with the official `WindowSizeClass` API (`androidx.compose.material3.adaptive:adaptive`, version-managed by the existing Compose BOM) instead of `LocalConfiguration.screenWidthDp`.
- Reserve real space for the floating navigation rail at the navigation-shell level with a single source of truth for rail metrics, so rail and content can never overlap.
- Apply horizontal safe-drawing window insets (display cutout, landscape navigation bar) at the navigation shell so all screens and floating controls respect them.
- Cap main content at a readable max width on expanded windows, centered in the remaining space.
- Size modal sheets from the actual window size and cap their width on wide windows.
- Add form-factor previews (phone, foldable, tablet, desktop) for the main screen contents in the debug source set.
- Non-goal: replacing the custom floating toolbar/rail with `NavigationSuiteScaffold` — the expressive floating toolbar is an intentional design; this change makes it behave correctly as a rail.
- Non-goal: multi-pane (list-detail / supporting-pane) layouts — Tempo has no detail routes; task and habit editors are modal sheets by design.
- Non-goal: multi-column task/habit grids — the routines timeline ordering and tasks manual drag-reorder are single-column interactions; readable max width is applied instead.
- Non-goal: introducing screenshot-test infrastructure.

## Capabilities

### New Capabilities

- `adaptive-window-layout`: Layout behavior across window size classes — navigation area placement and clearance, safe-drawing insets, readable content width, and window-based modal sheet sizing.

### Modified Capabilities

- None.

## Impact

- Affects the navigation shell (`TempoNavHost`, `PersistentFloatingBar`, `NavigationComponents`, `FloatingNavigationLayout`), Routines/Tasks screen scaffolding, and `TempoModalSheet`.
- Adds `androidx.compose.material3.adaptive:adaptive` (BOM-managed, no pinned version); `docs/agents/TECH_STACK.md` is updated accordingly.
- No domain, data, persistence, scheduling, or navigation-graph changes.
