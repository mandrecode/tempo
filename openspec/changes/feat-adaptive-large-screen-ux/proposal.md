## Why

Phase 2 of the adaptive-first work started in [issue #142](https://github.com/mandrecode/tempo/issues/142) / [PR #145](https://github.com/mandrecode/tempo/pull/145). Phase 1 made large-window layouts *correct* (no overlap, insets, readable width); it did not make them *good*:

- Task/habit editors still open as bottom sheets everywhere. On wide windows a bottom sheet is the wrong shape (a 640dp-capped strip at the bottom of a 1280dp canvas), and on landscape phones (compact height) the keyboard consumes most of it.
- The floating rail is a vertically centered icon pill with the add button below the tabs and the Tasks sort/clear buttons floating above it via offset animations. There is no visual hierarchy: the primary action (Add) does not read as primary, and contextual actions do not read as secondary.
- At tablet widths the rail stays icon-only, wasting space that could carry labels.

(No GitHub issue exists yet for this phase; link it here when filed.)

## What Changes

- **Modal side sheets.** `TempoModalSheet` gains an `End` direction (fixed ~412dp width, full height, start-rounded corners, modal scrim, horizontal drag/predictive-back). A single pure placement rule decides presentation: **side sheet when window width ≥ 840dp OR window height < 480dp; bottom sheet otherwise.** Task editor, habit editor, and category edit sheets adopt the rule; the sort sheet becomes an anchored dropdown menu in rail layouts instead of any sheet.
- **Rail hierarchy (all rail layouts, including landscape phones).** The rail becomes a top-left anchored column ordered by importance: primary **Add** button on top, navigation tabs below it, contextual secondary actions (sort, clear completed — Tasks only) below the tabs. This replaces the centered pill + offset-animated satellite buttons.
- **Expanded rail at width ≥ 840dp AND height ≥ 480dp.** Tabs render as icon+label rows (~220dp wide rail), Add becomes an extended FAB with its label. Below that tier the compact icon rail remains.
- **Rider cleanup**: `adaptiveScreenContentLayout` takes an explicit `railClearance: Dp` resolved by `floatingRailContentClearance()` (Copilot feedback on PR #145 — the hardcoded flag on Settings was intentional but read like a bug; `railClearance = 0.dp` states the intent). Note: the proposal originally also planned to remove the `ColorDrawable` in `TempoModalSheet`; on inspection it is the dialog *window's* transparent background (mandatory Android window plumbing), not the scrim — the scrim was already Compose-drawn. Dropped.
- Non-goal: persistent (non-modal) supporting panes and any Navigation 3 work — Phase 3 (`feat-nav3-supporting-pane`).
- Non-goal: `NavigationSuiteScaffold` or any departure from the floating-pill visual identity.
- Non-goal: multi-column content grids; content layout inside screens is unchanged.
- Non-goal: pointer/keyboard affordances (hover, Escape) — Phase 3.

## Capabilities

### New Capabilities

- `modal-sheet-placement`: which container (bottom sheet, side sheet, anchored menu) presents modal editors/pickers in each window size class, and how the side sheet behaves.

### Modified Capabilities

- `adaptive-window-layout`: rail anchoring, action hierarchy, and the expanded labeled-rail tier.

## Impact

- Affects `core/ui/components/TempoModalSheet*` (7 files), `core/ui/navigation/` (rail, floating bar, metrics), the 4 sheet consumers (task/habit/category/sort), and Routines/Tasks/Settings scaffolds (param rename).
- Removes the `TASK_ACTIONS_*` offset-animation constants in `PersistentFloatingBar`.
- No domain, data, persistence, or navigation-graph changes. No new dependencies.
- Ships as two PRs off this change: (1) side sheets + placement rule, (2) rail hierarchy + expanded tier. Each independently shippable and revertible.
