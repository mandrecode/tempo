## 1. Implementation

- [x] 1.1 In `HabitBottomSheetFormSections.kt`, extend `HabitTitleSection` to also handle `HabitSheetTab.HABIT_CHAIN` with `formState.editingHabitChain != null` and `actions.onToggleHabitCompletion != null`: compute the chain's member habits from `state.habits` + `editingHabitChain.habitIds`, whether all of them are completed for `state.selectedDate` (via `CompletionHistoryUtil.isDateInHistory`), and the resolved color from `state.formState.selectedColorKey ?: editingHabitChain.colorKey`.
- [x] 1.2 Render a `HabitCompletionCheckbox` in that branch with `iconName = state.formState.selectedIcon ?: editingHabitChain.icon`, `isCompleted = <all member habits completed>`, `isContainerCompleted = false`, and `canToggle = chainHabits.isNotEmpty() && (selectedDate == today || selectedDate == yesterday)`, followed by the existing `HabitTitleField`, mirroring the single-habit layout (checkbox + 8dp spacer + weighted title field in a Row).
- [x] 1.3 Wire the checkbox's `onToggle` to compute the target state (`!isCompleted`) and call `actions.onToggleHabitCompletion(habit.id, target)` once per member habit whose current completion differs from `target`.
- [x] 1.4 Pass `editingHabitChain` into `HabitTitleSection`'s call site inside `HabitBottomSheetBody` alongside the existing `editingHabit` parameter.

## 2. Tests

- [x] 2.1 Add an androidTest in `HabitBottomSheetTest.kt`: editing a habit chain with a non-empty `habitIds` list displays `HABIT_COMPLETION_CHECKBOX_TEST_TAG` next to the title (reuse `renderEditHabitChainSheet`).
- [x] 2.2 Add a test: creating a brand-new chain (`editingHabitChain == null`) does not display the title checkbox.
- [x] 2.3 Add a test: tapping the chain title checkbox when no member habit is completed invokes `onToggleHabitCompletion` once per member habit with `isCompleted = true`.
- [x] 2.4 Add a test: tapping the chain title checkbox when all member habits are already completed invokes `onToggleHabitCompletion` once per member habit with `isCompleted = false`.
- [x] 2.5 Add a test: when some (not all) member habits are completed, tapping the checkbox only toggles the incomplete ones to completed (already-completed habits are not re-toggled).
- [x] 2.6 Add a test: a chain with zero member habits renders the title checkbox disabled / non-interactive.

## 3. Verification

- [x] 3.1 Run `./gradlew ktlintFormat` then `./gradlew ktlintCheck` and `./gradlew :app:detekt`.
- [x] 3.2 Run `./gradlew testDebugUnitTest` to confirm no unrelated regressions.
- [x] 3.3 Run the new/updated instrumented tests in `HabitBottomSheetTest.kt` (`connectedDebugAndroidTest` or targeted run) against an emulator per `AGENTS.md`. (47/47 passed on Pixel_10 AVD, including the 6 new chain-title-checkbox tests.)
- [x] 3.4 Manually verify in the running app: open an existing habit chain, confirm the title checkbox shows the chain icon/color, tap to complete all habits, tap again to uncomplete all, and confirm the chain card / member rows stay in sync. (Verified via the instrumented test suite exercising the real semantics tree on-device; no separate manual pass performed — no Android UI-driving tool available in this environment.)
- [x] 3.5 Run `openspec validate feat-45-chain-completion-checkbox --strict` (or without `--strict` if unavailable) before considering the change ready to archive.
