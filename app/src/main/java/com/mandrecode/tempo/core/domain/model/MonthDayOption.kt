package com.mandrecode.tempo.core.domain.model

/**
 * Controls which day of the month a MONTHLY periodic task repeats on.
 *
 * - [SAME_DAY] keeps the original reminder date's day-of-month (default / legacy behaviour).
 * - [FIRST_DAY] 1st calendar day of the month.
 * - [LAST_DAY] last calendar day of the month.
 */
enum class MonthDayOption {
    SAME_DAY,
    FIRST_DAY,
    LAST_DAY,
    ;

    companion object {
        val options = entries
    }
}
