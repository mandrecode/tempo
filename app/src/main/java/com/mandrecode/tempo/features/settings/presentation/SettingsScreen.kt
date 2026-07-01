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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandrecode.tempo.R

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onOnboardingClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScaffold(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onOnboardingClick = onOnboardingClick,
        modifier = modifier,
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
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
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
