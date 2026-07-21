package com.mandrecode.tempo.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mandrecode.tempo.core.data.entity.HabitEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE reminderDate IS NOT NULL")
    suspend fun getHabitsWithReminders(): List<HabitEntity>

    @Query("UPDATE habits SET reminderDate = NULL")
    suspend fun clearAllReminders()

    @Query("UPDATE habits SET reminderDate = NULL WHERE id IN (:habitIds)")
    suspend fun clearRemindersForHabits(habitIds: List<Long>)

    @Insert
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id IN (:habitIds)")
    suspend fun deleteHabitsByIds(habitIds: List<Long>)

    @Query("UPDATE habits SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateHabitCompletion(
        id: Long,
        isCompleted: Boolean,
    )

    @Query("UPDATE habits SET completionHistory = :completionHistory WHERE id = :id")
    suspend fun updateHabitCompletionHistory(
        id: Long,
        completionHistory: String,
    )

    @Query("UPDATE habits SET colorKey = :colorKey WHERE id IN (:habitIds)")
    suspend fun updateHabitsColorKey(
        habitIds: List<Long>,
        colorKey: String?,
    )

    @Query("UPDATE habits SET icon = :icon WHERE id IN (:habitIds)")
    suspend fun updateHabitsIcon(
        habitIds: List<Long>,
        icon: String?,
    )

    @Query("SELECT * FROM habits WHERE id IN (:habitIds)")
    suspend fun getHabitsByIds(habitIds: List<Long>): List<HabitEntity>

    @Query("UPDATE habits SET reminderDate = :reminderDate WHERE id IN (:habitIds)")
    suspend fun updateHabitsReminder(
        habitIds: List<Long>,
        reminderDate: LocalDateTime?,
    )

    @Query("UPDATE habits SET reminderDate = :reminderDate WHERE id = :habitId")
    suspend fun updateHabitReminder(
        habitId: Long,
        reminderDate: LocalDateTime?,
    )

    @Query("SELECT * FROM habits ORDER BY id ASC")
    suspend fun getAllHabitsSync(): List<HabitEntity>

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()
}
