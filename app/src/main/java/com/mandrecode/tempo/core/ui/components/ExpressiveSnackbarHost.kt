package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.spacing
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation

@Composable
fun ExpressiveSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { snackbarData ->
        ExpressiveSnackbar(snackbarData)
    }
}

@Composable
internal fun ExpressiveSnackbar(snackbarData: SnackbarData) {
    Surface(
        modifier =
            Modifier
                .padding(horizontal = MaterialTheme.spacing.large, vertical = MaterialTheme.spacing.medium)
                .widthIn(max = 560.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        shadowElevation = 2.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.spacing.large,
                        vertical = MaterialTheme.spacing.default,
                    ),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.default),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val annotatedMessage = (snackbarData.visuals as? TempoSnackbarVisuals)?.annotatedMessage
            if (annotatedMessage != null) {
                Text(
                    text = annotatedMessage,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Text(
                    text = snackbarData.visuals.message,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
            snackbarData.visuals.actionLabel?.let { actionLabel ->
                ExpressiveSnackbarAction(
                    actionLabel = actionLabel,
                    onClick = snackbarData::performAction,
                )
            }
        }
    }
}

@Composable
private fun ExpressiveSnackbarAction(
    actionLabel: String,
    onClick: () -> Unit,
) {
    val (actionInteractionSource, actionCornerRadius) = rememberPressableButtonAnimation()
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(actionCornerRadius.value),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        interactionSource = actionInteractionSource,
    ) {
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier.padding(
                    horizontal = MaterialTheme.spacing.large,
                    vertical = MaterialTheme.spacing.default,
                ),
        )
    }
}
