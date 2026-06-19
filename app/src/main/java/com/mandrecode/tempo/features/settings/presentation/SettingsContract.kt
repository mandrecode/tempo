package com.mandrecode.tempo.features.settings.presentation

import com.mandrecode.tempo.core.domain.model.ThemeMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Contract for Settings screen following MVI pattern.
 */
object SettingsContract {
    /**
     * UI State for Settings screen.
     */
    data class UiState(
        val selectedThemeMode: ThemeMode = ThemeMode.SYSTEM,
        val availableThemeModes: ImmutableList<ThemeMode> =
            persistentListOf(
                ThemeMode.LIGHT,
                ThemeMode.DARK,
                ThemeMode.SYSTEM,
            ),
        val useTempoColors: Boolean = false,
        val appVersion: String = "",
        val isRoutinesTabEnabled: Boolean = true,
        val isTasksTabEnabled: Boolean = true,
        val defaultTab: DefaultTab = DefaultTab.ROUTINES,
    )

    /**
     * UI Events that can be triggered from the Settings screen.
     */
    sealed interface UiEvent {
        data class ThemeModeSelected(
            val mode: ThemeMode,
        ) : UiEvent

        data class TempoColorsToggled(
            val enabled: Boolean,
        ) : UiEvent

        data class RoutinesTabToggled(
            val enabled: Boolean,
        ) : UiEvent

        data class TasksTabToggled(
            val enabled: Boolean,
        ) : UiEvent

        data class DefaultTabSelected(
            val defaultTab: DefaultTab,
        ) : UiEvent
    }

    /**
     * One-time UI Effects for Settings screen.
     */
    sealed interface UiEffect {
        // No effects needed for now
    }

    /**
     * Represents the default tab option.
     */
    enum class DefaultTab {
        ROUTINES,
        TASKS,
    }
}
