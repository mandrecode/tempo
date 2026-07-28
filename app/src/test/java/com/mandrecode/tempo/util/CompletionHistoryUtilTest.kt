package com.mandrecode.tempo.util

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.VacationPeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.junit.Test

class CompletionHistoryUtilTest {
    @Test
    fun `updateCompletionHistory adds date when completed`() {
        val currentHistory = ""
        val date = LocalDate(2024, 1, 15)

        val result =
            CompletionHistoryUtil.updateCompletionHistoryForDate(
                currentHistory,
                date,
                true,
            )

        assertThat(result).isEqualTo("2024-01-15")
    }

    @Test
    fun `updateCompletionHistory adds multiple dates`() {
        val currentHistory = "2024-01-15"
        val date = LocalDate(2024, 1, 16)

        val result =
            CompletionHistoryUtil.updateCompletionHistoryForDate(
                currentHistory,
                date,
                true,
            )

        assertThat(result).isEqualTo("2024-01-15,2024-01-16")
    }

    @Test
    fun `updateCompletionHistory removes date when not completed`() {
        val currentHistory = "2024-01-15,2024-01-16,2024-01-17"
        val date = LocalDate(2024, 1, 16)

        val result =
            CompletionHistoryUtil.updateCompletionHistoryForDate(
                currentHistory,
                date,
                false,
            )

        assertThat(result).isEqualTo("2024-01-15,2024-01-17")
    }

    @Test
    fun `updateCompletionHistory maintains sorted order`() {
        val currentHistory = "2024-01-15,2024-01-20"
        val date = LocalDate(2024, 1, 17)

        val result =
            CompletionHistoryUtil.updateCompletionHistoryForDate(
                currentHistory,
                date,
                true,
            )

        assertThat(result).isEqualTo("2024-01-15,2024-01-17,2024-01-20")
    }

    @Test
    fun `updateCompletionHistory handles duplicates`() {
        val currentHistory = "2024-01-15,2024-01-16"
        val date = LocalDate(2024, 1, 15)

        val result =
            CompletionHistoryUtil.updateCompletionHistoryForDate(
                currentHistory,
                date,
                true,
            )

        // Should not add duplicate
        assertThat(result).isEqualTo("2024-01-15,2024-01-16")
    }

    @Test
    fun `getCurrentStreak returns zero for empty history`() {
        val result = CompletionHistoryUtil.getCurrentStreak("")
        assertThat(result).isEqualTo(0)
    }

    @Test
    fun `getCurrentStreak counts consecutive days including today`() {
        val today = LocalDate(2026, 2, 24)
        val yesterday = LocalDate(2026, 2, 23)
        val twoDaysAgo = LocalDate(2026, 2, 22)

        val history = "$twoDaysAgo,$yesterday,$today"
        val result = CompletionHistoryUtil.getCurrentStreak(history, today)

        assertThat(result).isEqualTo(3)
    }

    @Test
    fun `getCurrentStreak stops at first gap`() {
        val today = LocalDate(2026, 2, 24)
        val yesterday = LocalDate(2026, 2, 23)
        val threeDaysAgo = LocalDate(2026, 2, 21)

        // There's a gap at -2 days
        val history = "$threeDaysAgo,$yesterday,$today"
        val result = CompletionHistoryUtil.getCurrentStreak(history, today)

        assertThat(result).isEqualTo(2) // Only counts today and yesterday
    }

    @Test
    fun `getCurrentStreak returns zero if today not completed`() {
        val today = LocalDate(2026, 2, 24)
        val yesterday = LocalDate(2026, 2, 23)

        val history = "$yesterday"
        val result = CompletionHistoryUtil.getCurrentStreak(history, today)

        assertThat(result).isEqualTo(0) // Today is not completed, so streak is 0
    }

    @Test
    fun `getCurrentStreak skips days not planned`() {
        val today = LocalDate(2026, 2, 24) // Tuesday
        val yesterday = LocalDate(2026, 2, 23) // Monday
        val friday = LocalDate(2026, 2, 20)

        // The habit is planned only for Mon and Fri
        val repeatDays =
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.FRIDAY,
            )

        // Completed on Mon and Fri. Today is Tue (not planned).
        val history = "$friday,$yesterday"

        val result = CompletionHistoryUtil.getCurrentStreak(history, today, repeatDays)

