package com.mandrecode.tempo.features.focus.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import kotlin.time.Clock

private val CardCornerRadius = 22.dp
private val RingSize = 68.dp

/**
 * The Up next card while a session is running.
 *
 * Same ground and same place in the layout as [UpNextCard] — the card transforms rather than being
 * replaced, so starting a session does not make the screen jump. The agenda underneath stays
 * usable: the point is to keep working, not to take the screen over.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RunningSessionCard(
    session: FocusSession,
    onExpand: () -> Unit,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    clock: Clock = Clock.System,
) {
    val remaining by rememberSessionCountdown(session, clock)
    val progress =
        if (session.plannedLength.inWholeSeconds <= 0) {
            0f
        } else {
            1f - (remaining.inWholeSeconds.toFloat() / session.plannedLength.inWholeSeconds)
        }.coerceIn(0f, 1f)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(CardCornerRadius),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Column(
            modifier =
                Modifier
                    .clickable(onClick = onExpand)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularWavyProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(RingSize),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        trackColor =
                            MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = TRACK_ALPHA),
                    )
                    Text(
                        text = remaining.asCountdownLabel(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }

                SessionTitleBlock(
                    title = session.taskTitle,
                    isPaused = session.isPaused,
                    modifier = Modifier.weight(1f),
                )
            }

            SessionActionRow(
                isPaused = session.isPaused,
                onPauseResume = onPauseResume,
                onStop = onStop,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun SessionActionRow(
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SessionActionButton(
            label =
                if (isPaused) {
                    stringResource(R.string.focus_session_resume)
                } else {
                    stringResource(R.string.focus_session_pause)
                },
            onClick = onPauseResume,
            modifier = Modifier.weight(1f),
        )
        SessionActionButton(
            label = stringResource(R.string.focus_session_stop),
            onClick = onStop,
            modifier = Modifier.weight(1f),
            emphasised = true,
        )
    }
}

@Composable
private fun SessionTitleBlock(
    title: String,
    isPaused: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text =
                if (isPaused) {
                    stringResource(R.string.focus_session_paused)
                } else {
                    stringResource(R.string.focus_session_focusing)
                },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = LABEL_ALPHA),
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun SessionActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasised: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(percent = FULLY_ROUNDED),
        color =
            if (emphasised) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = SUBTLE_BUTTON_ALPHA)
            },
        contentColor =
            if (emphasised) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.onTertiaryContainer
            },
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 10.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

private const val TRACK_ALPHA = 0.24f
private const val LABEL_ALPHA = 0.75f
private const val SUBTLE_BUTTON_ALPHA = 0.12f
private const val FULLY_ROUNDED = 50
