## Context

`TempoLoadingIndicator` (`core/ui/components/TempoLoadingIndicator.kt`) is the single shared composable rendered while `TasksContent` and `RoutinesContent` are on their first load (`isLoading = true`, no data yet). It currently renders a Material 2-style `CircularProgressIndicator` (48dp, 4dp stroke) above a message `Text`. The pinned `material3` version (1.5.0-alpha24) ships the Material 3 Expressive `LoadingIndicator` family (`LoadingIndicator`, `ContainedLoadingIndicator`), gated behind `@ExperimentalMaterial3ExpressiveApi`, which the codebase already opts into in `SettingsScreen.kt`.

## Goals / Non-Goals

**Goals:**
- Swap the spinner for Material 3 Expressive's indeterminate morphing-shape `LoadingIndicator` inside `TempoLoadingIndicator`, so both Tasks' and Routines' first-load states get it for free.
- Keep the component's public API (`message`, `modifier`) and layout (icon above message, centered) unchanged.

**Non-Goals:**
- No pull-to-refresh or in-list loading affordance changes — only the full-screen first-load state.
- No custom shape sequence (`polygons` param) — use the library defaults (`LoadingIndicatorDefaults.IndeterminateIndicatorPolygons`).
- No change to loading state logic/timing in `TasksViewModel`/`RoutinesViewModel`.

## Decisions

- **Use the plain `LoadingIndicator(modifier, color, polygons)` overload, not `ContainedLoadingIndicator`.** The current spinner has no filled container background, just sits on the screen background — the uncontained variant matches that look most closely. `ContainedLoadingIndicator` adds a colored container shape that isn't present today and isn't requested by the issue.
- **Use default `color` and `polygons`.** `LoadingIndicatorDefaults.indicatorColor` resolves to the theme's primary color, matching the existing `MaterialTheme.colorScheme.primary` used by the spinner. No custom shape sequence is warranted for a first pass.
- **Size via `Modifier.size(48.dp)`**, matching the previous spinner's footprint, so the surrounding `Column`/`Spacer` layout needs no adjustment.
- **Opt in with `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` on `TempoLoadingIndicator`**, following the existing precedent in `SettingsScreen.kt` rather than introducing a new suppression mechanism.

## Risks / Trade-offs

- **[Risk] `LoadingIndicator` is `@ExperimentalMaterial3ExpressiveApi`** and its shape/animation behavior could change in a future alpha bump. → Mitigation: scoped to one shared component; a future library update only requires touching this one file.
- **[Risk] Any Compose UI test asserting on `CircularProgressIndicator` semantics for the loading state would break.** → Mitigation: verified no such tests exist (grep of `app/src/androidTest` and `app/src/test` found no references to `TempoLoadingIndicator` or `CircularProgressIndicator`).
