package com.mandrecode.tempo.features.tasks.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteCategoryUseCaseTest {
    private lateinit var useCase: DeleteCategoryUseCase
    private lateinit var taskRepository: TaskRepository
    private lateinit var categoryRepository: CategoryRepository

    @Before
    fun setup() {
        taskRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        coEvery { categoryRepository.getCategoryCount() } returns 3
        useCase = DeleteCategoryUseCase(taskRepository, categoryRepository)
    }

    @Test
    fun `deletes tasks by category then deletes category`() =
        runTest {
            val category = Category(id = 5L, name = "Work")

            val result = useCase(category)

            assertThat(result).isEqualTo(DeleteCategoryUseCase.Result.Success)
            coVerifyOrder {
                taskRepository.deleteTasksByCategoryId(5L)
                categoryRepository.deleteCategory(category)
            }
        }

    @Test
    fun `returns LastCategory when only one category exists`() =
        runTest {
            coEvery { categoryRepository.getCategoryCount() } returns 1

            val result = useCase(Category(id = 5L, name = "Work"))

            assertThat(result).isEqualTo(DeleteCategoryUseCase.Result.LastCategory)
            coVerify(exactly = 0) { taskRepository.deleteTasksByCategoryId(any()) }
            coVerify(exactly = 0) { categoryRepository.deleteCategory(any()) }
        }

    @Test
    fun `returns CannotDeleteDefault when deleting Inbox as default`() =
        runTest {
            val result = useCase(Category(id = -1L, name = "Inbox", isDefault = true))

            assertThat(result).isEqualTo(DeleteCategoryUseCase.Result.CannotDeleteDefault)
            coVerify(exactly = 0) { taskRepository.deleteTasksByCategoryId(any()) }
            coVerify(exactly = 0) { categoryRepository.deleteCategory(any()) }
        }

    @Test
    fun `returns CannotDeleteDefault when deleting default category`() =
        runTest {
            val result = useCase(Category(id = 5L, name = "Work", isDefault = true))

            assertThat(result).isEqualTo(DeleteCategoryUseCase.Result.CannotDeleteDefault)
            coVerify(exactly = 0) { taskRepository.deleteTasksByCategoryId(any()) }
            coVerify(exactly = 0) { categoryRepository.deleteCategory(any()) }
        }
}
