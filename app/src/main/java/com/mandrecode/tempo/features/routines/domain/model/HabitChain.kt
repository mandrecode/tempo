package com.mandrecode.tempo.features.routines.domain.model

import com.mandrecode.tempo.core.domain.model.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class HabitChain(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val colorKey: String? = null,
    val icon: String? = null,
    val habitIds: List<Long> = emptyList(),
    val periodicReminder: LocalDateTime? = null,
    val createdDate: LocalDateTime =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault()),
    val completionHistory: String = "",
    val repeatDays: Set<DayOfWeek>? = null,
)
