package com.mandrecode.tempo.features.routines.data.repository

import androidx.room.withTransaction
import com.mandrecode.tempo.core.data.local.TempoDatabase
import com.mandrecode.tempo.core.data.local.dao.HabitChainDao
import com.mandrecode.tempo.core.data.local.dao.HabitChainMemberDao
import com.mandrecode.tempo.features.routines.data.mapper.toDomain
import com.mandrecode.tempo.features.routines.data.mapper.toEntity
import com.mandrecode.tempo.features.routines.data.mapper.toMemberEntities
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.util.CompletionHistoryUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class HabitChainRepositoryImpl
    @Inject
    constructor(
        private val habitChainDao: HabitChainDao,
        private val habitChainMemberDao: HabitChainMemberDao,
        private val database: TempoDatabase,
    ) : HabitChainRepository {
        override fun getAllHabitChains(): Flow<List<HabitChain>> =
            habitChainDao.getAllHabitChainsWithMembers().map { list ->
                list.toDomain()
            }

        override suspend fun getHabitChainById(id: Long): HabitChain? = habitChainDao.getHabitChainWithMembersById(id)?.toDomain()

        override suspend fun getHabitChainsWithReminders(): List<HabitChain> = habitChainDao.getHabitChainsWithReminders().toDomain()

        override suspend fun insertHabitChain(habitChain: HabitChain): Long {
            val chainId = habitChainDao.insertHabitChain(habitChain.toEntity())
            val members = habitChain.copy(id = chainId).toMemberEntities()
            if (members.isNotEmpty()) {
                habitChainMemberDao.insertMembers(members)
            }
            return chainId
        }

        override suspend fun updateHabitChain(habitChain: HabitChain) {
            database.withTransaction {
                habitChainDao.updateHabitChain(habitChain.toEntity())
                habitChainMemberDao.deleteByChainId(habitChain.id)
                val members = habitChain.toMemberEntities()
                if (members.isNotEmpty()) {
                    habitChainMemberDao.insertMembers(members)
                }
            }
        }

        override suspend fun deleteHabitChain(habitChain: HabitChain) = habitChainDao.deleteHabitChain(habitChain.toEntity())

        override suspend fun toggleHabitChainCompletion(
            id: Long,
            isCompleted: Boolean,
            date: LocalDate,
        ) {
            val habitChain = habitChainDao.getHabitChainById(id)
            if (habitChain != null) {
                val updatedHistory =
                    CompletionHistoryUtil.updateCompletionHistoryForDate(
                        habitChain.completionHistory,
                        date,
                        isCompleted,
                    )
                habitChainDao.updateHabitChainCompletionHistory(id, updatedHistory)
            }
        }

        override suspend fun getChainsForHabit(habitId: Long): List<HabitChain> {
            val chainIds = habitChainMemberDao.getChainIdsForHabit(habitId)
            if (chainIds.isEmpty()) return emptyList()
            return habitChainDao.getHabitChainsWithMembersByIds(chainIds).toDomain()
        }
    }
