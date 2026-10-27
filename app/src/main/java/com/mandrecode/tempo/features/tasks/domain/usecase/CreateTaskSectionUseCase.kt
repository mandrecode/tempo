package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.core.domain.util.ValidationResult
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.features.tasks.domain.repository.TaskSectionRepository
import jakarta.inject.Inject

/**
 * Adds a section (#61) to its category, last or right below another. Names follow the category
 * name rules and are unique within the category, ignoring case.
 */
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

            data class AlreadyExists(
                val name: String,
            ) : Result()

            data object TooLong : Result()
        }

        suspend operator fun invoke(
            categoryId: Long,
            name: String,
            afterSectionId: Long? = null,
        ): Result {
            val trimmedName = name.trim()
            return when (ValidationUtils.validateCategoryName(trimmedName)) {
                ValidationResult.Empty -> Result.EmptyName
                ValidationResult.TooLong -> Result.TooLong
                else ->
                    if (sectionRepository.getSectionsByCategory(categoryId).any { it.name.equals(trimmedName, true) }) {
                        Result.AlreadyExists(trimmedName)
                    } else {
                        Result.Success(sectionRepository.createSection(categoryId, trimmedName, afterSectionId))
                    }
            }
        }
    }
