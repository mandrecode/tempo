package com.mandrecode.tempo.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val icon: String? = null,
    val colorKey: String? = null,
    val reminderDate: LocalDateTime? = null,
    val isCompleted: Boolean = false,
    val habitType: String = "BUILD",
    val createdDate: LocalDateTime,
    val completionHistory: String = "",
    val repeatDays: Set<DayOfWeek>? = null,
)
