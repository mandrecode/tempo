package com.mandrecode.tempo.features.tasks.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class GetOverdueIncompleteTasksUseCaseTest {
    private val taskRepository: TaskRepository = mockk(relaxed = true)
    private val useCase = GetOverdueIncompleteTasksUseCase(taskRepository)

    private val now = LocalDateTime(2026, 7, 28, 9, 0)

    @Test
    fun `includes incomplete task whose reminder already passed`() =
        runTest {
            val overdue = task(id = 1L, reminderDate = LocalDateTime(2026, 7, 27, 18, 0))
            coEvery { taskRepository.getTasksWithReminders() } returns listOf(overdue)

            assertThat(useCase(now)).containsExactly(overdue)
        }

    @Test
    fun `includes task whose reminder fired earlier the same day`() =
        runTest {
            val earlierToday = task(id = 2L, reminderDate = LocalDateTime(2026, 7, 28, 8, 59))
            coEvery { taskRepository.getTasksWithReminders() } returns listOf(earlierToday)

            assertThat(useCase(now)).containsExactly(earlierToday)
        }

    @Test
    fun `excludes completed task`() =
        runTest {
            val completed =
                task(id = 3L, reminderDate = LocalDateTime(2026, 7, 27, 18, 0), isCompleted = true)
            coEvery { taskRepository.getTasksWithReminders() } returns listOf(completed)

            assertThat(useCase(now)).isEmpty()
        }

    @Test
    fun `excludes task without a reminder`() =
        runTest {
            coEvery { taskRepository.getTasksWithReminders() } returns listOf(task(id = 4L, reminderDate = null))

            assertThat(useCase(now)).isEmpty()
        }

    @Test
    fun `excludes task whose reminder is still ahead`() =
        runTest {
            val future = task(id = 5L, reminderDate = LocalDateTime(2026, 7, 28, 9, 1))
            coEvery { taskRepository.getTasksWithReminders() } returns listOf(future)

            assertThat(useCase(now)).isEmpty()
        }

    @Test
    fun `excludes task whose reminder is exactly now`() =
        runTest {
            coEvery { taskRepository.getTasksWithReminders() } returns listOf(task(id = 6L, reminderDate = now))

            assertThat(useCase(now)).isEmpty()
        }

    @Test
    fun `keeps subtasks and periodic occurrences that would have notified`() =
        runTest {
            val subtask =
                task(id = 7L, reminderDate = LocalDateTime(2026, 7, 27, 8, 0), parentTaskId = 1L)
            val periodic =
                task(id = 8L, reminderDate = LocalDateTime(2026, 7, 26, 8, 0), nextInstanceId = 9L)
            coEvery { taskRepository.getTasksWithReminders() } returns listOf(subtask, periodic)

            assertThat(useCase(now)).containsExactly(subtask, periodic)
        }

    @Test
    fun `never writes to the repository`() =
        runTest {
            coEvery { taskRepository.getTasksWithReminders() } returns
                listOf(task(id = 10L, reminderDate = LocalDateTime(2026, 7, 27, 18, 0)))

            useCase(now)

            coVerify(exactly = 1) { taskRepository.getTasksWithReminders() }
            confirmVerified(taskRepository)
        }

    private fun task(
        id: Long,
        reminderDate: LocalDateTime?,
        isCompleted: Boolean = false,
        parentTaskId: Long? = null,
        nextInstanceId: Long? = null,
    ): Task =
        Task(
            id = id,
            title = "Task $id",
            description = "",
            isCompleted = isCompleted,
            reminderDate = reminderDate,
            parentTaskId = parentTaskId,
            nextInstanceId = nextInstanceId,
        )
}
