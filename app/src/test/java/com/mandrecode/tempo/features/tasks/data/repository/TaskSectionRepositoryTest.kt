package com.mandrecode.tempo.features.tasks.data.repository

import androidx.room.withTransaction
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.entity.TaskEntity
import com.mandrecode.tempo.core.data.entity.TaskSectionEntity
import com.mandrecode.tempo.core.data.local.TempoDatabase
import com.mandrecode.tempo.core.data.local.dao.TaskDao
import com.mandrecode.tempo.core.data.local.dao.TaskSectionDao
import com.mandrecode.tempo.features.tasks.domain.model.SectionDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.model.TaskSection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class TaskSectionRepositoryTest {
    private lateinit var sectionDao: TaskSectionDao
    private lateinit var taskDao: TaskDao
    private lateinit var database: TempoDatabase
    private lateinit var repository: TaskSectionRepositoryImpl

    @Before
    fun setUp() {
        sectionDao = mockk(relaxed = true)
        taskDao = mockk(relaxed = true)
        database = mockk(relaxed = true)
        mockkStatic("androidx.room.RoomDatabaseKt")
        @Suppress("UNCHECKED_CAST")
        coEvery { database.withTransaction(any<suspend () -> Any?>()) } coAnswers {
            (args[1] as (suspend () -> Any?)).invoke()
        }
        repository = TaskSectionRepositoryImpl(sectionDao, taskDao, database)
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `createSection appends after the last section of the category`() =
        runTest {
            coEvery { sectionDao.getMaxSortOrder(3L) } returns 2
            coEvery { sectionDao.insertSection(any()) } returns 12L

            val id = repository.createSection(3L, "Garden")

            assertThat(id).isEqualTo(12L)
            coVerify { sectionDao.insertSection(TaskSectionEntity(categoryId = 3L, name = "Garden", sortOrder = 3)) }
        }

    @Test
    fun `createSection after a section renumbers the category in one transaction`() =
        runTest {
            coEvery { sectionDao.getMaxSortOrder(3L) } returns 2
            coEvery { sectionDao.insertSection(any()) } returns 12L
            coEvery { sectionDao.getSectionsByCategorySync(3L) } returns
                listOf(1L, 2L, 3L, 12L).mapIndexed { index, id ->
                    TaskSectionEntity(id = id, categoryId = 3L, name = "S$id", sortOrder = index)
                }

            repository.createSection(3L, "Garden", afterSectionId = 1L)

            coVerifyOrder {
                sectionDao.updateSortOrder(1L, 0)
                sectionDao.updateSortOrder(12L, 1)
                sectionDao.updateSortOrder(2L, 2)
                sectionDao.updateSortOrder(3L, 3)
            }
        }

    @Test
    fun `createSection starts at zero in a category without sections`() =
        runTest {
            coEvery { sectionDao.getMaxSortOrder(3L) } returns null

            repository.createSection(3L, "Cat")

            coVerify { sectionDao.insertSection(TaskSectionEntity(categoryId = 3L, name = "Cat", sortOrder = 0)) }
        }

    @Test
    fun `updateSortOrders numbers sections by position`() =
        runTest {
            repository.updateSortOrders(listOf(5L, 4L))

            coVerifyOrder {
                sectionDao.updateSortOrder(5L, 0)
                sectionDao.updateSortOrder(4L, 1)
            }
        }

    @Test
    fun `deleteSectionWithSnapshot unsections tasks before deleting the section`() =
        runTest {
            val section = TaskSectionEntity(id = 8L, categoryId = 3L, name = "Shopping", sortOrder = 1)
            coEvery { sectionDao.getSectionById(8L) } returns section
            coEvery { taskDao.getTopLevelTaskIdsInSection(8L) } returns listOf(20L, 21L)

            val snapshot = repository.deleteSectionWithSnapshot(8L)

            assertThat(snapshot).isEqualTo(
                SectionDeletionSnapshot(
                    TaskSection(id = 8L, categoryId = 3L, name = "Shopping", sortOrder = 1),
                    listOf(20L, 21L),
                ),
            )
            coVerifyOrder {
                taskDao.clearSection(8L)
                sectionDao.deleteSection(section)
            }
        }

    @Test
    fun `deleteSectionWithSnapshot returns null for a missing section`() =
        runTest {
            coEvery { sectionDao.getSectionById(8L) } returns null

            assertThat(repository.deleteSectionWithSnapshot(8L)).isNull()
            coVerify(exactly = 0) { taskDao.clearSection(any()) }
        }

    @Test
    fun `restoreDeletedSection reinserts the section and reassigns tasks still in the category`() =
        runTest {
            val section = TaskSection(id = 8L, categoryId = 3L, name = "Shopping", sortOrder = 1)
            coEvery { sectionDao.getSectionById(8L) } returns null

            repository.restoreDeletedSection(SectionDeletionSnapshot(section, listOf(20L, 21L)))

            coVerifyOrder {
                sectionDao.insertSection(TaskSectionEntity(id = 8L, categoryId = 3L, name = "Shopping", sortOrder = 1))
                taskDao.assignSection(listOf(20L, 21L), 8L, 3L)
            }
        }

    @Test
    fun `moveTaskToSection inserts at the index and renumbers the target section`() =
        runTest {
            val moving = task(id = 30L, sectionId = 9L, sortOrder = 7)
            coEvery { taskDao.getTaskById(30L) } returns moving
            coEvery { sectionDao.getSectionById(8L) } returns TaskSectionEntity(id = 8L, categoryId = 3L, name = "Cat")
            coEvery { taskDao.getActiveTopLevelTasksInSection(3L, 8L) } returns
                listOf(
                    task(id = 20L, sectionId = 8L, sortOrder = 0),
                    task(id = 21L, sectionId = 8L, sortOrder = 1),
                )

            val moved = repository.moveTaskToSection(30L, 8L, 1)

            assertThat(moved).isTrue()
            coVerify { taskDao.updateTaskSectionAndSortOrder(30L, 8L, 1) }
            coVerify { taskDao.updateTaskSectionAndSortOrder(21L, 8L, 2) }
            coVerify(exactly = 0) { taskDao.updateTaskSectionAndSortOrder(20L, any(), any()) }
        }

    @Test
    fun `moveTaskToSection to unsectioned clears the section`() =
        runTest {
            val moving = TaskEntity(id = 30L, title = "Cat food", description = "", categoryId = 3L, sectionId = 9L)
            coEvery { taskDao.getTaskById(30L) } returns moving
            coEvery { taskDao.getActiveTopLevelTasksInSection(3L, null) } returns emptyList()

            assertThat(repository.moveTaskToSection(30L, null, 0)).isTrue()
            coVerify { taskDao.updateTaskSectionAndSortOrder(30L, null, 0) }
        }

    @Test
    fun `moveTaskToSection rejects a section of another category`() =
        runTest {
            coEvery { taskDao.getTaskById(30L) } returns
                TaskEntity(id = 30L, title = "Cat food", description = "", categoryId = 3L)
            coEvery { sectionDao.getSectionById(50L) } returns
                TaskSectionEntity(id = 50L, categoryId = 4L, name = "Sprint")

            assertThat(repository.moveTaskToSection(30L, 50L, 0)).isFalse()
            coVerify(exactly = 0) { taskDao.updateTaskSectionAndSortOrder(any(), any(), any()) }
        }

    @Test
    fun `moveTaskToSection rejects a subtask`() =
        runTest {
            coEvery { taskDao.getTaskById(31L) } returns
                TaskEntity(id = 31L, title = "Sub", description = "", categoryId = 3L, parentTaskId = 30L)

            assertThat(repository.moveTaskToSection(31L, null, 0)).isFalse()
        }

    private fun task(
        id: Long,
        sectionId: Long?,
        sortOrder: Int,
    ) = TaskEntity(
        id = id,
        title = "Task $id",
        description = "",
        categoryId = 3L,
        sectionId = sectionId,
        sortOrder = sortOrder,
    )
}
