## Why

GitHub issue [#691](https://github.com/mandrecode/tempo/issues/691) reports that the habit tracking history (the dot row shown in the habit/chain bottom sheet) renders one dot per calendar day for the last 21 days, regardless of the habit's `repeatDays` mask. For habits scheduled only on certain weekdays, most dots are unfillable, so a perfectly adhered Mon/Wed/Fri habit looks like ~43% completion. The history must reflect the habit's periodicity so it represents adherence accurately.

## What Changes

- Render unscheduled days in `HabitHistoryView` with a muted/greyed-out style and exclude them from the visual completion impression.
- Keep the 21 calendar-day window so daily habits look identical to today.
- Apply the same treatment for habit chains using the chain's `repeatDays`.
- Extract a shared `isScheduledOn(date, repeatDays)` helper in `CompletionHistoryUtil` and refactor `getCurrentStreak` to use it (no behavior change to streak math).

Non-goals:

- Do not change the streak calculation behavior (already periodicity-aware).
- Do not change how `completionHistory` is persisted, parsed, or mutated.
- Do not change the mini-history rendering on `HabitCards` (single-day check, not a graph).
- Do not change the `Periodicity` enum used by tasks; habits use `repeatDays: Set<DayOfWeek>?`.
- Do not change the 21-day window length or expand it to cover N scheduled occurrences.

## Capabilities

### New Capabilities

- `habit-history-display`: Defines how the habit and habit-chain tracking history view renders scheduled and unscheduled days based on `repeatDays`.

### Modified Capabilities

- None. No existing OpenSpec specs are present.

## Impact

- UI rendering in `features/routines/presentation/components/sections/HabitHistoryView.kt`.
- Shared util `util/CompletionHistoryUtil.kt` (new helper, internal refactor of `getCurrentStreak`).
- `HabitBottomSheet.kt` chain branch verified to pass `repeatDays` to `HabitHistoryView`.
- Unit tests for `CompletionHistoryUtil` (new helper, streak regression).
- Debug `@Preview`s under `src/debug/` for daily, MWF, and weekly Mon habits.
- No data layer, Room schema, DAO, repository, or domain model changes.
