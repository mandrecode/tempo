package com.mandrecode.tempo.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.glass.glassRim
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation

@Composable
fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val (interactionSource, cornerRadiusState) =
        rememberPressableButtonAnimation(
            baseRadius = 20.dp,
            pressedRadius = 14.dp,
            animationDuration = 220,
        )
    val isPressed by interactionSource.collectIsPressedAsState()
    val cornerRadius by cornerRadiusState
    val colors = settingsButtonColors(selected = selected, isPressed = isPressed)
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(48.dp)
                .semantics { this.selected = selected }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(40.dp)
                    // A light tint like the add-category chip's, not the chrome's: up here there is no
                    // backdrop to blur, and a solid fill reads as a heavy disc on the header gradient.
                    // Pressed or selected it is a clean, solid colour.
                    .clip(shape)
                    .background(colors.container)
                    .border(glassRim(), shape),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = stringResource(R.string.settings),
                tint = colors.content,
            )
        }
    }
}

private data class SettingsButtonColors(
    val container: Color,
    val content: Color,
)

@Composable
private fun settingsButtonColors(
    selected: Boolean,
    isPressed: Boolean,
): SettingsButtonColors {
    val colorScheme = MaterialTheme.colorScheme
    val container by animateColorAsState(
        targetValue =
            when {
                isPressed -> colorScheme.primaryContainer
                selected -> colorScheme.secondaryContainer
                // The add-category chip's fill, so the header gradient shows through it.
                else -> colorScheme.surfaceVariant.copy(alpha = REST_CONTAINER_ALPHA)
            },
        label = "settingsButtonContainerColor",
    )
    val content by animateColorAsState(
        targetValue =
            when {
                isPressed -> colorScheme.onPrimaryContainer
                selected -> colorScheme.onSecondaryContainer
                else -> colorScheme.primary
            },
        label = "settingsButtonContentColor",
    )
    return SettingsButtonColors(container = container, content = content)
}

private const val REST_CONTAINER_ALPHA = 0.5f
