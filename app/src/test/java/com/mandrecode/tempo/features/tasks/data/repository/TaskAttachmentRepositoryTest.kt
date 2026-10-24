package com.mandrecode.tempo.features.tasks.data.repository

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.entity.TaskAttachmentEntity
import com.mandrecode.tempo.core.data.local.dao.TaskAttachmentDao
import com.mandrecode.tempo.features.tasks.data.attachment.AttachmentFileStore
import com.mandrecode.tempo.features.tasks.data.attachment.AttachmentTooLargeException
import com.mandrecode.tempo.features.tasks.data.attachment.StoredAttachmentFile
import com.mandrecode.tempo.features.tasks.domain.model.AttachmentImportResult
import com.mandrecode.tempo.features.tasks.domain.model.TaskAttachment
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

class TaskAttachmentRepositoryTest {
    private lateinit var dao: TaskAttachmentDao
    private lateinit var fileStore: FakeAttachmentFileStore
    private lateinit var repository: TaskAttachmentRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
        fileStore = FakeAttachmentFileStore()
        repository = TaskAttachmentRepositoryImpl(dao, fileStore)
    }

    @Test
    fun `importing copies the file in and returns an attachment not yet tied to a task`() =
        runTest {
            fileStore.next = StoredAttachmentFile("s1", "lease.pdf", "application/pdf", 2048)

            val result = repository.importFile("content://docs/lease")

            assertThat(result).isEqualTo(
                AttachmentImportResult.Imported(
                    TaskAttachment(
                        displayName = "lease.pdf",
                        mimeType = "application/pdf",
                        sizeBytes = 2048,
                        storedName = "s1",
                    ),
                ),
            )
            assertThat(fileStore.lastMaxSize).isEqualTo(TaskAttachment.MAX_SIZE_BYTES)
        }

    @Test
    fun `a file over the limit is refused`() =
        runTest {
            fileStore.failure = AttachmentTooLargeException()

            assertThat(repository.importFile("content://docs/huge")).isEqualTo(AttachmentImportResult.TooLarge)
        }

    @Test
    fun `a file that cannot be read is reported as unreadable`() =
        runTest {
            fileStore.failure = IOException("gone")

            assertThat(repository.importFile("content://docs/gone")).isEqualTo(AttachmentImportResult.Unreadable)
        }

    @Test
    fun `attaching inserts fresh rows for the task`() =
        runTest {
            val inserted = slot<List<TaskAttachmentEntity>>()
            coEvery { dao.insertAll(capture(inserted)) } returns Unit

            repository.attach(listOf(attachment(storedName = "s1")), taskId = 9L)

            assertThat(inserted.captured.single().taskId).isEqualTo(9L)
            assertThat(inserted.captured.single().id).isEqualTo(0L)
        }

    @Test
    fun `removing keeps a file another row still shares`() =
        runTest {
            coEvery { dao.countByStoredName("s1") } returns 1

            repository.remove(attachment(id = 3L, storedName = "s1"))

            coVerify { dao.deleteById(3L) }
            assertThat(fileStore.deleted).isEmpty()
        }

    @Test
    fun `removing the last row for a file deletes the file`() =
        runTest {
            coEvery { dao.countByStoredName("s1") } returns 0

            repository.remove(attachment(id = 3L, storedName = "s1"))

            assertThat(fileStore.deleted).containsExactly("s1")
        }

    @Test
    fun `the next occurrence of a recurring task shares the same files`() =
        runTest {
            val original =
                TaskAttachmentEntity(
                    id = 5L,
                    taskId = 1L,
                    displayName = "a",
                    mimeType = null,
                    sizeBytes = 1,
                    storedName = "s1",
                )
            coEvery { dao.getForTask(1L) } returns listOf(original)
            val inserted = slot<List<TaskAttachmentEntity>>()
            coEvery { dao.insertAll(capture(inserted)) } returns Unit

            repository.copyAttachments(fromTaskId = 1L, toTaskId = 2L)

            assertThat(inserted.captured.single()).isEqualTo(original.copy(id = 0L, taskId = 2L))
        }

    @Test
    fun `pruning drops rows without a task and files without a row`() =
        runTest {
            fileStore.stored += listOf("kept", "orphan")
            coEvery { dao.getAllStoredNames() } returns listOf("kept")

            repository.pruneOrphans()

            coVerify { dao.deleteOrphanedRows() }
            assertThat(fileStore.deleted).containsExactly("orphan")
        }

    private fun attachment(
        id: Long = 0L,
        storedName: String,
    ) = TaskAttachment(
        id = id,
        displayName = "file",
        mimeType = null,
        sizeBytes = 1,
        storedName = storedName,
    )

    private class FakeAttachmentFileStore : AttachmentFileStore {
        var next: StoredAttachmentFile? = null
        var failure: Exception? = null
        var lastMaxSize: Long? = null
        val stored = mutableListOf<String>()
        val deleted = mutableListOf<String>()

        override suspend fun copyIn(
            sourceUri: String,
            maxSizeBytes: Long,
        ): StoredAttachmentFile {
            lastMaxSize = maxSizeBytes
            failure?.let { throw it }
            return requireNotNull(next)
        }

        override suspend fun delete(storedName: String) {
            deleted += storedName
        }

        override suspend fun listStoredNames(): List<String> = stored
    }
}
