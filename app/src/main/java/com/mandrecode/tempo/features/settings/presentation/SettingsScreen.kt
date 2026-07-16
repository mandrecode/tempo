package com.mandrecode.tempo.features.settings.presentation

import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TwoRowsTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.navigation.adaptiveScreenContentLayout
import com.mandrecode.tempo.core.ui.navigation.floatingRailContentClearance

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onOnboardingClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    viewModel: SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScaffold(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onOnboardingClick = onOnboardingClick,
        modifier = modifier,
        showBackButton = showBackButton,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SettingsScaffold(
    uiState: SettingsContract.UiState,
    onEvent: (SettingsContract.UiEvent) -> Unit,
    onBackClick: () -> Unit,
    onOnboardingClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val settingsContainerColor = MaterialTheme.colorScheme.background

    Scaffold(
        containerColor = settingsContainerColor,
        contentWindowInsets = WindowInsets(0),
        modifier =
            modifier
                .adaptiveScreenContentLayout(railClearance = floatingRailContentClearance())
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TwoRowsTopAppBar(
                title = { expanded ->
                    Text(
                        text = stringResource(R.string.settings),
                        style =
                            if (expanded) {
                                MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Normal)
                            } else {
                                MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Normal)
                            },
                    )
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                            )
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = settingsContainerColor,
                        scrolledContainerColor = settingsContainerColor,
                    ),
                collapsedHeight = TopAppBarDefaults.LargeAppBarCollapsedHeight,
                expandedHeight = TopAppBarDefaults.LargeAppBarExpandedHeight,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        SettingsContent(
            uiState = uiState,
            onEvent = onEvent,
            onOnboardingClick = onOnboardingClick,
            modifier = Modifier.padding(padding),
        )
    }
}
