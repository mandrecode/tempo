package com.mandrecode.tempo.features.tasks.presentation.components.cards

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.metadataLabel
import com.mandrecode.tempo.core.ui.util.color
import com.mandrecode.tempo.core.ui.util.containerColor
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.util.DateTimeFormatter
import kotlinx.datetime.LocalDateTime

/** The one date a card shows: when the task was finished, or else when it is due. */
internal sealed interface MetadataBadgeState {
    data object None : MetadataBadgeState

    data class Completed(
        val completedAt: LocalDateTime,
    ) : MetadataBadgeState

    data class Reminder(
        val reminderDate: LocalDateTime,
    ) : MetadataBadgeState
}

internal fun Task.metadataBadgeState(): MetadataBadgeState =
    when {
        isCompleted && completedAt != null -> MetadataBadgeState.Completed(completedAt)
        reminderDate != null -> MetadataBadgeState.Reminder(reminderDate)
        else -> MetadataBadgeState.None
    }

@Composable
internal fun TaskDateBadge(taskBadgeState: MetadataBadgeState) {
    Box(modifier = Modifier.heightIn(min = 22.dp), contentAlignment = Alignment.CenterStart) {
        AnimatedContent(
            targetState = taskBadgeState,
            transitionSpec = {
                (
                    fadeIn(animationSpec = tween(durationMillis = 180)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = 120))
                ).using(SizeTransform(clip = false))
            },
            label = "taskBadge",
        ) { kind ->
            when (kind) {
                is MetadataBadgeState.Completed -> {
                    CompletedDateBadgeContent(kind.completedAt)
                }

                is MetadataBadgeState.Reminder -> {
                    ReminderDateBadgeContent(kind.reminderDate)
                }

                MetadataBadgeState.None -> Unit
            }
        }
    }
}

@Composable
private fun CompletedDateBadgeContent(completedAt: LocalDateTime) {
    Badge(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier.padding(
                    horizontal = 6.dp,
                    vertical = 2.dp,
                ),
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.completed),
                modifier = Modifier.size(10.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = getSimpleFormattedDate(completedAt),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.metadataLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag(TASK_METADATA_COMPLETED_DATE_TAG),
            )
        }
    }
}

@Composable
private fun ReminderDateBadgeContent(reminderDate: LocalDateTime) {
    Badge(
        containerColor =
            MaterialTheme.colorScheme.tertiaryContainer.copy(
                alpha = 0.6f,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier.padding(
                    horizontal = 6.dp,
                    vertical = 2.dp,
                ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_reminder),
                contentDescription = stringResource(R.string.reminder_label),
                modifier = Modifier.size(10.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = getSimpleFormattedDate(reminderDate),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                style = MaterialTheme.typography.metadataLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag(TASK_METADATA_REMINDER_DATE_TAG),
            )
        }
    }
}

@Composable
internal fun getSimpleFormattedDate(dateTime: LocalDateTime): String {
    val context = LocalContext.current
    return remember(dateTime, context) {
        DateTimeFormatter.format(
            dateTime,
            DateTimeFormatter.Format.Short,
            context,
            useNaturalDates = true,
        )
    }
}
