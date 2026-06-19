package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SetDefaultCategoryUseCaseTest {
    private lateinit var useCase: SetDefaultCategoryUseCase
    private lateinit var categoryRepository: CategoryRepository

    @Before
    fun setup() {
        categoryRepository = mockk(relaxed = true)
        useCase = SetDefaultCategoryUseCase(categoryRepository)
    }

    @Test
    fun `delegates to repository setDefaultCategory`() =
        runTest {
            useCase(5L)

            coVerifyOrder {
                categoryRepository.setDefaultCategory(5L)
            }
        }
}
