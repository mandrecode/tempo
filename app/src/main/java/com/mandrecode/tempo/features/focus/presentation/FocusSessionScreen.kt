package com.mandrecode.tempo.features.focus.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TwoRowsTopAppBar
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.navigation.isFloatingNavigationRailLayout
import com.mandrecode.tempo.core.ui.theme.settingsTopBarTitleCollapsed
import com.mandrecode.tempo.core.ui.theme.settingsTopBarTitleExpanded
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.focus.presentation.components.SessionBody

/**
 * Entry point for the slide-in session route: resolves state and closes itself when the session
 * ends, so the caller only has to say how to go back.
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

    if (hasSomethingToShow) {
        FocusSessionScreen(
            session = session,
            title = session?.taskTitle ?: entry?.task?.title.orEmpty(),
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onBack = currentOnBack,
            onOpenTaskInTasks = onOpenTaskInTasks,
            modifier = modifier,
        )
    }
}

/**
 * The running session as its own screen.
 *
 * Presented by sliding in from the right, the same way Settings is — so it reads as somewhere the
 * user went, with a back button to leave, rather than a mode the current screen switched into. The
 * task title is the screen's title, because while a session runs that task *is* the subject.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FocusSessionScreen(
    session: FocusSession?,
    title: String,
    uiState: FocusContract.UiState,
    onEvent: (FocusContract.UiEvent) -> Unit,
    onBack: () -> Unit,
    onOpenTaskInTasks: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val isRailLayout = isFloatingNavigationRailLayout()

    val navigationIcon: @Composable () -> Unit = {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
            )
        }
    }
    val colors =
        TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
        )

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            SessionTopBar(
                title = title,
                isRailLayout = isRailLayout,
                navigationIcon = navigationIcon,
                colors = colors,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val task = uiState.sessionEntry?.task
        SessionBody(
            session = session,
            plannedLength =
                session?.plannedLength
                    ?: FocusSession.lengthOf(uiState.defaultSessionLengthMinutes),
            subtasks = uiState.sessionSubtasks,
            task = task,
            categoryName = uiState.sessionEntry?.categoryName,
            onStart = { onEvent(FocusContract.UiEvent.StartSession) },
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
            onToggleSubtask = { onEvent(FocusContract.UiEvent.ToggleTaskCompletion(it)) },
            onOpenInTasks = { task?.let { onOpenTaskInTasks(it.id) } },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SessionTopBar(
    title: String,
    isRailLayout: Boolean,
    navigationIcon: @Composable () -> Unit,
    colors: TopAppBarColors,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    // Same split as Settings: a rail window has no scroll gesture for the large title to collapse
    // from, so it gets the compact bar instead.
    if (isRailLayout) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.settingsTopBarTitleCollapsed,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = navigationIcon,
            colors = colors,
        )
    } else {
        TwoRowsTopAppBar(
            title = { expanded ->
                Text(
                    text = title,
                    style =
                        if (expanded) {
                            MaterialTheme.typography.settingsTopBarTitleExpanded
                        } else {
                            MaterialTheme.typography.settingsTopBarTitleCollapsed
                        },
                    maxLines = if (expanded) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = navigationIcon,
            colors = colors,
            collapsedHeight = TopAppBarDefaults.LargeAppBarCollapsedHeight,
            expandedHeight = TopAppBarDefaults.LargeAppBarExpandedHeight,
            scrollBehavior = scrollBehavior,
        )
    }
}
