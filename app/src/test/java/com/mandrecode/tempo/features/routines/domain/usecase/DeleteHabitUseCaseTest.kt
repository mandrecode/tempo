package com.mandrecode.tempo.features.routines.domain.usecase

import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Test

class DeleteHabitUseCaseTest {
    private lateinit var useCase: DeleteHabitUseCase
    private lateinit var habitRepository: HabitRepository
    private lateinit var habitReminderScheduler: HabitReminderScheduler

    @Before
    fun setup() {
        habitRepository = mockk(relaxed = true)
        habitReminderScheduler = mockk(relaxed = true)
        useCase = DeleteHabitUseCase(habitRepository, habitReminderScheduler)
    }

    @Test
    fun `deleting habit removes from repository and cancels reminder`() =
        runTest {
            val habit = habit()

            useCase(habit)

            coVerifyOrder {
                habitRepository.deleteHabit(habit)
                habitReminderScheduler.cancelHabit(habit)
            }
        }

    @Test
    fun `deleting habit without reminder still cancels scheduler`() =
        runTest {
            val habit = habit(reminderDate = null)

            useCase(habit)

            coVerify { habitRepository.deleteHabit(habit) }
            coVerify { habitReminderScheduler.cancelHabit(habit) }
        }

    private fun habit(
        id: Long = 1L,
        reminderDate: LocalDateTime? = LocalDateTime(2099, 1, 1, 10, 0),
    ) = Habit(
        id = id,
        title = "Test Habit",
        description = "desc",
        reminderDate = reminderDate,
        createdDate = LocalDateTime(2020, 1, 1, 0, 0),
    )
}
