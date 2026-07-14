package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import androidx.core.content.edit
import com.mandrecode.tempo.features.tasks.domain.repository.TaskReminderPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskReminderPreferencesImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : TaskReminderPreferences {
        private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        private val defaultTimeFlow = MutableStateFlow(readDefaultTime())

        override val defaultTime: StateFlow<LocalTime> = defaultTimeFlow.asStateFlow()

        override fun setDefaultTime(time: LocalTime) {
            val normalized = TaskReminderPreferences.normalize(time.hour, time.minute)
            prefs.edit {
                putInt(KEY_DEFAULT_HOUR, normalized.hour)
                putInt(KEY_DEFAULT_MINUTE, normalized.minute)
            }
            defaultTimeFlow.value = normalized
        }

        private fun readDefaultTime(): LocalTime =
            TaskReminderPreferences.normalize(
                hour = prefs.getInt(KEY_DEFAULT_HOUR, TaskReminderPreferences.DEFAULT_HOUR),
                minute = prefs.getInt(KEY_DEFAULT_MINUTE, TaskReminderPreferences.DEFAULT_MINUTE),
            )

        companion object {
            private const val PREFS_NAME = "task_reminder_prefs"
            private const val KEY_DEFAULT_HOUR = "default_hour"
            private const val KEY_DEFAULT_MINUTE = "default_minute"
        }
    }
