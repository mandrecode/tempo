package com.mandrecode.tempo.core.ui.model

import com.mandrecode.tempo.core.domain.model.TempoTab
import com.mandrecode.tempo.core.domain.model.ThemeMode

sealed interface MainUiState {
    data object Loading : MainUiState

    data class Success(
        val themeMode: ThemeMode,
        val useTempoColors: Boolean,
        val defaultTab: TempoTab,
        val enabledTabs: Set<TempoTab>,
        val isOnboardingCompleted: Boolean,
    ) : MainUiState
}
