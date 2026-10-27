package com.mandrecode.tempo.core.ui.model

import com.mandrecode.tempo.core.domain.model.TempoTab
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.whatsnew.presentation.model.WhatsNewEntry

sealed interface MainUiState {
    data object Loading : MainUiState

    data class Success(
        val themeMode: ThemeMode,
        val useTempoColors: Boolean,
        val defaultTab: TempoTab,
        val enabledTabs: Set<TempoTab>,
        val isOnboardingCompleted: Boolean,
        val whatsNewVersionName: String,
        // Null when there is no unseen feature to announce.
        val whatsNewEntry: WhatsNewEntry? = null,
    ) : MainUiState
}
