package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.TaskSectionRepository
import jakarta.inject.Inject

/**
 * Moves a top-level task into a section of its own category (#61), or out to unsectioned when
 * [sectionId] is null, at [index] among the section's active tasks.
 */
class MoveTaskToSectionUseCase
    @Inject
    constructor(
        private val sectionRepository: TaskSectionRepository,
    ) {
        /** Returns false when the move is not allowed (unknown task, or a section of another category). */
        suspend operator fun invoke(
            taskId: Long,
            sectionId: Long?,
            index: Int,
        ): Boolean = sectionRepository.moveTaskToSection(taskId, sectionId, index.coerceAtLeast(0))
    }
