package com.mandrecode.tempo.infrastructure.attachments

import android.provider.OpenableColumns
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

/** What a viewer app learns about an attachment from the URI it is handed (#380). */
class AttachmentFileProviderTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var stored: File

    @Before
    fun storeFile() {
        stored = File(attachmentDirectory(context).apply { mkdirs() }, "test-${System.nanoTime()}")
        stored.writeText("%PDF-1.4")
    }

    @After
    fun deleteFile() {
        stored.delete()
    }

    @Test
    fun sharedUri_reportsTheAttachmentsOwnNameAndType() {
        val uri = attachmentContentUri(context, stored.name, "lease.pdf", "application/pdf")

        assertThat(context.contentResolver.getType(uri)).isEqualTo("application/pdf")
        context.contentResolver.query(uri, null, null, null, null).use { cursor ->
            checkNotNull(cursor)
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))).isEqualTo("lease.pdf")
            assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))).isEqualTo(8L)
        }
    }

    @Test
    fun sharedUri_servesTheStoredFile() {
        val uri = attachmentContentUri(context, stored.name, "lease.pdf", "application/pdf")

        val content = context.contentResolver.openInputStream(uri).use { checkNotNull(it).reader().readText() }

        assertThat(content).isEqualTo("%PDF-1.4")
    }
}
