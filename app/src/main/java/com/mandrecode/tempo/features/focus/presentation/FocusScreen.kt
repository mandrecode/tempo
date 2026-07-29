package com.mandrecode.tempo.features.focus.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mandrecode.tempo.core.ui.navigation.adaptiveScreenContentLayout
import com.mandrecode.tempo.core.ui.navigation.floatingRailContentClearance

@OptIn(ExperimentalMaterial3Api::class)
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
    val railContentClearance = floatingRailContentClearance()

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

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .fillMaxHeight()
                // Explicit fill rather than relying on the root Surface: on rail layouts
                // adaptiveScreenContentLayout insets the Scaffold to clear the floating rail,
                // exposing this Box's background in that gutter. It must match the Scaffold's own
                // colour, matching what Routines and Tasks already do.
                .background(MaterialTheme.colorScheme.background),
    ) {
        Scaffold(
            modifier = Modifier.adaptiveScreenContentLayout(railClearance = railContentClearance),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0),
            topBar = topBar,
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                FocusContent(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                )
            }
        }
    }
}