        // The streak shouldn't break because Tue, Sun, Sat were not planned.
        // It should count Mon and Fri -> 2.
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun `getCurrentStreak breaks if planned day is missed`() {
        val today = LocalDate(2026, 2, 24) // Tuesday
        val monday = LocalDate(2026, 2, 23)
        val friday = LocalDate(2026, 2, 20)

        // The habit is planned for Mon, Fri, Sat
        val repeatDays =
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
            )

        // Completed on Mon and Fri, but missed Sat. Today is Tue.
        val history = "$friday,$monday"

        val result = CompletionHistoryUtil.getCurrentStreak(history, today, repeatDays)

        // Breaks because Sat was missed. Only Mon counts. -> 1.
        // Wait, today is Tue (not planned). It checks Tue -> skips.
        // Checks Mon -> completed (streak = 1).
        // Checks Sun -> not planned -> skips.
        // Checks Sat -> planned but missed -> breaks!
        // So streak should be 1.
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun `getCurrentStreak ignores completions on unplanned days`() {
        val today = LocalDate(2026, 2, 24) // Tuesday
        val monday = LocalDate(2026, 2, 23)
        val sunday = LocalDate(2026, 2, 22) // Not planned
        val friday = LocalDate(2026, 2, 20)

        // Planned for Mon and Fri only
        val repeatDays =
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.FRIDAY,
            )

        // Completed on Mon, Sun (unplanned), and Fri
        val history = "$friday,$sunday,$monday"

        val result = CompletionHistoryUtil.getCurrentStreak(history, today, repeatDays)

        // Sun completion should be ignored (not counted).
        // Streak: Mon (1) + Fri (2) = 2.
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun `getCurrentStreak counts MWF habit when all scheduled days completed`() {
        val today = LocalDate(2026, 2, 27) // Friday
        val wednesday = LocalDate(2026, 2, 25)
        val monday = LocalDate(2026, 2, 23)
        val previousFriday = LocalDate(2026, 2, 20)

        // MWF schedule
        val repeatDays =
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY,
            )

        // Completed today (Fri), Wed, Mon, prev Fri. Tue/Thu/Sat/Sun aren't scheduled.
        val history = "$previousFriday,$monday,$wednesday,$today"

        val result = CompletionHistoryUtil.getCurrentStreak(history, today, repeatDays)

