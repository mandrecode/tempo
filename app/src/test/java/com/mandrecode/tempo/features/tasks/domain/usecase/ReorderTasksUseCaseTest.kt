package com.mandrecode.tempo.features.tasks.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ReorderTasksUseCaseTest {
    private lateinit var useCase: ReorderTasksUseCase
    private lateinit var taskRepository: TaskRepository

    @Before
    fun setup() {
        taskRepository = mockk(relaxed = true)
        useCase = ReorderTasksUseCase(taskRepository)
    }

    @Test
    fun `reorder swaps tasks and updates sort orders`() =
        runTest {
            val tasks =
                listOf(
                    task(id = 1L, sortOrder = 0),
                    task(id = 2L, sortOrder = 1),
                    task(id = 3L, sortOrder = 2),
                )

            useCase(fromIndex = 0, toIndex = 2, tasks = tasks)

            val slot = slot<List<Task>>()
            coVerify { taskRepository.updateTasks(capture(slot)) }
            val updated = slot.captured
            // Task 1 moved from index 0 to index 2, so tasks 2 and 3 shift left
            assertThat(updated.any { it.id == 2L && it.sortOrder == 0 }).isTrue()
            assertThat(updated.any { it.id == 3L && it.sortOrder == 1 }).isTrue()
            assertThat(updated.any { it.id == 1L && it.sortOrder == 2 }).isTrue()
        }

    @Test
    fun `no-op when from and to are the same index`() =
        runTest {
            val tasks =
                listOf(
                    task(id = 1L, sortOrder = 0),
                    task(id = 2L, sortOrder = 1),
                )

            useCase(fromIndex = 0, toIndex = 0, tasks = tasks)

            coVerify(exactly = 0) { taskRepository.updateTasks(any()) }
        }

    @Test
    fun `uses minimum sort order from tasks as base`() =
        runTest {
            val tasks =
                listOf(
                    task(id = 1L, sortOrder = 10),
                    task(id = 2L, sortOrder = 11),
                    task(id = 3L, sortOrder = 12),
                )

            useCase(fromIndex = 2, toIndex = 0, tasks = tasks)

            val slot = slot<List<Task>>()
            coVerify { taskRepository.updateTasks(capture(slot)) }
            val updated = slot.captured
            // Task 3 moved to front: new order is 3,1,2 with base 10
            assertThat(updated.any { it.id == 3L && it.sortOrder == 10 }).isTrue()
            assertThat(updated.any { it.id == 1L && it.sortOrder == 11 }).isTrue()
            assertThat(updated.any { it.id == 2L && it.sortOrder == 12 }).isTrue()
        }

    @Test
    fun `only tasks with changed sort order are included in update`() =
        runTest {
            val tasks =
                listOf(
                    task(id = 1L, sortOrder = 0),
                    task(id = 2L, sortOrder = 1),
                    task(id = 3L, sortOrder = 2),
                )

            // Move task at index 1 to index 2 (swap last two)
            useCase(fromIndex = 1, toIndex = 2, tasks = tasks)

            val slot = slot<List<Task>>()
            coVerify { taskRepository.updateTasks(capture(slot)) }
            val updated = slot.captured
            // Task 1 stays at 0 (not included), tasks 2 and 3 swap
            assertThat(updated).hasSize(2)
            assertThat(updated.none { it.id == 1L }).isTrue()
        }

    private fun task(
        id: Long,
        sortOrder: Int,
    ) = Task(
        id = id,
        title = "Task $id",
        description = "",
        sortOrder = sortOrder,
    )
}
