## Context

`HabitBottomSheetFormSections.kt`'s `HabitTitleSection` currently branches only on `HabitSheetTab.HABIT` to decide whether to show a `HabitCompletionCheckbox` next to the title field. The `HabitSheetTab.HABIT_CHAIN` case falls through to the plain `HabitTitleField`. A chain has no first-class "completed" flag of its own — every existing surface (`HabitChainCard`, `ToggleHabitCompletionUseCase.rescheduleCompletedChains`) derives chain completion by checking that every member habit's `completionHistory` contains the selected date.

The single-habit checkbox already receives an `onToggleHabitCompletion: ((habitId, isCompleted) -> Unit)?` action that is fully wired to `RoutinesViewModel.toggleHabitCompletion`, which itself calls `ToggleHabitCompletionUseCase` (handles reminder rescheduling, chain-collapse-on-complete, etc.) per habit.

## Goals / Non-Goals

**Goals:**
- Show a chain-identity completion checkbox next to the chain title, visually consistent with the single-habit case.
- Bulk toggle all member habits' completion when tapped.
- Reuse the existing per-habit toggle path so all existing side effects (reminder rescheduling, chain auto-collapse, editing-state refresh) keep working unchanged.

**Non-Goals:**
- No new "chain completion" concept in the domain model or database — completion stays derived from member habits, as it is everywhere else in the app.
- No atomic/transactional bulk-toggle use case. Each member habit toggle goes through the existing single-habit path independently, exactly as if the user tapped each member checkbox in the chain list one at a time.
- No change to the chain list toggle UI further down the sheet (`HabitMultiSelector` / `SelectedHabitItem`) — it keeps working as-is alongside the new title checkbox.

## Decisions

- **Client-side bulk toggle over a new ViewModel action**: Compute the chain's "all completed" state and iterate `editingHabitChain.habitIds` inside the Composable, calling the existing `actions.onToggleHabitCompletion(habitId, target)` once per habit whose current state differs from the target. Alternative considered: add a `RoutinesViewModel.toggleHabitChainCompletion(chainId, isCompleted)` action plus a new `UiEvent`/use-case call. Rejected because the per-habit path already performs everything a bulk toggle needs (persistence, reminder rescheduling, chain-collapse, editing-state refresh via `refreshEditingHabitStateIfNeeded`), and a chain rarely holds more than a handful of habits, so looping client-side is simple and keeps the change UI-only.
- **"All completed" as the checked state**: Matches `HabitChainCard.allCompleted` exactly (`chainHabits.isNotEmpty() && chainHabits.all { isDateInHistory(...) }`), so the bottom-sheet checkbox and the chain card never disagree about whether a chain looks "done" for the day.
- **Toggle target is the inverse of "all completed"**: Tapping when not all-completed marks every member habit completed; tapping when all-completed marks every member habit incomplete. This mirrors a single habit's own checkbox semantics (tap flips exactly one boolean) applied at chain granularity.
- **Disabled when chain is empty or date is out of the toggle window**: `canToggle = chainHabits.isNotEmpty() && (selectedDate == today || selectedDate == yesterday)`, matching the existing single-habit `canToggle` rule plus the extra empty-chain guard (an empty chain has nothing to toggle, notably during chain creation before habits are added).
- **Icon/color resolution mirrors the single-habit branch**: `iconName = state.formState.selectedIcon ?: editingHabitChain.icon` and color resolved from `state.formState.selectedColorKey ?: editingHabitChain.colorKey`, so in-flight edits in the sheet (before saving) are reflected immediately, same as habits.

## Risks / Trade-offs

- [Risk] Looping N sequential `onToggleHabitCompletion` calls each launches its own coroutine in the ViewModel — for a chain with many habits this is N independent writes/reschedules rather than one batch operation. → Mitigation: chain size is already capped by `ValidationUtils.validateHabitChainSize`, and this matches the existing manual "tap every row" cost the user could already incur; no new failure mode is introduced, each toggle is independently retryable/idempotent.
- [Risk] Partial failure: if one member habit's toggle throws, the others still proceed (each call is its own `viewModelScope.launch` with its own try/catch in `toggleHabitCompletion`), so the chain could end up partially toggled. → Mitigation: this is identical to a user manually tapping several member checkboxes in sequence today; no regression, and existing per-habit error snackbars surface any failure.
