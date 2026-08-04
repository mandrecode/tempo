package com.mandrecode.tempo.features.focus.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.ExpressiveSnackbarHost
import com.mandrecode.tempo.core.ui.navigation.adaptiveScreenContentLayout
import com.mandrecode.tempo.core.ui.navigation.floatingNavigationSnackbarBottomPadding
import com.mandrecode.tempo.core.ui.navigation.floatingRailContentClearance
import com.mandrecode.tempo.features.focus.presentation.components.PlanTasksSheet
import com.mandrecode.tempo.features.focus.presentation.components.ReplaceSessionDialog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    onOpenSession: () -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Told whether today is paused, so the title can carry the same palm Routines does. Vacation is
     * one toggle across the app, and a surface with its own streak has to say when that streak is
     * being counted on holiday terms.
     */
    topBar: @Composable (isVacationModeActive: Boolean) -> Unit = {},
    viewModel: FocusViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val railContentClearance = floatingRailContentClearance()
    val snackbarHostState = remember { SnackbarHostState() }

    FocusEffects(
        uiEffect = viewModel.uiEffect,
        snackbarHostState = snackbarHostState,
        onOpenSession = onOpenSession,
        onEvent = viewModel::onEvent,
    )

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
            topBar = { topBar(uiState.isVacationModeActive) },
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                FocusContent(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                )

                // Hosted here rather than as the Scaffold's own snackbarHost, the way Tasks and
                // Routines do it: the floating bar owns the bottom of a compact window, and a
                // snackbar left where Material puts it comes up underneath it.
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .padding(bottom = floatingNavigationSnackbarBottomPadding()),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    ExpressiveSnackbarHost(snackbarHostState)
                }
            }
        }
    }

    val dismissEditor = { viewModel.onEvent(FocusContract.UiEvent.DismissEditor) }
    uiState.pendingStart?.let { pending ->
        ReplaceSessionDialog(pending = pending, onEvent = viewModel::onEvent)
    }

    // Under the editors, so opening a task from the sheet stacks the form over it rather than
    // replacing it — closing the form puts you back where you were planning from.
    uiState.planSheet?.let { planSheet ->
        PlanTasksSheet(state = planSheet, onEvent = viewModel::onEvent)
    }

    uiState.taskEditor?.let { target ->
        FocusTaskEditor(target = target, onDismiss = dismissEditor)
    }
    uiState.routineEditor?.let { target ->
        FocusRoutineEditor(target = target, onDismiss = dismissEditor)
    }
}

/**
 * The screen's one-shot events: opening the session, and the offer to take a batch of planning
 * back.
 *
 * The undo lives out here rather than in the sheet because it is about what the sheet did, and by
 * the time there is anything to say the sheet has closed.
 */
@Composable
private fun FocusEffects(
    uiEffect: Flow<FocusContract.UiEffect>,
    snackbarHostState: SnackbarHostState,
    onOpenSession: () -> Unit,
    onEvent: (FocusContract.UiEvent) -> Unit,
) {
    val undoLabel = stringResource(R.string.undo)
    val currentOnOpenSession by rememberUpdatedState(onOpenSession)
    val currentOnEvent by rememberUpdatedState(onEvent)
    // The count is only known when the effect arrives, so the message has to be formatted inside
    // the collector rather than read as a resource up here. Kept current rather than captured: the
    // effect does not restart on a configuration change, and a closed-over Resources would go on
    // answering in the locale the screen opened in.
    val currentResources by rememberUpdatedState(LocalContext.current.resources)

    LaunchedEffect(uiEffect, snackbarHostState) {
        uiEffect.collect { effect ->
            when (effect) {
                FocusContract.UiEffect.OpenSessionScreen -> currentOnOpenSession()

                // Launched rather than awaited. A snackbar stands for seconds, and this collector
                // is also where navigation arrives — awaiting it would leave a session started
                // from Up next running with no screen opening until the notice about the planning
                // had gone. Concurrent shows still queue on the host's own mutex, so nothing is
                // lost by not waiting here.
                is FocusContract.UiEffect.PlanBatchConfirmed ->
                    launch {
                        val result =
                            snackbarHostState.showSnackbar(
                                message =
                                    currentResources.getQuantityString(
                                        R.plurals.plan_tasks_undo_message,
                                        effect.count,
                                        effect.count,
                                    ),
                                actionLabel = undoLabel,
                                withDismissAction = true,
                            )
                        if (result == SnackbarResult.ActionPerformed) {
                            // The batch came with the offer, so it is still the one this snackbar
                            // was raised for even if another sheet has been and gone since.
                            currentOnEvent(FocusContract.UiEvent.UndoPlanBatch(effect.batch))
                        }
                    }
            }
        }
    }
}
