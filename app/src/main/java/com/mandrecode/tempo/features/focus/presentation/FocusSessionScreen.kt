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
    modifier: Modifier = Modifier,
    viewModel: FocusViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.session
    val currentOnBack by rememberUpdatedState(onBack)

    // Nothing left to show once the session is over — leave rather than stranding an empty screen.
    LaunchedEffect(session == null) {
        if (session == null) currentOnBack()
    }

    session?.let {
        FocusSessionScreen(
            session = it,
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onBack = currentOnBack,
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
    session: FocusSession,
    uiState: FocusContract.UiState,
    onEvent: (FocusContract.UiEvent) -> Unit,
    onBack: () -> Unit,
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
                title = session.taskTitle,
                isRailLayout = isRailLayout,
                navigationIcon = navigationIcon,
                colors = colors,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        SessionBody(
            session = session,
            subtasks = uiState.sessionSubtasks,
            task = uiState.sessionEntry?.task,
            categoryName = uiState.sessionEntry?.categoryName,
            onPauseResume = {
                onEvent(
                    if (session.isPaused) {
                        FocusContract.UiEvent.ResumeSession
                    } else {
                        FocusContract.UiEvent.PauseSession
                    },
                )
            },
            onStop = { onEvent(FocusContract.UiEvent.StopSession) },
            onComplete = { onEvent(FocusContract.UiEvent.CompleteSessionTask) },
            onToggleSubtask = { onEvent(FocusContract.UiEvent.ToggleTaskCompletion(it)) },
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
