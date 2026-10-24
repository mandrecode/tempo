package com.mandrecode.tempo.util

import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.VacationPeriod
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Utility functions for managing habit completion history.
 */
object CompletionHistoryUtil {
    /**
     * Updates the completion history by adding or removing today's date.
     * @param currentHistory The current completion history string (comma-separated dates)
     * @param isCompleted Whether the habit is being marked as completed today
     * @return Updated completion history string
     */
    fun updateCompletionHistory(
        currentHistory: String,
        isCompleted: Boolean,
    ): String {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return updateCompletionHistoryForDate(currentHistory, today, isCompleted)
    }

    /**
     * Updates the completion history for a specific date.
     * @param currentHistory The current completion history string
     * @param date The date to add or remove
     * @param isCompleted Whether to add (true) or remove (false) the date
     * @return Updated completion history string
     */
    fun updateCompletionHistoryForDate(
        currentHistory: String,
        date: LocalDate,
        isCompleted: Boolean,
    ): String {
        val dateStr = date.toString()
        val dates =
            if (currentHistory.isBlank()) {
                mutableSetOf()
            } else {
                currentHistory.split(",").map { it.trim() }.toMutableSet()
            }

        if (isCompleted) {
            dates.add(dateStr)
        } else {
            dates.remove(dateStr)
        }

        return dates.sorted().joinToString(",")
    }

    /**
     * Checks if a date is present in the completion history string.
     * This is a zero-allocation optimization over splitting the string.
     *
     * @param history The completion history string (comma-separated dates)
     * @param dateStr The date string to search for (e.g. "2023-10-27")
     * @return True if the date is in the history
     */
    fun isDateInHistory(
        history: String,
        dateStr: String,
    ): Boolean {
        if (history.isEmpty()) return false

        // Fast path for clean data (no spaces)
        // Check if history contains spaces to determine if we can use the fast path safely
        // or if we should fallback to split/trim for robustness with malformed data.
        // However, checking contains(" ") is O(N) anyway.
        // Let's assume standard format (no spaces) for the optimization,
        // but handle surrounding whitespace logic if needed?
        // For now, we implement strict comma separation which matches how we save data.

        var startIndex = 0
        while (true) {
            val index = history.indexOf(dateStr, startIndex)
            if (index == -1) return false

            // Check previous character (start of string or comma or space if we want to be loose)
            // To be robust like 'trim()', we should allow spaces around delimiters.
            // But doing that without allocation is complex.
            // Given CompletionHistoryUtil writes without spaces, strict check is likely fine.
            // If strict check fails, we could potentially verify boundaries more loosely.

            val startsCorrectly = (index == 0) || (history[index - 1] == ',')
            val endsCorrectly = (index + dateStr.length == history.length) || (history[index + dateStr.length] == ',')

            if (startsCorrectly && endsCorrectly) return true

            startIndex = index + 1
        }
    }

    /**
     * Returns whether [date] is a scheduled day for a habit with the given [repeatDays] mask.
     *
     * When [repeatDays] is `null` or empty, every day is treated as scheduled (daily habit).
     * Otherwise the date's day-of-week must be present in the mask.
     *
     * This is the single source of truth for "is this day in scope for the habit" and is
     * used by both the history dot rendering in `HabitHistoryView` and the streak math in
     * [getCurrentStreak].
     */
    fun isScheduledOn(
        date: LocalDate,
        repeatDays: Set<DayOfWeek>?,
    ): Boolean {
        if (repeatDays.isNullOrEmpty()) return true
        return repeatDays.contains(DayOfWeek.fromKotlinDayOfWeek(date.dayOfWeek))
    }

    /**
     * Returns whether [date] falls inside one of the user's [vacationPeriods] (vacation mode).
     */
    fun isPausedOn(
        date: LocalDate,
        vacationPeriods: List<VacationPeriod>,
    ): Boolean = vacationPeriods.any { it.covers(date) }

    /**
     * Returns whether the habit actually has to be done on [date] for the streak to survive:
     * the day is scheduled by [repeatDays] **and** not paused by vacation mode.
     *
     * This is the single "is this day in scope" rule shared by the history dots in
     * `HabitHistoryView` and the streak math in [getCurrentStreak], so the dots and the streak
     * label can never disagree about which days counted.
     */
    fun isRequiredOn(
        date: LocalDate,
        repeatDays: Set<DayOfWeek>?,
        vacationPeriods: List<VacationPeriod>,
    ): Boolean = isScheduledOn(date, repeatDays) && !isPausedOn(date, vacationPeriods)

