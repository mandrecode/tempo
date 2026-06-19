package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Test

class ClearAllTaskRemindersUseCaseTest {
    private lateinit var useCase: ClearAllTaskRemindersUseCase
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskReminderScheduler: TaskReminderScheduler

    @Before
    fun setup() {
        taskRepository = mockk(relaxed = true)
        taskReminderScheduler = mockk(relaxed = true)
        useCase = ClearAllTaskRemindersUseCase(taskRepository, taskReminderScheduler)
    }

    @Test
    fun `clears all reminders and cancels each task`() =
        runTest {
            val tasks = listOf(task(1L), task(2L))
            coEvery { taskRepository.getTasksWithReminders() } returns tasks

            useCase()

            coVerify { taskRepository.clearAllReminders() }
            tasks.forEach { t ->
                verify { taskReminderScheduler.cancel(t) }
            }
        }

    @Test
    fun `fetches tasks before clearing so cancel has correct data`() =
        runTest {
            val tasks = listOf(task(1L))
            coEvery { taskRepository.getTasksWithReminders() } returns tasks

            useCase()

            coVerifyOrder {
                taskRepository.getTasksWithReminders()
                taskRepository.clearAllReminders()
            }
            verify { taskReminderScheduler.cancel(tasks[0]) }
        }

    @Test
    fun `no tasks with reminders still clears repo`() =
        runTest {
            coEvery { taskRepository.getTasksWithReminders() } returns emptyList()

            useCase()

            coVerify { taskRepository.clearAllReminders() }
            verify(exactly = 0) { taskReminderScheduler.cancel(any()) }
        }

    private fun task(id: Long) =
        Task(
            id = id,
            title = "Task $id",
            description = "",
            reminderDate = LocalDateTime(2099, 1, 1, 10, 0),
        )
}
