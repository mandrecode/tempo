package com.mandrecode.tempo.features.onboarding.presentation

import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

object OnboardingContract {
    const val PAGE_COUNT = 4

    data class UiState(
        val currentPage: Int = 0,
        val selectedThemeMode: ThemeMode = ThemeMode.SYSTEM,
        val availableThemeModes: ImmutableList<ThemeMode> =
            persistentListOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM),
        val useTempoColors: Boolean = true,
        val isRoutinesTabEnabled: Boolean = true,
        val isTasksTabEnabled: Boolean = true,
        val defaultTab: DefaultTab = DefaultTab.ROUTINES,
        val autoRemoveCompletedTasksEnabled: Boolean = false,
        val completedTaskRetentionDays: Int = CompletedTaskRetentionPreferences.DEFAULT_RETENTION_DAYS,
    ) {
        val isFirstPage: Boolean get() = currentPage == 0
        val isLastPage: Boolean get() = currentPage == PAGE_COUNT - 1
    }

    sealed interface UiEvent {
        data object NextClicked : UiEvent

        data object BackClicked : UiEvent

        data object SkipClicked : UiEvent

        data object FinishClicked : UiEvent

        data class TempoColorsSelected(
            val enabled: Boolean,
        ) : UiEvent

        data class ThemeModeSelected(
            val mode: ThemeMode,
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

        data class AutoRemoveCompletedTasksToggled(
            val enabled: Boolean,
        ) : UiEvent

        data class CompletedTaskRetentionDaysChanged(
            val days: Int,
        ) : UiEvent
    }

    sealed interface UiEffect {
        data class Exit(
            val defaultTab: DefaultTab,
        ) : UiEffect
    }

    enum class DefaultTab {
        ROUTINES,
        TASKS,
    }
}
