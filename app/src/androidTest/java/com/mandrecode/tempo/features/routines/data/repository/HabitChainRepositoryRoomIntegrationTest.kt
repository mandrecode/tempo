package com.mandrecode.tempo.features.routines.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.entity.HabitEntity
import com.mandrecode.tempo.core.data.local.InMemoryTempoDatabaseRule
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitChainDeletionSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitChainRepositoryRoomIntegrationTest {
    @get:Rule
    val databaseRule = InMemoryTempoDatabaseRule()

    private lateinit var repository: HabitChainRepositoryImpl
    private val createdDate = LocalDateTime(2026, 6, 17, 12, 0, 0)

    @Before
    fun setUp() {
        repository =
            HabitChainRepositoryImpl(
                databaseRule.database.habitChainDao(),
                databaseRule.database.habitChainMemberDao(),
                databaseRule.database.habitDao(),
                databaseRule.database,
            )
    }

    @Test
    fun insertAndFetchHabitChain_preservesMemberOrder() =
        runTest {
            val habitAId = insertHabit("Hydrate")
            val habitBId = insertHabit("Stretch")
            val chainId =
                repository.insertHabitChain(
                    HabitChain(
                        title = "Morning",
                        description = "Kickoff",
                        habitIds = listOf(habitBId, habitAId),
                        createdDate = createdDate,
                    ),
                )

            val fetched = repository.getHabitChainById(chainId)

            assertThat(fetched).isNotNull()
            assertThat(fetched!!.habitIds).containsExactly(habitBId, habitAId).inOrder()
            assertThat(repository.getAllHabitChains().first()).hasSize(1)
        }

    @Test
    fun deleteChainAndHabits_restorePreservesMembersAndStableIds() =
        runTest {
            val habitAId = insertHabit("Hydrate")
            val habitBId = insertHabit("Stretch")
            val chainId =
                repository.insertHabitChain(
                    HabitChain(title = "Morning", habitIds = listOf(habitBId, habitAId), createdDate = createdDate),
                )

            val snapshot = repository.deleteHabitChainWithSnapshot(chainId, deleteHabits = true)
            assertThat(repository.getHabitChainById(chainId)).isNull()

            repository.restoreDeletedHabitChain(snapshot)
            repository.restoreDeletedHabitChain(snapshot)

            assertThat(repository.getHabitChainById(chainId)?.habitIds).containsExactly(habitBId, habitAId).inOrder()
            assertThat(databaseRule.database.habitDao().getHabitById(habitAId)).isNotNull()
            assertThat(databaseRule.database.habitDao().getHabitById(habitBId)).isNotNull()
        }

    @Test
    fun restoreDeletedHabitChain_reusedHabitIdRollsBackChainRestore() =
        runTest {
            val habitId = insertHabit("Deleted")
            val chainId =
                repository.insertHabitChain(
                    HabitChain(title = "Morning", habitIds = listOf(habitId), createdDate = createdDate),
                )
            val snapshot = repository.deleteHabitChainWithSnapshot(chainId, deleteHabits = true)
            databaseRule.database.habitDao().insertHabit(
                HabitEntity(id = habitId, title = "Unrelated", description = "", createdDate = createdDate),
            )

            val result = runCatching { repository.restoreDeletedHabitChain(snapshot) }

            assertThat(result.isFailure).isTrue()
            assertThat(repository.getHabitChainById(chainId)).isNull()
            assertThat(
                databaseRule.database
                    .habitDao()
                    .getHabitById(habitId)
                    ?.title,
            ).isEqualTo("Unrelated")
        }

    @Test
    fun restoreDeletedHabitChain_reusedChainIdDoesNotOverwriteNewerRow() =
        runTest {
            val habitId = insertHabit("Preserved")
            val chainId =
                repository.insertHabitChain(
                    HabitChain(title = "Deleted", habitIds = listOf(habitId), createdDate = createdDate),
                )
            val snapshot = repository.deleteHabitChainWithSnapshot(chainId, deleteHabits = false)
            repository.insertHabitChain(HabitChain(id = chainId, title = "Unrelated", createdDate = createdDate))

            val result = runCatching { repository.restoreDeletedHabitChain(snapshot) }

            assertThat(result.isFailure).isTrue()
            assertThat(repository.getHabitChainById(chainId)?.title).isEqualTo("Unrelated")
        }

    @Test
    fun failedRestore_rollsBackChainInsert() =
        runTest {
            val chain = HabitChain(id = 999, title = "Broken", habitIds = listOf(404), createdDate = createdDate)
            val snapshot =
                HabitChainDeletionSnapshot(
                    chain = chain,
                    habitsBeforeDeletion = emptyList(),
                    affectedChains = listOf(chain),
                    deletedHabits = true,
                )

            runCatching { repository.restoreDeletedHabitChain(snapshot) }

            assertThat(repository.getHabitChainById(chain.id)).isNull()
        }

    @Test
    fun updateAndToggleHabitChainCompletion_persistsChanges() =
        runTest {
            val habitAId = insertHabit("Hydrate")
            val habitBId = insertHabit("Stretch")
            val chainId =
                repository.insertHabitChain(
                    HabitChain(
                        title = "Morning",
                        habitIds = listOf(habitAId, habitBId),
                        createdDate = createdDate,
                    ),
                )
            val updated =
                HabitChain(
                    id = chainId,
                    title = "Morning Updated",
                    habitIds = listOf(habitBId),
                    createdDate = createdDate,
                )

            repository.updateHabitChain(updated)
            repository.toggleHabitChainCompletion(chainId, isCompleted = true, date = LocalDate(2026, 6, 17))

            val fetched = repository.getHabitChainById(chainId)
            assertThat(fetched).isNotNull()
            assertThat(fetched!!.title).isEqualTo("Morning Updated")
            assertThat(fetched.habitIds).containsExactly(habitBId)
            assertThat(fetched.completionHistory).contains("2026-06-17")
            assertThat(repository.getChainsForHabit(habitAId)).isEmpty()
            assertThat(repository.getChainsForHabit(habitBId)).hasSize(1)
        }

    private suspend fun insertHabit(title: String): Long =
        databaseRule.database.habitDao().insertHabit(
            HabitEntity(
                title = title,
                description = "",
                createdDate = createdDate,
            ),
        )
}
