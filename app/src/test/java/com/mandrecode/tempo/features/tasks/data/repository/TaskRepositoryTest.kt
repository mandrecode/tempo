package com.mandrecode.tempo.features.tasks.data.repository

import com.mandrecode.tempo.core.data.local.TempoDatabase
import com.mandrecode.tempo.core.data.local.dao.TaskDao
import com.mandrecode.tempo.features.tasks.data.repository.TaskRepositoryImpl
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
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
        repository = TaskRepositoryImpl(taskDao, database)
    }

    @Test
    fun `deleteCompletedTasksByCategoryId deletes top level tasks and subtasks`() =
        runTest {
            val categoryId = 1L
            val parentIds = listOf(10L, 11L)
            coEvery { taskDao.getCompletedTopLevelTaskIds(categoryId) } returns parentIds

            repository.deleteCompletedTasksByCategoryId(categoryId)

            coVerify { taskDao.getCompletedTopLevelTaskIds(categoryId) }
            coVerify { taskDao.deleteSubtasksByParentIds(parentIds) }
            coVerify { taskDao.deleteTasksByIds(parentIds) }
        }

    @Test
    fun `deleteCompletedTasksByCategoryId does nothing when no completed top level tasks`() =
        runTest {
            val categoryId = 1L
            coEvery { taskDao.getCompletedTopLevelTaskIds(categoryId) } returns emptyList()

            repository.deleteCompletedTasksByCategoryId(categoryId)

            coVerify { taskDao.getCompletedTopLevelTaskIds(categoryId) }
            coVerify(exactly = 0) { taskDao.deleteSubtasksByParentIds(any()) }
            coVerify(exactly = 0) { taskDao.deleteTasksByIds(any()) }
        }

    @Test
    fun `deleteCompletedTasksAtOrBefore deletes eligible task trees`() =
        runTest {
            val cutoff = LocalDateTime(2026, 6, 13, 10, 30)
            repository.deleteCompletedTasksAtOrBefore(cutoff)

            coVerify { taskDao.deleteCompletedTaskTreesAtOrBefore(cutoff.toString()) }
        }
}
