## 1. Side sheets (PR 1)

- [ ] 1.1 Generalize `TempoModalSheet` state/transforms from vertical-only (`offsetY`, height px) to axis-aware, keeping Top/Bottom behavior identical (existing transform tests stay green).
- [ ] 1.2 Add `TempoModalSheetDirection.End`: ~412dp width, full height, start-rounded corners, end-edge drag coercion/dismiss threshold, horizontal predictive-back, Compose-drawn scrim (drop `ColorDrawable`).
- [ ] 1.3 Add pure `sheetPlacement(widthDp, heightDp)` in `core/ui` with unit tests for the rule matrix (portrait phone, landscape phone <480dp, foldable ~673dp, 840dp+, 1280dp).
- [ ] 1.4 Route task editor, habit editor, and category edit sheets through the placement rule.
- [ ] 1.5 Present sort options as an anchored `DropdownMenu` in rail layouts; keep the bottom sheet on compact.

## 2. Rail hierarchy and expanded tier (PR 2)

- [ ] 2.1 Restructure the rail into a top-start anchored column: Add (primary) → tabs → contextual-actions slot; delete the `TASK_ACTIONS_*` offset choreography in `PersistentFloatingBar`.
- [ ] 2.2 Fill the contextual slot from the Tasks floating-bar state (sort, clear completed) as secondary-styled buttons.
- [ ] 2.3 Add the expanded tier (width ≥840dp AND height ≥480dp): icon+label rows, full-row selected pill, `TempoSoloActionButton` as labeled Add; extend rail metrics + guard test for the expanded clearance.
- [ ] 2.4 Rename `adaptiveScreenContentLayout(isRailLayout:)` → `reserveRailClearance:` across Routines/Tasks/Settings (answers Copilot thread on PR #145).
- [ ] 2.5 Update `PreviewFormFactors` previews for the new rail states.

## 3. Verification

- [ ] 3.1 Unit tests: placement rule matrix, axis transforms, expanded-rail clearance guard.
- [ ] 3.2 Run `./gradlew testDebugUnitTest`, `./gradlew ktlintFormat`, `./gradlew ktlintCheck`, `./gradlew :app:detekt`.
- [ ] 3.3 Run `openspec validate feat-adaptive-large-screen-ux` (when the CLI is available).
- [ ] 3.4 Device matrix smoke (android CLI AVDs): Pixel 10 portrait+landscape, Pixel Tablet landscape+portrait, Medium Tablet — editors, sort menu, rail hierarchy, keyboard interaction with side sheet, portrait regression.
- [ ] 3.5 Validate the landscape-phone side-sheet hypothesis on device; if it feels wrong, drop the `height < 480` arm (one line + tests).
