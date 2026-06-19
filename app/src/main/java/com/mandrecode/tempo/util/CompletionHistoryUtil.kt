package com.mandrecode.tempo.util

import com.mandrecode.tempo.core.domain.model.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
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
     * Gets the current streak of consecutive planned days completed up to today.
     *
     * When [repeatDays] is provided (non-null and non-empty), only planned days are
     * considered: unplanned days are skipped without breaking the streak, and
     * completions recorded on unplanned days are ignored (not counted toward the streak).
     * The streak breaks only when a **planned** day has no completion.
     *
     * When [repeatDays] is `null` or empty every day is treated as planned (daily habit).
     *
     * @param completionHistory Comma-separated ISO-8601 date strings.
     * @param today The reference date (defaults to the system's current date).
     * @param repeatDays The set of days the habit is planned for, or `null`/empty for daily.
     * @return Number of consecutive planned days completed.
     */
    fun getCurrentStreak(
        completionHistory: String,
        today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        repeatDays: Set<DayOfWeek>? = null,
    ): Int {
        if (completionHistory.isBlank()) return 0

        val completedDates =
            completionHistory
                .split(",")
                .mapNotNull {
                    try {
                        LocalDate.parse(it.trim())
                    } catch (_: Exception) {
                        null
                    }
                }.sortedDescending()

        if (completedDates.isEmpty()) return 0

        var streak = 0
        var checkDate = today
        var completedIndex = 0

        while (completedIndex < completedDates.size) {
            val date = completedDates[completedIndex]

            if (date == checkDate) {
                if (isScheduledOn(checkDate, repeatDays)) {
                    streak++
                }
                checkDate = LocalDate.fromEpochDays(checkDate.toEpochDays() - 1)
                completedIndex++
            } else if (date < checkDate) {
                if (!isScheduledOn(checkDate, repeatDays)) {
                    checkDate = LocalDate.fromEpochDays(checkDate.toEpochDays() - 1)
                } else {
                    break
                }
            } else {
                completedIndex++
            }
        }

        return streak
    }
}
