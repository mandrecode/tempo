package com.mandrecode.tempo.features.tasks.data.repository

import androidx.room.withTransaction
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.entity.TaskEntity
import com.mandrecode.tempo.core.data.local.TempoDatabase
import com.mandrecode.tempo.core.data.local.dao.TaskDao
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.TaskDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskRepositoryTest {
    private lateinit var repository: TaskRepository
    private lateinit var taskDao: TaskDao
    private lateinit var database: TempoDatabase

    @Before
    fun setup() {
        taskDao = mockk(relaxed = true)
        database = mockk(relaxed = true)
        mockkStatic("androidx.room.RoomDatabaseKt")
        @Suppress("UNCHECKED_CAST")
        coEvery { database.withTransaction(any<suspend () -> Any?>()) } coAnswers {
            (args[1] as (suspend () -> Any?)).invoke()
        }
        repository = TaskRepositoryImpl(taskDao, database)
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `deleteTaskWithSnapshot captures the full task tree before deleting`() =
        runTest {
            val root = TaskEntity(id = 10L, title = "Root", description = "")
            val child = TaskEntity(id = 11L, title = "Child", description = "", parentTaskId = 10L)
            val grandchild = TaskEntity(id = 12L, title = "Grandchild", description = "", parentTaskId = 11L)
            coEvery { taskDao.getTaskTrees(listOf(10L)) } returns listOf(root, child, grandchild)

            val result = repository.deleteTaskWithSnapshot(10L)

            assertThat(result.rootTaskId).isEqualTo(10L)
            assertThat(result.tasks.map(Task::id)).containsExactly(10L, 11L, 12L).inOrder()
            coVerify { taskDao.deleteTaskTrees(listOf(10L)) }
        }

    @Test
    fun `deleteCompletedTasksWithSnapshot captures completed trees`() =
        runTest {
            val root = TaskEntity(id = 20L, title = "Done", description = "", isCompleted = true, categoryId = 2L)
            val child = TaskEntity(id = 21L, title = "Child", description = "", categoryId = 2L, parentTaskId = 20L)
            val grandchild =
                TaskEntity(
                    id = 22L,
                    title = "Grandchild",
                    description = "",
                    categoryId = 2L,
                    parentTaskId = 21L,
                )
            coEvery { taskDao.getCompletedTopLevelTaskIds(2L) } returns listOf(20L)
            coEvery { taskDao.getTaskTrees(listOf(20L)) } returns listOf(root, child, grandchild)

            val result = repository.deleteCompletedTasksWithSnapshot(2L)

            assertThat(result.tasks.map(Task::id)).containsExactly(20L, 21L, 22L).inOrder()
            coVerify { taskDao.deleteTaskTrees(listOf(20L)) }
        }

    @Test
    fun `restoreDeletedTasks inserts missing tasks and accepts matching tasks parent first`() =
        runTest {
            val root = Task(id = 30L, title = "Root", description = "")
            val child = Task(id = 31L, title = "Child", description = "", parentTaskId = 30L)
            coEvery { taskDao.getTaskById(30L) } returns null
            coEvery { taskDao.getTaskById(31L) } returns
                TaskEntity(id = 31L, title = "Child", description = "", categoryId = 0L, parentTaskId = 30L)

            repository.restoreDeletedTasks(
                TaskDeletionSnapshot.TaskTree(rootTaskId = 30L, tasks = listOf(child, root)),
            )

            coVerifyOrder {
                taskDao.insertTask(TaskEntity(id = 30L, title = "Root", description = "", categoryId = 0L))
                taskDao.getTaskById(31L)
            }
            coVerify(exactly = 0) { taskDao.updateTask(any()) }
        }

    @Test
    fun `restoreDeletedTasks rejects a reused task id`() =
        runTest {
            val snapshot =
                TaskDeletionSnapshot.TaskTree(
                    rootTaskId = 30L,
                    tasks = listOf(Task(id = 30L, title = "Deleted", description = "")),
                )
            coEvery { taskDao.getTaskById(30L) } returns TaskEntity(id = 30L, title = "Unrelated", description = "")

            val result = runCatching { repository.restoreDeletedTasks(snapshot) }

            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
            coVerify(exactly = 0) { taskDao.updateTask(any()) }
        }

    @Test
    fun `deleteCompletedTasksByCategoryId deletes top level tasks and subtasks`() =
        runTest {
            val categoryId = 1L
            val parentIds = listOf(10L, 11L)
            coEvery { taskDao.getCompletedTopLevelTaskIds(categoryId) } returns parentIds

            repository.deleteCompletedTasksByCategoryId(categoryId)

            coVerify { taskDao.getCompletedTopLevelTaskIds(categoryId) }
            coVerify { taskDao.deleteTaskTrees(parentIds) }
        }

    @Test
    fun `deleteCompletedTasksByCategoryId does nothing when no completed top level tasks`() =
        runTest {
            val categoryId = 1L
            coEvery { taskDao.getCompletedTopLevelTaskIds(categoryId) } returns emptyList()

            repository.deleteCompletedTasksByCategoryId(categoryId)

            coVerify { taskDao.getCompletedTopLevelTaskIds(categoryId) }
            coVerify(exactly = 0) { taskDao.deleteTaskTrees(any()) }
        }

    @Test
    fun `deleteCompletedTasksAtOrBefore deletes eligible task trees`() =
        runTest {
            val cutoff = LocalDateTime(2026, 6, 13, 10, 30)
            repository.deleteCompletedTasksAtOrBefore(cutoff)

            coVerify { taskDao.deleteCompletedTaskTreesAtOrBefore(cutoff.toString()) }
        }
}
