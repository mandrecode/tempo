package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingPreferencesRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : OnboardingPreferencesRepository {
        private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        private val completed = MutableStateFlow(preferences.getBoolean(KEY_COMPLETED, false))

        override val isCompleted: StateFlow<Boolean> = completed.asStateFlow()

        override fun markStarted(): Boolean {
            if (preferences.getBoolean(KEY_STARTED, false)) return false

            preferences.edit { putBoolean(KEY_STARTED, true) }
            return true
        }

        override fun setCompleted() {
            if (completed.value) return

            preferences.edit { putBoolean(KEY_COMPLETED, true) }
            completed.value = true
        }

        private companion object {
            const val PREFERENCES_NAME = "onboarding_preferences"
            const val KEY_COMPLETED = "completed"
            const val KEY_STARTED = "started"
        }
    }
