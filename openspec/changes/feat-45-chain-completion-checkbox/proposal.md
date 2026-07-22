## Why

In the habit bottom sheet, editing a single habit shows a completion checkbox with the habit's icon right next to the title, letting the user mark it done without leaving the sheet. Editing a habit chain shows no equivalent affordance next to the chain title — the only way to mark progress is to scroll down and toggle each member habit individually in the chain's habit list. This is inconsistent and slower for the common case of completing (or undoing) an entire chain at once. [GitHub issue #45](https://github.com/mandrecode/tempo/issues/45).

## What Changes

- When editing a Habit Chain in the bottom sheet, render a `HabitCompletionCheckbox` next to the title field, mirroring the layout already used for single habits.
- The checkbox uses the chain's icon and resolved color (falling back to the form's in-progress icon/color selection while editing, same as the single-habit case).
- The checkbox's checked state reflects whether **all** member habits are completed for the selected date (same "all completed" rule already used by `HabitChainCard`).
- Tapping the checkbox toggles completion for **all** member habits to the opposite of the current state (check all / uncheck all), reusing the existing per-habit `onToggleHabitCompletion` callback for each habit that needs to change — no new ViewModel action or use case.
- The checkbox is disabled (non-interactive) when the chain has no member habits (e.g. mid-creation) or when the selected date is outside the existing toggle window (today/yesterday only), matching current single-habit rules.

## Capabilities

### New Capabilities
- `habit-chain-sheet-completion`: Bottom-sheet completion checkbox for habit chains that shows chain identity (icon/color) and bulk-toggles all member habits' completion for the selected date.

### Modified Capabilities
(none — no existing spec covers this behavior)

## Impact

- `app/src/main/java/com/mandrecode/tempo/features/routines/presentation/components/HabitBottomSheetFormSections.kt` (`HabitTitleSection`): extend the checkbox branch to cover `HabitSheetTab.HABIT_CHAIN` with `editingHabitChain != null`.
- No changes to domain, data, or ViewModel layers — the existing `onToggleHabitCompletion(habitId, isCompleted)` callback is invoked once per member habit that needs to flip state.
- No new strings required (reuses existing `mark_as_completed` / `mark_as_not_completed` content descriptions).
