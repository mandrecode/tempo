package com.mandrecode.tempo.core.data.preferences

import com.mandrecode.tempo.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemePreferencesRepository {
    fun getThemeMode(): Flow<ThemeMode>

    fun setThemeMode(mode: ThemeMode)

    fun getUseTempoColors(): Flow<Boolean>

    /**
     * The current value, without collecting. Notifications are built outside a composition and
     * outside a coroutine, and still have to know which palette the app is wearing.
     */
    fun currentUseTempoColors(): Boolean

    fun setUseTempoColors(enabled: Boolean)
}
