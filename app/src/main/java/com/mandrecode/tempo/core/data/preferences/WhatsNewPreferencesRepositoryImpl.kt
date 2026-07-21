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
class WhatsNewPreferencesRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : WhatsNewPreferencesRepository {
        private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        private val lastSeen = MutableStateFlow(preferences.getInt(KEY_LAST_SEEN_VERSION_CODE, 0))

        override val lastSeenVersionCode: StateFlow<Int> = lastSeen.asStateFlow()

        override fun setLastSeenVersionCode(versionCode: Int) {
            if (lastSeen.value >= versionCode) return

            preferences.edit(commit = true) { putInt(KEY_LAST_SEEN_VERSION_CODE, versionCode) }
            lastSeen.value = versionCode
        }

        private companion object {
            const val PREFERENCES_NAME = "whats_new_preferences"
            const val KEY_LAST_SEEN_VERSION_CODE = "last_seen_version_code"
        }
    }
