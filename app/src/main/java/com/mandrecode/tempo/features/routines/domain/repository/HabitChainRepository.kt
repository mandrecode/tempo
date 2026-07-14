package com.mandrecode.tempo.features.routines.domain.repository

import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitChainDeletionSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface HabitChainRepository {
    fun getAllHabitChains(): Flow<List<HabitChain>>

    suspend fun getHabitChainById(id: Long): HabitChain?

    suspend fun getHabitChainsWithReminders(): List<HabitChain>

    suspend fun insertHabitChain(habitChain: HabitChain): Long

    suspend fun updateHabitChain(habitChain: HabitChain)

    suspend fun deleteHabitChain(habitChain: HabitChain)

    suspend fun deleteHabitChainWithSnapshot(
        habitChainId: Long,
        deleteHabits: Boolean,
    ): HabitChainDeletionSnapshot

    suspend fun restoreDeletedHabitChain(snapshot: HabitChainDeletionSnapshot)

    suspend fun toggleHabitChainCompletion(
        id: Long,
        isCompleted: Boolean,
        date: LocalDate,
    )

    suspend fun getChainsForHabit(habitId: Long): List<HabitChain>
}
