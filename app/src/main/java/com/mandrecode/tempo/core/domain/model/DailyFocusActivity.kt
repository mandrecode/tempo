package com.mandrecode.tempo.core.domain.model

import kotlinx.datetime.LocalDate

/**
 * What a single day amounted to: how much was scheduled, how much got done, and how long was spent
 * in focus sessions.
 *
 * The summary reports the day rather than grading it, so this model deliberately carries no score,
 * ratio, or judgement — only counts, plus the coarse [state] used to draw the history.
 */
data class DailyFocusActivity(
    val date: LocalDate,
    val scheduledCount: Int = 0,
    val completedCount: Int = 0,
    val focusMinutes: Int = 0,
) {
    val state: FocusDayState
        get() =
            when {
                // A day with nothing scheduled and a day where nothing got done look the same on
                // purpose: neither is worth calling out.
                scheduledCount <= 0 || completedCount <= 0 -> FocusDayState.QUIET
                completedCount >= scheduledCount -> FocusDayState.ALL
                else -> FocusDayState.SOME
            }

    /** Whether this day extends a focus streak. */
    val hasActivity: Boolean get() = completedCount > 0

    /** Whether this day is skipped by the streak rather than breaking it. */
    val isUnscheduled: Boolean get() = scheduledCount <= 0
}

/**
 * Three states, no gradient: a ramp would imply a precision these counts do not have, and would
 * turn the history into a score.
 */
enum class FocusDayState {
    QUIET,
    SOME,
    ALL,
}
