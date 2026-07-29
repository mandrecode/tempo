package com.mandrecode.tempo.features.focus.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    clock: Clock = Clock.System,
) {
    val haptic = LocalHapticFeedback.current
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
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onExpand()
                    }.padding(horizontal = 16.dp, vertical = 14.dp),
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
                        // The wave is the session's heartbeat: it travels while time is running
                        // down and stands still the moment the user pauses.
                        waveSpeed = if (session.isPaused) STILL else WaveSpeed,
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

            SessionCardActions(
                session = session,
                onPauseResume = onPauseResume,
                onStop = onStop,
                onComplete = onComplete,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun SessionCardActions(
    session: FocusSession,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = sessionCardActionColors()

    val running = runningGroupActions(session, onComplete, onPauseResume)
    val stop = stopSessionAction(onStop)

    // All three across one row where they fit, unlike the session screen's two-plus-one: the card
    // is a summary sitting above the rest of the day, and a second row of controls would cost it
    // more height than the actions are worth. On a narrow window three columns cannot hold their
    // labels, and a clipped "Mark do…" is worse than the extra row, so it splits like the screen.
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (maxWidth >= ThreeAcrossMinWidth) {
            SessionActionGroup(
                actions = running + stop,
                colors = colors,
                modifier = Modifier.fillMaxWidth(),
                compact = true,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SessionActionGroup(
                    actions = running,
                    colors = colors,
                    modifier = Modifier.fillMaxWidth(),
                    compact = true,
                )
                SessionActionButton(action = stop, colors = colors, compact = true)
            }
        }
    }
}

/**
 * Below this the three labels stop fitting side by side. Measured against the longest of them
 * rather than a screen breakpoint, since the card is also narrowed by a rail or a docked pane.
 */
private val ThreeAcrossMinWidth = 340.dp

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

private val STILL = 0.dp
private val WaveSpeed = 10.dp
private const val TRACK_ALPHA = 0.24f
private const val LABEL_ALPHA = 0.75f
private val PillRadius = 20.dp
private val PillPressedRadius = 10.dp
