package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus
import javax.inject.Inject

class DeleteExpiredCompletedTasksUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
    ) {
        suspend operator fun invoke(
            now: LocalDateTime,
            retentionDays: Int,
        ) {
            val safeRetentionDays =
                retentionDays.coerceIn(
                    CompletedTaskRetentionPreferences.MIN_RETENTION_DAYS,
                    CompletedTaskRetentionPreferences.MAX_RETENTION_DAYS,
                )
            val cutoff = LocalDateTime(now.date.minus(safeRetentionDays, DateTimeUnit.DAY), now.time)
            taskRepository.deleteCompletedTasksAtOrBefore(cutoff)
        }
    }
