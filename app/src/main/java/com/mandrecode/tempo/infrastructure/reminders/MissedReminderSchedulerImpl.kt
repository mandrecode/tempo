package com.mandrecode.tempo.infrastructure.reminders

import com.mandrecode.tempo.features.tasks.domain.repository.MissedReminderPreferences
import com.mandrecode.tempo.features.tasks.domain.scheduler.MissedReminderScheduler
import com.mandrecode.tempo.infrastructure.reminders.scheduler.MissedReminderAlarmScheduler
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

@Singleton
class MissedReminderSchedulerImpl
    @Inject
    constructor(
        private val preferences: MissedReminderPreferences,
        private val alarmScheduler: MissedReminderAlarmScheduler,
        private val clock: Clock,
    ) : MissedReminderScheduler {
        override fun sync() {
            if (!preferences.isEnabled.value) {
                alarmScheduler.cancelCatchUp()
                return
            }

            val timeZone = TimeZone.currentSystemDefault()
            val now = clock.now().toLocalDateTime(timeZone)
            val next = nextCatchUp(now, preferences.catchUpTime.value)
            alarmScheduler.scheduleCatchUp(next.toInstant(timeZone).toEpochMilliseconds())
        }

        /**
         * Today at [catchUpTime] when that is still ahead, otherwise tomorrow. Equality counts as
         * passed so a catch-up that just fired never re-arms itself for the same instant.
         */
        private fun nextCatchUp(
            now: LocalDateTime,
            catchUpTime: LocalTime,
        ): LocalDateTime {
            val today = now.date.atTime(catchUpTime)
            return if (today > now) today else now.date.plus(1, DateTimeUnit.DAY).atTime(catchUpTime)
        }
    }
