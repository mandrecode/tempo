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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation

@Composable
fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (interactionSource, cornerRadiusState) =
        rememberPressableButtonAnimation(
            baseRadius = 20.dp,
            pressedRadius = 14.dp,
            animationDuration = 220,
        )
    val isPressed by interactionSource.collectIsPressedAsState()
    val cornerRadius by cornerRadiusState
    val containerColor by animateColorAsState(
        targetValue =
            if (isPressed) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        label = "settingsButtonContainerColor",
    )
    val contentColor by animateColorAsState(
        targetValue =
            if (isPressed) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.primary
            },
        label = "settingsButtonContentColor",
    )
    val borderColor by animateColorAsState(
        targetValue =
            if (isPressed) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.48f)
            },
        label = "settingsButtonBorderColor",
    )
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(40.dp)
                .clip(shape)
                .background(containerColor)
                .border(1.dp, borderColor, shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                ),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_settings),
            contentDescription = stringResource(R.string.settings),
            tint = contentColor,
        )
    }
}
