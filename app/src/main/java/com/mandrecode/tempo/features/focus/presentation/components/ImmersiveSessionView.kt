package com.mandrecode.tempo.features.focus.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TaskCompletionCheckbox
import com.mandrecode.tempo.core.ui.util.color
import com.mandrecode.tempo.core.ui.util.containerColor
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation
import com.mandrecode.tempo.core.ui.util.titleResId
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.util.DateTimeFormatter
import kotlin.time.Clock

private val RingSize = 220.dp
private const val FULLY_ROUNDED = 50

/**
 * The session itself: ring, task subtasks and controls. Hosted by [FocusSessionScreen], which
 * supplies the top bar and the back affordance — this composable deliberately owns neither, so the
 * screen around it decides how the user leaves.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SessionBody(
    session: FocusSession,
    subtasks: List<Task>,
    task: Task?,
    categoryName: String?,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onComplete: () -> Unit,
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

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Description sits above the timer: it is context for the work, and the title it belongs
        // to is already the screen's own title in the bar above.
        task?.description?.takeIf { it.isNotBlank() }?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }

        SessionRing(
            progress = progress,
            label = remaining.asCountdownLabel(),
            isPaused = session.isPaused,
            modifier = Modifier.padding(top = 24.dp),
        )

        if (task != null) {
            SessionTaskMetadata(
                task = task,
                categoryName = categoryName,
                modifier = Modifier.padding(top = 24.dp),
            )
        }

        if (subtasks.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                subtasks.forEach { subtask ->
                    SubtaskRow(subtask = subtask, onToggle = { onToggleSubtask(subtask) })
                }
            }
        }

        SessionControls(
            isPaused = session.isPaused,
            isBreak = session.isBreak,
            onPauseResume = onPauseResume,
            onStop = onStop,
            onComplete = onComplete,
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
            // Travels while running, still while paused — same rule as the card's ring.
            waveSpeed = if (isPaused) STILL else WaveSpeed,
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
private fun SessionControls(
    isPaused: Boolean,
    isBreak: Boolean,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onComplete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ImmersiveActionRow(
            isPaused = isPaused,
            onPauseResume = onPauseResume,
            onStop = onStop,
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        )

        // Separate from stopping: ending the timer and finishing the work are different intents,
        // and only this one ticks the task.
        if (!isBreak) {
            ImmersiveButton(
                label = stringResource(R.string.focus_session_mark_done),
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 32.dp),
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

/**
 * Category, priority and reminder for the task under way, as the full pills the task editor uses
 * rather than the compact badges its card shows — this screen has the room, and the editor's
 * vocabulary is the one that names each property outright.
 *
 * The reminder is the exception: the date is already implied by the session being under way, so it
 * shows the time alone.
 */
@Composable
private fun SessionTaskMetadata(
    task: Task,
    categoryName: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categoryName?.takeIf { it.isNotBlank() }?.let { name ->
            SessionMetadataPill(
                iconRes = R.drawable.ic_category,
                label = name,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        task.priority?.let { priority ->
            SessionMetadataPill(
                iconRes = R.drawable.ic_flag,
                label = stringResource(priority.titleResId),
                containerColor = priority.containerColor,
                contentColor = priority.color,
            )
        }
        task.reminderDate?.let { reminder ->
            SessionMetadataPill(
                iconRes = R.drawable.ic_notifications,
                label = DateTimeFormatter.formatTimeOfDay(reminder.time, context),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun SessionMetadataPill(
    iconRes: Int,
    label: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(percent = FULLY_ROUNDED),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
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

private val STILL = 0.dp
private val WaveSpeed = 10.dp
private const val TRACK_ALPHA = 0.22f
private val PillRadius = 24.dp
private val PillPressedRadius = 12.dp
