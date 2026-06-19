package com.mandrecode.tempo.features.tasks.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateCategoryUseCaseTest {
    private lateinit var useCase: UpdateCategoryUseCase
    private lateinit var categoryRepository: CategoryRepository

    @Before
    fun setup() {
        categoryRepository = mockk(relaxed = true)
        coEvery { categoryRepository.getCategoryByName(any()) } returns null
        useCase = UpdateCategoryUseCase(categoryRepository)
    }

    @Test
    fun `valid name updates category and returns success`() =
        runTest {
            val category = Category(id = 1L, name = "Work")

            val result = useCase(category)

            assertThat(result).isEqualTo(UpdateCategoryUseCase.Result.Success)

            val slot = slot<Category>()
            coVerify { categoryRepository.updateCategory(capture(slot)) }
            assertThat(slot.captured.name).isEqualTo("Work")
            assertThat(slot.captured.id).isEqualTo(1L)
        }

    @Test
    fun `name is trimmed before validation`() =
        runTest {
            val category = Category(id = 1L, name = "  Work  ")

            val result = useCase(category)

            assertThat(result).isEqualTo(UpdateCategoryUseCase.Result.Success)
            val slot = slot<Category>()
            coVerify { categoryRepository.updateCategory(capture(slot)) }
            assertThat(slot.captured.name).isEqualTo("Work")
        }

    @Test
    fun `empty name returns EmptyName`() =
        runTest {
            val result = useCase(Category(id = 1L, name = ""))

            assertThat(result).isEqualTo(UpdateCategoryUseCase.Result.EmptyName)
            coVerify(exactly = 0) { categoryRepository.updateCategory(any()) }
        }

    @Test
    fun `name exceeding max length returns TooLong`() =
        runTest {
            val result = useCase(Category(id = 1L, name = "A".repeat(51)))

            assertThat(result).isEqualTo(UpdateCategoryUseCase.Result.TooLong)
            coVerify(exactly = 0) { categoryRepository.updateCategory(any()) }
        }

    @Test
    fun `duplicate name returns AlreadyExists`() =
        runTest {
            coEvery { categoryRepository.getCategoryByName("Work") } returns Category(id = 2L, name = "Work")

            val result = useCase(Category(id = 1L, name = "Work"))

            assertThat(result).isInstanceOf(UpdateCategoryUseCase.Result.AlreadyExists::class.java)
            assertThat((result as UpdateCategoryUseCase.Result.AlreadyExists).name).isEqualTo("Work")
            coVerify(exactly = 0) { categoryRepository.updateCategory(any()) }
        }

    @Test
    fun `updating only color or icon with same name succeeds`() =
        runTest {
            coEvery { categoryRepository.getCategoryByName("Work") } returns Category(id = 1L, name = "Work")

            val result = useCase(Category(id = 1L, name = "Work", color = "blue", icon = "star"))

            assertThat(result).isEqualTo(UpdateCategoryUseCase.Result.Success)

            val slot = slot<Category>()
            coVerify { categoryRepository.updateCategory(capture(slot)) }
            assertThat(slot.captured.color).isEqualTo("blue")
            assertThat(slot.captured.icon).isEqualTo("star")
        }
}
