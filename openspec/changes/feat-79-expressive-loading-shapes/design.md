## Context

`TempoLoadingIndicator` (`core/ui/components/TempoLoadingIndicator.kt`) is the single shared composable rendered while `TasksContent` and `RoutinesContent` are on their first load (`isLoading = true`, no data yet). It currently renders a Material 2-style `CircularProgressIndicator` (48dp, 4dp stroke) above a message `Text`. The pinned `material3` version (1.5.0-alpha24) ships the Material 3 Expressive `LoadingIndicator` family (`LoadingIndicator`, `ContainedLoadingIndicator`), gated behind `@ExperimentalMaterial3ExpressiveApi`, which the codebase already opts into in `SettingsScreen.kt`.

## Goals / Non-Goals

**Goals:**
- Swap the spinner for Material 3 Expressive's indeterminate morphing-shape `LoadingIndicator` inside `TempoLoadingIndicator`, so both Tasks' and Routines' first-load states get it for free.
- Keep the component's public API (`message`, `modifier`) unchanged, even though `message` is no longer rendered as visible text (see below).
- Make the shape itself the focal point of the first-load state: larger, with no supporting text underneath.

**Non-Goals:**
- No pull-to-refresh or in-list loading affordance changes — only the full-screen first-load state.
- No custom shape sequence (`polygons` param) — use the library defaults (`LoadingIndicatorDefaults.IndeterminateIndicatorPolygons`).
- No change to loading state logic/timing in `TasksViewModel`/`RoutinesViewModel`.

## Decisions

- **Use the plain `LoadingIndicator(modifier, color)` overload, not `ContainedLoadingIndicator`.** The current spinner has no filled container background, just sits on the screen background — the uncontained variant matches that look most closely. `ContainedLoadingIndicator` adds a colored container shape that isn't present today and isn't requested by the issue.
- **Pass `color = MaterialTheme.colorScheme.primary` explicitly (matching `LoadingIndicatorDefaults.indicatorColor`'s own resolution) and leave `polygons` at its default.** This mirrors the color the previous spinner used explicitly, while no custom shape sequence is warranted for a first pass.
- **Remove the message `Text` and enlarge the shape to `Modifier.size(96.dp)`** (2x the original 48dp spinner). Following user feedback during implementation, the loading message reads as redundant next to an already-prominent morphing shape on a full-screen state — the shape alone communicates "loading" clearly, and a larger indicator makes it the clear focal point rather than a small icon competing with text.
- **Keep `message` as a required parameter, but attach it as `Modifier.semantics { contentDescription = message }`** on the indicator instead of dropping it, so screen readers still announce "Loading tasks…"/"Loading habits…" — matching the existing `contentDescription`-for-non-text-visuals pattern used elsewhere in `core/ui/components` (e.g. `HabitCompletionCheckbox`, `TaskCompletionCheckbox`).
- **Opt in with `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` on `TempoLoadingIndicator`**, following the existing precedent in `SettingsScreen.kt` rather than introducing a new suppression mechanism.

## Risks / Trade-offs

- **[Risk] `LoadingIndicator` is `@ExperimentalMaterial3ExpressiveApi`** and its shape/animation behavior could change in a future alpha bump. → Mitigation: scoped to one shared component; a future library update only requires touching this one file.
- **[Risk] Any Compose UI test asserting on `CircularProgressIndicator` semantics for the loading state would break.** → Mitigation: verified no such tests exist (grep of `app/src/androidTest` and `app/src/test` found no references to `TempoLoadingIndicator` or `CircularProgressIndicator`).
- **[Risk] `TasksContentTest.showsLoadingIndicator_whenLoading` and `RoutinesContentTest.showsLoadingIndicator_whenLoading` asserted on the visible "Loading tasks…"/"Loading habits…" `Text`, which no longer exists.** → Mitigation: updated both to assert via `onNodeWithContentDescription(...)` against the `LoadingIndicator`'s semantics label instead of `onNodeWithText(...)`.
