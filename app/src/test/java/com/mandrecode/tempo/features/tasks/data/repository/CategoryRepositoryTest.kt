package com.mandrecode.tempo.features.tasks.data.repository

import androidx.room.withTransaction
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.entity.CategoryEntity
import com.mandrecode.tempo.core.data.entity.TaskEntity
import com.mandrecode.tempo.core.data.local.TempoDatabase
import com.mandrecode.tempo.core.data.local.dao.CategoryDao
import com.mandrecode.tempo.core.data.local.dao.TaskDao
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.CategoryDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class CategoryRepositoryTest {
    private lateinit var categoryDao: CategoryDao
    private lateinit var taskDao: TaskDao
    private lateinit var database: TempoDatabase
    private lateinit var repository: CategoryRepository

    @Before
    fun setUp() {
        categoryDao = mockk(relaxed = true)
        taskDao = mockk(relaxed = true)
        database = mockk(relaxed = true)
        mockkStatic("androidx.room.RoomDatabaseKt")
        @Suppress("UNCHECKED_CAST")
        coEvery { database.withTransaction(any<suspend () -> Any?>()) } coAnswers {
            (args[1] as (suspend () -> Any?)).invoke()
        }
        repository = CategoryRepositoryImpl(categoryDao, taskDao, database)
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `deleteCategoryWithSnapshot captures category and tasks before deleting`() =
        runTest {
            val category = Category(id = 4L, name = "Work")
            val task = TaskEntity(id = 40L, title = "Task", description = "", categoryId = 4L)
            coEvery { categoryDao.getCategoryById(4L) } returns CategoryEntity(id = 4L, name = "Work")
            coEvery { taskDao.getTasksByCategoryId(4L) } returns listOf(task)

            val result = repository.deleteCategoryWithSnapshot(category)

            assertThat(result.category).isEqualTo(category)
            assertThat(result.tasks.map(Task::id)).containsExactly(40L)
            coVerifyOrder {
                taskDao.deleteTasksByCategoryId(4L)
                categoryDao.deleteCategoryById(4L)
            }
        }

    @Test
    fun `restoreDeletedCategory inserts missing category and accepts matching tasks parent first`() =
        runTest {
            val category = Category(id = 5L, name = "Restored")
            val root = Task(id = 50L, title = "Root", description = "", categoryId = 5L)
            val child = Task(id = 51L, title = "Child", description = "", categoryId = 5L, parentTaskId = 50L)
            val grandchild =
                Task(id = 52L, title = "Grandchild", description = "", categoryId = 5L, parentTaskId = 51L)
            coEvery { categoryDao.getCategoryById(5L) } returns null
            coEvery { taskDao.getTaskById(50L) } returns null
            coEvery { taskDao.getTaskById(51L) } returns
                TaskEntity(id = 51L, title = "Child", description = "", categoryId = 5L, parentTaskId = 50L)
            coEvery { taskDao.getTaskById(52L) } returns null

            repository.restoreDeletedCategory(CategoryDeletionSnapshot(category, listOf(grandchild, child, root)))

            coVerify { categoryDao.insertCategory(CategoryEntity(id = 5L, name = "Restored")) }
            coVerifyOrder {
                taskDao.insertTask(TaskEntity(id = 50L, title = "Root", description = "", categoryId = 5L))
                taskDao.getTaskById(51L)
                taskDao.insertTask(
                    TaskEntity(
                        id = 52L,
                        title = "Grandchild",
                        description = "",
                        categoryId = 5L,
                        parentTaskId = 51L,
                    ),
                )
            }
            coVerify(exactly = 0) { taskDao.updateTask(any()) }
        }

    @Test
    fun `restoreDeletedCategory rejects a reused category id`() =
        runTest {
            val snapshot = CategoryDeletionSnapshot(Category(id = 5L, name = "Deleted"), emptyList())
            coEvery { categoryDao.getCategoryById(5L) } returns CategoryEntity(id = 5L, name = "Unrelated")

            val result = runCatching { repository.restoreDeletedCategory(snapshot) }

            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
            coVerify(exactly = 0) { categoryDao.updateCategory(any()) }
        }

    @Test
    fun `restoreDeletedCategory rejects a reused task id`() =
        runTest {
            val category = Category(id = 5L, name = "Restored")
            val task = Task(id = 50L, title = "Deleted", description = "", categoryId = 5L)
            coEvery { categoryDao.getCategoryById(5L) } returns CategoryEntity(id = 5L, name = "Restored")
            coEvery { taskDao.getTaskById(50L) } returns
                TaskEntity(id = 50L, title = "Unrelated", description = "", categoryId = 5L)

            val result =
                runCatching {
                    repository.restoreDeletedCategory(CategoryDeletionSnapshot(category, listOf(task)))
                }

            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
            coVerify(exactly = 0) { taskDao.updateTask(any()) }
        }

    @Test
    fun `getAllCategories delegates to dao`() =
        runTest {
            val entities =
                listOf(
                    CategoryEntity(id = 1, name = "Work"),
                    CategoryEntity(id = 2, name = "Personal"),
                )
            val expected =
                listOf(
                    Category(id = 1, name = "Work"),
                    Category(id = 2, name = "Personal"),
                )
            every { categoryDao.getAllCategories() } returns flowOf(entities)

            val result = repository.getAllCategories().first()
            assertThat(result).isEqualTo(expected)
        }

    @Test
    fun `getCategoryById delegates to dao`() =
        runTest {
            val entity = CategoryEntity(id = 1, name = "Work")
            coEvery { categoryDao.getCategoryById(1) } returns entity

            val result = repository.getCategoryById(1)
            assertThat(result).isEqualTo(Category(id = 1, name = "Work"))
        }

    @Test
    fun `getCategoryById returns null for non-existent id`() =
        runTest {
            coEvery { categoryDao.getCategoryById(999) } returns null

            val result = repository.getCategoryById(999)
            assertThat(result).isNull()
        }

    @Test
    fun `insertCategory delegates to dao and returns id`() =
        runTest {
            val category = Category(name = "New Category")
            coEvery { categoryDao.insertCategory(CategoryEntity(name = "New Category")) } returns 5L

            val result = repository.insertCategory(category)
            assertThat(result).isEqualTo(5L)
        }

    @Test
    fun `updateCategory delegates to dao`() =
        runTest {
            val category = Category(id = 1, name = "Updated")
            repository.updateCategory(category)
            coVerify { categoryDao.updateCategory(CategoryEntity(id = 1, name = "Updated")) }
        }

    @Test
    fun `deleteCategory delegates to dao`() =
        runTest {
            val category = Category(id = 1, name = "Work")
            repository.deleteCategory(category)
            coVerify { categoryDao.deleteCategory(CategoryEntity(id = 1, name = "Work")) }
        }

    @Test
    fun `deleteCategoryById delegates to dao`() =
        runTest {
            repository.deleteCategoryById(1)
            coVerify { categoryDao.deleteCategoryById(1) }
        }

    @Test
    fun `getCategoryByName delegates to dao`() =
        runTest {
            val entity = CategoryEntity(id = 1, name = "Work")
            coEvery { categoryDao.getCategoryByName("Work") } returns entity

            val result = repository.getCategoryByName("Work")
            assertThat(result).isEqualTo(Category(id = 1, name = "Work"))
        }
}
