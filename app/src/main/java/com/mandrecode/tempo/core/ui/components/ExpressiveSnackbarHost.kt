package com.mandrecode.tempo.core.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.glass.glass
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

private val SnackbarShape = RoundedCornerShape(28.dp)

@Composable
internal fun ExpressiveSnackbar(snackbarData: SnackbarData) {
    // Floats over the page like the bar does, so it is the same chrome glass, with no shadow (it
    // would show through the glass as a grey cast).
    Surface(
        modifier =
            Modifier
                .padding(horizontal = MaterialTheme.spacing.large, vertical = MaterialTheme.spacing.medium)
                .widthIn(max = 560.dp)
                .glass(SnackbarShape),
        shape = SnackbarShape,
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 0.dp,
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
    val actionShape = RoundedCornerShape(actionCornerRadius.value)
    Surface(
        onClick = onClick,
        shape = actionShape,
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
