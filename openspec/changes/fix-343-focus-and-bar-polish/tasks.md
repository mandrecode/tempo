## 1. #343 — Habit chains open from Focus

- [x] 1.1 Add `FocusContract.RoutineEditorTarget` (`SingleHabit(habit)` / `Chain(chain)`) and replace `UiState.editingHabit: Habit?` with `routineEditor: RoutineEditorTarget?`
- [x] 1.2 Add `FocusContract.UiEvent.EditChain(chain)` and handle it in `FocusViewModel.onEditorEvent` beside `EditHabit`, both clearing `taskEditor`
- [x] 1.3 Rename `FocusHabitEditor` to `FocusRoutineEditor`, take a `RoutineEditorTarget`, and dispatch `ShowHabitBottomSheet` or `ShowHabitChainBottomSheet` into the same `RoutinesViewModel`
- [x] 1.4 Host `DeleteHabitChainConfirmDialog` in `FocusRoutineEditor` beside the existing `DeleteHabitConfirmDialog`
- [x] 1.5 Wire `onEdit = { onEvent(EditChain(entry.chain)) }` on `HabitChainCard` in `FocusContent.AgendaRow` and update `FocusScreen` to render the renamed editor
- [x] 1.6 Deleted the unused private `FocusAgendaItem.editEvent()` helper in `FocusContent.kt`

## 2. #344 — The floating bar re-centres continuously

- [x] 2.1 In `PersistentPortraitFloatingBar`, drop `Arrangement.spacedBy` from the centred `Row` and give each `AnimatedVisibility` child its own `FloatingToolbarItemSpacing` padding on the side facing the pill
- [x] 2.2 Apply the same change to `TaskActionButtons` for the sort / clear-completed gap, using `TASK_ACTIONS_BUTTON_SPACING`
- [x] 2.3 Confirmed `TASK_ACTIONS_WIDTH` still holds: the moved gap sits inside clear-completed, so both buttons shown still measure 48 + 6 + 48

## 3. #345 — The undated-tasks hand-off follows Tempo's style

- [x] 3.1 Promote `ImmersiveTextButton` out of `ImmersiveSessionView.kt` into `core/ui/components/` as `TempoLinkButton`, keeping the rounded shape, leading icon, primary tint and haptic
- [x] 3.2 Point `ImmersiveSessionView`'s "Open in Tasks" at the shared composable
- [x] 3.3 Rebuild `UndatedTasksFooter` on `TempoLinkButton` with `ic_open_in_new` and the existing `focus_undated_tasks` plural, centred rather than full-width
- [x] 3.4 Check the empty-state path (`FocusEmptyState`) renders the same control

## 4. Tests

- [x] 4.1 `FocusViewModelTest`: `EditChain` sets `routineEditor` to the chain and clears `taskEditor`; `EditHabit` still sets the habit target; `DismissEditor` clears both
- [x] 4.2 Compose UI test for `FocusContent`: tapping a chain card body emits `EditChain`; tapping the chevron emits `ToggleChainExpanded` and not `EditChain`
- [x] 4.3 Compose UI test for `FocusContent`: the undated footer is present with a count and absent at zero, and tapping it emits `UndatedTasksClicked`
- [x] 4.4 Compose UI test for the portrait floating bar: the group must settle where its motion stopped (verified to fail by 4.19dp against the unfixed code), and the resting gaps match the constants
- [x] 4.5 Updated the one test that referenced `editingHabit`; no preview did

## 5. Verification

- [x] 5.1 `./gradlew compileDebugKotlin compileDebugUnitTestKotlin`
- [x] 5.2 `./gradlew testDebugUnitTest`
- [x] 5.3 `./gradlew ktlintFormat` then `./gradlew ktlintCheck :app:detekt` (baseline must not grow)
- [x] 5.4 `./gradlew lintDebug` not needed — no string resource changed (the footer reuses `focus_undated_tasks`)
- [x] 5.5 Manual pass on device: chain opens from Focus, bar transition lands without a late shift, footer reads as a hand-off
