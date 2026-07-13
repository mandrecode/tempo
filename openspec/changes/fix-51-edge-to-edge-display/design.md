## Context

`MainActivity` calls `ComponentActivity.enableEdgeToEdge()`, and `TempoTheme` reapplies transparent, theme-aware system-bar styles. Settings already passes `WindowInsets(0)` to its top-level `Scaffold`, allowing the screen surface to draw through the system-bar regions while its top app bar and scroll content provide their own safe-area handling. Tasks and Routines rely on the Material 3 `Scaffold` default content insets, so the scaffold consumes system bars around the entire content and prevents those screens from behaving edge-to-edge.

## Goals / Non-Goals

**Goals:**

- Make Tasks and Routines use the same top-level edge-to-edge inset policy as Settings.
- Preserve Material 3 top-app-bar status-bar handling.
- Keep the final list item, floating controls, fallback FAB, and snackbars reachable above the navigation bar in portrait and rail layouts.
- Keep inset ownership explicit so system insets are not applied twice.

**Non-Goals:**

- Redesign navigation, app bars, sheets, dialogs, or page colors.
- Change the activity or theme system-bar styling that already enables edge-to-edge.
- Introduce a new screen abstraction or dependency.

## Decisions

### Top-level scaffolds opt out of automatic content insets

Tasks and Routines will pass `contentWindowInsets = WindowInsets(0)` to `Scaffold`, matching Settings. Their Material 3 top bars continue to own status-bar insets, and the scaffold's content padding continues to account for top-bar height.

Applying `safeDrawingPadding()` to each screen was rejected because it would inset and clip the whole page, preventing backgrounds and scrollable content from drawing behind system bars. Leaving the scaffold defaults was rejected because it is the source of the inconsistent behavior.

### Scrollable content owns bottom safe clearance

The Tasks and Routines list bottom padding will include the navigation-bar bottom inset in addition to existing floating-navigation clearance. This keeps the final row scrollable above both the system navigation area and the floating bar while allowing list drawing and overscroll effects to extend behind them.

Applying navigation-bar padding to the list's parent was rejected because it would shrink and clip the scrolling viewport. The existing floating bar and fallback FAB retain their own `navigationBarsPadding()` because they are overlay controls outside the scaffold slots.

### Verify the inset policy at the screen and helper boundaries

Regression coverage will validate the shared bottom-clearance calculation and exercise the main screen compositions with the zero-inset scaffold policy. Existing screen UI tests remain responsible for core content visibility and interaction behavior.

## Risks / Trade-offs

- [Risk] Bottom clearance is accidentally applied twice on a screen. → The scaffold supplies zero system-bar insets, while only lists and overlay controls consume the navigation-bar inset.
- [Risk] Three-button navigation requires more clearance than gesture navigation. → Calculate clearance from `WindowInsets.navigationBars` at composition time instead of using a fixed system inset.
- [Risk] Landscape rail layouts gain unnecessary bottom spacing. → Preserve the existing layout-specific floating-bar clearance and add only the actual navigation-bar inset.