        // All scheduled days completed -> streak = 4.
        assertThat(result).isEqualTo(4)
    }

    @Test
    fun `getCurrentStreak breaks MWF habit when a scheduled day is missed`() {
        val today = LocalDate(2026, 2, 27) // Friday
        val wednesday = LocalDate(2026, 2, 25)
        // Mon (2/23) intentionally missed
        val previousFriday = LocalDate(2026, 2, 20)

        val repeatDays =
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY,
            )

        // Completed today (Fri) and Wed, but skipped Mon. Prev Fri also completed.
        val history = "$previousFriday,$wednesday,$today"

        val result = CompletionHistoryUtil.getCurrentStreak(history, today, repeatDays)

        // Walking back from today: Fri done (1), Thu skip, Wed done (2),
        // Tue skip, Mon scheduled but missed -> break. Streak = 2.
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun `isDateInHistory returns true for existing date`() {
        val history = "2024-01-01,2024-01-02,2024-01-03"
        assertThat(CompletionHistoryUtil.isDateInHistory(history, "2024-01-02")).isTrue()
    }

    @Test
    fun `isDateInHistory returns true for start date`() {
        val history = "2024-01-01,2024-01-02"
        assertThat(CompletionHistoryUtil.isDateInHistory(history, "2024-01-01")).isTrue()
    }

    @Test
    fun `isDateInHistory returns true for end date`() {
        val history = "2024-01-01,2024-01-02"
        assertThat(CompletionHistoryUtil.isDateInHistory(history, "2024-01-02")).isTrue()
    }

    @Test
    fun `isDateInHistory returns true for single date`() {
        val history = "2024-01-01"
        assertThat(CompletionHistoryUtil.isDateInHistory(history, "2024-01-01")).isTrue()
    }

    @Test
    fun `isDateInHistory returns false for missing date`() {
        val history = "2024-01-01,2024-01-02"
        assertThat(CompletionHistoryUtil.isDateInHistory(history, "2024-01-03")).isFalse()
    }

    @Test
    fun `isDateInHistory returns false for partial match`() {
        val history = "2024-01-11,2024-01-12"
        assertThat(CompletionHistoryUtil.isDateInHistory(history, "2024-01-1")).isFalse()
    }

    @Test
    fun `isDateInHistory returns false for empty history`() {
        assertThat(CompletionHistoryUtil.isDateInHistory("", "2024-01-01")).isFalse()
    }

    @Test
    fun `isDateInHistory handles suffix match correctly`() {
        val history = "2024-01-01"
        assertThat(CompletionHistoryUtil.isDateInHistory(history, "01-01")).isFalse()
    }

    // region isScheduledOn

    @Test
    fun `isScheduledOn returns true for null repeatDays`() {
        // 2026-02-23 is a Monday
        val date = LocalDate(2026, 2, 23)
        assertThat(CompletionHistoryUtil.isScheduledOn(date, null)).isTrue()
    }

    @Test
    fun `isScheduledOn returns true for empty repeatDays`() {
        val date = LocalDate(2026, 2, 23) // Monday
        assertThat(CompletionHistoryUtil.isScheduledOn(date, emptySet())).isTrue()
    }

    @Test
    fun `isScheduledOn returns true when day is in mask`() {
        val monday = LocalDate(2026, 2, 23)
        val repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        assertThat(CompletionHistoryUtil.isScheduledOn(monday, repeatDays)).isTrue()
    }

    @Test
    fun `isScheduledOn returns false when day is not in mask`() {
        val tuesday = LocalDate(2026, 2, 24)
        val repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        assertThat(CompletionHistoryUtil.isScheduledOn(tuesday, repeatDays)).isFalse()
    }

    @Test
    fun `isScheduledOn covers each weekday correctly`() {
        // 2026-02-23 (Mon) through 2026-03-01 (Sun)
        val monday = LocalDate(2026, 2, 23)
        val dates = (0..6).map { monday.plus(it, DateTimeUnit.DAY) }
        val mondayOnly = setOf(DayOfWeek.MONDAY)
        val results = dates.map { CompletionHistoryUtil.isScheduledOn(it, mondayOnly) }
        assertThat(results).containsExactly(true, false, false, false, false, false, false).inOrder()
    }

    // endregion

    // region getCurrentStreak regression with new shared predicate

    @Test
    fun `getCurrentStreak handles daily habit (null repeatDays)`() {
        val today = LocalDate(2026, 2, 24)
        val yesterday = LocalDate(2026, 2, 23)
        val history = "$yesterday,$today"
        val result = CompletionHistoryUtil.getCurrentStreak(history, today, repeatDays = null)
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun `getCurrentStreak handles weekly Monday habit`() {
        // 2026-02-23 Mon, 2026-02-16 Mon, 2026-02-09 Mon
        val today = LocalDate(2026, 2, 23)
        val twoWeeksAgo = LocalDate(2026, 2, 9)
        val oneWeekAgo = LocalDate(2026, 2, 16)
        val repeatDays = setOf(DayOfWeek.MONDAY)
        val history = "$twoWeeksAgo,$oneWeekAgo,$today"
        val result = CompletionHistoryUtil.getCurrentStreak(history, today, repeatDays)
        assertThat(result).isEqualTo(3)
    }

    @Test
    fun `getCurrentStreak with empty repeatDays behaves like daily`() {
        val today = LocalDate(2026, 2, 24)
        val yesterday = LocalDate(2026, 2, 23)
        val history = "$yesterday,$today"
        val result = CompletionHistoryUtil.getCurrentStreak(history, today, repeatDays = emptySet())
        assertThat(result).isEqualTo(2)
    }

    // endregion

    // region vacation mode

    @Test
    fun `isPausedOn only matches dates inside a period`() {
        val periods = listOf(VacationPeriod(LocalDate(2026, 3, 10), LocalDate(2026, 3, 14)))

        assertThat(CompletionHistoryUtil.isPausedOn(LocalDate(2026, 3, 12), periods)).isTrue()
        assertThat(CompletionHistoryUtil.isPausedOn(LocalDate(2026, 3, 15), periods)).isFalse()
        assertThat(CompletionHistoryUtil.isPausedOn(LocalDate(2026, 3, 12), emptyList())).isFalse()
    }

    @Test
    fun `isRequiredOn is false for paused and for unscheduled days`() {
        val monday = LocalDate(2026, 3, 9)
        val tuesday = LocalDate(2026, 3, 10)
        val mondaysOnly = setOf(DayOfWeek.MONDAY)
        val periods = listOf(VacationPeriod(monday, monday))

        // Scheduled but paused.
        assertThat(CompletionHistoryUtil.isRequiredOn(monday, mondaysOnly, periods)).isFalse()
        // Not scheduled.
        assertThat(CompletionHistoryUtil.isRequiredOn(tuesday, mondaysOnly, emptyList())).isFalse()
        // Scheduled and not paused.
        assertThat(CompletionHistoryUtil.isRequiredOn(monday, mondaysOnly, emptyList())).isTrue()
    }

    @Test
    fun `getCurrentStreak resumes across a week away`() {
        // Completed daily 2026-03-01..2026-03-05, paused 03-06..03-12, completed again on 03-13.
        val history =
            (1..5).joinToString(",") { LocalDate(2026, 3, it).toString() } + ",2026-03-13"
        val periods = listOf(VacationPeriod(LocalDate(2026, 3, 6), LocalDate(2026, 3, 12)))

        val result =
            CompletionHistoryUtil.getCurrentStreak(
                completionHistory = history,
                today = LocalDate(2026, 3, 13),
                vacationPeriods = periods,
            )

        // Five days before the pause plus the first day back; the seven paused days are skipped.
        assertThat(result).isEqualTo(6)
    }

    @Test
    fun `getCurrentStreak reads unchanged during an active pause`() {
        val history = (1..5).joinToString(",") { LocalDate(2026, 3, it).toString() }
        val periods = listOf(VacationPeriod(LocalDate(2026, 3, 6)))

        val result =
            CompletionHistoryUtil.getCurrentStreak(
                completionHistory = history,
                today = LocalDate(2026, 3, 9),
                vacationPeriods = periods,
            )

        assertThat(result).isEqualTo(5)
    }

    @Test
    fun `getCurrentStreak counts a completion recorded while paused`() {
        val history = "2026-03-01,2026-03-02,2026-03-03"
        val periods = listOf(VacationPeriod(LocalDate(2026, 3, 2)))

        val result =
            CompletionHistoryUtil.getCurrentStreak(
                completionHistory = history,
                today = LocalDate(2026, 3, 3),
                vacationPeriods = periods,
            )

        // 03-02 and 03-03 are paused but were completed, so they still count.
        assertThat(result).isEqualTo(3)
    }

    @Test
    fun `getCurrentStreak still breaks on a missed day after the pause`() {
        // Paused 03-06..03-08, back on 03-09 but 03-10 was missed.
        val history = "2026-03-04,2026-03-05,2026-03-09"
        val periods = listOf(VacationPeriod(LocalDate(2026, 3, 6), LocalDate(2026, 3, 8)))

        val result =
            CompletionHistoryUtil.getCurrentStreak(
                completionHistory = history,
                today = LocalDate(2026, 3, 11),
                vacationPeriods = periods,
            )

        assertThat(result).isEqualTo(0)
    }

    @Test
    fun `getCurrentStreak skips paused days that are also unplanned`() {
        // Mondays-only habit: 2026-03-02, 03-09, 03-16 are Mondays.
        val history = "2026-03-02,2026-03-16"
        val periods = listOf(VacationPeriod(LocalDate(2026, 3, 7), LocalDate(2026, 3, 12)))

        val result =
            CompletionHistoryUtil.getCurrentStreak(
                completionHistory = history,
                today = LocalDate(2026, 3, 16),
                repeatDays = setOf(DayOfWeek.MONDAY),
                vacationPeriods = periods,
            )

        // The only planned day inside the pause (03-09) is skipped, so 03-02 still counts.
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun `getCurrentStreak without vacation periods is unchanged`() {
        val history = "2026-03-01,2026-03-02"

        val withDefault =
            CompletionHistoryUtil.getCurrentStreak(history, LocalDate(2026, 3, 2))
        val withEmptyPeriods =
            CompletionHistoryUtil.getCurrentStreak(
                completionHistory = history,
                today = LocalDate(2026, 3, 2),
                vacationPeriods = emptyList(),
            )

        assertThat(withDefault).isEqualTo(2)
        assertThat(withEmptyPeriods).isEqualTo(2)
    }

    // endregion
}
