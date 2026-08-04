package com.mandrecode.tempo.features.tasks.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class GetUndatedTasksUseCaseTest {
    private val taskRepository: TaskRepository = mockk(relaxed = true)
    private val categoryRepository: CategoryRepository = mockk(relaxed = true)
    private val useCase = GetUndatedTasksUseCase(taskRepository, categoryRepository)

    private val work = Category(id = 1L, name = "Work", icon = "work")
    private val home = Category(id = 2L, name = "Home", icon = null)

    @Test
    fun `lists open top-level tasks without a reminder`() =
        runTest {
            givenTasks(
                task(id = 1L, title = "Call the plumber"),
                task(id = 2L, title = "Renew the passport"),
            )

            useCase().test {
                assertThat(awaitItem().map { it.task.title })
                    .containsExactly("Call the plumber", "Renew the passport")
                awaitComplete()
            }
        }

    @Test
    fun `excludes subtasks, completed work and tasks that already have a date`() =
        runTest {
            givenTasks(
                task(id = 1L, title = "Loose end"),
                task(id = 2L, title = "Step", parentTaskId = 1L),
                task(id = 3L, title = "Done", isCompleted = true),
                task(id = 4L, title = "Dated", reminderDate = LocalDateTime(2026, 8, 4, 9, 0)),
            )

            useCase().test {
                assertThat(awaitItem().map { it.task.title }).containsExactly("Loose end")
                awaitComplete()
            }
        }

    @Test
    fun `carries a task's own steps, in their order`() =
        runTest {
            givenTasks(
                task(id = 1L, title = "Move flat"),
                task(id = 3L, title = "Second step", parentTaskId = 1L, sortOrder = 2),
                task(id = 2L, title = "First step", parentTaskId = 1L, sortOrder = 1),
                task(id = 4L, title = "Unrelated"),
            )

            useCase().test {
                val rows = awaitItem().associateBy { it.task.title }
                assertThat(rows.getValue("Move flat").subtasks.map { it.title })
                    .containsExactly("First step", "Second step")
                    .inOrder()
                assertThat(rows.getValue("Unrelated").subtasks).isEmpty()
                awaitComplete()
            }
        }

    @Test
    fun `carries each task's category`() =
        runTest {
            givenTasks(
                task(id = 1L, title = "Invoice", categoryId = work.id),
                task(id = 2L, title = "Bins", categoryId = home.id),
            )

            useCase().test {
                val byTitle = awaitItem().associateBy { it.task.title }
                assertThat(byTitle.getValue("Invoice").category).isEqualTo(work)
                assertThat(byTitle.getValue("Bins").category).isEqualTo(home)
                awaitComplete()
            }
        }

    @Test
    fun `a task whose category is missing still appears`() =
        runTest {
            givenTasks(task(id = 1L, title = "Orphan", categoryId = 99L))

            useCase().test {
                val only = awaitItem().single()
                assertThat(only.task.title).isEqualTo("Orphan")
                assertThat(only.category).isNull()
                awaitComplete()
            }
        }

    @Test
    fun `rows are ordered by sort order`() =
        runTest {
            givenTasks(
                task(id = 1L, title = "Third", sortOrder = 3),
                task(id = 2L, title = "First", sortOrder = 1),
                task(id = 3L, title = "Second", sortOrder = 2),
            )

            useCase().test {
                assertThat(awaitItem().map { it.task.title })
                    .containsExactly("First", "Second", "Third")
                    .inOrder()
                awaitComplete()
            }
        }

    @Test
    fun `pinnedTo keeps a task that has since been given a date`() =
        runTest {
            givenTasks(
                task(id = 1L, title = "Just planned", reminderDate = LocalDateTime(2026, 8, 4, 9, 0)),
                task(id = 2L, title = "Still loose"),
                task(id = 3L, title = "Never in the sheet"),
            )

            useCase.pinnedTo(setOf(1L, 2L)).test {
                assertThat(awaitItem().map { it.task.title })
                    .containsExactly("Just planned", "Still loose")
                awaitComplete()
            }
        }

    @Test
    fun `pinnedTo drops a task that no longer exists`() =
        runTest {
            givenTasks(task(id = 2L, title = "Survivor"))

            useCase.pinnedTo(setOf(1L, 2L)).test {
                assertThat(awaitItem().map { it.task.title }).containsExactly("Survivor")
                awaitComplete()
            }
        }

    private fun givenTasks(vararg tasks: Task) {
        every { taskRepository.getAllTasks() } returns flowOf(tasks.toList())
        every { categoryRepository.getAllCategories() } returns flowOf(listOf(work, home))
    }

    private fun task(
        id: Long,
        title: String,
        categoryId: Long = work.id,
        isCompleted: Boolean = false,
        reminderDate: LocalDateTime? = null,
        parentTaskId: Long? = null,
        sortOrder: Int = 0,
    ) = Task(
        id = id,
        title = title,
        description = "",
        categoryId = categoryId,
        isCompleted = isCompleted,
        reminderDate = reminderDate,
        parentTaskId = parentTaskId,
        sortOrder = sortOrder,
    )
}
