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
        private val lastSeen = MutableStateFlow(preferences.getString(KEY_LAST_SEEN_ENTRY_ID, null))

        override val lastSeenEntryId: StateFlow<String?> = lastSeen.asStateFlow()

        override fun setLastSeenEntryId(id: String) {
            if (lastSeen.value == id) return

            preferences.edit(commit = true) { putString(KEY_LAST_SEEN_ENTRY_ID, id) }
            lastSeen.value = id
        }

        private companion object {
            const val PREFERENCES_NAME = "whats_new_preferences"
            const val KEY_LAST_SEEN_ENTRY_ID = "last_seen_entry_id"
        }
    }
