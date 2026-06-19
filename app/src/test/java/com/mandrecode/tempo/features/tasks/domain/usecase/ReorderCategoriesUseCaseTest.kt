package com.mandrecode.tempo.features.tasks.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ReorderCategoriesUseCaseTest {
    private lateinit var useCase: ReorderCategoriesUseCase
    private lateinit var categoryRepository: CategoryRepository

    @Before
    fun setup() {
        categoryRepository = mockk(relaxed = true)
        useCase = ReorderCategoriesUseCase(categoryRepository)
    }

    @Test
    fun `reorder updates sortOrder for affected categories`() =
        runTest {
            val categories =
                listOf(
                    Category(id = 1L, name = "A", sortOrder = 0),
                    Category(id = 2L, name = "B", sortOrder = 1),
                    Category(id = 3L, name = "C", sortOrder = 2),
                )

            useCase(fromIndex = 0, toIndex = 2, categories = categories)

            val slot = slot<List<Category>>()
            coVerify { categoryRepository.updateCategories(capture(slot)) }
            val updated = slot.captured.sortedBy { it.id }
            assertThat(updated.find { it.id == 1L }?.sortOrder).isEqualTo(2)
            assertThat(updated.find { it.id == 2L }?.sortOrder).isEqualTo(0)
            assertThat(updated.find { it.id == 3L }?.sortOrder).isEqualTo(1)
        }

    @Test
    fun `no update when same position`() =
        runTest {
            val categories =
                listOf(
                    Category(id = 1L, name = "A", sortOrder = 0),
                    Category(id = 2L, name = "B", sortOrder = 1),
                )

            useCase(fromIndex = 0, toIndex = 0, categories = categories)

            coVerify(exactly = 0) { categoryRepository.updateCategories(any()) }
        }
}
