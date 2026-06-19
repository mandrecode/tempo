package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.core.domain.util.ValidationResult
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import jakarta.inject.Inject

class UpdateCategoryUseCase
    @Inject
    constructor(
        private val categoryRepository: CategoryRepository,
    ) {
        sealed class Result {
            data object Success : Result()

            data object EmptyName : Result()

            data object TooLong : Result()

            data class AlreadyExists(
                val name: String,
            ) : Result()
        }

        suspend operator fun invoke(category: Category): Result {
            val trimmedName = category.name.trim()
            when (ValidationUtils.validateCategoryName(trimmedName)) {
                ValidationResult.Empty -> return Result.EmptyName
                ValidationResult.TooLong -> return Result.TooLong
                ValidationResult.Valid -> {}
                else -> {}
            }
            categoryRepository.getCategoryByName(trimmedName)?.let { existing ->
                if (existing.id != category.id) {
                    return Result.AlreadyExists(trimmedName)
                }
            }
            categoryRepository.updateCategory(category.copy(name = trimmedName))
            return Result.Success
        }
    }
