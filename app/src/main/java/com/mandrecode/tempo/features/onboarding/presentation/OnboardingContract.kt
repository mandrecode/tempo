package com.mandrecode.tempo.features.onboarding.presentation

import com.mandrecode.tempo.core.domain.model.TempoTab
import com.mandrecode.tempo.core.domain.model.ThemeMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentSet

object OnboardingContract {
    /**
     * The pages, in the order they are shown. Named rather than counted at each call site: the
     * order has changed more than once, and every magic index was somewhere else that had to be
     * found and corrected when it did.
     */
    object Page {
        const val FOCUS = 0
        const val TASKS = 1
        const val ROUTINES = 2
        const val APPEARANCE = 3
        const val SETUP = 4
        const val WELCOME = 5
    }

    const val PAGE_COUNT = 6

    data class UiState(
        val currentPage: Int = 0,
        val selectedThemeMode: ThemeMode = ThemeMode.SYSTEM,
        val availableThemeModes: ImmutableList<ThemeMode> =
            persistentListOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM),
        val useTempoColors: Boolean = true,
        val enabledTabs: ImmutableSet<TempoTab> = TempoTab.entries.toPersistentSet(),
        val defaultTab: TempoTab = TempoTab.DEFAULT,
    ) {
        val isFirstPage: Boolean get() = currentPage == 0
        val isLastPage: Boolean get() = currentPage == PAGE_COUNT - 1
    }

    sealed interface UiEvent {
        data object NextClicked : UiEvent

        data object BackClicked : UiEvent

        data object SkipClicked : UiEvent

        data object FinishClicked : UiEvent

        data class UseTempoColorsToggled(
            val useTempoColors: Boolean,
        ) : UiEvent

        data class ThemeModeSelected(
            val mode: ThemeMode,
        ) : UiEvent

        data class TabToggled(
            val tab: TempoTab,
            val enabled: Boolean,
        ) : UiEvent

        data class DefaultTabSelected(
            val defaultTab: TempoTab,
        ) : UiEvent
    }

    sealed interface UiEffect {
        data class Exit(
            val defaultTab: TempoTab,
        ) : UiEffect
    }
}
