package com.mandrecode.tempo.features.routines.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitChainDeletionSnapshot
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class RestoreDeletedHabitChainUseCaseTest {
    @Test
    fun `restores snapshot before scheduling chain reminder`() =
        runTest {
            val repository = mockk<HabitChainRepository>(relaxed = true)
            val scheduler = mockk<HabitReminderScheduler>(relaxed = true)
            val reminder = LocalDateTime(2099, 1, 1, 10, 0)
            val chain = HabitChain(id = 7, title = "Morning", periodicReminder = reminder)
            val snapshot = HabitChainDeletionSnapshot(chain, emptyList(), listOf(chain), false)
            coEvery { scheduler.scheduleHabitChain(chain) } returns ScheduleResult.Success(reminder)

            val result = RestoreDeletedHabitChainUseCase(repository, scheduler)(snapshot)

            coVerifyOrder {
                repository.restoreDeletedHabitChain(snapshot)
                scheduler.scheduleHabitChain(chain)
            }
            assertThat(result.hasSchedulingFailure).isFalse()
        }
}
