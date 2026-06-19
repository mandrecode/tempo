package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.mandrecode.tempo.core.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePreferencesRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : ThemePreferencesRepository {
        private val prefs: SharedPreferences =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE,
            )

        private val _themeMode = MutableStateFlow(getCurrentThemeMode())
        private val _useTempoColors = MutableStateFlow(getCurrentUseTempoColors())

        /**
         * Gets the current theme mode as a Flow.
         */
        override fun getThemeMode(): Flow<ThemeMode> = _themeMode.asStateFlow()

        /**
         * Saves the selected theme mode.
         */
        override fun setThemeMode(mode: ThemeMode) {
            prefs.edit { putString(KEY_THEME_MODE, mode.name) }
            _themeMode.value = mode
        }

        /**
         * Gets the current Tempo colors preference as a Flow.
         */
        override fun getUseTempoColors(): Flow<Boolean> = _useTempoColors.asStateFlow()

        /**
         * Saves the Tempo colors preference.
         */
        override fun setUseTempoColors(enabled: Boolean) {
            prefs.edit { putBoolean(KEY_USE_TEMPO_COLORS, enabled) }
            _useTempoColors.value = enabled
        }

        private fun getCurrentThemeMode(): ThemeMode {
            val modeName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
            return try {
                ThemeMode.valueOf(modeName ?: ThemeMode.SYSTEM.name)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }

        private fun getCurrentUseTempoColors(): Boolean = prefs.getBoolean(KEY_USE_TEMPO_COLORS, false)

        companion object {
            private const val PREFS_NAME = "theme_prefs"
            private const val KEY_THEME_MODE = "theme_mode"
            private const val KEY_USE_TEMPO_COLORS = "use_tempo_colors"
        }
    }
