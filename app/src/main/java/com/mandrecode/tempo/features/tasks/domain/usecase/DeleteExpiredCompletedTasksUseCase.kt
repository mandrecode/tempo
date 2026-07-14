package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import jakarta.inject.Inject
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus

class DeleteExpiredCompletedTasksUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
    ) {
        suspend operator fun invoke(
            now: LocalDateTime,
            retentionDays: Int,
        ) {
            val safeRetentionDays = CompletedTaskRetentionPreferences.normalizeRetentionDays(retentionDays)
            val cutoff = LocalDateTime(now.date.minus(safeRetentionDays, DateTimeUnit.DAY), now.time)
            taskRepository.deleteCompletedTasksAtOrBefore(cutoff)
        }
    }
