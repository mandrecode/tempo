package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.model.SectionDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.repository.TaskSectionRepository
import jakarta.inject.Inject

/**
 * Undoes [DeleteTaskSectionUseCase] (#61): the section comes back in its place and takes back
 * the tasks that are still in its category.
 */
class RestoreDeletedTaskSectionUseCase
    @Inject
    constructor(
        private val sectionRepository: TaskSectionRepository,
    ) {
        suspend operator fun invoke(snapshot: SectionDeletionSnapshot) {
            sectionRepository.restoreDeletedSection(snapshot)
        }
    }
