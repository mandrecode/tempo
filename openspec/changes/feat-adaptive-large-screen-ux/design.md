## Context

Phase 1 (fix-142) established: `WindowSizeClass` via `currentWindowAdaptiveInfo()` (sole call site `FloatingNavigationLayout.kt`), derived rail metrics with a guard test (`NavigationComponents.kt`), 840dp readable width, safeDrawing insets, and window-based sheet sizing with a 640dp cap. `TempoModalSheet` is already direction-abstracted (`TempoModalSheetDirection.Top/Bottom` with per-direction `alignment`/`shape`/`hiddenOffset`/drag transforms in `TempoModalSheetTransforms.kt`). The rail today is a vertically centered pill: tabs, then Add below, with Tasks sort/clear buttons positioned above it through `TASK_ACTIONS_*` offset animations in `PersistentFloatingBar.kt`.

## Goals / Non-Goals

**Goals:**

- Editors present as modal side sheets on wide or height-compact windows; bottom sheets remain on portrait phones.
- One pure, unit-tested placement function decides sheet presentation; feature sheets never read window classes themselves.
- Rail communicates hierarchy: primary Add on top, tabs, then contextual secondary actions; top-left anchored; labeled at the expanded tier.
- Keep the floating-pill identity and the modal (scrim + unsaved-changes guard) editing model.

**Non-Goals:**

- Navigation 3, persistent supporting panes, hover/keyboard input (Phase 3).
- Redesigning sheet content, `NavigationSuiteScaffold`, multi-column grids.

## Decisions

- **`End` as a third `TempoModalSheetDirection`, not a new component.** The transforms file is a per-direction `when` over alignment, shape, hidden offset, drag coercion, and dismiss threshold; `End` adds a horizontal axis case (`offsetX`, `widthIn(412.dp)`, full height, start-rounded corners). A parallel SideSheet component would fork state, gestures, and the unsaved-changes guard. The existing axis-specific fields (`offsetY`, `screenHeightPx`) generalize to axis-neutral names as part of this change.
  - IME note: the side sheet is full-height, so `imePadding` applies to its content column rather than displacing the sheet — verify the keyboard-sync behavior that motivated the custom sheet (fix-89) still holds.

- **Placement rule as a pure function** `sheetPlacement(widthDp, heightDp): SheetPlacement` (`BottomSheet` | `SideSheet`) in `core/ui`, unit-tested against the device matrix, mirroring the fix-142 derived-metrics + guard-test pattern. Breakpoints: side when `width ≥ 840` **or** `height < 480`. Rationale: ≥840dp is M3's expanded threshold (two-column feel the issue asks for); height-compact landscape phones are where bottom sheets and the keyboard collide — the full-height side panel is strictly better there.

- **Sort stays a sheet everywhere** (design review): an anchored-menu variant was built and reverted — the sheet keeps one presentation model across the app and follows the same placement rule as the editors. The rail also stays visible beneath modal sheet scrims instead of hiding (review feedback: the disappearing rail read as a glitch).

- **Expanded rail is the screen's command sidebar** (design review): screen title at the rail's top, labeled sort/clear actions, labeled Settings pinned at the bottom, and the screen top bar collapses to a status-bar inset so content reclaims the vertical space.

- **Rail = one top-left anchored column ordered by importance** (per design review): `[Add primary] → [tabs pill] → [contextual actions]`, in *all* rail layouts. The Add button is the screen's primary action and reads first; sort/clear are secondary and sit below the navigation. This deletes the centered-anchor + `TASK_ACTIONS_*` offset choreography — contextual actions become a plain slot in the rail column filled by the current screen's floating-bar state, so Routines/Settings gain no rail knowledge.

- **Expanded tier keyed on `width ≥ 840 AND height ≥ 480`.** Labels spend vertical space; landscape phones (height < 480) keep the compact icon rail. Rail metrics extend the existing single source of truth: expanded rail width (~220dp) derives content clearance for that tier exactly as `FloatingRailContentStartPadding` does today, guarded by the same test style. Selected-tab treatment generalizes the current `secondaryContainer` circle to a full-width row pill; Add reuses `TempoSoloActionButton` (already an ExtendedFAB).

- **Rider renames**: `adaptiveScreenContentLayout(isRailLayout:)` → `reserveRailClearance:` (call sites: Routines/Tasks pass the rail flag, Settings passes `false` because `PersistentFloatingBar` never renders on Settings); `ColorDrawable` scrim → Compose `Box` scrim.

## Risks / Trade-offs

- **Two axes in one sheet state machine.** Generalizing offsetY→axis offset touches drag gestures and predictive back; mitigated by the existing transform unit tests (fix-89 added some) plus new placement/axis tests, and by shipping sheets as their own PR.
- **Landscape-phone side sheet is a UX hypothesis** (the `height < 480` arm). It is one line in the placement function; validate on device during verification and drop the arm if it feels wrong — the width arm alone still delivers the tablet goal.
- **Rail re-anchoring changes muscle memory on landscape phones** (tabs move up). Accepted: hierarchy consistency across tiers is worth it, and phase-1 already moved the rail.

## Migration Plan

Two PRs: (1) sheet axis + placement rule + sort menu; (2) rail hierarchy + expanded tier + renames. Each is a clean revert. No data changes.

## Open Questions

- Does the expanded rail collapse to icons while a side sheet is open on 1280dp screens? Current answer: no (modal scrim makes overlap moot); revisit with the Phase 3 persistent pane.
