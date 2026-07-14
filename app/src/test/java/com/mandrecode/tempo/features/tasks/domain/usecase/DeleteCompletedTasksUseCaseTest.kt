package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.TaskDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteCompletedTasksUseCaseTest {
    private lateinit var useCase: DeleteCompletedTasksUseCase
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskReminderScheduler: TaskReminderScheduler

    @Before
    fun setup() {
        taskRepository = mockk(relaxed = true)
        taskReminderScheduler = mockk(relaxed = true)
        useCase = DeleteCompletedTasksUseCase(taskRepository, taskReminderScheduler)
    }

    @Test
    fun `deletes completed tasks for given category`() =
        runTest {
            val task = Task(id = 7L, title = "Completed", description = "", isCompleted = true)
            coEvery { taskRepository.deleteCompletedTasksWithSnapshot(42L) } returns
                TaskDeletionSnapshot.CompletedTasks(categoryId = 42L, tasks = listOf(task))

            useCase(categoryId = 42L)

            coVerify { taskRepository.deleteCompletedTasksWithSnapshot(42L) }
            coVerify { taskReminderScheduler.cancel(task) }
        }
}
