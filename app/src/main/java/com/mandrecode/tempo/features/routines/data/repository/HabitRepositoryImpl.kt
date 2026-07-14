package com.mandrecode.tempo.features.routines.data.repository

import androidx.room.withTransaction
import com.mandrecode.tempo.core.data.entity.HabitEntity
import com.mandrecode.tempo.core.data.insertOrVerifyRestoredEntity
import com.mandrecode.tempo.core.data.local.TempoDatabase
import com.mandrecode.tempo.core.data.local.dao.HabitChainDao
import com.mandrecode.tempo.core.data.local.dao.HabitChainMemberDao
import com.mandrecode.tempo.core.data.local.dao.HabitDao
import com.mandrecode.tempo.features.routines.data.mapper.toDomain
import com.mandrecode.tempo.features.routines.data.mapper.toEntity
import com.mandrecode.tempo.features.routines.data.mapper.toMemberEntities
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitDeletionSnapshot
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.infrastructure.liveactivity.HabitChainLiveActivityManager
import com.mandrecode.tempo.util.CompletionHistoryUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject
import kotlin.time.Clock

class HabitRepositoryImpl
    @Inject
    constructor(
        private val habitDao: HabitDao,
        private val habitChainDao: HabitChainDao,
        private val habitChainMemberDao: HabitChainMemberDao,
        private val liveActivityManager: HabitChainLiveActivityManager,
        private val database: TempoDatabase,
    ) : HabitRepository {
        override fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits().map { it.toDomain() }

        override suspend fun getHabitById(id: Long): Habit? = habitDao.getHabitById(id)?.toDomain()

        override suspend fun getHabitsByIds(habitIds: List<Long>): List<Habit> = habitDao.getHabitsByIds(habitIds).toDomain()

        override suspend fun getHabitsWithReminders(): List<Habit> = habitDao.getHabitsWithReminders().toDomain()

        override suspend fun clearAllReminders() = habitDao.clearAllReminders()

        override suspend fun clearRemindersForHabits(habitIds: List<Long>) = habitDao.clearRemindersForHabits(habitIds)

        override suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit.toEntity())

        override suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit.toEntity())

        override suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit.toEntity())

        override suspend fun deleteHabitWithSnapshot(habitId: Long): HabitDeletionSnapshot =
            database.withTransaction {
                val habit = requireNotNull(habitDao.getHabitById(habitId))
                val chainIds = habitChainMemberDao.getChainIdsForHabit(habitId)
                val affectedChains =
                    if (chainIds.isEmpty()) {
                        emptyList()
                    } else {
                        habitChainDao.getHabitChainsWithMembersByIds(chainIds).toDomain()
                    }
                habitDao.deleteHabit(habit)
                HabitDeletionSnapshot(
                    habit = habit.toDomain(),
                    affectedChains = affectedChains,
                )
            }

        override suspend fun restoreDeletedHabit(snapshot: HabitDeletionSnapshot) {
            database.withTransaction {
                val habitEntity = snapshot.habit.toEntity()
                insertOrVerifyRestoredEntity(
                    existing = habitDao.getHabitById(snapshot.habit.id),
                    snapshot = habitEntity,
                    recordDescription = "habit ${snapshot.habit.id}",
                ) {
                    habitDao.insertHabit(habitEntity)
                }
                snapshot.affectedChains.forEach { chain ->
                    val chainEntity = chain.toEntity()
                    insertOrVerifyRestoredEntity(
                        existing = habitChainDao.getHabitChainById(chain.id),
                        snapshot = chainEntity,
                        recordDescription = "habit chain ${chain.id}",
                    ) {
                        habitChainDao.insertHabitChain(chainEntity)
                    }
                    habitChainMemberDao.deleteByChainId(chain.id)
                    val members = chain.toMemberEntities()
                    if (members.isNotEmpty()) {
                        habitChainMemberDao.insertMembers(members)
                    }
                }
            }
        }

        override suspend fun deleteHabitsByIds(habitIds: List<Long>) = habitDao.deleteHabitsByIds(habitIds)

        override suspend fun toggleHabitCompletion(
            id: Long,
            isCompleted: Boolean,
            date: LocalDate,
            fromNotification: Boolean,
        ) {
            // Collect data for live activity updates inside the transaction,
            // but perform the actual live activity updates AFTER the transaction
            // commits so the DB lock is released as quickly as possible.
            data class LiveActivitySnapshot(
                val chain: HabitChain,
                val habits: List<HabitEntity>,
            )

            val liveActivitySnapshots =
                database.withTransaction {
                    val snapshots = mutableListOf<LiveActivitySnapshot>()

                    val habit = habitDao.getHabitById(id)
                    if (habit != null) {
                        val updatedHistory =
                            CompletionHistoryUtil.updateCompletionHistoryForDate(
                                habit.completionHistory,
                                date,
                                isCompleted,
                            )
                        habitDao.updateHabitCompletionHistory(id, updatedHistory)

                        // Update the legacy isCompleted field if it's for today
                        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                        if (date == today) {
                            habitDao.updateHabitCompletion(id, isCompleted)
                        }

                        // Update chain completionHistory and collect snapshots for live activity.
                        // For past-date in-app toggles, keep syncing only chains with an active
                        // live activity session (e.g. overnight continuation after notification start).
                        val chainIdsForUpdate =
                            when {
                                date == today || fromNotification -> habitChainMemberDao.getChainIdsForHabit(id)
                                else ->
                                    habitChainMemberDao
                                        .getChainIdsForHabit(id)
                                        .filter { chainId ->
                                            liveActivityManager.hasActiveLiveActivity(chainId)
                                        }
                            }

                        if (chainIdsForUpdate.isNotEmpty()) {
                            val allHabitIds =
                                chainIdsForUpdate
                                    .flatMap { chainId ->
                                        habitChainMemberDao.getHabitIdsForChain(chainId)
                                    }.toSet()

                            val allHabits = habitDao.getHabitsByIds(allHabitIds.toList())
                            val habitsMap = allHabits.associateBy { it.id }

                            for (chainId in chainIdsForUpdate) {
                                val chainWithMembers =
                                    habitChainDao.getHabitChainWithMembersById(chainId) ?: continue
                                val chainHabitIds =
                                    chainWithMembers.members.sortedBy { it.sortOrder }.map { it.habitId }
                                val chainHabits = habitsInChainOrder(chainHabitIds, habitsMap)

                                // Collect snapshot for post-transaction live activity update
                                snapshots.add(
                                    LiveActivitySnapshot(chainWithMembers.toDomain(), chainHabits),
                                )

                                // Update the chain's own completionHistory
                                val allChainHabitsCompleted =
                                    chainHabits.isNotEmpty() &&
                                        chainHabits.all { h ->
                                            CompletionHistoryUtil.isDateInHistory(
                                                h.completionHistory,
                                                date.toString(),
                                            )
                                        }
                                val updatedChainHistory =
                                    CompletionHistoryUtil.updateCompletionHistoryForDate(
                                        chainWithMembers.chain.completionHistory,
                                        date,
                                        allChainHabitsCompleted,
                                    )
                                habitChainDao.updateHabitChainCompletionHistory(
                                    chainId,
                                    updatedChainHistory,
                                )
                            }
                        }
                    }

                    snapshots
                }

            // Update live activities outside the transaction so the DB lock is released
            for (snapshot in liveActivitySnapshots) {
                updateLiveActivityForChain(snapshot.chain, snapshot.habits, date, fromNotification)
            }
        }

        /**
         * Centralized method to update the Live Activity for a specific chain.
         * This ensures consistent behavior between Notifications, Receivers, and UI toggles.
         */
        override suspend fun refreshHabitChainLiveActivity(
            chainId: Long,
            date: LocalDate?,
            fromNotification: Boolean,
        ) {
            val chain = habitChainDao.getHabitChainWithMembersById(chainId) ?: return
            refreshHabitChainLiveActivity(chain.toDomain(), date, fromNotification)
        }

        override suspend fun refreshHabitChainLiveActivity(
            chain: HabitChain,
            date: LocalDate?,
            fromNotification: Boolean,
        ) {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val targetDate = date ?: today

            // Live Activities are only relevant for today's progress, unless triggered
            // from a notification action (handles overnight interactions where the user
            // acts on a previous day's notification after midnight).
            if (targetDate != today && !fromNotification) return

            val habitIds = chain.habitIds
            if (habitIds.isEmpty()) return

            // Fetch the latest state of all habits in the chain, preserving sortOrder
            val habitsMap = habitDao.getHabitsByIds(habitIds).associateBy { it.id }
            val habits = habitsInChainOrder(habitIds, habitsMap)
            updateLiveActivityForChain(chain, habits, targetDate, fromNotification)
        }

        private fun habitsInChainOrder(
            orderedIds: List<Long>,
            habitsMap: Map<Long, HabitEntity>,
        ): List<HabitEntity> = orderedIds.mapNotNull { habitsMap[it] }

        private fun updateLiveActivityForChain(
            chain: HabitChain,
            habits: List<HabitEntity>,
            date: LocalDate,
            fromNotification: Boolean = false,
        ) {
            val dateStr = date.toString()
            val totalCount = habits.size
            val completedCount =
                habits.count { h ->
                    CompletionHistoryUtil.isDateInHistory(h.completionHistory, dateStr)
                }

            // Find the first incomplete habit for the target date
            val currentHabit =
                habits.firstOrNull { h ->
                    !CompletionHistoryUtil.isDateInHistory(h.completionHistory, dateStr)
                }

            liveActivityManager.updateLiveActivity(
                chain = chain,
                completedCount = completedCount,
                totalCount = totalCount,
                currentHabitId = currentHabit?.id,
                currentHabitTitle = currentHabit?.title,
                fromNotification = fromNotification,
                scheduledDate = date,
            )
        }

        override suspend fun updateHabitsColorKey(
            habitIds: List<Long>,
            colorKey: String?,
        ) {
            habitDao.updateHabitsColorKey(habitIds, colorKey)
        }

        override suspend fun updateHabitsReminder(
            habitIds: List<Long>,
            reminderDate: LocalDateTime?,
        ) {
            habitDao.updateHabitsReminder(habitIds, reminderDate)
        }
    }
