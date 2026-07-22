package com.mandrecode.tempo.features.widget.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.core.ui.theme.TempoTheme

@Composable
fun QuickAddTaskScreen(
    onClose: () -> Unit,
    viewModel: QuickAddTaskViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnClose by rememberUpdatedState(onClose)

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                QuickAddTaskContract.UiEffect.Close -> currentOnClose()
            }
        }
    }

    val darkTheme =
        when (uiState.themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }

    TempoTheme(darkTheme = darkTheme, useTempoColors = uiState.useTempoColors) {
        QuickAddTaskContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
