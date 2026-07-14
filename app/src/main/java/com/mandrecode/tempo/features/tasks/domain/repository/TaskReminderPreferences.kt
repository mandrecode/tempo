package com.mandrecode.tempo.features.tasks.domain.repository

import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalTime

interface TaskReminderPreferences {
    val defaultTime: StateFlow<LocalTime>

    fun setDefaultTime(time: LocalTime)

    companion object {
        const val DEFAULT_HOUR = 9
        const val DEFAULT_MINUTE = 0
        private const val MAX_HOUR = 23
        private const val MAX_MINUTE = 59

        val DEFAULT_TIME = LocalTime(DEFAULT_HOUR, DEFAULT_MINUTE)

        fun normalize(
            hour: Int,
            minute: Int,
        ): LocalTime =
            LocalTime(
                hour = hour.coerceIn(0, MAX_HOUR),
                minute = minute.coerceIn(0, MAX_MINUTE),
            )
    }
}
