package com.mandrecode.tempo.features.routines.domain.repository

import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitDeletionSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>

    suspend fun getHabitById(id: Long): Habit?

    suspend fun getHabitsByIds(habitIds: List<Long>): List<Habit>

    suspend fun getHabitsWithReminders(): List<Habit>

    suspend fun clearAllReminders()

    suspend fun clearRemindersForHabits(habitIds: List<Long>)

    suspend fun insertHabit(habit: Habit): Long

    suspend fun updateHabit(habit: Habit)

    suspend fun deleteHabit(habit: Habit)

    suspend fun deleteHabitWithSnapshot(habitId: Long): HabitDeletionSnapshot

    suspend fun restoreDeletedHabit(snapshot: HabitDeletionSnapshot)

    suspend fun deleteHabitsByIds(habitIds: List<Long>)

    suspend fun toggleHabitCompletion(
        id: Long,
        isCompleted: Boolean,
        date: LocalDate,
        fromNotification: Boolean = false,
    )

    suspend fun refreshHabitChainLiveActivity(
        chainId: Long,
        date: LocalDate? = null,
        fromNotification: Boolean = false,
    )

    suspend fun refreshHabitChainLiveActivity(
        chain: HabitChain,
        date: LocalDate? = null,
        fromNotification: Boolean = false,
    )

    suspend fun updateHabitsColorKey(
        habitIds: List<Long>,
        colorKey: String?,
    )

    suspend fun updateHabitsReminder(
        habitIds: List<Long>,
        reminderDate: LocalDateTime?,
    )
}
