package com.mandrecode.tempo.features.routines.data.repository

import androidx.room.withTransaction
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.entity.HabitChainEntity
import com.mandrecode.tempo.core.data.entity.HabitChainMemberEntity
import com.mandrecode.tempo.core.data.entity.HabitChainWithMembers
import com.mandrecode.tempo.core.data.entity.HabitEntity
import com.mandrecode.tempo.core.data.local.TempoDatabase
import com.mandrecode.tempo.core.data.local.dao.HabitChainDao
import com.mandrecode.tempo.core.data.local.dao.HabitChainMemberDao
import com.mandrecode.tempo.core.data.local.dao.HabitDao
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitDeletionSnapshot
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.infrastructure.liveactivity.HabitChainLiveActivityManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class HabitRepositoryTest {
    private lateinit var repository: HabitRepository
    private lateinit var habitDao: HabitDao
    private lateinit var habitChainDao: HabitChainDao
    private lateinit var habitChainMemberDao: HabitChainMemberDao
    private lateinit var liveActivityManager: HabitChainLiveActivityManager
    private lateinit var database: TempoDatabase

    private lateinit var today: kotlinx.datetime.LocalDate
    private lateinit var yesterday: kotlinx.datetime.LocalDate
    private lateinit var now: kotlinx.datetime.LocalDateTime

    @Before
    fun setup() {
        habitDao = mockk(relaxed = true)
        habitChainDao = mockk(relaxed = true)
        habitChainMemberDao = mockk(relaxed = true)
        liveActivityManager = mockk(relaxed = true)
        database = mockk(relaxed = true)

        mockkStatic("androidx.room.RoomDatabaseKt")
        @Suppress("UNCHECKED_CAST")
        coEvery { database.withTransaction(any<suspend () -> Any?>()) } coAnswers {
            (args[1] as (suspend () -> Any?)).invoke()
        }

        repository = HabitRepositoryImpl(habitDao, habitChainDao, habitChainMemberDao, liveActivityManager, database)

        today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        yesterday = today.minus(1, DateTimeUnit.DAY)
        now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `deleteHabitWithSnapshot captures affected chains before deleting`() =
        runTest {
            val habit = HabitEntity(id = 7L, title = "Read", description = "", createdDate = now)
            val chain =
                HabitChainWithMembers(
                    chain = HabitChainEntity(id = 8L, title = "Evening", createdDate = now),
                    members = listOf(HabitChainMemberEntity(chainId = 8L, habitId = 7L, sortOrder = 0)),
                )
            coEvery { habitDao.getHabitById(7L) } returns habit
            coEvery { habitChainMemberDao.getChainIdsForHabit(7L) } returns listOf(8L)
            coEvery { habitChainDao.getHabitChainsWithMembersByIds(listOf(8L)) } returns listOf(chain)

            val result = repository.deleteHabitWithSnapshot(7L)

            assertThat(result.habit.id).isEqualTo(7L)
            assertThat(result.affectedChains.map(HabitChain::id)).containsExactly(8L)
            coVerify { habitDao.deleteHabit(habit) }
        }

    @Test
    fun `restoreDeletedHabit inserts habit and restores matching chain memberships`() =
        runTest {
            val habit = Habit(id = 9L, title = "Read", description = "", createdDate = now)
            val chain = HabitChain(id = 10L, title = "Evening", habitIds = listOf(9L), createdDate = now)
            coEvery { habitDao.getHabitById(9L) } returns null
            coEvery { habitChainDao.getHabitChainById(10L) } returns
                HabitChainEntity(id = 10L, title = "Evening", createdDate = now)

            repository.restoreDeletedHabit(HabitDeletionSnapshot(habit, listOf(chain)))

            coVerify { habitDao.insertHabit(match { it.id == 9L }) }
            coVerify(exactly = 0) { habitChainDao.updateHabitChain(any()) }
            coVerify { habitChainMemberDao.deleteByChainId(10L) }
            coVerify { habitChainMemberDao.insertMembers(match { it.single().habitId == 9L }) }
        }

    @Test
    fun `restoreDeletedHabit rejects a reused habit id`() =
        runTest {
            val habit = Habit(id = 9L, title = "Deleted", description = "", createdDate = now)
            coEvery { habitDao.getHabitById(9L) } returns
                HabitEntity(id = 9L, title = "Unrelated", description = "", createdDate = now)

            val result = runCatching { repository.restoreDeletedHabit(HabitDeletionSnapshot(habit, emptyList())) }

            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
            coVerify(exactly = 0) { habitDao.updateHabit(any()) }
        }

    @Test
    fun `restoreDeletedHabit rejects a reused chain id`() =
        runTest {
            val habit = Habit(id = 9L, title = "Read", description = "", createdDate = now)
            val chain = HabitChain(id = 10L, title = "Evening", habitIds = listOf(9L), createdDate = now)
            coEvery { habitDao.getHabitById(9L) } returns null
            coEvery { habitChainDao.getHabitChainById(10L) } returns HabitChainEntity(id = 10L, title = "Unrelated")

            val result = runCatching { repository.restoreDeletedHabit(HabitDeletionSnapshot(habit, listOf(chain))) }

            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
            coVerify(exactly = 0) { habitChainDao.updateHabitChain(any()) }
            coVerify(exactly = 0) { habitChainMemberDao.deleteByChainId(any()) }
        }

    @Test
    fun `toggleHabitCompletion uses optimized query and avoids unnecessary fetches`() =
        runTest {
            val habitId = 1L
            val habitEntity =
                HabitEntity(
                    id = habitId,
                    title = "Test Habit",
                    description = "Description",
                    createdDate = now,
                )

            val chainId = 10L
            val habitChainEntity =
                HabitChainEntity(
                    id = chainId,
                    title = "Test Chain",
                )
            val chainWithMembers =
                HabitChainWithMembers(
                    chain = habitChainEntity,
                    members =
                        listOf(
                            HabitChainMemberEntity(chainId = chainId, habitId = 1L, sortOrder = 0),
                            HabitChainMemberEntity(chainId = chainId, habitId = 2L, sortOrder = 1),
                        ),
                )

            coEvery { habitDao.getHabitById(habitId) } returns habitEntity
            coEvery { habitChainMemberDao.getChainIdsForHabit(habitId) } returns listOf(chainId)
            coEvery { habitChainMemberDao.getHabitIdsForChain(chainId) } returns listOf(1L, 2L)
            coEvery { habitChainDao.getHabitChainWithMembersById(chainId) } returns chainWithMembers
            coEvery { habitDao.getHabitsByIds(any()) } returns listOf(habitEntity)

            repository.toggleHabitCompletion(habitId, true, today)

            // Verify junction table query is used
            coVerify { habitChainMemberDao.getChainIdsForHabit(habitId) }

            // Verify we do NOT fetch all chains
            coVerify(exactly = 0) { habitChainDao.getAllHabitChainsWithMembers() }

            // Verify Live Activity update is called
            coVerify {
                liveActivityManager.updateLiveActivity(
                    chain = any(),
                    completedCount = any(),
                    totalCount = any(),
                    currentHabitId = any(),
                    currentHabitTitle = any(),
                    fromNotification = any(),
                    scheduledDate = any(),
                )
            }
        }

    @Test
    fun `toggleHabitCompletion batches habit fetches for multiple chains`() =
        runTest {
            val habitId = 1L
            val habitEntity1 = HabitEntity(id = habitId, title = "H1", description = "", createdDate = now)
            val habitEntity2 = HabitEntity(id = 2L, title = "H2", description = "", createdDate = now)

            val chainEntity1 = HabitChainEntity(id = 10L, title = "C1")
            val chainEntity2 = HabitChainEntity(id = 11L, title = "C2")
            val chainWithMembers1 =
                HabitChainWithMembers(
                    chain = chainEntity1,
                    members =
                        listOf(
                            HabitChainMemberEntity(chainId = 10L, habitId = 1L, sortOrder = 0),
                            HabitChainMemberEntity(chainId = 10L, habitId = 2L, sortOrder = 1),
                        ),
                )
            val chainWithMembers2 =
                HabitChainWithMembers(
                    chain = chainEntity2,
                    members =
                        listOf(
                            HabitChainMemberEntity(chainId = 11L, habitId = 1L, sortOrder = 0),
                        ),
                )

            coEvery { habitDao.getHabitById(habitId) } returns habitEntity1
            coEvery { habitChainMemberDao.getChainIdsForHabit(habitId) } returns listOf(10L, 11L)
            coEvery { habitChainMemberDao.getHabitIdsForChain(10L) } returns listOf(1L, 2L)
            coEvery { habitChainMemberDao.getHabitIdsForChain(11L) } returns listOf(1L)
            coEvery { habitChainDao.getHabitChainWithMembersById(10L) } returns chainWithMembers1
            coEvery { habitChainDao.getHabitChainWithMembersById(11L) } returns chainWithMembers2
            // Ensure getHabitsByIds returns correct habits for any call
            coEvery { habitDao.getHabitsByIds(any()) } answers {
                val ids = firstArg<List<Long>>()
                ids.map { id -> if (id == 1L) habitEntity1 else habitEntity2 }
            }

            repository.toggleHabitCompletion(habitId, true, today)

            // It should fetch habits only ONCE (batching all unique IDs)
            coVerify(exactly = 1) { habitDao.getHabitsByIds(any()) }
        }

    @Test
    fun `toggleHabitCompletion marks chain completed when all habits done`() =
        runTest {
            val todayStr = today.toString()

            // Habit 2 was already completed today; we're now completing habit 1
            val habitEntity1 =
                HabitEntity(
                    id = 1L,
                    title = "H1",
                    description = "",
                    createdDate = now,
                    completionHistory = "",
                )
            val habitEntity2 =
                HabitEntity(
                    id = 2L,
                    title = "H2",
                    description = "",
                    createdDate = now,
                    completionHistory = todayStr,
                )

            val chainId = 10L
            val chainEntity = HabitChainEntity(id = chainId, title = "Chain", completionHistory = "")
            val chainWithMembers =
                HabitChainWithMembers(
                    chain = chainEntity,
                    members =
                        listOf(
                            HabitChainMemberEntity(chainId = chainId, habitId = 1L, sortOrder = 0),
                            HabitChainMemberEntity(chainId = chainId, habitId = 2L, sortOrder = 1),
                        ),
                )

            coEvery { habitDao.getHabitById(1L) } returns habitEntity1
            coEvery { habitChainMemberDao.getChainIdsForHabit(1L) } returns listOf(chainId)
            coEvery { habitChainMemberDao.getHabitIdsForChain(chainId) } returns listOf(1L, 2L)
            coEvery { habitChainDao.getHabitChainWithMembersById(chainId) } returns chainWithMembers
            // After DAO update, getHabitsByIds returns the updated habit 1 with today in history
            coEvery { habitDao.getHabitsByIds(any()) } returns
                listOf(
                    habitEntity1.copy(completionHistory = todayStr),
                    habitEntity2,
                )

            repository.toggleHabitCompletion(1L, true, today)

            // Chain should be marked as completed for today
            coVerify {
                habitChainDao.updateHabitChainCompletionHistory(chainId, todayStr)
            }
        }

    @Test
    fun `toggleHabitCompletion removes chain completion when habit unchecked`() =
        runTest {
            val todayStr = today.toString()

            // Both habits were completed; we're now unchecking habit 1
            val habitEntity1 =
                HabitEntity(
                    id = 1L,
                    title = "H1",
                    description = "",
                    createdDate = now,
                    completionHistory = todayStr,
                )
            val habitEntity2 =
                HabitEntity(
                    id = 2L,
                    title = "H2",
                    description = "",
                    createdDate = now,
                    completionHistory = todayStr,
                )

            val chainId = 10L
            val chainEntity =
                HabitChainEntity(
                    id = chainId,
                    title = "Chain",
                    completionHistory = todayStr,
                )
            val chainWithMembers =
                HabitChainWithMembers(
                    chain = chainEntity,
                    members =
                        listOf(
                            HabitChainMemberEntity(chainId = chainId, habitId = 1L, sortOrder = 0),
                            HabitChainMemberEntity(chainId = chainId, habitId = 2L, sortOrder = 1),
                        ),
                )

            coEvery { habitDao.getHabitById(1L) } returns habitEntity1
            coEvery { habitChainMemberDao.getChainIdsForHabit(1L) } returns listOf(chainId)
            coEvery { habitChainMemberDao.getHabitIdsForChain(chainId) } returns listOf(1L, 2L)
            coEvery { habitChainDao.getHabitChainWithMembersById(chainId) } returns chainWithMembers
            // After DAO update, habit 1 no longer has today in history
            coEvery { habitDao.getHabitsByIds(any()) } returns
                listOf(
                    habitEntity1.copy(completionHistory = ""),
                    habitEntity2,
                )

            repository.toggleHabitCompletion(1L, false, today)

            // Chain should have today removed from its completion history
            coVerify {
                habitChainDao.updateHabitChainCompletionHistory(chainId, "")
            }
        }

    @Test
    fun `refreshHabitChainLiveActivity preserves chain sortOrder for currentHabit`() =
        runTest {
            // Chain order: habit 3 (sortOrder 0), habit 1 (sortOrder 1), habit 2 (sortOrder 2)
            // DB will return them in ID order: 1, 2, 3
            val habitEntity1 = HabitEntity(id = 1L, title = "Second", description = "", createdDate = now)
            val habitEntity2 = HabitEntity(id = 2L, title = "Third", description = "", createdDate = now)
            val habitEntity3 = HabitEntity(id = 3L, title = "First", description = "", createdDate = now)

            val chainId = 10L
            val chainWithMembers =
                HabitChainWithMembers(
                    chain = HabitChainEntity(id = chainId, title = "Chain"),
                    members =
                        listOf(
                            HabitChainMemberEntity(chainId = chainId, habitId = 3L, sortOrder = 0),
                            HabitChainMemberEntity(chainId = chainId, habitId = 1L, sortOrder = 1),
                            HabitChainMemberEntity(chainId = chainId, habitId = 2L, sortOrder = 2),
                        ),
                )

            coEvery { habitChainDao.getHabitChainWithMembersById(chainId) } returns chainWithMembers
            // DAO returns habits in ID order (1, 2, 3), not chain sortOrder (3, 1, 2)
            coEvery { habitDao.getHabitsByIds(any()) } returns
                listOf(habitEntity1, habitEntity2, habitEntity3)

            repository.refreshHabitChainLiveActivity(chainId, today)

            // The first incomplete habit should be habit 3 ("First"), not habit 1 ("Second")
            coVerify {
                liveActivityManager.updateLiveActivity(
                    chain = any(),
                    completedCount = 0,
                    totalCount = 3,
                    currentHabitId = 3L,
                    currentHabitTitle = "First",
                    fromNotification = any(),
                    scheduledDate = any(),
                )
            }
        }

    // --- Overnight notification gating ---

    @Test
    fun `toggleHabitCompletion from notification updates chains even when date is not today`() =
        runTest {
            val yesterdayStr = yesterday.toString()
            val habitEntity =
                HabitEntity(id = 1L, title = "H1", description = "", createdDate = now)
            val chainId = 10L
            val chainWithMembers =
                HabitChainWithMembers(
                    chain = HabitChainEntity(id = chainId, title = "Chain"),
                    members =
                        listOf(
                            HabitChainMemberEntity(chainId = chainId, habitId = 1L, sortOrder = 0),
                        ),
                )

            coEvery { habitDao.getHabitById(1L) } returns habitEntity
            coEvery { habitChainMemberDao.getChainIdsForHabit(1L) } returns listOf(chainId)
            coEvery { habitChainMemberDao.getHabitIdsForChain(chainId) } returns listOf(1L)
            coEvery { habitChainDao.getHabitChainWithMembersById(chainId) } returns chainWithMembers
            coEvery { habitDao.getHabitsByIds(any()) } returns
                listOf(habitEntity.copy(completionHistory = yesterdayStr))

            repository.toggleHabitCompletion(1L, true, yesterday, fromNotification = true)

            // Chain/live activity should still be updated despite date being yesterday
            coVerify {
                liveActivityManager.updateLiveActivity(
                    chain = any(),
                    completedCount = any(),
                    totalCount = any(),
                    currentHabitId = any(),
                    currentHabitTitle = any(),
                    fromNotification = any(),
                    scheduledDate = any(),
                )
            }
            coVerify {
                habitChainDao.updateHabitChainCompletionHistory(chainId, yesterdayStr)
            }
        }

    @Test
    fun `toggleHabitCompletion for past date without notification skips chain updates`() =
        runTest {
            val habitEntity =
                HabitEntity(id = 1L, title = "H1", description = "", createdDate = now)
            val chainId = 10L

            coEvery { habitDao.getHabitById(1L) } returns habitEntity
            coEvery { habitChainMemberDao.getChainIdsForHabit(1L) } returns listOf(chainId)
            every { liveActivityManager.hasActiveLiveActivity(chainId) } returns false

            repository.toggleHabitCompletion(1L, true, yesterday, fromNotification = false)

            // Completion history is always updated regardless of date
            coVerify { habitDao.updateHabitCompletionHistory(1L, any()) }

            // Inactive past-date chains still skip live activity and chain updates.
            coVerify { habitChainMemberDao.getChainIdsForHabit(1L) }
            verify { liveActivityManager.hasActiveLiveActivity(chainId) }
            coVerify(exactly = 0) { habitChainMemberDao.getHabitIdsForChain(any()) }
            coVerify(exactly = 0) { habitChainDao.updateHabitChainCompletionHistory(any(), any()) }
            coVerify(exactly = 0) {
                liveActivityManager.updateLiveActivity(
                    chain = any(),
                    completedCount = any(),
                    totalCount = any(),
                    currentHabitId = any(),
                    currentHabitTitle = any(),
                    fromNotification = any(),
                    scheduledDate = any(),
                )
            }
        }

    @Test
    fun `toggleHabitCompletion for past date without notification updates active live activity chains`() =
        runTest {
            val yesterdayStr = yesterday.toString()
            val chainId = 10L
            val habitEntity =
                HabitEntity(id = 1L, title = "H1", description = "", createdDate = now)
            val chainWithMembers =
                HabitChainWithMembers(
                    chain = HabitChainEntity(id = chainId, title = "Chain"),
                    members =
                        listOf(
                            HabitChainMemberEntity(chainId = chainId, habitId = 1L, sortOrder = 0),
                        ),
                )

            coEvery { habitDao.getHabitById(1L) } returns habitEntity
            coEvery { habitChainMemberDao.getChainIdsForHabit(1L) } returns listOf(chainId)
            every { liveActivityManager.hasActiveLiveActivity(chainId) } returns true
            coEvery { habitChainMemberDao.getHabitIdsForChain(chainId) } returns listOf(1L)
            coEvery { habitChainDao.getHabitChainWithMembersById(chainId) } returns chainWithMembers
            coEvery { habitDao.getHabitsByIds(any()) } returns
                listOf(habitEntity.copy(completionHistory = yesterdayStr))

            repository.toggleHabitCompletion(1L, true, yesterday, fromNotification = false)

            coVerify { habitChainMemberDao.getChainIdsForHabit(1L) }
            verify { liveActivityManager.hasActiveLiveActivity(chainId) }
            coVerify { habitChainDao.updateHabitChainCompletionHistory(chainId, yesterdayStr) }
            coVerify {
                liveActivityManager.updateLiveActivity(
                    chain = any(),
                    completedCount = any(),
                    totalCount = any(),
                    currentHabitId = any(),
                    currentHabitTitle = any(),
                    fromNotification = false,
                    scheduledDate = yesterday,
                )
            }
        }

    @Test
    fun `toggleHabitCompletion from notification does not update legacy isCompleted for past date`() =
        runTest {
            val habitEntity =
                HabitEntity(id = 1L, title = "H1", description = "", createdDate = now)

            coEvery { habitDao.getHabitById(1L) } returns habitEntity
            coEvery { habitChainMemberDao.getChainIdsForHabit(1L) } returns emptyList()

            repository.toggleHabitCompletion(1L, true, yesterday, fromNotification = true)

            // Completion history is updated for yesterday
            coVerify { habitDao.updateHabitCompletionHistory(1L, any()) }
            // Legacy isCompleted field should NOT be touched — it reflects today's state
            coVerify(exactly = 0) { habitDao.updateHabitCompletion(any(), any()) }
        }

    @Test
    fun `toggleHabitCompletion for today updates legacy isCompleted`() =
        runTest {
            val habitEntity =
                HabitEntity(id = 1L, title = "H1", description = "", createdDate = now)

            coEvery { habitDao.getHabitById(1L) } returns habitEntity
            coEvery { habitChainMemberDao.getChainIdsForHabit(1L) } returns emptyList()

            repository.toggleHabitCompletion(1L, true, today)

            coVerify { habitDao.updateHabitCompletion(1L, true) }
        }

    // --- refreshHabitChainLiveActivity overnight ---

    @Test
    fun `refreshHabitChainLiveActivity from notification shows live activity for past date`() =
        runTest {
            val habitEntity = HabitEntity(id = 1L, title = "H1", description = "", createdDate = now)
            val chainId = 10L
            val chainWithMembers =
                HabitChainWithMembers(
                    chain = HabitChainEntity(id = chainId, title = "Chain"),
                    members =
                        listOf(
                            HabitChainMemberEntity(chainId = chainId, habitId = 1L, sortOrder = 0),
                        ),
                )

            coEvery { habitChainDao.getHabitChainWithMembersById(chainId) } returns chainWithMembers
            coEvery { habitDao.getHabitsByIds(any()) } returns listOf(habitEntity)

            repository.refreshHabitChainLiveActivity(chainId, yesterday, fromNotification = true)

            // Live activity should be updated despite date being yesterday
            coVerify {
                liveActivityManager.updateLiveActivity(
                    chain = any(),
                    completedCount = any(),
                    totalCount = 1,
                    currentHabitId = any(),
                    currentHabitTitle = any(),
                    fromNotification = any(),
                    scheduledDate = any(),
                )
            }
        }

    @Test
    fun `refreshHabitChainLiveActivity without notification skips past date`() =
        runTest {
            val chainId = 10L
            val chainWithMembers =
                HabitChainWithMembers(
                    chain = HabitChainEntity(id = chainId, title = "Chain"),
                    members =
                        listOf(
                            HabitChainMemberEntity(chainId = chainId, habitId = 1L, sortOrder = 0),
                        ),
                )

            coEvery { habitChainDao.getHabitChainWithMembersById(chainId) } returns chainWithMembers

            repository.refreshHabitChainLiveActivity(chainId, yesterday, fromNotification = false)

            // Live activity should NOT be updated for past date without notification
            coVerify(exactly = 0) { habitDao.getHabitsByIds(any()) }
            coVerify(exactly = 0) {
                liveActivityManager.updateLiveActivity(
                    chain = any(),
                    completedCount = any(),
                    totalCount = any(),
                    currentHabitId = any(),
                    currentHabitTitle = any(),
                    fromNotification = any(),
                    scheduledDate = any(),
                )
            }
        }

    // --- Transaction atomicity ---

    @Test
    fun `toggleHabitCompletion wraps all writes in a single transaction`() =
        runTest {
            val habitEntity =
                HabitEntity(id = 1L, title = "H1", description = "", createdDate = now)
            val chainId = 10L
            val chainWithMembers =
                HabitChainWithMembers(
                    chain = HabitChainEntity(id = chainId, title = "Chain"),
                    members =
                        listOf(
                            HabitChainMemberEntity(chainId = chainId, habitId = 1L, sortOrder = 0),
                        ),
                )

            coEvery { habitDao.getHabitById(1L) } returns habitEntity
            coEvery { habitChainMemberDao.getChainIdsForHabit(1L) } returns listOf(chainId)
            coEvery { habitChainMemberDao.getHabitIdsForChain(chainId) } returns listOf(1L)
            coEvery { habitChainDao.getHabitChainWithMembersById(chainId) } returns chainWithMembers
            coEvery { habitDao.getHabitsByIds(any()) } returns
                listOf(habitEntity.copy(completionHistory = today.toString()))

            repository.toggleHabitCompletion(1L, true, today)

            // Verify withTransaction was called — all DB writes happen atomically
            coVerify { database.withTransaction(any()) }

            // Verify all writes occurred inside the transaction
            coVerify { habitDao.updateHabitCompletionHistory(1L, any()) }
            coVerify { habitDao.updateHabitCompletion(1L, true) }
            coVerify { habitChainDao.updateHabitChainCompletionHistory(chainId, any()) }
        }

    @Test
    fun `toggleHabitCompletion uses transaction even for standalone habits`() =
        runTest {
            val habitEntity =
                HabitEntity(id = 1L, title = "H1", description = "", createdDate = now)

            coEvery { habitDao.getHabitById(1L) } returns habitEntity
            coEvery { habitChainMemberDao.getChainIdsForHabit(1L) } returns emptyList()

            repository.toggleHabitCompletion(1L, true, today)

            // Even standalone habits use transaction for consistency
            coVerify { database.withTransaction(any()) }
            coVerify { habitDao.updateHabitCompletionHistory(1L, any()) }
            coVerify { habitDao.updateHabitCompletion(1L, true) }
        }
}
