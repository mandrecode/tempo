package com.mandrecode.tempo.features.tasks.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface CompletedTaskRetentionPreferences {
    val isEnabled: StateFlow<Boolean>
    val retentionDays: StateFlow<Int>

    fun setEnabled(enabled: Boolean)

    fun setRetentionDays(days: Int)

    companion object {
        const val DEFAULT_RETENTION_DAYS = 30
        const val MIN_RETENTION_DAYS = 1
        const val MAX_RETENTION_DAYS = 365
    }
}
