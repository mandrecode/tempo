package com.mandrecode.tempo.features.settings.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TwoRowsTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.navigation.adaptiveScreenContentLayout
import com.mandrecode.tempo.core.ui.navigation.floatingRailContentClearance
import com.mandrecode.tempo.core.ui.navigation.isFloatingNavigationRailLayout

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onOnboardingClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    showTitle: Boolean = true,
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
        showTitle = showTitle,
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
    showTitle: Boolean = true,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val settingsContainerColor = MaterialTheme.colorScheme.background
    val isRailLayout = isFloatingNavigationRailLayout()

    Scaffold(
        containerColor = settingsContainerColor,
        contentWindowInsets = WindowInsets(0),
        modifier =
            modifier
                .adaptiveScreenContentLayout(railClearance = floatingRailContentClearance())
                .let {
                    if (isRailLayout) it else it.nestedScroll(scrollBehavior.nestedScrollConnection)
                },
        topBar = {
            SettingsTopBar(
                showTitle = showTitle,
                showBackButton = showBackButton,
                isRailLayout = isRailLayout,
                onBackClick = onBackClick,
                settingsContainerColor = settingsContainerColor,
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

/**
 * Deliberately does not use [com.mandrecode.tempo.core.ui.theme.topBarTitle] (bold, primary
 * color) like the Tasks/Routines root tabs. Those are persistent tab destinations with a
 * branded title; Settings is a pushed detail screen using the M3 large-collapsing-title
 * pattern, so its title stays normal-weight and onSurface-colored per that pattern instead of
 * competing with the tab title's brand emphasis.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsTopBar(
    showTitle: Boolean,
    showBackButton: Boolean,
    isRailLayout: Boolean,
    onBackClick: () -> Unit,
    settingsContainerColor: Color,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    if (!showTitle) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        return
    }

    val navigationIcon: @Composable () -> Unit = {
        if (showBackButton) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
        }
    }
    val topAppBarColors =
        TopAppBarDefaults.topAppBarColors(
            containerColor = settingsContainerColor,
            scrolledContainerColor = settingsContainerColor,
        )

    // Medium/expanded windows keep a fixed-size rail alongside this screen, so the large
    // display-style title has no scroll gesture to expand from and would just waste space.
    if (isRailLayout) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Normal),
                )
            },
            navigationIcon = navigationIcon,
            colors = topAppBarColors,
        )
    } else {
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
            navigationIcon = navigationIcon,
            colors = topAppBarColors,
            collapsedHeight = TopAppBarDefaults.LargeAppBarCollapsedHeight,
            expandedHeight = TopAppBarDefaults.LargeAppBarExpandedHeight,
            scrollBehavior = scrollBehavior,
        )
    }
}
