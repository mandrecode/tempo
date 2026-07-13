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

        val supportedRetentionDays: List<Int> = listOf(1, 3, 5, 7, 14, 21, 30, 45, 90, 180, 365)

        fun normalizeRetentionDays(days: Int): Int =
            supportedRetentionDays.minBy { candidate ->
                kotlin.math.abs(candidate.toLong() - days.toLong())
            }
    }
}
