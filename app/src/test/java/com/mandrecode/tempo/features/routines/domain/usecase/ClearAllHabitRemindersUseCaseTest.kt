package com.mandrecode.tempo.features.routines.domain.usecase

import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Test

class ClearAllHabitRemindersUseCaseTest {
    private lateinit var useCase: ClearAllHabitRemindersUseCase
    private lateinit var habitRepository: HabitRepository
    private lateinit var habitReminderScheduler: HabitReminderScheduler

    @Before
    fun setup() {
        habitRepository = mockk(relaxed = true)
        habitReminderScheduler = mockk(relaxed = true)
        useCase = ClearAllHabitRemindersUseCase(habitRepository, habitReminderScheduler)
    }

    @Test
    fun `clears all reminders and cancels each habit`() =
        runTest {
            val habits = listOf(habit(1L), habit(2L), habit(3L))
            coEvery { habitRepository.getHabitsWithReminders() } returns habits

            useCase()

            coVerify { habitRepository.clearAllReminders() }
            habits.forEach { habit ->
                coVerify { habitReminderScheduler.cancelHabit(habit) }
            }
        }

    @Test
    fun `fetches habits before clearing so cancel has correct data`() =
        runTest {
            val habits = listOf(habit(1L))
            coEvery { habitRepository.getHabitsWithReminders() } returns habits

            useCase()

            coVerifyOrder {
                habitRepository.getHabitsWithReminders()
                habitRepository.clearAllReminders()
                habitReminderScheduler.cancelHabit(habits[0])
            }
        }

    @Test
    fun `no habits with reminders still clears repo`() =
        runTest {
            coEvery { habitRepository.getHabitsWithReminders() } returns emptyList()

            useCase()

            coVerify { habitRepository.clearAllReminders() }
            coVerify(exactly = 0) { habitReminderScheduler.cancelHabit(any<Habit>()) }
        }

    private fun habit(id: Long) =
        Habit(
            id = id,
            title = "Habit $id",
            description = "",
            reminderDate = LocalDateTime(2099, 1, 1, 10, 0),
            createdDate = LocalDateTime(2020, 1, 1, 0, 0),
        )
}
