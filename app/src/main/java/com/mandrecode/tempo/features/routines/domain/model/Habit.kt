package com.mandrecode.tempo.features.routines.domain.model

import com.mandrecode.tempo.core.domain.model.DayOfWeek
import kotlinx.datetime.LocalDateTime

data class Habit(
    val id: Long = 0,
    val title: String,
    val description: String,
    val icon: String? = null,
    val colorKey: String? = null,
    val reminderDate: LocalDateTime? = null,
    val isCompleted: Boolean = false,
    val habitType: HabitType = HabitType.BUILD,
    val createdDate: LocalDateTime,
    val completionHistory: String = "",
    val repeatDays: Set<DayOfWeek>? = null,
)
