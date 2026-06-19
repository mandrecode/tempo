package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.core.domain.util.ValidationResult
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import jakarta.inject.Inject

class CreateCategoryUseCase
    @Inject
    constructor(
        private val categoryRepository: CategoryRepository,
    ) {
        sealed class Result {
            data class Success(
                val name: String,
            ) : Result()

            data object EmptyName : Result()

            data object TooLong : Result()

            data class AlreadyExists(
                val name: String,
            ) : Result()
        }

        suspend operator fun invoke(
            name: String,
            color: String? = null,
            icon: String? = null,
        ): Result {
            val trimmedName = name.trim()
            when (ValidationUtils.validateCategoryName(trimmedName)) {
                ValidationResult.Empty -> return Result.EmptyName
                ValidationResult.TooLong -> return Result.TooLong
                ValidationResult.Valid -> {}
                else -> {}
            }
            categoryRepository.getCategoryByName(trimmedName)?.let {
                return Result.AlreadyExists(trimmedName)
            }
            val nextSortOrder = categoryRepository.getMaxSortOrder() + 1
            categoryRepository.insertCategory(
                Category(name = trimmedName, color = color, icon = icon, sortOrder = nextSortOrder),
            )
            return Result.Success(trimmedName)
        }
    }
