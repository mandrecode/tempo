package com.mandrecode.tempo.core.data.preferences

import kotlinx.coroutines.flow.StateFlow

interface WhatsNewPreferencesRepository {
    val lastSeenVersionCode: StateFlow<Int>

    fun setLastSeenVersionCode(versionCode: Int)
}
