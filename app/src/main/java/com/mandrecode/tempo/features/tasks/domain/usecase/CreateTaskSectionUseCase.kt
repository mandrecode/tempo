package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.core.domain.util.ValidationResult
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.features.tasks.domain.repository.TaskSectionRepository
import jakarta.inject.Inject

/** Adds a section (#61) as the last one of its category. Names follow the category name rules. */
class CreateTaskSectionUseCase
    @Inject
    constructor(
        private val sectionRepository: TaskSectionRepository,
    ) {
        sealed class Result {
            data class Success(
                val sectionId: Long,
            ) : Result()

            data object EmptyName : Result()

            data object TooLong : Result()
        }

        suspend operator fun invoke(
            categoryId: Long,
            name: String,
        ): Result {
            val trimmedName = name.trim()
            return when (ValidationUtils.validateCategoryName(trimmedName)) {
                ValidationResult.Empty -> Result.EmptyName
                ValidationResult.TooLong -> Result.TooLong
                else -> Result.Success(sectionRepository.createSection(categoryId, trimmedName))
            }
        }
    }
