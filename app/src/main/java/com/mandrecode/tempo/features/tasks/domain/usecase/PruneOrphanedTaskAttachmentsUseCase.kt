package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.TaskAttachmentRepository
import jakarta.inject.Inject

/**
 * Clears attachment files nothing points to any more: those of a deletion whose Undo has expired
 * (their rows were dropped when the database opened) and those picked into a new task that was
 * never saved. Run once per app start, in the background.
 */
class PruneOrphanedTaskAttachmentsUseCase
    @Inject
    constructor(
        private val taskAttachmentRepository: TaskAttachmentRepository,
    ) {
        /** [processStartedAtEpochMillis]: files this run of the app wrote are never pruned by it. */
        suspend operator fun invoke(processStartedAtEpochMillis: Long) =
            taskAttachmentRepository.pruneOrphans(keepFilesFromEpochMillis = processStartedAtEpochMillis)
    }
