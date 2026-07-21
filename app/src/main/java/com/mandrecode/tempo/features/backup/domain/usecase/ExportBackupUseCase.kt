package com.mandrecode.tempo.features.backup.domain.usecase

import com.mandrecode.tempo.features.backup.domain.repository.BackupRepository
import jakarta.inject.Inject
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class ExportBackupUseCase
    @Inject
    constructor(
        private val backupRepository: BackupRepository,
    ) {
        data class Export(
            val json: String,
            val suggestedFileName: String,
        )

        suspend operator fun invoke(passphrase: CharArray): Export {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            return Export(
                json = backupRepository.exportEncrypted(passphrase),
                suggestedFileName = suggestedFileName(now),
            )
        }

        private fun suggestedFileName(now: LocalDateTime): String {
            val date =
                "%04d%02d%02d".format(now.year, now.month.number, now.day)
            val time = "%02d%02d".format(now.hour, now.minute)
            return "backup-$date-$time.tempo"
        }
    }
