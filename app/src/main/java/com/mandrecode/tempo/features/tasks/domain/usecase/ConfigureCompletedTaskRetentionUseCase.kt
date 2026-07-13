package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import com.mandrecode.tempo.features.tasks.domain.scheduler.CompletedTaskCleanupScheduler
import javax.inject.Inject

class ConfigureCompletedTaskRetentionUseCase
    @Inject
    constructor(
        private val preferences: CompletedTaskRetentionPreferences,
        private val scheduler: CompletedTaskCleanupScheduler,
    ) {
        operator fun invoke(
            enabled: Boolean,
            retentionDays: Int,
        ) {
            preferences.setRetentionDays(
                CompletedTaskRetentionPreferences.normalizeRetentionDays(retentionDays),
            )
            preferences.setEnabled(enabled)

            if (enabled) {
                scheduler.schedule()
            } else {
                scheduler.cancel()
            }
        }
    }
