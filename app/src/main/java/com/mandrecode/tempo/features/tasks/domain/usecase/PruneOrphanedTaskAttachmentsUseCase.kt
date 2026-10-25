package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.TaskAttachmentRepository
import jakarta.inject.Inject

/**
 * Clears attachments whose task is gone, and files nothing points to any more: those of a
 * deletion whose undo has expired, and those picked into a new task that was never saved.
 *
 * Run when the app starts, while no deletion can still be undone — the undo lives only as long
 * as the process that offered it.
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
