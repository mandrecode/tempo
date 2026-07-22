package com.mandrecode.tempo.core.ui.model

import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.whatsnew.presentation.model.WhatsNewEntry

sealed interface MainUiState {
    data object Loading : MainUiState

    data class Success(
        val themeMode: ThemeMode,
        val useTempoColors: Boolean,
        val defaultTab: String,
        val isRoutinesTabEnabled: Boolean,
        val isTasksTabEnabled: Boolean,
        val isOnboardingCompleted: Boolean,
        val whatsNewVersionName: String,
        // Null when there is no unseen feature to announce.
        val whatsNewEntry: WhatsNewEntry? = null,
    ) : MainUiState
}
