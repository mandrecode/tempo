package com.mandrecode.tempo.features.focus.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TempoModalBottomSheet
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.focus.presentation.FocusContract

/**
 * What the user sees when a session's time is up.
 *
 * States what happened and offers the next move. Deliberately no streak, no score, no celebration:
 * the summary reports, it does not congratulate. "Stop here" is a first-class choice sitting beside
 * the others rather than a dismissal hidden in the corner — a Pomodoro app that makes stopping feel
 * like failure is the thing this feature is trying not to be.
 */
@Composable
internal fun SessionFinishedSheet(
    finished: FocusContract.FinishedSession,
    onEvent: (FocusContract.UiEvent) -> Unit,
) {
    TempoModalBottomSheet(
        onDismissRequest = { onEvent(FocusContract.UiEvent.DismissFinishedSession) },
    ) { _ ->
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.focus_session_finished_title, finished.minutes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = finished.taskTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SheetButton(
                    label = stringResource(R.string.focus_session_stop),
                    onClick = { onEvent(FocusContract.UiEvent.DismissFinishedSession) },
                    modifier = Modifier.weight(1f),
                )
                SheetButton(
                    label =
                        stringResource(
                            R.string.focus_session_take_break,
                            FocusSession.BREAK_LENGTH.inWholeMinutes.toInt(),
                        ),
                    onClick = { onEvent(FocusContract.UiEvent.TakeBreak) },
                    modifier = Modifier.weight(1f),
                    emphasised = true,
                )
            }

            SheetButton(
                label = stringResource(R.string.focus_session_another, finished.minutes),
                onClick = { onEvent(FocusContract.UiEvent.StartAnotherSession) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                transparent = true,
            )
        }
    }
}

@Composable
private fun SheetButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasised: Boolean = false,
    transparent: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(percent = FULLY_ROUNDED),
        color =
            when {
                transparent -> androidx.compose.ui.graphics.Color.Transparent
                emphasised -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.secondaryContainer
            },
        contentColor =
            when {
                transparent -> MaterialTheme.colorScheme.onSurfaceVariant
                emphasised -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSecondaryContainer
            },
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
    }
}

private const val FULLY_ROUNDED = 50
