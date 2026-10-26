package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.model.SectionDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.repository.TaskSectionRepository
import jakarta.inject.Inject

/**
 * Deletes a section (#61), moving its tasks — completed ones too — to unsectioned. Reminders are
 * independent of sections, so there is no scheduler work here.
 */
class DeleteTaskSectionUseCase
    @Inject
    constructor(
        private val repository: TaskSectionRepository,
    ) {
        /** The snapshot undo needs, or null when the section was already gone. */
        suspend operator fun invoke(sectionId: Long): SectionDeletionSnapshot? = repository.deleteSectionWithSnapshot(sectionId)
    }
