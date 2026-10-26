package com.mandrecode.tempo.core.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.entity.TaskSectionEntity
import com.mandrecode.tempo.core.data.local.InMemoryTempoDatabaseRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskSectionDaoTest {
    @get:Rule
    val databaseRule = InMemoryTempoDatabaseRule()

    private val dao get() = databaseRule.database.taskSectionDao()

    @Test
    fun sectionsAreScopedToTheirCategoryAndOrdered() =
        runTest {
            dao.insertSection(TaskSectionEntity(categoryId = 1L, name = "Shopping", sortOrder = 1))
            dao.insertSection(TaskSectionEntity(categoryId = 1L, name = "Cat", sortOrder = 0))
            dao.insertSection(TaskSectionEntity(categoryId = 2L, name = "Sprint", sortOrder = 0))

            val sections = dao.getSectionsByCategorySync(1L)

            assertThat(sections.map { it.name }).containsExactly("Cat", "Shopping").inOrder()
            assertThat(dao.getMaxSortOrder(1L)).isEqualTo(1)
        }

    @Test
    fun updateSortOrderAndDeleteByCategory() =
        runTest {
            val id = dao.insertSection(TaskSectionEntity(categoryId = 1L, name = "Cat", sortOrder = 0))
            dao.insertSection(TaskSectionEntity(categoryId = 2L, name = "Sprint", sortOrder = 0))

            dao.updateSortOrder(id, 5)
            assertThat(dao.getSectionById(id)?.sortOrder).isEqualTo(5)

            dao.deleteSectionsByCategory(1L)
            assertThat(dao.getSectionsByCategorySync(1L)).isEmpty()
            assertThat(dao.getAllSectionsSync().map { it.name }).containsExactly("Sprint")
        }
}
