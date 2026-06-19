package com.mandrecode.tempo.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Entity(tableName = "habit_chains")
data class HabitChainEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val colorKey: String? = null,
    val icon: String? = null,
    val periodicReminder: LocalDateTime? = null,
    val createdDate: LocalDateTime =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault()),
    val completionHistory: String = "",
    val repeatDays: Set<DayOfWeek>? = null,
)
