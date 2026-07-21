package com.mandrecode.tempo.infrastructure.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.IOException

class BackupFileDataSourceTest {
    private lateinit var context: Context
    private lateinit var dataSource: BackupFileDataSource

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        every { context.getString(R.string.backup_export_location_downloads) } returns "Downloads"
        every { context.getString(R.string.backup_export_location_documents) } returns "Documents"
        every { context.getString(R.string.backup_export_location_internal_storage) } returns "internal storage"
        mockkStatic(DocumentsContract::class)
        dataSource = BackupFileDataSource(context, Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        unmockkStatic(DocumentsContract::class)
    }

    @Test
    fun `downloads provider authority maps to Downloads without inspecting the document id`() {
        val uri = uriWithAuthority("com.android.providers.downloads.documents")

        assertThat(dataSource.locationLabel(uri)).isEqualTo("Downloads")
    }

    @Test
    fun `external storage document under Download folder maps to Downloads`() {
        val uri = uriWithAuthority("com.android.externalstorage.documents")
        every { DocumentsContract.getDocumentId(uri) } returns "primary:Download/tempo-backup.json"

        assertThat(dataSource.locationLabel(uri)).isEqualTo("Downloads")
    }

    @Test
    fun `external storage document at the volume root maps to internal storage`() {
        val uri = uriWithAuthority("com.android.externalstorage.documents")
        every { DocumentsContract.getDocumentId(uri) } returns "primary:tempo-backup.json"

        assertThat(dataSource.locationLabel(uri)).isEqualTo("internal storage")
    }

    @Test
    fun `external storage document under Documents folder maps to Documents`() {
        val uri = uriWithAuthority("com.android.externalstorage.documents")
        every { DocumentsContract.getDocumentId(uri) } returns "primary:Documents/tempo-backup.json"

        assertThat(dataSource.locationLabel(uri)).isEqualTo("Documents")
    }

    @Test
    fun `external storage document under a nested unknown folder shows the immediate folder name`() {
        val uri = uriWithAuthority("com.android.externalstorage.documents")
        every { DocumentsContract.getDocumentId(uri) } returns "primary:Documents/Backups/tempo-backup.json"

        assertThat(dataSource.locationLabel(uri)).isEqualTo("Backups")
    }

    @Test
    fun `external storage document on a secondary volume Download folder still maps to Downloads`() {
        val uri = uriWithAuthority("com.android.externalstorage.documents")
        every { DocumentsContract.getDocumentId(uri) } returns "1234-5678:Download/tempo-backup.json"

        assertThat(dataSource.locationLabel(uri)).isEqualTo("Downloads")
    }

    @Test
    fun `malformed document id is treated as unknown`() {
        val uri = uriWithAuthority("com.android.externalstorage.documents")
        every { DocumentsContract.getDocumentId(uri) } throws IllegalArgumentException("bad uri")

        assertThat(dataSource.locationLabel(uri)).isNull()
    }

    @Test
    fun `unrecognized authority is unknown`() {
        val uri = uriWithAuthority("com.google.android.apps.docs.storage")

        assertThat(dataSource.locationLabel(uri)).isNull()
    }

    @Test
    fun `write opens the truncating wt mode and writes the content`() =
        runTest {
            val uri = uriWithAuthority("com.android.externalstorage.documents")
            val resolver = mockResolver()
            val outputStream = ByteArrayOutputStream()
            every { resolver.openOutputStream(uri, "wt") } returns outputStream

            dataSource.write(uri, "hello")

            assertThat(outputStream.toString()).isEqualTo("hello")
        }

    @Test
    fun `write falls back to rwt when wt throws IllegalArgumentException`() =
        runTest {
            val uri = uriWithAuthority("com.android.externalstorage.documents")
            val resolver = mockResolver()
            every { resolver.openOutputStream(uri, "wt") } throws IllegalArgumentException("mode not supported")
            val outputStream = ByteArrayOutputStream()
            every { resolver.openOutputStream(uri, "rwt") } returns outputStream

            dataSource.write(uri, "hello")

            assertThat(outputStream.toString()).isEqualTo("hello")
        }

    @Test
    fun `write falls back to rwt when wt returns null instead of throwing`() =
        runTest {
            val uri = uriWithAuthority("com.android.externalstorage.documents")
            val resolver = mockResolver()
            every { resolver.openOutputStream(uri, "wt") } returns null
            val outputStream = ByteArrayOutputStream()
            every { resolver.openOutputStream(uri, "rwt") } returns outputStream

            dataSource.write(uri, "hello")

            verify { resolver.openOutputStream(uri, "rwt") }
            assertThat(outputStream.toString()).isEqualTo("hello")
        }

    @Test
    fun `write throws IOException when neither mode is available`() =
        runTest {
            val uri = uriWithAuthority("com.android.externalstorage.documents")
            val resolver = mockResolver()
            every { resolver.openOutputStream(uri, "wt") } returns null
            every { resolver.openOutputStream(uri, "rwt") } returns null

            val thrown = runCatching { dataSource.write(uri, "hello") }.exceptionOrNull()

            assertThat(thrown).isInstanceOf(IOException::class.java)
        }

    private fun mockResolver(): ContentResolver {
        val resolver = mockk<ContentResolver>()
        every { context.contentResolver } returns resolver
        return resolver
    }

    private fun uriWithAuthority(authority: String): Uri =
        mockk<Uri> {
            every { this@mockk.authority } returns authority
        }
}
