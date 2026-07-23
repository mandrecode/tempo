package com.mandrecode.tempo.features.tasks.presentation.components.cards

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.theme.metadataLabel
import com.mandrecode.tempo.core.ui.theme.subtaskTitle
import com.mandrecode.tempo.core.ui.util.color
import com.mandrecode.tempo.core.ui.util.containerColor
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.util.DateTimeFormatter
import kotlinx.datetime.LocalDateTime

@Composable
internal fun SubtaskItem(
    subtask: Task,
    onToggleCompletion: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier,
) {
    val hasMetadata =
        subtask.priority != null ||
            subtask.reminderDate != null ||
            (subtask.isCompleted && subtask.completedAt != null) ||
            subtask.description.isNotBlank()

    val checkboxRadius by animateDpAsState(
        targetValue = if (subtask.isCompleted) 12.dp else 18.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "subtask_checkbox_radius",
    )

    val subtaskColor by animateColorAsState(
        targetValue =
            if (subtask.isCompleted) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            },
        animationSpec = tween(CARD_CONTENT_ANIM_DURATION_MS),
        label = "subtask_color",
    )

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(subtaskColor)
                .clickable { onEdit(subtask) }
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_drag_handle),
            contentDescription = stringResource(R.string.reorder_subtask),
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(checkboxRadius))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleCompletion(subtask)
                    }.background(
                        if (subtask.isCompleted) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        },
                    ).then(
                        if (!subtask.isCompleted) {
                            Modifier.border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(checkboxRadius),
                            )
                        } else {
                            Modifier
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (subtask.isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.completed),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .animateContentSize(animationSpec = tween(CARD_CONTENT_ANIM_DURATION_MS)),
        ) {
            Text(
                text = subtask.title,
                style =
                    MaterialTheme.typography.subtaskTitle.copy(
                        textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else null,
                    ),
                color =
                    if (subtask.isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            AnimatedVisibility(
                visible = hasMetadata,
                enter = fadeIn(animationSpec = tween(METADATA_ROW_FADE_IN_MS)),
                exit = fadeOut(animationSpec = tween(METADATA_ROW_FADE_OUT_MS)),
            ) {
                SubtaskMetadataRow(subtask = subtask)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = painterResource(R.drawable.ic_edit),
            contentDescription = stringResource(R.string.edit_subtask),
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        )
    }
}

@Composable
private fun SubtaskMetadataRow(subtask: Task) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 4.dp),
    ) {
        if (subtask.description.isNotBlank()) {
            Icon(
                painter = painterResource(R.drawable.ic_short_text),
                contentDescription = stringResource(R.string.description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
        }

        if (subtask.priority != null) {
            PriorityFlagIcon(priority = subtask.priority, badgeSize = 20.dp, iconSize = 12.dp)
        }

        Box(modifier = Modifier.heightIn(min = 20.dp), contentAlignment = Alignment.CenterStart) {
            val badgeState =
                when {
                    subtask.isCompleted && subtask.completedAt != null ->
                        MetadataBadgeState.Completed(subtask.completedAt)
                    subtask.reminderDate != null -> MetadataBadgeState.Reminder(subtask.reminderDate)
                    else -> MetadataBadgeState.None
                }
            AnimatedContent(
                targetState = badgeState,
                transitionSpec = {
                    (
                        fadeIn(animationSpec = tween(durationMillis = 180)) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 120))
                    ).using(SizeTransform(clip = false))
                },
                label = "subtaskBadge",
            ) { kind ->
                when (kind) {
                    is MetadataBadgeState.Completed -> {
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
                                    text = getSimpleFormattedDate(kind.completedAt),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.metadataLabel,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    is MetadataBadgeState.Reminder -> {
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
                                    text = getSimpleFormattedDate(kind.reminderDate),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    style = MaterialTheme.typography.metadataLabel,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    MetadataBadgeState.None -> {
                        Spacer(modifier = Modifier.size(0.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityFlagIcon(
    priority: Priority,
    badgeSize: Dp,
    iconSize: Dp,
) {
    Box(
        modifier =
            Modifier
                .size(badgeSize)
                .clip(CircleShape)
                .background(priority.containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_flag),
            contentDescription = stringResource(R.string.priority_label),
            tint = priority.color,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
internal fun MetadataRow(
    task: Task,
    subtasks: List<Task>,
) {
    val taskBadgeState =
        when {
            task.isCompleted && task.completedAt != null -> MetadataBadgeState.Completed(task.completedAt)
            task.reminderDate != null -> MetadataBadgeState.Reminder(task.reminderDate)
            else -> MetadataBadgeState.None
        }

    val completedCount = remember(subtasks) { subtasks.count { it.isCompleted } }
    val metadataItems =
        buildList<MetadataItem> {
            if (task.priority != null) {
                add(
                    MetadataItem("priority") {
                        PriorityFlagIcon(priority = task.priority, badgeSize = 18.dp, iconSize = 12.dp)
                    },
                )
            }
            if (task.periodicity != null) {
                add(
                    MetadataItem("periodicity") {
                        Icon(
                            painter = painterResource(R.drawable.ic_repeat),
                            contentDescription = stringResource(R.string.periodicity_label),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
            if (taskBadgeState != MetadataBadgeState.None) {
                add(
                    MetadataItem("taskBadge", flexible = true) {
                        TaskDateBadge(taskBadgeState)
                    },
                )
            }
            if (subtasks.isNotEmpty()) {
                add(
                    MetadataItem("subtaskCount") {
                        SubtaskCountBadge(
                            completedCount = completedCount,
                            totalCount = subtasks.size,
                        )
                    },
                )
            }
        }

    MetadataOverflowRow(
        items = metadataItems,
        modifier = Modifier.padding(top = 4.dp),
    )
}

private data class MetadataItem(
    val key: String,
    val flexible: Boolean = false,
    val content: @Composable () -> Unit,
)

@Composable
private fun MetadataOverflowRow(
    items: List<MetadataItem>,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        items.forEach { item ->
            if (item.flexible) {
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    item.content()
                }
            } else {
                item.content()
            }
        }
    }
}

@Composable
private fun TaskDateBadge(taskBadgeState: MetadataBadgeState) {
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
private fun SubtaskCountBadge(
    completedCount: Int,
    totalCount: Int,
) {
    Badge(
        containerColor =
            MaterialTheme.colorScheme.secondaryContainer.copy(
                alpha = 0.3f,
            ),
    ) {
        Text(
            text = "$completedCount/$totalCount",
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier.padding(
                    horizontal = 6.dp,
                    vertical = 2.dp,
                ),
            style = MaterialTheme.typography.metadataLabel,
        )
    }
}

private sealed interface MetadataBadgeState {
    data object None : MetadataBadgeState

    data class Completed(
        val completedAt: LocalDateTime,
    ) : MetadataBadgeState

    data class Reminder(
        val reminderDate: LocalDateTime,
    ) : MetadataBadgeState
}

@Composable
private fun getSimpleFormattedDate(dateTime: LocalDateTime): String {
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
