package com.mandrecode.tempo.features.routines.domain.util

import com.mandrecode.tempo.core.domain.model.DayOfWeek
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Shared utility for advancing habit/chain reminder dates to the next future
 * occurrence. Habits always repeat (at least daily), so when [repeatDays] is
 * null/empty we default to [DayOfWeek.ALL_DAYS].
 */
object HabitReminderDateUtil {
    /**
     * Default hour-of-day used to pre-fill an evening reminder for quit habits when the
     * user has not provided one. Centralised here so presentation, create, and update
     * code paths agree on the same default without coupling to a specific use case.
     */
    const val QUIT_DEFAULT_REMINDER_HOUR = 21

    fun advanceReminderIfNeeded(
        reminderDate: LocalDateTime?,
        repeatDays: Set<DayOfWeek>?,
        now: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    ): LocalDateTime? {
        reminderDate ?: return null
        val effectiveRepeatDays = repeatDays?.takeIf { it.isNotEmpty() } ?: DayOfWeek.ALL_DAYS
        if (reminderDate >= now) {
            val reminderDayOfWeek = DayOfWeek.fromKotlinDayOfWeek(reminderDate.dayOfWeek)
            if (reminderDayOfWeek in effectiveRepeatDays) return reminderDate
        }
        return ReminderDateUtil.calculateNextReminderDate(reminderDate, effectiveRepeatDays, now)
    }

    /**
     * Returns the next upcoming occurrence of [hour]:[minute] relative to [now]:
     * today if [now] is strictly before that time, otherwise tomorrow. Used to
     * pre-fill a sane default reminder (e.g. 21:00 for quit habits) without
     * landing in the past and triggering a silent advancement.
     */
    fun nextUpcomingTime(
        hour: Int,
        minute: Int = 0,
        now: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    ): LocalDateTime {
        val target = LocalTime(hour, minute)
        val date = if (now.time < target) now.date else now.date.plus(DatePeriod(days = 1))
        return LocalDateTime(date, target)
    }
}
