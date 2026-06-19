package com.mandrecode.tempo.features.routines.data.repository

import androidx.room.withTransaction
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.entity.HabitChainEntity
import com.mandrecode.tempo.core.data.entity.HabitChainMemberEntity
import com.mandrecode.tempo.core.data.entity.HabitChainWithMembers
import com.mandrecode.tempo.core.data.local.TempoDatabase
import com.mandrecode.tempo.core.data.local.dao.HabitChainDao
import com.mandrecode.tempo.core.data.local.dao.HabitChainMemberDao
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Test

class HabitChainRepositoryTest {
    private lateinit var habitChainDao: HabitChainDao
    private lateinit var habitChainMemberDao: HabitChainMemberDao
    private lateinit var database: TempoDatabase
    private lateinit var repository: HabitChainRepository

    private val fixedDate = LocalDateTime(2024, 1, 1, 12, 0, 0)

    private val testChainEntity =
        HabitChainEntity(
            id = 1,
            title = "Morning Routine",
            completionHistory = "",
            createdDate = fixedDate,
        )

    private val testChainMembers =
        listOf(
            HabitChainMemberEntity(chainId = 1, habitId = 1, sortOrder = 0),
            HabitChainMemberEntity(chainId = 1, habitId = 2, sortOrder = 1),
            HabitChainMemberEntity(chainId = 1, habitId = 3, sortOrder = 2),
        )

    private val testChainWithMembers =
        HabitChainWithMembers(
            chain = testChainEntity,
            members = testChainMembers,
        )

    private val testChain =
        HabitChain(
            id = 1,
            title = "Morning Routine",
            habitIds = listOf(1L, 2L, 3L),
            completionHistory = "",
            createdDate = fixedDate,
        )

    @Before
    fun setUp() {
        habitChainDao = mockk(relaxed = true)
        habitChainMemberDao = mockk(relaxed = true)
        database = mockk(relaxed = true)

        mockkStatic("androidx.room.RoomDatabaseKt")
        @Suppress("UNCHECKED_CAST")
        coEvery { database.withTransaction(any<suspend () -> Any?>()) } coAnswers {
            (args[1] as (suspend () -> Any?)).invoke()
        }

        repository = HabitChainRepositoryImpl(habitChainDao, habitChainMemberDao, database)
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `getAllHabitChains delegates to dao`() =
        runTest {
            every { habitChainDao.getAllHabitChainsWithMembers() } returns flowOf(listOf(testChainWithMembers))

            val result = repository.getAllHabitChains().first()
            assertThat(result).containsExactly(testChain)
        }

    @Test
    fun `getHabitChainById delegates to dao`() =
        runTest {
            coEvery { habitChainDao.getHabitChainWithMembersById(1) } returns testChainWithMembers

            val result = repository.getHabitChainById(1)
            assertThat(result).isEqualTo(testChain)
        }

    @Test
    fun `insertHabitChain delegates to dao and returns id`() =
        runTest {
            coEvery { habitChainDao.insertHabitChain(any()) } returns 1L

            val result = repository.insertHabitChain(testChain)
            assertThat(result).isEqualTo(1L)
            coVerify { habitChainMemberDao.insertMembers(any()) }
        }

    @Test
    fun `updateHabitChain delegates to dao`() =
        runTest {
            repository.updateHabitChain(testChain)
            coVerify { habitChainDao.updateHabitChain(any()) }
            coVerify { habitChainMemberDao.deleteByChainId(testChain.id) }
            coVerify { habitChainMemberDao.insertMembers(any()) }
        }

    @Test
    fun `deleteHabitChain delegates to dao`() =
        runTest {
            repository.deleteHabitChain(testChain)
            coVerify { habitChainDao.deleteHabitChain(any()) }
        }

    @Test
    fun `toggleHabitChainCompletion updates history when chain exists`() =
        runTest {
            val date = LocalDate(2024, 6, 15)
            coEvery { habitChainDao.getHabitChainById(1) } returns testChainEntity

            repository.toggleHabitChainCompletion(1, true, date)

            coVerify {
                habitChainDao.updateHabitChainCompletionHistory(1, any())
            }
        }

    @Test
    fun `toggleHabitChainCompletion does nothing when chain not found`() =
        runTest {
            val date = LocalDate(2024, 6, 15)
            coEvery { habitChainDao.getHabitChainById(999) } returns null

            repository.toggleHabitChainCompletion(999, true, date)

            coVerify(exactly = 0) {
                habitChainDao.updateHabitChainCompletionHistory(any(), any())
            }
        }

    @Test
    fun `getHabitChainsWithReminders delegates to dao`() =
        runTest {
            coEvery { habitChainDao.getHabitChainsWithReminders() } returns listOf(testChainWithMembers)

            val result = repository.getHabitChainsWithReminders()
            assertThat(result).containsExactly(testChain)
        }

    @Test
    fun `getChainsForHabit returns chains containing the habit`() =
        runTest {
            coEvery { habitChainMemberDao.getChainIdsForHabit(1L) } returns listOf(1L)
            coEvery { habitChainDao.getHabitChainsWithMembersByIds(listOf(1L)) } returns listOf(testChainWithMembers)

            val result = repository.getChainsForHabit(1L)
            assertThat(result).containsExactly(testChain)
        }

    @Test
    fun `getChainsForHabit returns empty list when habit is not in any chain`() =
        runTest {
            coEvery { habitChainMemberDao.getChainIdsForHabit(999L) } returns emptyList()

            val result = repository.getChainsForHabit(999L)
            assertThat(result).isEmpty()
        }

    @Test
    fun `getChainsForHabit fetches multiple chains in single query`() =
        runTest {
            val secondChainEntity =
                HabitChainEntity(
                    id = 2,
                    title = "Evening Routine",
                    completionHistory = "",
                    createdDate = fixedDate,
                )
            val secondChainWithMembers =
                HabitChainWithMembers(
                    chain = secondChainEntity,
                    members = listOf(HabitChainMemberEntity(chainId = 2, habitId = 1, sortOrder = 0)),
                )

            coEvery { habitChainMemberDao.getChainIdsForHabit(1L) } returns listOf(1L, 2L)
            coEvery { habitChainDao.getHabitChainsWithMembersByIds(listOf(1L, 2L)) } returns
                listOf(testChainWithMembers, secondChainWithMembers)

            val result = repository.getChainsForHabit(1L)
            assertThat(result).hasSize(2)
            coVerify(exactly = 1) { habitChainDao.getHabitChainsWithMembersByIds(any()) }
        }
}
