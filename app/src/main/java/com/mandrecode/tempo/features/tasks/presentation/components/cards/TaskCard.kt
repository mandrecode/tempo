package com.mandrecode.tempo.features.tasks.presentation.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoSpacing.cardContentPadding
import com.mandrecode.tempo.core.ui.theme.cardTitle
import com.mandrecode.tempo.core.ui.util.EnhancedDescriptionText
import com.mandrecode.tempo.core.ui.util.color
import com.mandrecode.tempo.core.ui.util.sanitizeDescription
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlin.math.roundToInt

private val SubtaskFallbackItemHeight = 68.dp

// Shared duration for card content size and color animations so layout, color, and
// metadata-slot transitions finish together, avoiding a "sluggish" feeling where
// layout snaps mid color-fade.
internal const val CARD_CONTENT_ANIM_DURATION_MS = 200
internal const val TASK_METADATA_COMPLETED_DATE_TAG = "task_metadata_completed_date"
internal const val TASK_METADATA_REMINDER_DATE_TAG = "task_metadata_reminder_date"

// Asymmetric fade durations for the SubtaskMetadataRow appearance/disappearance:
// fade-in is slightly slower so the badge feels like it settles into place, while
// fade-out is quicker so it gets out of the way for the toggle action.
internal const val METADATA_ROW_FADE_IN_MS = 180
internal const val METADATA_ROW_FADE_OUT_MS = 120

