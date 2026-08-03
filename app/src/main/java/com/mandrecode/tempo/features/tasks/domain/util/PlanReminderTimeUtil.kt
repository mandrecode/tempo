package com.mandrecode.tempo.features.tasks.domain.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime

/**
 * The instant a quick-plan choice actually means.
 *
 * Planning from the Focus sheet picks a *day* — "today", "tomorrow", a date off the picker — but a
 * reminder needs a time, and picking one badly costs the user their reminder: a time already gone
 * is skipped by the scheduler, so the task would end up dated and silent.
 */
object PlanReminderTimeUtil {
    /** Early enough to be part of the morning, late enough not to wake anyone. */
    const val PLAN_DEFAULT_REMINDER_HOUR = 9

    private val LastMinuteOfDay = LocalTime(23, 59)

    /**
     * [date] at [PLAN_DEFAULT_REMINDER_HOUR], or — when that hour has already passed, which only
     * happens when [date] is the day [now] falls on — the next whole hour instead.
     *
     * Never spills into the following day: planning something for today and having it land tomorrow
     * would be the sheet quietly answering a different question. Late enough at night there is no
     * good time left, and the last minute of the day is the honest answer — the day is what the
     * user chose, and the reminder is the part that may not survive it.
     */
    fun resolve(
        date: LocalDate,
        now: LocalDateTime,
    ): LocalDateTime {
        val preferred = date.atTime(PLAN_DEFAULT_REMINDER_HOUR, 0)
        if (preferred > now) return preferred

        val endOfChosenDay = date.atTime(LastMinuteOfDay)
        val nextWholeHour = now.date.atTime(now.hour, 0).plusOneHourWithinDay()
        return minOf(nextWholeHour, endOfChosenDay)
    }

    private fun LocalDateTime.plusOneHourWithinDay(): LocalDateTime =
        if (hour + 1 > LastMinuteOfDay.hour) {
            date.atTime(LastMinuteOfDay)
        } else {
            date.atTime(hour + 1, 0)
        }
}
