package com.mandrecode.tempo.core.data.preferences

import kotlinx.coroutines.flow.StateFlow

interface WhatsNewPreferencesRepository {
    val lastSeenEntryId: StateFlow<String?>

    fun setLastSeenEntryId(id: String)
}
