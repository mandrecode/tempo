package com.mandrecode.tempo.features.tasks.data.repository

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.entity.CategoryEntity
import com.mandrecode.tempo.core.data.local.dao.CategoryDao
import com.mandrecode.tempo.features.tasks.data.repository.CategoryRepositoryImpl
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CategoryRepositoryTest {
    private lateinit var categoryDao: CategoryDao
    private lateinit var repository: CategoryRepository

    @Before
    fun setUp() {
        categoryDao = mockk(relaxed = true)
        repository = CategoryRepositoryImpl(categoryDao)
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
