## Context

Tempo's navigation shell renders a custom floating toolbar: a bottom-centered bar in compact widths and a left-edge vertical rail from 600dp width. Width is classified with `LocalConfiguration.current.screenWidthDp` in `isFloatingNavigationRailLayout()`. Screens individually pad their scaffolds with `floatingRailContentPadding` (96dp), which is less than the rail's actual footprint (56dp start padding + 64dp surface = 120dp), so the rail overlaps content in every rail layout. No horizontal window insets are consumed anywhere in the shell, and modal sheets derive their height from `LocalConfiguration.screenHeightDp` and always fill the window width.

The official Android `adaptive` skill recommends: window size classes via `currentWindowAdaptiveInfo()`, an edge navigation area on medium+ widths, and adaptive content sizing. The Compose BOM already in use manages `androidx.compose.material3.adaptive:adaptive` (1.2.0), which provides `currentWindowAdaptiveInfo()` and `androidx.window.core.layout.WindowSizeClass`.

## Goals / Non-Goals

**Goals:**

- Correct, overlap-free rail layout on medium+ window widths (landscape phones, tablets, foldables, desktop windows).
- Classify window width with `WindowSizeClass` breakpoints (`WIDTH_DP_MEDIUM_LOWER_BOUND`) instead of `Configuration` fields, so split-screen and freeform windows behave correctly.
- Respect horizontal safe-drawing insets once, at the shell level.
- Readable content width (840dp cap) on expanded windows.
- Modal sheets sized from the real window: height from `LocalWindowInfo.containerSize`, width capped at 640dp.

**Non-Goals:**

- `NavigationSuiteScaffold`, Navigation 3 scene strategies, or multi-pane canonical layouts (no detail routes exist).
- Multi-column list grids (conflicts with timeline ordering and manual drag-reorder).
- Visual redesign of the floating toolbar; hide-on-form behavior stays as is.
- Screenshot-test tooling.

## Decisions

- **Window classification: `currentWindowAdaptiveInfo().windowSizeClass` with `WIDTH_DP_MEDIUM_LOWER_BOUND`** in `isFloatingNavigationRailLayout()`. The public contract (a boolean "rail layout" flag) is kept, so call sites don't change.
  - Alternative: hand-rolled dp math from `LocalWindowInfo.containerSize`; rejected because the window-size-class API is the documented adaptive-first primitive and the artifact is already BOM-managed.

- **Single source of truth for rail metrics** in the navigation package: rail start padding (reduced 56dp → 24dp, closer to the standard 80dp rail footprint), rail surface width derived from the toolbar item size and surface padding, and `FloatingRailContentStartPadding` derived as start + surface + gap (24 + 64 + 16 = 104dp). A guard unit test asserts the clearance covers the rail footprint.
  - Rationale: the overlap bug existed precisely because the rail's position (56dp + 64dp) and the content clearance (96dp) were independent constants; deriving one from the other makes the overlap unrepresentable.
  - Clearance stays applied at each screen's scaffold (not at the shell) so outgoing screens keep their padding during navigation transitions; the former `floatingRailContentPadding` grows into `adaptiveScreenContentLayout`, which also applies the readable-width cap. It is keyed on width class only (not transient bar visibility) so content does not jump when the bar hides while a form sheet is open.

- **Horizontal safe-drawing insets at the shell root**: `TempoNavHost`'s root box applies `WindowInsets.safeDrawing.only(Horizontal)` padding, covering the nav graph and the floating controls in one place. Bottom/status insets keep their existing per-component handling.
  - Alternative: per-screen inset handling; rejected as repetitive and easy to miss (the current bug).

- **Readable content width per screen**: `adaptiveScreenContentLayout` caps each top-level scaffold at `widthIn(max = 840.dp)`, centered in the space remaining after rail clearance (`wrapContentWidth`). 840dp is the expanded-width breakpoint from the M3 guidance. On landscape phones this is a no-op (remaining width ≤ 840dp); on tablets/desktop it prevents absurdly wide cards. Applied to Routines, Tasks, and Settings; Onboarding keeps its own centered layout.

- **Modal sheet sizing from the window**: `rememberTempoModalSheetState` uses `LocalWindowInfo.current.containerSize.height` (converted with `LocalDensity`) instead of `LocalConfiguration.screenHeightDp`, and the sheet surface gets `widthIn(max = 640.dp)` before `fillMaxWidth()`, centering via the existing top/bottom-center alignment. Off-screen offsets keep working because the container height is ≥ the configuration height.

- **Form-factor previews**: a `PreviewFormFactors` multipreview annotation (phone, foldable, tablet, desktop) is added under `src/debug` and applied to the Routines and Tasks content previews, per the adaptive skill's "verify current UI" step.

## Risks / Trade-offs

- **Rail start padding change (56dp → 24dp)** slightly shifts the rail toward the edge; mitigated by it matching standard nav-rail metrics and keeping the add/action buttons aligned with it.
- **`containerSize` includes system bar areas** unlike `screenHeightDp`, so max sheet heights grow by the bar height; acceptable because `maxHeight` already subtracts the status bar inset and bottom sheets apply navigation-bar padding internally.
- **Kover thresholds**: changes are almost entirely `@Composable` layout code; the one new pure calculation (clearance derivation) mirrors the existing tested `calculateFloatingNavigationBottomClearancePadding` pattern and gets a unit test.

## Migration Plan

Single PR; no data or API migration. Revert is a clean git revert.

## Open Questions

- None blocking. If wider adoption of `WindowSizeClass` (e.g., height classes for the day-filter row) is wanted later, it can build on the same utility.
