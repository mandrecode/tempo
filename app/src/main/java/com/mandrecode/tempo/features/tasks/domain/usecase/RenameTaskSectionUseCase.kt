package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.core.domain.util.ValidationResult
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.features.tasks.domain.repository.TaskSectionRepository
import jakarta.inject.Inject

/** Renames a section (#61); a blank or over-long name leaves it unchanged. */
class RenameTaskSectionUseCase
    @Inject
    constructor(
        private val sectionRepository: TaskSectionRepository,
    ) {
        /** Returns true when the section was renamed. */
        suspend operator fun invoke(
            sectionId: Long,
            name: String,
        ): Boolean {
            val trimmedName = name.trim()
            return when (ValidationUtils.validateCategoryName(trimmedName)) {
                ValidationResult.Empty, ValidationResult.TooLong -> false
                else -> {
                    sectionRepository.renameSection(sectionId, trimmedName)
                    true
                }
            }
        }
    }
