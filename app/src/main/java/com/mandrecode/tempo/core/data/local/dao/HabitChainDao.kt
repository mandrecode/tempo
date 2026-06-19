package com.mandrecode.tempo.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mandrecode.tempo.core.data.entity.HabitChainEntity
import com.mandrecode.tempo.core.data.entity.HabitChainWithMembers
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitChainDao {
    @Transaction
    @Query("SELECT * FROM habit_chains ORDER BY id DESC")
    fun getAllHabitChainsWithMembers(): Flow<List<HabitChainWithMembers>>

    @Transaction
    @Query("SELECT * FROM habit_chains WHERE id = :id")
    suspend fun getHabitChainWithMembersById(id: Long): HabitChainWithMembers?

    @Query("SELECT * FROM habit_chains WHERE id = :id")
    suspend fun getHabitChainById(id: Long): HabitChainEntity?

    @Transaction
    @Query("SELECT * FROM habit_chains WHERE id IN (:ids)")
    suspend fun getHabitChainsWithMembersByIds(ids: List<Long>): List<HabitChainWithMembers>

    @Transaction
    @Query("SELECT * FROM habit_chains WHERE periodicReminder IS NOT NULL")
    suspend fun getHabitChainsWithReminders(): List<HabitChainWithMembers>

    @Insert
    suspend fun insertHabitChain(habitChain: HabitChainEntity): Long

    @Update
    suspend fun updateHabitChain(habitChain: HabitChainEntity)

    @Delete
    suspend fun deleteHabitChain(habitChain: HabitChainEntity)

    @Query("UPDATE habit_chains SET completionHistory = :completionHistory WHERE id = :id")
    suspend fun updateHabitChainCompletionHistory(
        id: Long,
        completionHistory: String,
    )
}
