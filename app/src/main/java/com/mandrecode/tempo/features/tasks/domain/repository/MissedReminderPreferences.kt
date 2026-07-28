package com.mandrecode.tempo.features.tasks.domain.repository

import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalTime

/**
 * User configuration for the daily missed-reminder catch-up: whether overdue incomplete
 * tasks are re-notified, and at which local time that happens.
 */
interface MissedReminderPreferences {
    val isEnabled: StateFlow<Boolean>
    val catchUpTime: StateFlow<LocalTime>

    fun setEnabled(enabled: Boolean)

    fun setCatchUpTime(time: LocalTime)

    companion object {
        const val DEFAULT_ENABLED = true
        const val MINUTES_PER_HOUR = 60
        const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR

        val DEFAULT_CATCH_UP_TIME: LocalTime = LocalTime(hour = 9, minute = 0)

        /**
         * Falls back to [DEFAULT_CATCH_UP_TIME] for values outside a day, so a corrupt or
         * hand-edited preference can never produce an unschedulable time.
         */
        fun minuteOfDayToTime(minuteOfDay: Int): LocalTime =
            if (minuteOfDay in 0 until MINUTES_PER_DAY) {
                LocalTime(
                    hour = minuteOfDay / MINUTES_PER_HOUR,
                    minute = minuteOfDay % MINUTES_PER_HOUR,
                )
            } else {
                DEFAULT_CATCH_UP_TIME
            }

        fun timeToMinuteOfDay(time: LocalTime): Int = time.hour * MINUTES_PER_HOUR + time.minute
    }
}
