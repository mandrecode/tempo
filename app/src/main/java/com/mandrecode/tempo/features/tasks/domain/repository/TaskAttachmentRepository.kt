package com.mandrecode.tempo.features.tasks.domain.repository

import com.mandrecode.tempo.features.tasks.domain.model.AttachmentImportResult
import com.mandrecode.tempo.features.tasks.domain.model.TaskAttachment
import kotlinx.coroutines.flow.Flow

/**
 * Task attachments: their rows, and the private copies of the files behind them.
 *
 * Rows are tied to a task by id but not by a foreign key, on purpose. Deleting a task keeps its
 * attachments, so undoing the deletion brings them back with it; [pruneOrphans] clears what is
 * left once nothing can be undone any more.
 */
interface TaskAttachmentRepository {
    fun observeAttachments(taskId: Long): Flow<List<TaskAttachment>>

    /**
     * Copies the file at [sourceUri] into private storage. The result is not yet attached to
     * anything — pass it to [attach] once the task it belongs to has an id.
     */
    suspend fun importFile(sourceUri: String): AttachmentImportResult

    suspend fun attach(
        attachments: List<TaskAttachment>,
        taskId: Long,
    )

    /** Removes [attachment] and, unless another row still shares it, its file. */
    suspend fun remove(attachment: TaskAttachment)

    /** Drops the file of an attachment that was imported but never attached. */
    suspend fun discard(attachment: TaskAttachment)

    /**
     * Gives [toTaskId] the same attachments as [fromTaskId], sharing their files — a recurring
     * task's next occurrence keeps what the last one had.
     */
    suspend fun copyAttachments(
        fromTaskId: Long,
        toTaskId: Long,
    )

    /**
     * Deletes rows whose task is gone and files no row points to. Only safe when no deletion can
     * still be undone, which is why it runs when the app starts.
     *
     * Files written at or after [keepFilesFromEpochMillis] are left alone even when no row points
     * to them yet: the editor may be copying one in for a task about to be saved, and the sweep
     * runs alongside the app rather than before it.
     */
    suspend fun pruneOrphans(keepFilesFromEpochMillis: Long)
}
