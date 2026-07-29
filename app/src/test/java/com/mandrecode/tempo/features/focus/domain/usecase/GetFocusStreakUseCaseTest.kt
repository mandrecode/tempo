package com.mandrecode.tempo.features.focus.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.DailyFocusActivity
import com.mandrecode.tempo.core.domain.repository.DailyFocusActivityRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.Test

class GetFocusStreakUseCaseTest {
    private val today = LocalDate(2026, 7, 29)
    private val repository = mockk<DailyFocusActivityRepository>()
    private val useCase = GetFocusStreakUseCase(repository)

    private fun daysAgo(n: Int): LocalDate = today.minus(n, DateTimeUnit.DAY)

    private fun day(
        n: Int,
        scheduled: Int,
        completed: Int,
    ) = DailyFocusActivity(date = daysAgo(n), scheduledCount = scheduled, completedCount = completed)

    private suspend fun streakOf(vararg days: DailyFocusActivity): Int {
        coEvery { repository.getAll() } returns days.toList()
        return useCase(today)
    }

    @Test
    fun `no history means no streak`() =
        runTest {
            assertThat(streakOf()).isEqualTo(0)
        }

    @Test
    fun `consecutive completed days count`() =
        runTest {
            val streak =
                streakOf(
                    day(0, scheduled = 3, completed = 3),
                    day(1, scheduled = 3, completed = 2),
                    day(2, scheduled = 3, completed = 1),
                )

            assertThat(streak).isEqualTo(3)
        }

    @Test
    fun `a partial day still counts`() =
        runTest {
            assertThat(streakOf(day(0, scheduled = 9, completed = 1))).isEqualTo(1)
        }

    @Test
    fun `a scheduled day with nothing completed breaks the streak`() =
        runTest {
            val streak =
                streakOf(
                    day(0, scheduled = 2, completed = 2),
                    day(1, scheduled = 2, completed = 0),
                    day(2, scheduled = 2, completed = 2),
                )

            assertThat(streak).isEqualTo(1)
        }

    @Test
    fun `an unscheduled day is skipped rather than breaking the streak`() =
        runTest {
            val streak =
                streakOf(
                    day(0, scheduled = 2, completed = 2),
                    day(1, scheduled = 0, completed = 0),
                    day(2, scheduled = 2, completed = 2),
                )

            assertThat(streak).isEqualTo(2)
        }

    @Test
    fun `today not started yet does not break a run built up to yesterday`() =
        runTest {
            val streak =
                streakOf(
                    day(0, scheduled = 5, completed = 0),
                    day(1, scheduled = 2, completed = 2),
                    day(2, scheduled = 2, completed = 1),
                )

            assertThat(streak).isEqualTo(2)
        }

    @Test
    fun `today with no record at all does not break the run`() =
        runTest {
            val streak =
                streakOf(
                    day(1, scheduled = 2, completed = 2),
                    day(2, scheduled = 2, completed = 2),
                )

            assertThat(streak).isEqualTo(2)
        }

    @Test
    fun `an unrecorded past day breaks the streak`() =
        runTest {
            val streak =
                streakOf(
                    day(0, scheduled = 2, completed = 2),
                    // nothing recorded for yesterday
                    day(2, scheduled = 2, completed = 2),
                )

            assertThat(streak).isEqualTo(1)
        }

    @Test
    fun `the streak stops at the earliest recorded day`() =
        runTest {
            val streak =
                streakOf(
                    day(0, scheduled = 1, completed = 1),
                    day(1, scheduled = 1, completed = 1),
                )

            assertThat(streak).isEqualTo(2)
        }
}