@Composable
fun TaskItem(
    task: Task,
    onToggleCompletion: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    modifier: Modifier = Modifier,
    onToggleSubtasksExpansion: (Boolean) -> Unit = {},
    onAddSubtask: (Long) -> Unit = {},
    onReorderSubtasks: (fromIndex: Int, toIndex: Int, subtasks: List<Task>) -> Unit = { _, _, _ -> },
    subtasks: List<Task> = emptyList(),
    isSubtasksExpanded: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    var isDescriptionOverflowing by remember { mutableStateOf(false) }
    val sanitizedDescription = remember(task.description) { sanitizeDescription(task.description) }

    var isDescriptionExpanded by remember { mutableStateOf(false) }

    val cardColor by animateColorAsState(
        targetValue =
            if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        animationSpec = tween(CARD_CONTENT_ANIM_DURATION_MS),
        label = "card_color",
    )

    val cardScale by animateFloatAsState(
        targetValue = if (task.isCompleted) 0.98f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        label = "card_scale",
    )

    val cardOffset by animateDpAsState(
        targetValue = if (task.isCompleted) 6.dp else 0.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "card_offset",
    )

    val cardCornerRadius by animateDpAsState(
        targetValue = if (task.isCompleted) 24.dp else 32.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "card_corner_radius",
    )

    val checkboxRadius by animateDpAsState(
        targetValue = if (task.isCompleted) 16.dp else 24.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "checkbox_radius",
    )

    val checkboxScale by animateFloatAsState(
        targetValue = if (task.isCompleted) 1.1f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "checkbox_scale",
    )

    val cardShape = RoundedCornerShape(cardCornerRadius)

    Card(
        onClick = { onEdit(task) },
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = cardOffset.toPx()
                }.clip(cardShape)
                .scale(cardScale),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(cardContentPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                scaleX = checkboxScale
                                scaleY = checkboxScale
                            }.clip(RoundedCornerShape(checkboxRadius))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleCompletion(task)
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(48.dp)
                                .background(
                                    color =
                                        if (task.isCompleted) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                        },
                                    shape = RoundedCornerShape(checkboxRadius),
                                ).then(
                                    if (!task.isCompleted) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(checkboxRadius),
                                        )
                                    } else {
                                        Modifier
                                    },
                                ),
                    )

                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = stringResource(R.string.completed),
                            modifier = Modifier.size(24.dp),
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
                    Box(
                        modifier = Modifier.heightIn(min = 48.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = task.title,
                                style =
                                    MaterialTheme.typography.cardTitle.copy(
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                    ),
                                color =
                                    if (task.isCompleted) {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis,
                            )

                            if (sanitizedDescription.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                EnhancedDescriptionText(
                                    text = sanitizedDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color =
                                        if (task.isCompleted) {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        },
                                    maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 1,
                                    overflow = TextOverflow.Ellipsis,
                                    onTextLayout = { textLayoutResult ->
                                        isDescriptionOverflowing =
                                            if (isDescriptionExpanded) {
                                                textLayoutResult.lineCount > 1
                                            } else {
                                                textLayoutResult.hasVisualOverflow
                                            }
                                    },
                                )
                            }

                            if (task.parentTaskId == null &&
                                (
                                    task.priority != null ||
                                        task.periodicity != null ||
                                        task.reminderDate != null ||
                                        task.completedAt != null ||
                                        subtasks.isNotEmpty()
                                )
                            ) {
                                // Gated render: keeps title-only top-level tasks vertically
                                // centered against the checkbox. Tasks that already have any
                                // metadata still benefit from MetadataRow's internal
                                // always-reserved badge slot, so reminder<->completedAt
                                // swaps do not change card height. The rare title-only
                                // first-completion case animates smoothly via the
                                // animateContentSize on the enclosing Column.
                                MetadataRow(task, subtasks)
                            }
                        }
                    }
                }

                val showExpandButton = subtasks.isNotEmpty() || isDescriptionOverflowing
                val isExpanded =
                    if (subtasks.isNotEmpty()) isSubtasksExpanded else isDescriptionExpanded

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showExpandButton) {
                        IconButton(
                            onClick = {
                                if (subtasks.isNotEmpty()) {
                                    onToggleSubtasksExpansion(!isSubtasksExpanded)
                                    isDescriptionExpanded = !isSubtasksExpanded
                                } else {
                                    isDescriptionExpanded = !isDescriptionExpanded
                                }
                            },
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                painter =
                                    painterResource(
                                        if (isExpanded) {
                                            R.drawable.ic_expand_less
                                        } else {
                                            R.drawable.ic_expand_more
                                        },
                                    ),
                                contentDescription =
                                    if (isExpanded) {
                                        stringResource(R.string.collapse)
                                    } else {
                                        stringResource(R.string.expand)
                                    },
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }

                    IconButton(
                        onClick = { onAddSubtask(task.id) },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add_row_below),
                            contentDescription = stringResource(R.string.add_subtask),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = subtasks.isNotEmpty() && isSubtasksExpanded,
                enter = expandVertically(spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
                exit = shrinkVertically(spring(stiffness = Spring.StiffnessMedium)) + fadeOut(),
            ) {
                val density = LocalDensity.current
                var draggedIndex by remember { mutableIntStateOf(-1) }
                var dragOffset by remember { mutableFloatStateOf(0f) }
                var targetIndex by remember { mutableIntStateOf(-1) }
                var measuredItemHeight by remember { mutableFloatStateOf(0f) }
                val spacingPx = remember(density) { with(density) { 8.dp.toPx() } }

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    subtasks.forEachIndexed { index, subtask ->
                        val isDragging = draggedIndex == index
                        val isTarget = targetIndex == index

                        SubtaskItem(
                            subtask = subtask,
                            onToggleCompletion = onToggleCompletion,
                            onEdit = onEdit,
                            haptic = haptic,
                            modifier =
                                Modifier
                                    .onSizeChanged { size ->
                                        if (measuredItemHeight == 0f) {
                                            measuredItemHeight = size.height.toFloat()
                                        }
                                    }.zIndex(if (isDragging) 1f else 0f)
                                    .graphicsLayer {
                                        if (isDragging) {
                                            translationY = dragOffset
                                            alpha = 0.8f
                                        } else if (isTarget) {
                                            alpha = 0.5f
                                        }
                                    }.pointerInput(subtasks) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                draggedIndex = index
                                                dragOffset = 0f
                                                targetIndex = index
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffset += dragAmount.y

                                                val itemHeight =
                                                    if (measuredItemHeight > 0f) {
                                                        measuredItemHeight + spacingPx
                                                    } else {
                                                        with(density) { SubtaskFallbackItemHeight.toPx() }
                                                    }
                                                val newTargetIndex =
                                                    (index + (dragOffset / itemHeight).roundToInt())
                                                        .coerceIn(0, subtasks.size - 1)
                                                if (newTargetIndex != targetIndex) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                                targetIndex = newTargetIndex
                                            },
                                            onDragEnd = {
                                                if (draggedIndex != targetIndex && targetIndex >= 0) {
                                                    onReorderSubtasks(
                                                        draggedIndex,
                                                        targetIndex,
                                                        subtasks,
                                                    )
                                                }
                                                draggedIndex = -1
                                                dragOffset = 0f
                                                targetIndex = -1
                                            },
                                            onDragCancel = {
                                                draggedIndex = -1
                                                dragOffset = 0f
                                                targetIndex = -1
                                            },
                                        )
                                    },
                        )
                    }
                }
            }
        }
    }
}
