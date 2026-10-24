package com.mandrecode.tempo.infrastructure.attachments

import android.content.Context
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.mandrecode.tempo.core.di.IoDispatcher
import com.mandrecode.tempo.features.tasks.data.attachment.AttachmentFileStore
import com.mandrecode.tempo.features.tasks.data.attachment.AttachmentTooLargeException
import com.mandrecode.tempo.features.tasks.data.attachment.StoredAttachmentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Keeps attachment copies in `files/attachments/`, which is app-private, excluded from Auto
 * Backup like the database it belongs with, and shared with viewer apps only one file at a time
 * through [FileProvider] (see [attachmentContentUri]).
 */
class AndroidAttachmentFileStore
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : AttachmentFileStore {
        private val directory: File get() = attachmentDirectory(context)

        override suspend fun copyIn(
            sourceUri: String,
            maxSizeBytes: Long,
        ): StoredAttachmentFile =
            withContext(ioDispatcher) {
                val uri = sourceUri.toUri()
                val metadata = queryMetadata(uri)
                if ((metadata.size ?: 0L) > maxSizeBytes) throw AttachmentTooLargeException()

                directory.mkdirs()
                val storedName = UUID.randomUUID().toString()
                val target = File(directory, storedName)
                var copied = -1L
                try {
                    val input =
                        try {
                            context.contentResolver.openInputStream(uri)
                        } catch (e: SecurityException) {
                            throw IOException(e)
                        } ?: throw IOException("Cannot open $uri")
                    copied = input.use { source -> target.outputStream().use { source.copyLimited(it, maxSizeBytes) } }
                } finally {
                    // Whatever stopped the copy, a partial file is no use to anyone.
                    if (copied < 0) target.delete()
                }

                StoredAttachmentFile(
                    storedName = storedName,
                    displayName = metadata.displayName ?: uri.lastPathSegment ?: storedName,
                    mimeType = context.contentResolver.getType(uri),
                    sizeBytes = copied,
                )
            }

        private data class Metadata(
            val displayName: String?,
            val size: Long?,
        )

        private fun queryMetadata(uri: android.net.Uri): Metadata {
            val projection = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
            return context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                Metadata(
                    displayName = if (nameIndex >= 0) cursor.getString(nameIndex) else null,
                    size = if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else null,
                )
            } ?: Metadata(displayName = null, size = null)
        }

        override suspend fun delete(storedName: String) {
            withContext(ioDispatcher) { File(directory, storedName).delete() }
        }

        override suspend fun listStoredNames(): List<String> =
            withContext(ioDispatcher) {
                directory.list()?.toList().orEmpty()
            }

        /** Providers do not always report a size, so the limit is enforced on the bytes as well. */
        private fun java.io.InputStream.copyLimited(
            out: java.io.OutputStream,
            limit: Long,
        ): Long {
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0L
            while (true) {
                val read = read(buffer)
                if (read < 0) break
                total += read
                if (total > limit) throw AttachmentTooLargeException()
                out.write(buffer, 0, read)
            }
            return total
        }
    }

private const val ATTACHMENT_DIRECTORY = "attachments"

internal fun attachmentDirectory(context: Context): File = File(context.filesDir, ATTACHMENT_DIRECTORY)

/** The authority declared for the attachment [FileProvider] in the manifest. */
internal fun attachmentAuthority(context: Context): String = "${context.packageName}.attachments"

/**
 * A `content://` URI for one stored attachment, for handing to a viewer app with a read grant.
 * Throws [IllegalArgumentException] if the file is outside the provider's paths.
 */
fun attachmentContentUri(
    context: Context,
    storedName: String,
): android.net.Uri {
    val file = File(attachmentDirectory(context), storedName)
    return FileProvider.getUriForFile(context, attachmentAuthority(context), file)
}
