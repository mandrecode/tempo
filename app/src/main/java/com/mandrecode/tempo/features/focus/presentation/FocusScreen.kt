package com.mandrecode.tempo.features.focus.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FocusScreen(
    onNavigateToTasks: () -> Unit,
    onNavigateToRoutines: () -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    viewModel: FocusViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnNavigateToTasks by rememberUpdatedState(onNavigateToTasks)
    val currentOnNavigateToRoutines by rememberUpdatedState(onNavigateToRoutines)

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                // Focus shows the day; editing an item happens in the tab that owns it, so there is
                // one editor per entity rather than a second copy living here.
                is FocusContract.UiEffect.OpenTaskInTasksTab -> currentOnNavigateToTasks()
                FocusContract.UiEffect.OpenTasksTab -> currentOnNavigateToTasks()
                is FocusContract.UiEffect.OpenHabitInRoutinesTab -> currentOnNavigateToRoutines()
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        topBar()
        FocusContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
