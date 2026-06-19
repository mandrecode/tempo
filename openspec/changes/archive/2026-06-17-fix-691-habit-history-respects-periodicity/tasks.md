## 1. Shared Scheduled-Day Predicate

- [x] 1.1 Add `isScheduledOn(date: LocalDate, repeatDays: Set<DayOfWeek>?): Boolean` to `util/CompletionHistoryUtil.kt` returning `true` when `repeatDays` is null or empty, otherwise `date.dayOfWeek in repeatDays`.
- [x] 1.2 Refactor `CompletionHistoryUtil.getCurrentStreak` to call `isScheduledOn` for its skip-day check, preserving the existing public signature and behavior.

## 2. Habit History Rendering

- [x] 2.1 In `features/routines/presentation/components/sections/HabitHistoryView.kt`, compute a per-date `isScheduled` flag using `CompletionHistoryUtil.isScheduledOn(date, repeatDays)` for each entry of `dateRange`.
- [x] 2.2 When `isScheduled` is `false`, render the dot with the muted/low-emphasis color (using an existing theme color such as `outlineVariant` or the existing empty-dot color at lower alpha) and treat `isCompleted` as `false` for visual purposes regardless of `completionHistory` membership.
- [x] 2.3 Keep the visible window at the last 21 calendar days, bounded below by `effectiveCreatedDate`, with no change for habits whose `repeatDays` is null or empty.
- [x] 2.4 Add an accessibility content description that distinguishes scheduled, scheduled-completed, and unscheduled days.

## 3. Habit Chain Symmetry

- [x] 3.1 Verify that `HabitBottomSheet.kt` chain branch passes the chain's `repeatDays` to `HabitHistoryView` for both the dot graph and the streak helper; correct any mismatch in the same change.

## 4. Verification

- [x] 4.1 Add unit tests in `app/src/test/java/.../util/CompletionHistoryUtilTest.kt` covering `isScheduledOn` for null, empty, single-day, and MWF masks, including weekday boundary cases.
- [x] 4.2 Add regression unit tests for `getCurrentStreak` covering daily, weekly Mon, and MWF habits to ensure the refactor preserves results.
- [x] 4.3 Add or update `@Preview` composables under `app/src/debug/.../HabitHistoryViewPreviews.kt` for daily, MWF, and weekly Mon habits in light and dark themes.
- [x] 4.4 Run `./gradlew ktlintFormat`.
- [x] 4.5 Run `./gradlew testDebugUnitTest`.
- [x] 4.6 Run `./gradlew koverVerifyDebug`.
- [x] 4.7 Run `./gradlew :app:detekt`.
- [x] 4.8 Run `openspec validate fix-691-habit-history-respects-periodicity`.
