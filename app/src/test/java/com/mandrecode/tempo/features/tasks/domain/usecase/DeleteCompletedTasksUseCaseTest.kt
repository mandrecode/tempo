package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteCompletedTasksUseCaseTest {
    private lateinit var useCase: DeleteCompletedTasksUseCase
    private lateinit var taskRepository: TaskRepository

    @Before
    fun setup() {
        taskRepository = mockk(relaxed = true)
        useCase = DeleteCompletedTasksUseCase(taskRepository)
    }

    @Test
    fun `deletes completed tasks for given category`() =
        runTest {
            useCase(categoryId = 42L)

            coVerify { taskRepository.deleteCompletedTasksWithSnapshot(42L) }
        }
}
