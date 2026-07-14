package com.mandrecode.tempo.features.routines.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitDeletionSnapshot
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class RestoreDeletedHabitUseCaseTest {
    @Test
    fun `restores habit and skips absent reminder`() =
        runTest {
            val repository = mockk<HabitRepository>(relaxed = true)
            val scheduler = mockk<HabitReminderScheduler>(relaxed = true)
            val habit = Habit(id = 3, title = "Read", description = "", createdDate = LocalDateTime(2026, 1, 1, 0, 0))
            val snapshot = HabitDeletionSnapshot(habit, emptyList())

            val result = RestoreDeletedHabitUseCase(repository, scheduler)(snapshot)

            coVerify { repository.restoreDeletedHabit(snapshot) }
            coVerify(exactly = 0) { scheduler.scheduleHabit(any()) }
            assertThat(result.scheduleResults).isEmpty()
        }
}
