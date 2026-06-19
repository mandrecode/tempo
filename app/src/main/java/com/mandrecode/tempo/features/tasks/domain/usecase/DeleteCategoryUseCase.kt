package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import jakarta.inject.Inject

class DeleteCategoryUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val categoryRepository: CategoryRepository,
    ) {
        sealed class Result {
            data object Success : Result()

            data object LastCategory : Result()

            data object CannotDeleteDefault : Result()
        }

        suspend operator fun invoke(category: Category): Result {
            if (category.isDefault) {
                return Result.CannotDeleteDefault
            }
            if (categoryRepository.getCategoryCount() <= 1) {
                return Result.LastCategory
            }
            taskRepository.deleteTasksByCategoryId(category.id)
            categoryRepository.deleteCategory(category)
            return Result.Success
        }
    }
