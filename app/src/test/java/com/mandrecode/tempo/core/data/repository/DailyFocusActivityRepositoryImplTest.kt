package com.mandrecode.tempo.core.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.entity.DailyFocusActivityEntity
import com.mandrecode.tempo.core.data.local.dao.DailyFocusActivityDao
import com.mandrecode.tempo.core.domain.model.DailyFocusActivity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Test

class DailyFocusActivityRepositoryImplTest {
    private val date = LocalDate(2026, 7, 29)
    private val dao = mockk<DailyFocusActivityDao>(relaxed = true)
    private val repository = DailyFocusActivityRepositoryImpl(dao)

    @Test
    fun `range results map to domain in order`() =
        runTest {
            every { dao.observeRange(any(), any()) } returns
                flowOf(
                    listOf(
                        DailyFocusActivityEntity("2026-07-28", 3, 1, 25),
                        DailyFocusActivityEntity("2026-07-29", 2, 2, 0),
                    ),
                )

            repository.observeRange(LocalDate(2026, 7, 28), date).test {
                val activities = awaitItem()

                assertThat(activities).hasSize(2)
                assertThat(activities.first().date).isEqualTo(LocalDate(2026, 7, 28))
                assertThat(activities.first().focusMinutes).isEqualTo(25)
                awaitComplete()
            }
        }

    @Test
    fun `a row with an unparseable date is dropped rather than taking the history down`() =
        runTest {
            every { dao.observeRange(any(), any()) } returns
                flowOf(
                    listOf(
                        DailyFocusActivityEntity("not-a-date", 1, 1, 0),
                        DailyFocusActivityEntity("2026-07-29", 2, 2, 0),
                    ),
                )

            repository.observeRange(date, date).test {
                assertThat(awaitItem()).hasSize(1)
                awaitComplete()
            }
        }

    @Test
    fun `getAll maps every readable row`() =
        runTest {
            coEvery { dao.getAll() } returns
                listOf(
                    DailyFocusActivityEntity("2026-07-29", 4, 2, 50),
                    DailyFocusActivityEntity("broken", 1, 0, 0),
                )

            val all = repository.getAll()

            assertThat(all).hasSize(1)
            assertThat(all.single().completedCount).isEqualTo(2)
        }

    @Test
    fun `getForDate returns null when the day was never recorded`() =
        runTest {
            coEvery { dao.getByDate(any()) } returns null

            assertThat(repository.getForDate(date)).isNull()
        }

    @Test
    fun `recording counts passes an ISO date through`() =
        runTest {
            repository.recordCounts(date, scheduledCount = 5, completedCount = 2)

            coVerify { dao.recordCounts("2026-07-29", 5, 2) }
        }

    @Test
    fun `negative counts are clamped rather than stored`() =
        runTest {
            repository.recordCounts(date, scheduledCount = -3, completedCount = -1)

            coVerify { dao.recordCounts("2026-07-29", 0, 0) }
        }

    @Test
    fun `focus minutes are added through the dao`() =
        runTest {
            repository.addFocusMinutes(date, 25)

            coVerify { dao.addFocusMinutes("2026-07-29", 25) }
        }

    @Test
    fun `a non-positive minute count is not written at all`() =
        runTest {
            repository.addFocusMinutes(date, 0)
            repository.addFocusMinutes(date, -5)

            coVerify(exactly = 0) { dao.addFocusMinutes(any(), any()) }
        }

    @Test
    fun `replaceAll converts the whole history to entities`() =
        runTest {
            repository.replaceAll(
                listOf(DailyFocusActivity(date = date, scheduledCount = 1, completedCount = 1)),
            )

            coVerify {
                dao.replaceAll(listOf(DailyFocusActivityEntity("2026-07-29", 1, 1, 0)))
            }
        }
}
