package com.mandrecode.tempo.infrastructure.attachments

import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider

/**
 * The stock [FileProvider], telling a viewer app what an attachment really is (#380).
 *
 * Attachments are stored under random, extensionless names, so the stock provider would report
 * that name as the file's display name and `application/octet-stream` as its type — a viewer
 * that asks the provider (rather than trusting the intent) would show "3f2a…" and could refuse a
 * PDF it only opens as `application/pdf`. [attachmentContentUri] puts the saved name and type on
 * the URI; this answers with them. Which file is served is still decided by the path alone.
 */
class AttachmentFileProvider : FileProvider() {
    override fun getType(uri: Uri): String? = uri.getQueryParameter(PARAM_MIME_TYPE) ?: super.getType(uri)

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        val cursor = super.query(uri, projection, selection, selectionArgs, sortOrder)
        val displayName = uri.getQueryParameter(PARAM_DISPLAY_NAME) ?: return cursor
        return cursor.use { it.withDisplayName(displayName) }
    }

    private fun Cursor.withDisplayName(displayName: String): Cursor {
        val renamed = MatrixCursor(columnNames, count)
        val nameColumn = getColumnIndex(OpenableColumns.DISPLAY_NAME)
        while (moveToNext()) {
            renamed.addRow(
                Array<Any?>(columnCount) { column ->
                    when {
                        column == nameColumn -> displayName
                        getType(column) == Cursor.FIELD_TYPE_INTEGER -> getLong(column)
                        getType(column) == Cursor.FIELD_TYPE_NULL -> null
                        else -> getString(column)
                    }
                },
            )
        }
        return renamed
    }

    internal companion object {
        const val PARAM_DISPLAY_NAME = "displayName"
        const val PARAM_MIME_TYPE = "mimeType"
    }
}
