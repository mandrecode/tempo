## Context

`HabitHistoryView` (`features/routines/presentation/components/sections/HabitHistoryView.kt:111-189`) renders a row of dots representing the last 21 calendar days. Each dot is filled when the date is in the habit's `completionHistory` set. The view ignores `Habit.repeatDays` (a `Set<DayOfWeek>?` mask defining which weekdays the habit is scheduled), so for habits scheduled on a subset of weekdays the row contains many empty dots that the user could never have filled.

The streak label rendered alongside the dot row already honors periodicity: `CompletionHistoryUtil.getCurrentStreak()` (`util/CompletionHistoryUtil.kt:113-162`) skips unscheduled days when walking back. The habit chain bottom sheet branch passes `repeatDays` to the same view (`HabitBottomSheet.kt:970-975`).

There is no `Periodicity` enum on habits — periodicity is expressed exclusively via `repeatDays`, where `null` or empty means "every day".

## Goals / Non-Goals

**Goals:**

- Make the dot graph reflect the habit's `repeatDays` so adherence is read correctly.
- Keep the existing 21 calendar-day window so daily habits look unchanged.
- Apply the same treatment to habits and habit chains.
- Share the periodicity check between dot rendering and streak math.

**Non-Goals:**

- Do not change the streak math or its result for any habit.
- Do not change `completionHistory` parsing or persistence (`CompletionHistoryUtil.updateCompletionHistoryForDate`, repository writes, DAO).
- Do not change the mini-history check on `HabitCards`.
- Do not extend the window to "last 21 scheduled occurrences" (would change the look for daily habits; see Decisions §2).
- Do not introduce a new `Periodicity` enum on habits.

## Decisions

1. **Render unscheduled days muted; do not hide them.**

   Two options were considered: (a) drop unscheduled dates from `dateRange` so only scheduled days render, or (b) keep all 21 calendar days and render unscheduled days with a muted color and `isCompleted = false` for visual purposes. Option (b) is chosen because it preserves a continuous calendar rhythm (no "gaps that look like missed days"), and because the user explicitly preferred the muted style. Daily habits then look identical to today.

2. **Add a single shared predicate, not a date-walker.**

   We will add `CompletionHistoryUtil.isScheduledOn(date: LocalDate, repeatDays: Set<DayOfWeek>?): Boolean` returning `true` if `repeatDays` is null/empty or `date.dayOfWeek in repeatDays`. The dot renderer uses this to pick the styling; `getCurrentStreak` is refactored to use the same predicate so the "is this day in scope" definition lives in one place. A more complex `lastNScheduledDates` walker is unnecessary because we keep the calendar-day window per Decision §1.

3. **No new color tokens.**

   The muted dot uses an existing low-emphasis color from the theme (e.g. `MaterialTheme.colorScheme.outlineVariant` or the existing empty-dot color at lower alpha — to be selected by reading the current theme usage in `HabitHistoryView`). Adding a new design token is out of scope for a bug fix.

4. **Chain symmetry through verification, not refactor.**

   `HabitBottomSheet.kt` already calls `HabitHistoryView` with the chain's `repeatDays` for the dot graph (line 970–975). The fix verifies this rather than restructuring the call sites. If the chain branch turns out to drop `repeatDays` for the streak helper, that is corrected in the same PR.

5. **No data, schema, or domain model changes.**

   The fix is purely presentational plus a util refactor. No Room migration, no DAO change, no use case change. This keeps the scope tight and avoids touching the periodic-task scheduling code paths.

## Risks / Trade-offs

- Muted dots may still be visually parsed as "missed" by users not familiar with the convention → Use a clearly low-emphasis color and provide an `a11y` content description that distinguishes scheduled vs unscheduled days. A future iteration may add a legend.
- Refactoring `getCurrentStreak` to call the new predicate touches code with existing tests → Keep the public signature unchanged and run the existing `CompletionHistoryUtil` tests as regression coverage; add tests for the new helper.
- Themes that do not provide `outlineVariant` at sufficient contrast could make muted dots invisible → Verify in dark and light previews under `src/debug/`.
