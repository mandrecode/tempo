## Why

The Tasks and Routines (habits) list screens show a first-load state (`TempoLoadingIndicator`) built on the legacy `CircularProgressIndicator` spinner. Material 3 Expressive introduces a dedicated `LoadingIndicator` component with morphing shapes ([m3.material.io/components/loading-indicator](https://m3.material.io/components/loading-indicator/overview)) that the app's Material3 1.5.0-alpha24 dependency already ships. Issue #79 asks to adopt this Expressive loading indicator for the first-load state instead of the plain spinner, aligning the loading experience with the rest of the app's Expressive-era Material 3 usage (already opted into via `ExperimentalMaterial3ExpressiveApi` in Settings).

## What Changes

- Replace the `CircularProgressIndicator` inside the shared `TempoLoadingIndicator` composable (`app/src/main/java/com/mandrecode/tempo/core/ui/components/TempoLoadingIndicator.kt`) with Material 3's Expressive `LoadingIndicator` (indeterminate, morphing-shape variant), opting into `@ExperimentalMaterial3ExpressiveApi`.
- Enlarge the indicator (48dp → 96dp) and remove the visible loading message text, so the morphing shape is the sole, prominent visual on the first-load state. The `message` string is retained as an accessibility (`contentDescription`) label rather than dropped entirely.
- No changes to the callers (`TasksContent`, `RoutinesContent`) or to the component's public signature (`message`, `modifier`) — this is a visual change inside the shared component, so both first-load states pick it up automatically.
- Update/add `@Preview` composables under `src/debug/` if needed to reflect the new visual.

## Capabilities

### New Capabilities
- `expressive-loading-indicator`: Defines that the shared first-load loading state used by Tasks and Routines lists renders Material 3 Expressive's morphing-shape `LoadingIndicator` instead of a plain circular spinner.

### Modified Capabilities
(none — no existing spec covers this component)

## Impact

- **Affected code**: `core/ui/components/TempoLoadingIndicator.kt` only; no ViewModel, domain, or data layer changes.
- **Affected screens**: Tasks first-load state, Routines (habits) first-load state — both consume the shared component.
- **Dependencies**: Uses `androidx.compose.material3.LoadingIndicator`, already available in the pinned `material3` 1.5.0-alpha24 BOM; no version bump needed. Requires opting into `@ExperimentalMaterial3ExpressiveApi`, an established pattern already used in `SettingsScreen.kt`.
- **Tests**: No behavioral/logic change; existing Compose UI tests referencing loading state (if any) should be checked for spinner-specific assertions (e.g. `CircularProgressIndicator` semantics matchers) that would need updating to match the new component.
