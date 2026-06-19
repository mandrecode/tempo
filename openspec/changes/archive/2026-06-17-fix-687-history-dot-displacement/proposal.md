## Why

GitHub issue [#687](https://github.com/mandrecode/tempo/issues/687) reports a layout glitch in the habit tracking history row: when the row is already at dot capacity and the user checks the habit, the newly checked dot appears with a horizontal displacement/jump.

The problem happens during completion-state transitions in `HabitHistoryView`, where adaptive dot fitting depends on the remaining width after the streak label, while streak text/label animations can transiently change that remaining width.

## What Changes

- Stabilize `HabitHistoryView` horizontal layout during completion toggles so full-capacity rows do not shift or reflow unexpectedly.
- Preserve existing behavior for date windowing, streak calculation semantics, and #681 right-edge alignment of the streak pill in bottom-sheet row context.
- Update and repair related debug previews to include explicit full-row before/after toggle states used as visual regression checks.
- Add regression coverage in `HabitHistoryViewTest` for constrained-width/full-capacity transitions.

Non-goals:

- Do not change completion-history persistence, parsing, or mutation logic.
- Do not change domain/data models, repository/DAO behavior, or Room schema.
- Do not redesign the habit history component; this is a layout-stability bug fix.

## Capabilities

### New Capabilities

- `habit-history-layout-stability`: Defines layout invariants for habit history dot rendering during completion-state transitions in constrained/full-capacity rows.

### Modified Capabilities

- None.

## Impact

- `app/src/main/java/com/mandrecode/tempo/features/routines/presentation/components/sections/HabitHistoryView.kt`
- `app/src/debug/java/com/mandrecode/tempo/features/routines/presentation/components/sections/HabitHistoryViewPreviews.kt`
- `app/src/debug/java/com/mandrecode/tempo/features/routines/presentation/components/HabitBottomSheetPreviews.kt` (if needed by the final preview fix)
- `app/src/androidTest/java/com/mandrecode/tempo/features/routines/presentation/components/HabitHistoryViewTest.kt`
