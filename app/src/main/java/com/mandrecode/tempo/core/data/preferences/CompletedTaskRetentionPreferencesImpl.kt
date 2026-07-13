package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import androidx.core.content.edit
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompletedTaskRetentionPreferencesImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : CompletedTaskRetentionPreferences {
        private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        private val enabledFlow = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, false))
        private val retentionDaysFlow = MutableStateFlow(readRetentionDays())

        override val isEnabled: StateFlow<Boolean> = enabledFlow.asStateFlow()
        override val retentionDays: StateFlow<Int> = retentionDaysFlow.asStateFlow()

        override fun setEnabled(enabled: Boolean) {
            prefs.edit { putBoolean(KEY_ENABLED, enabled) }
            enabledFlow.value = enabled
        }

        override fun setRetentionDays(days: Int) {
            val safeDays =
                days.coerceIn(
                    CompletedTaskRetentionPreferences.MIN_RETENTION_DAYS,
                    CompletedTaskRetentionPreferences.MAX_RETENTION_DAYS,
                )
            prefs.edit { putInt(KEY_RETENTION_DAYS, safeDays) }
            retentionDaysFlow.value = safeDays
        }

        private fun readRetentionDays(): Int =
            prefs
                .getInt(
                    KEY_RETENTION_DAYS,
                    CompletedTaskRetentionPreferences.DEFAULT_RETENTION_DAYS,
                ).coerceIn(
                    CompletedTaskRetentionPreferences.MIN_RETENTION_DAYS,
                    CompletedTaskRetentionPreferences.MAX_RETENTION_DAYS,
                )

        companion object {
            private const val PREFS_NAME = "completed_task_retention_prefs"
            private const val KEY_ENABLED = "enabled"
            private const val KEY_RETENTION_DAYS = "retention_days"
        }
    }
