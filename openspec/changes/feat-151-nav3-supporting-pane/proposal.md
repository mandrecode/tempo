## Why

Phase 3 of the adaptive-first work ([issue #151](https://github.com/mandrecode/tempo/issues/151), continuing [issue #142](https://github.com/mandrecode/tempo/issues/142) and `feat-adaptive-large-screen-ux`). Below 1200dp editors remain modal sheets: the list underneath is scrimmed and inert. On genuinely large windows (1200dp+: large tablets landscape, desktop windows) the canonical experience is a **persistent supporting pane** — the editor docks beside the content, both stay live, no scrim. That requires the editor to participate in the back stack (predictive back, config-change survival, state restoration), which the classic `androidx.navigation` NavHost cannot express for two simultaneous panes; Navigation 3's scene model can, and its artifacts are already declared (unused) in the version catalog. Nav3 is also the KMP-oriented successor, grounding the app's stated Web/Desktop/Multiplatform goal. Large screens also come with keyboards and mice, which the app currently ignores.

## What Changes

- **Navigation 3 migration (mechanical, PR 1).** The 4 typed routes (`Routines`, `Tasks`, `Settings`, `Onboarding`) become `NavKey`s on a `NavBackStack` rendered by `NavDisplay`; ViewModels scope via `lifecycle-viewmodel-navigation3` (already in the catalog). Zero user-visible behavior change — existing navigation behavior (tab switching with state save/restore, notification deep-links, onboarding handoff, settings transitions) is preserved and is the acceptance test.
- **Persistent supporting pane at width ≥ 1200dp (PR 2).** The task/habit editor renders as a docked, non-modal pane beside the content via a supporting-pane scene: list stays interactive, no scrim, editor survives rotation/resize in the back stack. Below that breakpoint editors remain bottom sheets, while category editing retains its top-origin sheet.
- **M3 breakpoint retune (PR 2).** The labeled rail moves to ≥1200dp, 600–1199dp uses the collapsed rail, Settings becomes a selected rail destination at ≥1200dp, and Sort uses a bottom sheet only at compact widths with an anchored menu at medium and larger widths.
- **Pointer and keyboard affordances (PR 3).** Hover indication on rail items, list rows, and buttons; Escape dismisses sheets, menus, and the supporting pane (same dismiss path as back, honoring the unsaved-changes guard); sensible focus traversal on the rail and editors.
- Non-goal: list-detail restructuring of Routines or Tasks — the pane hosts editor/creation flows only.
- Non-goal: removing Hilt, module extraction, or adding non-Android targets (later phases of the multiplatform track).
- Non-goal: onboarding or settings redesign — they migrate engines unchanged.

## Capabilities

### New Capabilities

- `large-screen-supporting-pane`: docked non-modal editor pane on ≥1200dp windows, back-stack integrated.
- `pointer-keyboard-input`: hover, Escape-to-dismiss, and focus-order behavior for large-screen input devices.

### Modified Capabilities

- `modal-sheet-placement`: placement rule gains the ≥1200dp docked-pane arm.

## Impact

- Replaces `androidx.navigation` usage in `core/ui/navigation/` (NavHost, controller extensions, notification navigation effects) with Nav3 equivalents; activates the already-declared `navigation3` artifacts and removes the classic dependency.
- Adds a supporting-pane scene strategy and extends the placement function; touches the editor sheet consumers from Phase 2.
- ViewModel acquisition sites (4 `*Screen.kt`) move from `hiltViewModel()` to Nav3-scoped retrieval.
- No domain or data changes. Three PRs, each shippable; PR 1 must land alone and soak before PR 2.
