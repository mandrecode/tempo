package com.mandrecode.tempo.features.focus.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TaskCompletionCheckbox
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlin.time.Clock

private val RingSize = 220.dp

/**
 * The session, alone on the screen.
 *
 * Reached by tapping the session card — going deeper is a choice, not the default. Everything the
 * agenda normally shows steps away; what remains is the ring, the task, and its subtasks as a
 * checklist. This is the one screen where subtasks earn top billing, because they are the shape of
 * the work being done right now.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ImmersiveSessionView(
    session: FocusSession,
    subtasks: List<Task>,
    onCollapse: () -> Unit,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onToggleSubtask: (Task) -> Unit,
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

    // Collapsing returns to the list with the session still running — back should never be the
    // thing that stops your work.
    BackHandler(onBack = onCollapse)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CollapseRow(
            plannedMinutes = session.plannedLength.inWholeMinutes.toInt(),
            onCollapse = onCollapse,
        )

        SessionRing(
            progress = progress,
            label = remaining.asCountdownLabel(),
            isPaused = session.isPaused,
            modifier = Modifier.padding(top = 20.dp),
        )

        Text(
            text = session.taskTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp),
        )

        if (subtasks.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                subtasks.forEach { subtask ->
                    SubtaskRow(subtask = subtask, onToggle = { onToggleSubtask(subtask) })
                }
            }
        }

        ImmersiveActionRow(
            isPaused = session.isPaused,
            onPauseResume = onPauseResume,
            onStop = onStop,
            modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 32.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SessionRing(
    progress: Float,
    label: String,
    isPaused: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularWavyProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(RingSize),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = TRACK_ALPHA),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ImmersiveActionRow(
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ImmersiveButton(
            label = stringResource(R.string.focus_session_stop),
            onClick = onStop,
            modifier = Modifier.weight(1f),
        )
        ImmersiveButton(
            label =
                if (isPaused) {
                    stringResource(R.string.focus_session_resume)
                } else {
                    stringResource(R.string.focus_session_pause)
                },
            onClick = onPauseResume,
            modifier = Modifier.weight(1f),
            emphasised = true,
        )
    }
}

@Composable
private fun CollapseRow(
    plannedMinutes: Int,
    onCollapse: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.focus_session_collapse),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCollapse()
                    }.padding(vertical = 8.dp, horizontal = 4.dp),
        )
        Text(
            text = stringResource(R.string.focus_session_length_minutes, plannedMinutes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SubtaskRow(
    subtask: Task,
    onToggle: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    // Same weight as ticking the checkbox itself, which the row stands in for.
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TaskCompletionCheckbox(
            isCompleted = subtask.isCompleted,
            onToggle = { onToggle() },
        )
        Text(
            text = subtask.title,
            style = MaterialTheme.typography.bodyLarge,
            color =
                if (subtask.isCompleted) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
        )
    }
}

@Composable
private fun ImmersiveButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasised: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val (interactionSource, cornerRadius) =
        rememberPressableButtonAnimation(baseRadius = PillRadius, pressedRadius = PillPressedRadius)

    Surface(
        onClick = {
            haptic.performHapticFeedback(
                if (emphasised) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove,
            )
            onClick()
        },
        modifier = modifier,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(cornerRadius.value),
        color =
            if (emphasised) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
        contentColor =
            if (emphasised) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        )
    }
}

private const val TRACK_ALPHA = 0.22f
private val PillRadius = 24.dp
private val PillPressedRadius = 12.dp
