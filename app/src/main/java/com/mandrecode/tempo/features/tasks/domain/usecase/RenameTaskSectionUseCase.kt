package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.core.domain.util.ValidationResult
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.features.tasks.domain.repository.TaskSectionRepository
import jakarta.inject.Inject

/** Renames a section (#61); a blank, over-long or already used name leaves it unchanged. */
class RenameTaskSectionUseCase
    @Inject
    constructor(
        private val sectionRepository: TaskSectionRepository,
    ) {
        sealed class Result {
            data object Success : Result()

            data object EmptyName : Result()

            data object TooLong : Result()

            data object NotFound : Result()

            data class AlreadyExists(
                val name: String,
            ) : Result()
        }

        suspend operator fun invoke(
            sectionId: Long,
            name: String,
        ): Result {
            val trimmedName = name.trim()
            return when (ValidationUtils.validateCategoryName(trimmedName)) {
                ValidationResult.Empty -> Result.EmptyName
                ValidationResult.TooLong -> Result.TooLong
                else -> {
                    val section = sectionRepository.getSectionById(sectionId) ?: return Result.NotFound
                    val clash =
                        sectionRepository
                            .getSectionsByCategory(section.categoryId)
                            .any { it.id != sectionId && it.name.equals(trimmedName, ignoreCase = true) }
                    if (clash) {
                        Result.AlreadyExists(trimmedName)
                    } else {
                        sectionRepository.renameSection(sectionId, trimmedName)
                        Result.Success
                    }
                }
            }
        }
    }
