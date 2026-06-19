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

class CreateCategoryUseCaseTest {
    private lateinit var useCase: CreateCategoryUseCase
    private lateinit var categoryRepository: CategoryRepository

    @Before
    fun setup() {
        categoryRepository = mockk(relaxed = true)
        coEvery { categoryRepository.getCategoryByName(any()) } returns null
        useCase = CreateCategoryUseCase(categoryRepository)
    }

    @Test
    fun `valid name creates category and returns success`() =
        runTest {
            val result = useCase("Work")

            assertThat(result).isInstanceOf(CreateCategoryUseCase.Result.Success::class.java)
            assertThat((result as CreateCategoryUseCase.Result.Success).name).isEqualTo("Work")

            val slot = slot<Category>()
            coVerify { categoryRepository.insertCategory(capture(slot)) }
            assertThat(slot.captured.name).isEqualTo("Work")
        }

    @Test
    fun `name is trimmed before validation and insertion`() =
        runTest {
            val result = useCase("  Work  ")

            assertThat((result as CreateCategoryUseCase.Result.Success).name).isEqualTo("Work")
        }

    @Test
    fun `empty name returns EmptyName`() =
        runTest {
            val result = useCase("")

            assertThat(result).isEqualTo(CreateCategoryUseCase.Result.EmptyName)
            coVerify(exactly = 0) { categoryRepository.insertCategory(any()) }
        }

    @Test
    fun `blank name returns EmptyName`() =
        runTest {
            val result = useCase("   ")

            assertThat(result).isEqualTo(CreateCategoryUseCase.Result.EmptyName)
        }

    @Test
    fun `name exceeding max length returns TooLong`() =
        runTest {
            val longName = "A".repeat(51)

            val result = useCase(longName)

            assertThat(result).isEqualTo(CreateCategoryUseCase.Result.TooLong)
            coVerify(exactly = 0) { categoryRepository.insertCategory(any()) }
        }

    @Test
    fun `duplicate name returns AlreadyExists`() =
        runTest {
            coEvery { categoryRepository.getCategoryByName("Work") } returns Category(id = 1L, name = "Work")

            val result = useCase("Work")

            assertThat(result).isInstanceOf(CreateCategoryUseCase.Result.AlreadyExists::class.java)
            assertThat((result as CreateCategoryUseCase.Result.AlreadyExists).name).isEqualTo("Work")
            coVerify(exactly = 0) { categoryRepository.insertCategory(any()) }
        }

    @Test
    fun `color and icon are passed to inserted category`() =
        runTest {
            val result = useCase("Work", color = "red", icon = "briefcase")

            assertThat(result).isInstanceOf(CreateCategoryUseCase.Result.Success::class.java)

            val slot = slot<Category>()
            coVerify { categoryRepository.insertCategory(capture(slot)) }
            assertThat(slot.captured.color).isEqualTo("red")
            assertThat(slot.captured.icon).isEqualTo("briefcase")
        }
}
