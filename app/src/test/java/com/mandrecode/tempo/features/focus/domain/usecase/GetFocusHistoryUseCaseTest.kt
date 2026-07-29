package com.mandrecode.tempo.features.focus.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.DailyFocusActivity
import com.mandrecode.tempo.core.domain.model.FocusDayState
import com.mandrecode.tempo.core.domain.repository.DailyFocusActivityRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.Test

class GetFocusHistoryUseCaseTest {
    private val today = LocalDate(2026, 7, 29)
    private val repository = mockk<DailyFocusActivityRepository>()
    private val useCase = GetFocusHistoryUseCase(repository)

    private fun daysAgo(n: Int): LocalDate = today.minus(n, DateTimeUnit.DAY)

    @Test
    fun `returns one entry per day ending today, oldest first`() =
        runTest {
            every { repository.observeRange(any(), any()) } returns flowOf(emptyList())

            useCase(today, days = 7).test {
                val history = awaitItem()

                assertThat(history).hasSize(7)
                assertThat(history.first().date).isEqualTo(daysAgo(6))
                assertThat(history.last().date).isEqualTo(today)
                awaitComplete()
            }
        }

    @Test
    fun `days with no record are filled in as quiet`() =
        runTest {
            every { repository.observeRange(any(), any()) } returns
                flowOf(
                    listOf(
                        DailyFocusActivity(date = today, scheduledCount = 2, completedCount = 2),
                    ),
                )

            useCase(today, days = 3).test {
                val history = awaitItem()

                assertThat(history.map { it.state })
                    .containsExactly(FocusDayState.QUIET, FocusDayState.QUIET, FocusDayState.ALL)
                    .inOrder()
                awaitComplete()
            }
        }

    @Test
    fun `recorded days keep their counts`() =
        runTest {
            every { repository.observeRange(any(), any()) } returns
                flowOf(
                    listOf(
                        DailyFocusActivity(date = daysAgo(1), scheduledCount = 4, completedCount = 1),
                    ),
                )

            useCase(today, days = 2).test {
                val history = awaitItem()

                assertThat(history.first().scheduledCount).isEqualTo(4)
                assertThat(history.first().completedCount).isEqualTo(1)
                assertThat(history.first().state).isEqualTo(FocusDayState.SOME)
                awaitComplete()
            }
        }

    @Test
    fun `requests the range it intends to render`() =
        runTest {
            every { repository.observeRange(daysAgo(6), today) } returns flowOf(emptyList())

            useCase(today, days = 7).test {
                assertThat(awaitItem()).hasSize(7)
                awaitComplete()
            }
        }

    @Test
    fun `a non-positive day count still yields today`() =
        runTest {
            every { repository.observeRange(any(), any()) } returns flowOf(emptyList())

            useCase(today, days = 0).test {
                val history = awaitItem()

                assertThat(history).hasSize(1)
                assertThat(history.single().date).isEqualTo(today)
                awaitComplete()
            }
        }
}
