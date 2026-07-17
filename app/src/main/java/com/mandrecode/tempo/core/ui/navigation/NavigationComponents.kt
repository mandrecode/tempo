package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation

internal val FloatingRailStartPadding = 24.dp
internal val FloatingRailSurfaceWidth = FloatingToolbarItemSize + FloatingToolbarRailSurfacePadding * 2
internal val FloatingRailExpandedSurfaceWidth = 220.dp
private val FloatingRailContentGap = 16.dp
internal val FloatingRailContentStartPadding =
    FloatingRailStartPadding + FloatingRailSurfaceWidth + FloatingRailContentGap
internal val FloatingRailExpandedContentStartPadding =
    FloatingRailStartPadding + FloatingRailExpandedSurfaceWidth + FloatingRailContentGap
internal val ReadableContentMaxWidth = 840.dp

/**
 * Lays out top-level screen content adaptively: reserves [railClearance] for the floating rail
 * and caps content at a readable width,
 * centered in the remaining space, on wide windows. Obtain the clearance for the current window
 * from [floatingRailContentClearance].
 */
fun Modifier.adaptiveScreenContentLayout(railClearance: Dp): Modifier =
    padding(start = railClearance)
        .fillMaxWidth()
        .wrapContentWidth(Alignment.CenterHorizontally)
        .widthIn(max = ReadableContentMaxWidth)

@Composable
internal fun rememberIsSingleTabMode(navigationPreferencesRepository: NavigationPreferencesRepository): Boolean {
    val isRoutinesTabEnabled by navigationPreferencesRepository
        .isRoutinesTabEnabled()
        .collectAsStateWithLifecycle(initialValue = true)
    val isTasksTabEnabled by navigationPreferencesRepository
        .isTasksTabEnabled()
        .collectAsStateWithLifecycle(initialValue = true)
    return !isRoutinesTabEnabled || !isTasksTabEnabled
}

@Composable
fun TempoBottomRail(
    navigationContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    actionButton: (@Composable () -> Unit)? = null,
    soloActionContent: (@Composable () -> Unit)? = null,
    isRailLayout: Boolean = false,
) {
    val navContent = remember(navigationContent) { movableContentOf(navigationContent) }
    val actionContent = remember(actionButton) { actionButton?.let { movableContentOf(it) } }

    when {
        soloActionContent != null -> {
            SoloBottomRail(
                soloActionContent = soloActionContent,
                modifier = modifier,
            )
        }

        isRailLayout -> {
            LandscapeBottomRail(
                navigationContent = navContent,
                actionContent = actionContent,
                modifier = modifier,
            )
        }

        else -> {
            PortraitBottomRail(
                navigationContent = navContent,
                actionContent = actionContent,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun SoloBottomRail(
    soloActionContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            soloActionContent()
        }
    }
}

@Composable
private fun LandscapeBottomRail(
    navigationContent: @Composable () -> Unit,
    actionContent: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .navigationBarsPadding()
                .padding(
                    start = FloatingRailStartPadding,
                    top = 16.dp,
                    bottom = 16.dp,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                navigationContent()
                actionContent?.invoke()
            }
        }
    }
}

@Composable
private fun PortraitBottomRail(
    navigationContent: @Composable () -> Unit,
    actionContent: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigationContent()
            actionContent?.invoke()
        }
    }
}

@Composable
fun TempoBottomRailActionButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val (buttonInteractionSource, buttonCornerRadius) = rememberPressableButtonAnimation()

    FloatingActionButton(
        modifier = modifier.size(FloatingToolbarActionButtonSize),
        shape = RoundedCornerShape(buttonCornerRadius.value),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        elevation =
            FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp,
            ),
        interactionSource = buttonInteractionSource,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
fun TempoSoloActionButton(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    val (buttonInteractionSource, buttonCornerRadius) = rememberPressableButtonAnimation()

    ExtendedFloatingActionButton(
        text = { Text(label) },
        icon = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
            )
        },
        expanded = expanded,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        shape = RoundedCornerShape(buttonCornerRadius.value),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        elevation =
            FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp,
            ),
        interactionSource = buttonInteractionSource,
    )
}
