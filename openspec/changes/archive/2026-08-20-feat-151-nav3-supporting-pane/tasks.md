## 1. Navigation 3 migration (PR 1 — lands and soaks alone)

- [x] 1.1 Add an instrumented tab-switch state-retention test on the current engine if none exists (migration acceptance baseline).
- [x] 1.2 Convert the 4 routes to `NavKey`s on a `NavBackStack` rendered by `NavDisplay`; reproduce tab save/restore, onboarding handoff, and settings transitions.
- [x] 1.3 Re-wire notification triggers and the floating bar's current-destination checks to back-stack operations through one entry point.
- [x] 1.4 Move ViewModel retrieval to `lifecycle-viewmodel-navigation3`; drop the classic `androidx.navigation`/`hilt-navigation-compose` dependency.
- [x] 1.5 Full regression: existing navigation instrumented tests unchanged and green; deep-link smoke from real notifications.

## 2. Supporting pane (PR 2)

- [x] 2.1 Extend `sheetPlacement` to dock editors at ≥1200dp while retaining bottom sheets below, with unit tests.
- [x] 2.2 Implement the supporting-pane scene (Material `adaptive-navigation3` strategy, or custom scene per design fallback) hosting the editor beside content.
- [x] 2.3 Promote editor visibility into the back stack at ≥1200dp; keep UiState modal ownership below; verify draft survives breakpoint crossings and rotation.
- [x] 2.4 Wire back/predictive-back and the unsaved-changes guard for the docked pane.
- [x] 2.5 Retune rail tiers: collapsed rail at 600–1199dp and labeled rail at ≥1200dp; keep Settings as a selected Nav3 destination on both rails while preserving the compact pushed transition.
- [x] 2.6 Present Sort as a bottom sheet at compact widths and an anchored M3 Expressive menu at medium and larger widths.

## 3. Pointer and keyboard (PR 3)

- [ ] 3.1 Hover indication on rail items, cards, and buttons.
- [ ] 3.2 Escape-to-dismiss routed through the back/dismiss path for menus, sheets, and panes.
- [ ] 3.3 Focus order: rail (add → tabs → actions) → content → editor; fix any traps.

## 4. Verification

- [x] 4.1 `./gradlew testDebugUnitTest`, `ktlintFormat`, `ktlintCheck`, `:app:detekt` per PR.
- [x] 4.2 `openspec validate feat-151-nav3-supporting-pane` (when the CLI is available).
- [ ] 4.3 Device matrix: Pixel 10 (portrait/landscape), Pixel Tablet, Medium Tablet, resizable/desktop AVD ≥1200dp; hardware keyboard + mouse attached for hover/Escape/focus checks.
- [x] 4.4 `:app:connectedDebugAndroidTest` on the Pixel 10 AVD after PR 1.
- [x] 4.5 `:app:connectedDebugAndroidTest` on the Pixel 10 AVD after PR 2.
