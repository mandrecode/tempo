package com.mandrecode.tempo.features.tasks.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.TaskDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class RestoreDeletedTasksUseCaseTest {
    private val repository = mockk<TaskRepository>(relaxed = true)
    private val scheduler = mockk<TaskReminderScheduler>(relaxed = true)
    private val useCase = RestoreDeletedTasksUseCase(repository, scheduler)

    @Test
    fun `restores data before scheduling eligible reminders`() =
        runTest {
            val reminder = LocalDateTime(2099, 1, 1, 10, 0)
            val task = Task(id = 1, title = "Task", description = "", reminderDate = reminder)
            val snapshot = TaskDeletionSnapshot.TaskTree(task.id, listOf(task))
            every { scheduler.schedule(task) } returns ScheduleResult.Success(reminder)

            val result = useCase(snapshot)

            coVerifyOrder {
                repository.restoreDeletedTasks(snapshot)
                scheduler.schedule(task)
            }
            assertThat(result.hasSchedulingFailure).isFalse()
        }

    @Test
    fun `reports scheduling failure while keeping restore complete`() =
        runTest {
            val task = Task(id = 1, title = "Task", description = "", reminderDate = LocalDateTime(2099, 1, 1, 10, 0))
            val snapshot = TaskDeletionSnapshot.CompletedTasks(2, listOf(task))
            every { scheduler.schedule(task) } returns ScheduleResult.PermissionError("permission")

            assertThat(useCase(snapshot).hasSchedulingFailure).isTrue()
        }
}
