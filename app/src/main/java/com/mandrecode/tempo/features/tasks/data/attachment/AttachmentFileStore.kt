package com.mandrecode.tempo.features.tasks.data.attachment

/** A picked file once it has been copied into the app's private attachment storage. */
data class StoredAttachmentFile(
    val storedName: String,
    val displayName: String,
    val mimeType: String?,
    val sizeBytes: Long,
)

/** Thrown by [AttachmentFileStore.copyIn] for a file larger than the given limit. */
class AttachmentTooLargeException : Exception()

/**
 * The private folder attachments are copied into. Behind an interface so the repository can be
 * tested without a file system or a content resolver.
 */
interface AttachmentFileStore {
    /**
     * Copies the document at [sourceUri] in. Throws [AttachmentTooLargeException] for a file over
     * [maxSizeBytes], and [java.io.IOException] for one that cannot be read.
     */
    suspend fun copyIn(
        sourceUri: String,
        maxSizeBytes: Long,
    ): StoredAttachmentFile

    suspend fun delete(storedName: String)

    /** Stored files last written before [modifiedBeforeEpochMillis]. */
    suspend fun listStoredNames(modifiedBeforeEpochMillis: Long): List<String>
}
