package com.mandrecode.tempo.core.data.preferences

import com.mandrecode.tempo.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemePreferencesRepository {
    fun getThemeMode(): Flow<ThemeMode>

    fun setThemeMode(mode: ThemeMode)

    fun getUseTempoColors(): Flow<Boolean>

    fun setUseTempoColors(enabled: Boolean)
}
