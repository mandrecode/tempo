package com.mandrecode.tempo.core.ui.model

import com.mandrecode.tempo.core.domain.model.ThemeMode

sealed interface MainUiState {
    data object Loading : MainUiState

    data class Success(
        val themeMode: ThemeMode,
        val useTempoColors: Boolean,
        val defaultTab: String,
        val isRoutinesTabEnabled: Boolean,
        val isTasksTabEnabled: Boolean,
    ) : MainUiState
}
