package com.mandrecode.tempo.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mandrecode.tempo.core.data.entity.HabitChainMemberEntity

@Dao
interface HabitChainMemberDao {
    @Query("SELECT habitId FROM habit_chain_members WHERE chainId = :chainId ORDER BY sortOrder")
    suspend fun getHabitIdsForChain(chainId: Long): List<Long>

    @Query("SELECT chainId FROM habit_chain_members WHERE habitId = :habitId")
    suspend fun getChainIdsForHabit(habitId: Long): List<Long>

    @Query("SELECT DISTINCT chainId FROM habit_chain_members WHERE habitId IN (:habitIds)")
    suspend fun getChainIdsForHabits(habitIds: List<Long>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<HabitChainMemberEntity>)

    @Query("DELETE FROM habit_chain_members WHERE chainId = :chainId")
    suspend fun deleteByChainId(chainId: Long)
}
