package com.mandrecode.tempo.features.routines.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.local.InMemoryTempoDatabaseRule
import com.mandrecode.tempo.features.routines.domain.model.Habit
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Clock

@RunWith(AndroidJUnit4::class)
class HabitRepositoryRoomIntegrationTest {
    @get:Rule
    val databaseRule = InMemoryTempoDatabaseRule()

    private lateinit var repository: HabitRepositoryImpl

    @Before
    fun setUp() {
        repository =
            HabitRepositoryImpl(
                habitDao = databaseRule.database.habitDao(),
                habitChainDao = databaseRule.database.habitChainDao(),
                habitChainMemberDao = databaseRule.database.habitChainMemberDao(),
                liveActivityManager = mockk(relaxed = true),
                database = databaseRule.database,
            )
    }

    @Test
    fun insertAndFetchHabit_roundTripsThroughRoom() =
        runTest {
            val createdDate = LocalDateTime(2026, 6, 17, 12, 0, 0)
            val insertedId =
                repository.insertHabit(
                    Habit(
                        title = "Meditate",
                        description = "10 min",
                        createdDate = createdDate,
                    ),
                )

            val fetched = repository.getHabitById(insertedId)
            val all = repository.getAllHabits().first()

            assertThat(fetched).isNotNull()
            assertThat(fetched!!.title).isEqualTo("Meditate")
            assertThat(fetched.createdDate).isEqualTo(createdDate)
            assertThat(all).hasSize(1)
        }

    @Test
    fun toggleHabitCompletion_updatesCompletionHistoryAndLegacyFlag() =
        runTest {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val createdDate = LocalDateTime(today.year, today.month, today.day, 12, 0, 0)
            val habitId =
                repository.insertHabit(
                    Habit(
                        title = "Read",
                        description = "20 pages",
                        createdDate = createdDate,
                    ),
                )

            repository.toggleHabitCompletion(habitId, isCompleted = true, date = today, fromNotification = false)

            val fetched = repository.getHabitById(habitId)
            assertThat(fetched).isNotNull()
            assertThat(fetched!!.isCompleted).isTrue()
            assertThat(fetched.completionHistory).contains(today.toString())
        }

    @Test
    fun toggleHabitCompletion_forPastDate_updatesCompletionHistoryWithoutLegacyFlag() =
        runTest {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val pastDate = today.minus(1, DateTimeUnit.DAY)
            val createdDate = LocalDateTime(pastDate.year, pastDate.month, pastDate.day, 12, 0, 0)
            val habitId =
                repository.insertHabit(
                    Habit(
                        title = "Read",
                        description = "20 pages",
                        createdDate = createdDate,
                    ),
                )

            repository.toggleHabitCompletion(habitId, isCompleted = true, date = pastDate, fromNotification = false)

            val fetched = repository.getHabitById(habitId)
            assertThat(fetched).isNotNull()
            assertThat(fetched!!.completionHistory).contains(pastDate.toString())
            assertThat(fetched.isCompleted).isFalse()
        }
}
