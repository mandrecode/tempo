package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
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

            useCase(task)

            coVerifyOrder {
                taskReminderScheduler.cancel(task)
                taskRepository.deleteTask(task)
            }
        }

    @Test
    fun `deleting task without reminder still calls cancel and delete`() =
        runTest {
            val task = task(reminderDate = null)

            useCase(task)

            verify { taskReminderScheduler.cancel(task) }
            coVerify { taskRepository.deleteTask(task) }
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
