package com.mandrecode.tempo.features.focus.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mandrecode.tempo.core.ui.components.TempoModalBottomSheet
import com.mandrecode.tempo.core.ui.theme.inputTitle
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.focus.presentation.components.ReplaceSessionDialog
import com.mandrecode.tempo.features.focus.presentation.components.SessionBody

/**
 * Entry point for the session sheet: resolves state and closes itself when the session ends, so the
 * caller only has to say how to dismiss it.
 */
@Composable
fun FocusSessionRoute(
    onBack: () -> Unit,
    onOpenTaskInTasks: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FocusViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.session
    val entry = uiState.sessionEntry
    val currentOnBack by rememberUpdatedState(onBack)

    // Nothing left to show once the session is over and no task is being previewed — leave rather
    // than stranding an empty screen. Keyed off the raw preview id, not the resolved entry: the
    // agenda this screen looks the task up in has not loaded on the first composition, and reading
    // that as "nothing to show" would close the screen the moment it opened.
    val hasSomethingToShow = session != null || uiState.previewTaskId != null
    LaunchedEffect(hasSomethingToShow) {
        if (!hasSomethingToShow) currentOnBack()
    }

    // The preview is this screen's own state: keeping it alive after leaving would make the next
    // visit open on a task the user did not pick.
    DisposableEffect(Unit) {
        onDispose { viewModel.onEvent(FocusContract.UiEvent.ClearSessionPreview) }
    }

    uiState.pendingStart?.let { pending ->
        ReplaceSessionDialog(pending = pending, onEvent = viewModel::onEvent)
    }

    uiState.taskEditor?.let { target ->
        FocusTaskEditor(
            target = target,
            onDismiss = { viewModel.onEvent(FocusContract.UiEvent.DismissEditor) },
        )
    }

    if (hasSomethingToShow) {
        FocusSessionSheet(
            session = session,
            title = session?.taskTitle ?: entry?.task?.title.orEmpty(),
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onDismiss = currentOnBack,
            onOpenTaskInTasks = onOpenTaskInTasks,
            modifier = modifier,
        )
    }
}

/**
 * The running session, as a bottom sheet opened to its full height.
 *
 * A sheet rather than a slide-in screen of its own. Everything else in this app that shows you one
 * item — a task, a habit, the length of a break — arrives as a sheet from the bottom, and the
 * session arriving from the side made it the one surface with its own way of appearing. The task
 * title heads it, because while a session runs that task *is* the subject.
 */
@Composable
fun FocusSessionSheet(
    session: FocusSession?,
    title: String,
    uiState: FocusContract.UiState,
    onEvent: (FocusContract.UiEvent) -> Unit,
    onDismiss: () -> Unit,
    onOpenTaskInTasks: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    TempoModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) { _ ->
        val task = uiState.sessionEntry?.task

        // The style the task and habit editors give their titles, so a session reads as another
        // sheet about one item rather than a screen with its own heading.
        Text(
            text = title,
            style = MaterialTheme.typography.inputTitle,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(horizontal = SheetTitlePadding),
        )

        SessionBody(
            session = session,
            plannedLength =
                session?.plannedLength
                    ?: FocusSession.lengthOf(uiState.defaultSessionLengthMinutes),
            sessionsToday = uiState.sessionEntry?.sessionsToday ?: 0,
            subtasks = uiState.sessionSubtasks,
            task = task,
            categoryName = uiState.sessionEntry?.categoryName,
            onStart = { minutes -> onEvent(FocusContract.UiEvent.StartSession(minutes)) },
            onPauseResume = {
                onEvent(
                    if (session?.isPaused != false) {
                        FocusContract.UiEvent.ResumeSession
                    } else {
                        FocusContract.UiEvent.PauseSession
                    },
                )
            },
            onStop = { onEvent(FocusContract.UiEvent.StopSession) },
            onComplete = { onEvent(FocusContract.UiEvent.CompleteSessionTask) },
            onBackToWork = { onEvent(FocusContract.UiEvent.BackToWork) },
            onToggleSubtask = { onEvent(FocusContract.UiEvent.ToggleTaskCompletion(it)) },
            onEditSubtask = { onEvent(FocusContract.UiEvent.EditTask(it)) },
            onOpenInTasks = { task?.let { onOpenTaskInTasks(it.id) } },
        )
    }
}

private val SheetTitlePadding = 16.dp
