package com.mandrecode.tempo.infrastructure.backup

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

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

    private fun uriWithAuthority(authority: String): Uri =
        mockk<Uri> {
            every { this@mockk.authority } returns authority
        }
}