    /**
     * Gets the current streak of planned days completed up to today.
     *
     * See [getStreakStatus] for the rules; this returns only the count.
     */
    fun getCurrentStreak(
        completionHistory: String,
        today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        repeatDays: Set<DayOfWeek>? = null,
        vacationPeriods: List<VacationPeriod> = emptyList(),
    ): Int = getStreakStatus(completionHistory, today, repeatDays, vacationPeriods).streak

    /**
     * Computes the current streak of planned days completed up to today.
     *
     * When [repeatDays] is provided (non-null and non-empty), only planned days are
     * considered: unplanned days are skipped without breaking the streak, and
     * completions recorded on unplanned days are ignored (not counted toward the streak).
     *
     * When [repeatDays] is `null` or empty every day is treated as planned (daily habit).
     *
     * Missing a lone **required** day is forgiven (#422): the streak breaks only after
     * [MAX_FORGIVEN_MISSES] + 1 consecutive missed required days, and missed days never add to
     * the count. Today is still open, so leaving it unchecked never counts as a miss.
     *
     * Days covered by [vacationPeriods] are *optional* rather than out of scope: missing one
     * never breaks the streak nor extends a run of misses, and a completion recorded on one
     * is credited to the count without resetting a run of required-day misses.
     *
     * @param completionHistory Comma-separated ISO-8601 date strings.
     * @param today The reference date (defaults to the system's current date).
     * @param repeatDays The set of days the habit is planned for, or `null`/empty for daily.
     * @param vacationPeriods The stored vacation-mode periods; empty means nothing is paused.
     * @return The streak count and whether it is surviving a missed day before today.
     */
    fun getStreakStatus(
        completionHistory: String,
        today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        repeatDays: Set<DayOfWeek>? = null,
        vacationPeriods: List<VacationPeriod> = emptyList(),
    ): StreakStatus {
        val completedDates =
            completionHistory
                .split(",")
                .mapNotNull { runCatching { LocalDate.parse(it.trim()) }.getOrNull() }
                .toSet()
        val oldestCompletion = completedDates.minOrNull() ?: return StreakStatus.NONE

        var streak = 0
        var consecutiveMisses = 0
        // Whether the most recent required day before today was missed; null until one is seen.
        var lastPastRequiredMissed: Boolean? = null
        var checkDate = today

        while (checkDate >= oldestCompletion) {
            if (isScheduledOn(checkDate, repeatDays)) {
                val isCompleted = checkDate in completedDates
                // Paused days are optional: a completion is credited, but neither a miss nor a
                // completion there changes the required-day miss tracking.
                val isPaused = isPausedOn(checkDate, vacationPeriods)
                // Today is still open, so leaving it unchecked is not a miss yet.
                val isRequiredPastDay = checkDate < today && !isPaused
                if (isCompleted) {
                    streak++
                    if (!isPaused) consecutiveMisses = 0
                } else if (isRequiredPastDay) {
                    consecutiveMisses++
                    if (consecutiveMisses > MAX_FORGIVEN_MISSES) break
                }
                if (lastPastRequiredMissed == null && isRequiredPastDay) {
                    lastPastRequiredMissed = !isCompleted
                }
            }
            checkDate = checkDate.minus(DatePeriod(days = 1))
        }

        return StreakStatus(
            streak = streak,
            isRecovering = streak > 0 && lastPastRequiredMissed == true,
        )
    }

    /** Consecutive missed required days a streak survives (#422). */
    const val MAX_FORGIVEN_MISSES = 1
}

/**
 * The current streak of a habit.
 *
 * @property streak Number of planned days completed in the current streak.
 * @property isRecovering Whether the streak is alive although the most recent required day
 *   before today was missed, so the user should be cheered on to keep it going.
 */
data class StreakStatus(
    val streak: Int,
    val isRecovering: Boolean,
) {
    companion object {
        val NONE = StreakStatus(streak = 0, isRecovering = false)
    }
}
