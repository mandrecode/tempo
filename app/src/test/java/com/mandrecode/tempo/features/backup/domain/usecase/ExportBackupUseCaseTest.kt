package com.mandrecode.tempo.features.backup.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.backup.domain.repository.BackupRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ExportBackupUseCaseTest {
    private val backupRepository: BackupRepository = mockk(relaxed = true)
    private val useCase = ExportBackupUseCase(backupRepository)

    @Test
    fun `returns repository json`() =
        runTest {
            coEvery { backupRepository.exportEncrypted(any()) } returns """{"encryptionVersion":1}"""

            val export = useCase("passphrase".toCharArray())

            assertThat(export.json).isEqualTo("""{"encryptionVersion":1}""")
        }

    @Test
    fun `suggested file name follows the documented pattern`() =
        runTest {
            val export = useCase("passphrase".toCharArray())

            assertThat(export.suggestedFileName)
                .matches("tempo-backup-\\d{8}-\\d{4}\\.tempo")
        }
}
