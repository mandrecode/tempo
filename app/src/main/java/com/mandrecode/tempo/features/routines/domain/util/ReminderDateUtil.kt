package com.mandrecode.tempo.features.routines.domain.util

import com.mandrecode.tempo.core.domain.model.DayOfWeek
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

object ReminderDateUtil {
    fun calculateNextReminderDate(
        from: LocalDateTime,
        repeatDays: Set<DayOfWeek>,
        now: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    ): LocalDateTime {
        if (repeatDays.isEmpty()) return from

        // Jump ahead to approximately the right week to avoid O(n) day-by-day iteration
        val daysUntilNow = from.date.daysUntil(now.date)
        val jumpDays = if (daysUntilNow > 7) (daysUntilNow / 7) * 7 - 7 else 0
        var candidate =
            if (jumpDays > 0) {
                LocalDateTime(from.date.plus(DatePeriod(days = jumpDays)), from.time)
            } else {
                from
            }

        while (true) {
            val nextDate = candidate.date.plus(DatePeriod(days = 1))
            candidate = LocalDateTime(nextDate, candidate.time)
            val candidateDayOfWeek = DayOfWeek.fromKotlinDayOfWeek(candidate.dayOfWeek)
            if (candidate > now && candidateDayOfWeek in repeatDays) {
                return candidate
            }
        }
    }
}
