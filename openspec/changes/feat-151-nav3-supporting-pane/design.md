## Context

After `feat-adaptive-large-screen-ux`, sheet presentation is decided by a pure placement function (bottom vs side sheet) and the rail has hierarchy across tiers. Navigation still runs on classic `androidx.navigation` compose (`NavHost`, 4 `@Serializable` routes, `currentBackStackEntryAsState` driving the floating bar, notification triggers navigating with `popUpTo`+`saveState`). The `navigation3` artifacts (`navigation3-runtime`, `navigation3-ui`, `lifecycle-viewmodel-navigation3`, version 1.1.4) have been declared in `gradle/libs.versions.toml` and `app/build.gradle.kts` since early in the project but are unused. Editors are modal overlays owned by screen UiState (`habitForm.isVisible`, `taskForm.isVisible`), not navigation destinations.

## Goals / Non-Goals

**Goals:**

- Behavior-identical Nav3 migration: same tab save/restore semantics, notification deep-links, onboarding handoff transitions, predictive back.
- Docked, non-modal editor pane at ≥1200dp that survives configuration change and participates in back.
- Hover, Escape, and focus-order support suitable for tablets with keyboards and desktop windows.
- Keep the multiplatform trajectory in mind: back-stack keys and scene decisions stay in plain Kotlin, renderer wiring stays thin.

**Non-Goals:**

- List-detail panes, Hilt replacement, module extraction, non-Android targets, redesigns of migrated screens.

## Decisions

- **Migrate the engine before building on it (PR 1 alone).** A nav-engine swap mixed with new UX is unbisectable. PR 1 converts routes to `NavKey`s + `NavDisplay` with the existing four destinations and must pass the existing instrumented navigation tests unchanged — that suite is the migration's acceptance criterion.
  - The floating bar's `currentDestination` checks (`hasRoute<T>()`) become top-of-stack key checks; `topLevelPopUpToId` semantics map to Nav3 back-stack list operations. Notification triggers (`routinesNavigationTrigger`/`tasksNavigationTrigger` in `MainActivity`) mutate the back stack through the same single entry point.
  - Implementation: Routines, Tasks, and initial onboarding each own a saved `NavBackStack`. All three remain decorated while composed, so inactive tab entries retain saveable state and ViewModel stores. `TempoNavigator` is the only mutation entry point and switches the active decorated-entry list rendered by `NavDisplay`.
  - `rememberViewModelStoreNavEntryDecorator` propagates the Hilt activity's default factory into each entry; screens use lifecycle `viewModel()` and no longer depend on `hilt-navigation-compose`.
- **Editor-as-pane only at ≥1200dp; editors stay UiState-owned below it.** At ≥1200dp the editor form state is promoted into a back-stack entry rendered by a supporting-pane scene beside the main content (~412dp pane, content keeps its readable width). Below 1200dp nothing changes from Phase 2. The placement function becomes three-way (`BottomSheet | SideSheet | DockedPane`) so there remains exactly one decision point.
  - The unsaved-changes guard moves with the editor: back/Escape on a dirty docked pane shows the same discard confirmation.
  - Trade-off: two ownership models for editor visibility (UiState modal below 1200dp, back stack above). Accepted: promoting all editors into navigation everywhere would churn every screen contract for no benefit on phones; revisit if a future list-detail phase lands.
- **Apply the M3 breakpoint tiers consistently in PR 2.** Compact (<600dp) keeps bottom navigation and sheet-based sort/editors. Medium (600–839dp) and expanded (840–1199dp) use the collapsed rail; sort becomes an anchored menu and editors remain modal bottom/side sheets according to height and width. Large/XL (≥1200dp) uses the labeled rail, docked 412dp editor pane, and anchored sort menu.
  - At ≥1200dp Settings is a rail destination that replaces the content pane in place while the rail remains visible and selected. Below 1200dp Settings keeps the pushed full-screen behavior.
  - The content pane keeps its readable width and shifts in place when the fixed editor pane opens.
- **Custom supporting-pane scene over `SupportingPaneSceneStrategy` if the Material artifact fights the floating-pill shell.** Try `androidx.compose.material3.adaptive:adaptive-navigation3` first (it is the documented path per the official adaptive skill); fall back to a small custom `Scene` if its scaffolding imposes pane chrome that conflicts with the custom rail. Decision recorded at implementation time in this doc.
- **Escape = back, not a parallel path.** Escape key events dispatch the same dismiss-request flow as predictive back (sheet/pane/menu), so guards and animations stay single-sourced. Hover uses standard `hoverable`/indication on rail rows, list cards, and buttons — no custom pointer plumbing.
- **Remove `androidx.navigation` classic after PR 1** (it currently arrives transitively via `hilt-navigation-compose`; the `hiltViewModel()` sites move to `lifecycle-viewmodel-navigation3` retrieval). Keeping both engines resident invites drift.

## Risks / Trade-offs

- **Nav3 API maturity (1.1.x).** Scenes are the newest part of the stack; mitigation: PR 1 is engine-only and heavily covered by existing tests, and PR 2's scene is isolated behind the placement function.
- **State-restoration parity.** Classic nav's `saveState`/`restoreState` tab behavior must be reproduced exactly (users notice lost scroll positions). Add an instrumented test for tab-switch state retention before migrating if none exists.
- **`hiltViewModel()` coupling.** VM retrieval changes are mechanical but touch all 4 screens; regression risk is contained by the MVI contracts staying untouched.
- **Desktop-style windows on Android (freeform resize)** can cross 1200dp dynamically; pane ↔ side-sheet transitions must not lose editor draft state — the form state lives in the ViewModel either way, which is the safeguard.

## Migration Plan

Three PRs: (1) Nav3 engine swap, behavior-identical, soak on main; (2) three-way placement + supporting-pane scene at ≥1200dp; (3) hover/Escape/focus. Revert path per PR; PR 2/3 do not start until PR 1 has shipped in a release.

## Open Questions

- `adaptive-navigation3` CMP artifact availability for the future multiplatform track — check at implementation time; does not gate this change on Android.
