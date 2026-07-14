package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.TaskDeletionSnapshot
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

class DeleteTaskUseCaseTest {
    private lateinit var useCase: DeleteTaskUseCase
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskReminderScheduler: TaskReminderScheduler

    @Before
    fun setup() {
        taskRepository = mockk(relaxed = true)
        taskReminderScheduler = mockk(relaxed = true)
        useCase = DeleteTaskUseCase(taskRepository, taskReminderScheduler)
    }

    @Test
    fun `deleting task cancels reminder then removes from repository`() =
        runTest {
            val task = task()
            coEvery { taskRepository.deleteTaskWithSnapshot(task.id) } returns
                TaskDeletionSnapshot.TaskTree(task.id, listOf(task))

            useCase(task)

            coVerifyOrder {
                taskRepository.deleteTaskWithSnapshot(task.id)
                taskReminderScheduler.cancel(task)
            }
        }

    @Test
    fun `deleting task without reminder still calls cancel and delete`() =
        runTest {
            val task = task(reminderDate = null)
            coEvery { taskRepository.deleteTaskWithSnapshot(task.id) } returns
                TaskDeletionSnapshot.TaskTree(task.id, listOf(task))

            useCase(task)

            verify { taskReminderScheduler.cancel(task) }
            coVerify { taskRepository.deleteTaskWithSnapshot(task.id) }
        }

    private fun task(
        id: Long = 1L,
        reminderDate: LocalDateTime? = LocalDateTime(2099, 1, 1, 10, 0),
    ) = Task(
        id = id,
        title = "Test Task",
        description = "desc",
        reminderDate = reminderDate,
    )
}
