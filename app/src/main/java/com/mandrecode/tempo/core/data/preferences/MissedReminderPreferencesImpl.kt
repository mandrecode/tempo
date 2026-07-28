package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import androidx.core.content.edit
import com.mandrecode.tempo.features.tasks.domain.repository.MissedReminderPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissedReminderPreferencesImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : MissedReminderPreferences {
        private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        private val enabledFlow =
            MutableStateFlow(
                prefs.getBoolean(KEY_ENABLED, MissedReminderPreferences.DEFAULT_ENABLED),
            )
        private val catchUpTimeFlow = MutableStateFlow(readCatchUpTime())

        override val isEnabled: StateFlow<Boolean> = enabledFlow.asStateFlow()
        override val catchUpTime: StateFlow<LocalTime> = catchUpTimeFlow.asStateFlow()

        override fun setEnabled(enabled: Boolean) {
            prefs.edit { putBoolean(KEY_ENABLED, enabled) }
            enabledFlow.value = enabled
        }

        override fun setCatchUpTime(time: LocalTime) {
            prefs.edit { putInt(KEY_CATCH_UP_MINUTE_OF_DAY, MissedReminderPreferences.timeToMinuteOfDay(time)) }
            catchUpTimeFlow.value = time
        }

        private fun readCatchUpTime(): LocalTime =
            prefs
                .getInt(
                    KEY_CATCH_UP_MINUTE_OF_DAY,
                    MissedReminderPreferences.timeToMinuteOfDay(MissedReminderPreferences.DEFAULT_CATCH_UP_TIME),
                ).let(MissedReminderPreferences::minuteOfDayToTime)

        companion object {
            private const val PREFS_NAME = "missed_reminder_prefs"
            private const val KEY_ENABLED = "enabled"
            private const val KEY_CATCH_UP_MINUTE_OF_DAY = "catch_up_minute_of_day"
        }
    }
