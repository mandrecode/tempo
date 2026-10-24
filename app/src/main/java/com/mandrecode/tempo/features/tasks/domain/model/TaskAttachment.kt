package com.mandrecode.tempo.features.tasks.domain.model

/**
 * A file attached to a task: a picture, a document, anything the user picked.
 *
 * The file itself is a private copy the app keeps under [storedName], so it stays readable after
 * the original is moved or deleted and needs no lasting permission on someone else's storage.
 * An attachment that is still only in the editor of a task not yet saved has [id] and [taskId]
 * of `0`; saving the task is what gives it a row.
 */
data class TaskAttachment(
    val id: Long = 0,
    val taskId: Long = 0,
    val displayName: String,
    val mimeType: String?,
    val sizeBytes: Long,
    val storedName: String,
) {
    val kind: Kind
        get() =
            when {
                mimeType == null -> Kind.FILE
                mimeType.startsWith("image/") -> Kind.IMAGE
                mimeType == "application/pdf" -> Kind.PDF
                else -> Kind.FILE
            }

    val isSaved: Boolean get() = id != 0L

    enum class Kind { IMAGE, PDF, FILE }

    companion object {
        /** Files above this are refused rather than copied: the copy lives on the user's device. */
        const val MAX_SIZE_BYTES: Long = 25L * 1024 * 1024
    }
}

/** What happened to one picked file. */
sealed interface AttachmentImportResult {
    data class Imported(
        val attachment: TaskAttachment,
    ) : AttachmentImportResult

    data object TooLarge : AttachmentImportResult

    data object Unreadable : AttachmentImportResult
}
