package com.mandrecode.tempo.features.routines.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.entity.HabitEntity
import com.mandrecode.tempo.core.data.local.InMemoryTempoDatabaseRule
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
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
