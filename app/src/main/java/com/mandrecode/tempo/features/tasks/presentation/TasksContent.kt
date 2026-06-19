package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.theme.groupLabel
import com.mandrecode.tempo.core.ui.theme.sectionHeader
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.presentation.components.cards.TaskItem
import com.mandrecode.tempo.features.tasks.presentation.components.sections.CategoryChipRow
import com.mandrecode.tempo.features.tasks.presentation.components.sections.EmptyStateContent
import com.mandrecode.tempo.features.tasks.presentation.model.ActiveGroupKey
import com.mandrecode.tempo.features.tasks.presentation.model.CompletedGroupKey
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption
import com.mandrecode.tempo.util.DateTimeFormatter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.math.roundToInt
import kotlin.time.Clock

private const val ACTIVE_GROUP_OVERDUE_RANK = 0
private const val ACTIVE_GROUP_DATED_RANK = 1
private const val ACTIVE_GROUP_NO_DATE_RANK = 2
private const val ACTIVE_GROUP_FALLBACK_RANK = 3
private const val COMPLETED_GROUP_DATED_RANK = 0
private const val COMPLETED_GROUP_NO_DATE_RANK = 1
private const val COMPLETED_GROUP_FALLBACK_RANK = 2

@Composable
fun TasksContent(
    uiState: TasksContract.UiState,
    onEvent: (TasksContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onScrolledFromTopChange: (Boolean) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val currentOnScrolledFromTopChange by rememberUpdatedState(onScrolledFromTopChange)

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }.distinctUntilChanged().collect { isScrolledFromTop ->
            currentOnScrolledFromTopChange(isScrolledFromTop)
        }
    }

    val activeTaskGroups = uiState.activeTasks
    val completedTaskGroups = uiState.completedTaskGroups
    val subtasksMap = uiState.subtasksMap

    val hasCompletedTasks by remember(completedTaskGroups) {
        derivedStateOf {
            completedTaskGroups.isNotEmpty()
        }
    }

    val hasActiveTasks by remember(activeTaskGroups) {
        derivedStateOf {
            activeTaskGroups.isNotEmpty()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.loading_tasks),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                CategoryChipRow(
                    categories = uiState.categories,
                    counts = uiState.uncompletedTasksCounts,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onSelectCategory = { onEvent(TasksContract.UiEvent.CategorySelected(it)) },
                    onShowCategoryDialog = { onEvent(TasksContract.UiEvent.ShowCategoryDialog(it)) },
                    onRequestDeleteCategory = {
                        onEvent(
                            TasksContract.UiEvent.RequestDeleteCategory(
                                it,
                            ),
                        )
                    },
                    onReorderCategories = { fromIndex, toIndex, categories ->
                        onEvent(
                            TasksContract.UiEvent.ReorderCategories(fromIndex, toIndex, categories),
                        )
                    },
                )

                Box(
                    modifier = Modifier.weight(1f),
                ) {
                    if (!hasActiveTasks && completedTaskGroups.isEmpty()) {
                        EmptyStateContent()
                    } else {
                        val density = LocalDensity.current
                        val haptic = LocalHapticFeedback.current
                        var draggedIndex by remember { mutableIntStateOf(-1) }
                        var dragOffset by remember { mutableFloatStateOf(0f) }
                        var targetIndex by remember { mutableIntStateOf(-1) }

                        LazyColumn(
                            state = listState,
                            contentPadding =
                                PaddingValues(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 8.dp,
                                    bottom = 16.dp,
                                ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            item(key = "scroll_anchor") {
                                Spacer(Modifier.height(0.dp))
                            }

                            val activeEntries =
                                sortedActiveEntries(
                                    groups = activeTaskGroups,
                                    sortOption = uiState.sortOption,
                                )
                            val showActiveHeaders =
                                uiState.sortOption == SortOption.BY_DATE ||
                                    uiState.sortOption == SortOption.BY_PRIORITY
                            val flatActiveTasks =
                                if (uiState.sortOption == SortOption.MANUAL) {
                                    activeTaskGroups.values.flatten()
                                } else {
                                    emptyList()
                                }

                            if (uiState.sortOption == SortOption.MANUAL) {
                                // MANUAL mode: flat list with drag-and-drop
                                itemsIndexed(
                                    items = flatActiveTasks,
                                    key = { _, task -> task.id },
                                    contentType = { _, _ -> "task" },
                                ) { index, task ->
                                    val taskSubtasks = subtasksMap[task.id] ?: emptyList()
                                    val isSubtasksExpanded =
                                        task.id in uiState.expandedTaskIds || taskSubtasks.isEmpty()

                                    val isDragging = draggedIndex == index
                                    val isTarget = targetIndex == index

                                    val onToggleSubtasksExpansion =
                                        remember(task.id) {
                                            { _: Boolean ->
                                                onEvent(
                                                    TasksContract.UiEvent.ToggleTaskExpanded(
                                                        task.id,
                                                    ),
                                                )
                                            }
                                        }
                                    val onAddSubtask =
                                        remember {
                                            { parentId: Long ->
                                                onEvent(
                                                    TasksContract.UiEvent.ShowTaskDialog(
                                                        parentTaskId = parentId,
                                                    ),
                                                )
                                            }
                                        }

                                    TaskItem(
                                        task = task,
                                        onToggleCompletion = {
                                            onEvent(
                                                TasksContract.UiEvent.ToggleTaskCompletion(
                                                    it,
                                                ),
                                            )
                                        },
                                        onToggleSubtasksExpansion = onToggleSubtasksExpansion,
                                        onEdit = { onEvent(TasksContract.UiEvent.ShowTaskDialog(task = it)) },
                                        onAddSubtask = onAddSubtask,
                                        onReorderSubtasks = { from, to, subs ->
                                            onEvent(
                                                TasksContract.UiEvent.ReorderSubtasks(from, to, subs),
                                            )
                                        },
                                        subtasks = taskSubtasks,
                                        isSubtasksExpanded = isSubtasksExpanded,
                                        modifier =
                                            Modifier
                                                .animateItem()
                                                .zIndex(if (isDragging) 1f else 0f)
                                                .graphicsLayer {
                                                    if (isDragging) {
                                                        translationY = dragOffset
                                                        alpha = 0.8f
                                                    } else if (isTarget) {
                                                        alpha = 0.5f
                                                    }
                                                }.pointerInput(flatActiveTasks) {
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
                                                                with(density) { 96.dp.toPx() }
                                                            val newTargetIndex =
                                                                (index + (dragOffset / itemHeight).roundToInt())
                                                                    .coerceIn(
                                                                        0,
                                                                        flatActiveTasks.size - 1,
                                                                    )
                                                            if (newTargetIndex != targetIndex) {
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            }
                                                            targetIndex = newTargetIndex
                                                        },
                                                        onDragEnd = {
                                                            if (draggedIndex != targetIndex && targetIndex >= 0) {
                                                                onEvent(
                                                                    TasksContract.UiEvent.ReorderTasks(
                                                                        draggedIndex,
                                                                        targetIndex,
                                                                        flatActiveTasks,
                                                                    ),
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
                            } else {
                                // Grouped modes: render with headers
                                activeEntries.forEachIndexed { groupIndex, (key, tasksForGroup) ->
                                    if (showActiveHeaders) {
                                        item(key = "active_header_${key.stableKey}") {
                                            ActiveGroupHeader(
                                                label = resolveActiveGroupLabel(key),
                                                isFirst = groupIndex == 0,
                                                modifier = Modifier.animateItem(),
                                            )
                                        }
                                    }

                                    itemsIndexed(
                                        items = tasksForGroup,
                                        key = { _, task -> task.id },
                                        contentType = { _, _ -> "task" },
                                    ) { _, task ->
                                        val taskSubtasks = subtasksMap[task.id] ?: emptyList()
                                        val isSubtasksExpanded =
                                            task.id in uiState.expandedTaskIds || taskSubtasks.isEmpty()

                                        val onToggleSubtasksExpansion =
                                            remember(task.id) {
                                                { _: Boolean ->
                                                    onEvent(
                                                        TasksContract.UiEvent.ToggleTaskExpanded(
                                                            task.id,
                                                        ),
                                                    )
                                                }
                                            }
                                        val onAddSubtask =
                                            remember {
                                                { parentId: Long ->
                                                    onEvent(
                                                        TasksContract.UiEvent.ShowTaskDialog(
                                                            parentTaskId = parentId,
                                                        ),
                                                    )
                                                }
                                            }

                                        TaskItem(
                                            task = task,
                                            onToggleCompletion = {
                                                onEvent(
                                                    TasksContract.UiEvent.ToggleTaskCompletion(
                                                        it,
                                                    ),
                                                )
                                            },
                                            onToggleSubtasksExpansion = onToggleSubtasksExpansion,
                                            onEdit = { onEvent(TasksContract.UiEvent.ShowTaskDialog(task = it)) },
                                            onAddSubtask = onAddSubtask,
                                            onReorderSubtasks = { from, to, subs ->
                                                onEvent(
                                                    TasksContract.UiEvent.ReorderSubtasks(from, to, subs),
                                                )
                                            },
                                            subtasks = taskSubtasks,
                                            isSubtasksExpanded = isSubtasksExpanded,
                                            modifier = Modifier.animateItem(),
                                        )
                                    }
                                }
                            }

                            if (hasCompletedTasks) {
                                val sortOption = uiState.sortOption
                                val showDivider =
                                    sortOption != SortOption.MANUAL
                                val completedEntries =
                                    sortedCompletedEntries(
                                        groups = completedTaskGroups,
                                        sortOption = sortOption,
                                    )

                                item(key = "completed_separator") {
                                    val firstGroupLabel =
                                        if (showDivider) {
                                            resolveGroupLabel(completedEntries.firstOrNull()?.key)
                                        } else {
                                            null
                                        }
                                    CompletedTasksSeparator(
                                        isExpanded = uiState.showCompletedTasks,
                                        onToggle = { onEvent(TasksContract.UiEvent.ToggleCompletedTasksVisibility) },
                                        showDivider = showDivider,
                                        firstGroupLabel = firstGroupLabel,
                                        modifier = Modifier.animateItem(),
                                    )
                                }

                                if (uiState.showCompletedTasks) {
                                    completedEntries.forEachIndexed { index, (key, tasksForGroup) ->
                                        // Skip first group header — it's shown in the separator
                                        if (index > 0 && showDivider) {
                                            item(key = "group_header_${key.stableKey}") {
                                                CompletedGroupHeader(
                                                    label = resolveGroupLabel(key),
                                                    modifier = Modifier.animateItem(),
                                                )
                                            }
                                        }

                                        itemsIndexed(
                                            items = tasksForGroup,
                                            key = { _, task -> task.id },
                                            contentType = { _, _ -> "task" },
                                        ) { _, task ->
                                            val taskSubtasks = subtasksMap[task.id] ?: emptyList()
                                            val isSubtasksExpanded =
                                                task.id in uiState.expandedTaskIds || taskSubtasks.isEmpty()

                                            val onToggleSubtasksExpansion =
                                                remember(task.id) {
                                                    { _: Boolean ->
                                                        onEvent(
                                                            TasksContract.UiEvent.ToggleTaskExpanded(
                                                                task.id,
                                                            ),
                                                        )
                                                    }
                                                }
                                            val onAddSubtask =
                                                remember {
                                                    { parentId: Long ->
                                                        onEvent(
                                                            TasksContract.UiEvent.ShowTaskDialog(
                                                                parentTaskId = parentId,
                                                            ),
                                                        )
                                                    }
                                                }

                                            TaskItem(
                                                task = task,
                                                onToggleCompletion = {
                                                    onEvent(
                                                        TasksContract.UiEvent.ToggleTaskCompletion(
                                                            it,
                                                        ),
                                                    )
                                                },
                                                onToggleSubtasksExpansion = onToggleSubtasksExpansion,
                                                onEdit = {
                                                    onEvent(
                                                        TasksContract.UiEvent.ShowTaskDialog(
                                                            task = it,
                                                        ),
                                                    )
                                                },
                                                onAddSubtask = onAddSubtask,
                                                onReorderSubtasks = { from, to, subs ->
                                                    onEvent(
                                                        TasksContract.UiEvent.ReorderSubtasks(from, to, subs),
                                                    )
                                                },
                                                subtasks = taskSubtasks,
                                                isSubtasksExpanded = isSubtasksExpanded,
                                                modifier =
                                                    Modifier.animateItem(),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CompletedTasksSeparator(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    firstGroupLabel: String? = null,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation",
    )

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 28.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 3.dp,
            modifier =
                Modifier
                    .padding(end = if (showDivider) 12.dp else 0.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onToggle() },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.completed_tasks),
                    style = MaterialTheme.typography.sectionHeader,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(modifier = Modifier.size(8.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(20.dp)
                            .rotate(rotation),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                thickness = 2.dp,
            )
            if (isExpanded && firstGroupLabel != null) {
                Text(
                    text = firstGroupLabel,
                    style = MaterialTheme.typography.groupLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                )
            }
        }
    }
}

@Composable
internal fun CompletedGroupHeader(
    label: String?,
    modifier: Modifier = Modifier,
) {
    if (label == null) return

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            thickness = 2.dp,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.groupLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, end = 8.dp),
        )
    }
}

/**
 * Resolves a [CompletedGroupKey] to its user-visible label string.
 */
@Composable
private fun resolveGroupLabel(key: CompletedGroupKey?): String? {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val yesterday = today.minus(1, DateTimeUnit.DAY)

    return when (key) {
        is CompletedGroupKey.ByDate ->
            when (key.date) {
                today -> stringResource(R.string.completed_today)
                yesterday -> stringResource(R.string.completed_yesterday)
                null -> stringResource(R.string.completed_older)
                else -> DateTimeFormatter.formatDate(key.date)
            }

        is CompletedGroupKey.ByPriority ->
            when (key.priority) {
                Priority.HIGH -> stringResource(R.string.priority_high)
                Priority.MEDIUM -> stringResource(R.string.priority_medium)
                Priority.LOW -> stringResource(R.string.priority_low)
                null -> stringResource(R.string.priority_none)
            }

        CompletedGroupKey.ByTitle -> stringResource(R.string.completed_alphabetical)

        CompletedGroupKey.Flat, null -> null
    }
}

private fun sortedActiveEntries(
    groups: Map<ActiveGroupKey, ImmutableList<Task>>,
    sortOption: SortOption,
): List<Map.Entry<ActiveGroupKey, ImmutableList<Task>>> {
    val entries = groups.entries.toList()
    return when (sortOption) {
        SortOption.BY_DATE ->
            entries.sortedWith(
                compareBy<Map.Entry<ActiveGroupKey, ImmutableList<Task>>> { entry ->
                    when (val key = entry.key) {
                        ActiveGroupKey.Overdue -> ACTIVE_GROUP_OVERDUE_RANK
                        is ActiveGroupKey.ByDate ->
                            if (key.date != null) {
                                ACTIVE_GROUP_DATED_RANK
                            } else {
                                ACTIVE_GROUP_NO_DATE_RANK
                            }

                        else -> ACTIVE_GROUP_FALLBACK_RANK
                    }
                }.thenBy { (it.key as? ActiveGroupKey.ByDate)?.date },
            )

        SortOption.BY_PRIORITY ->
            entries.sortedBy { (it.key as? ActiveGroupKey.ByPriority)?.priority?.sortOrder ?: Int.MAX_VALUE }

        else -> entries
    }
}

private fun sortedCompletedEntries(
    groups: Map<CompletedGroupKey, ImmutableList<Task>>,
    sortOption: SortOption,
): List<Map.Entry<CompletedGroupKey, ImmutableList<Task>>> {
    val entries = groups.entries.toList()
    return when (sortOption) {
        SortOption.BY_DATE ->
            entries.sortedWith(
                compareBy<Map.Entry<CompletedGroupKey, ImmutableList<Task>>> { entry ->
                    when (val key = entry.key) {
                        is CompletedGroupKey.ByDate ->
                            if (key.date == null) {
                                COMPLETED_GROUP_NO_DATE_RANK
                            } else {
                                COMPLETED_GROUP_DATED_RANK
                            }

                        else -> COMPLETED_GROUP_FALLBACK_RANK
                    }
                }.thenByDescending { (it.key as? CompletedGroupKey.ByDate)?.date },
            )

        SortOption.BY_PRIORITY ->
            entries.sortedBy { (it.key as? CompletedGroupKey.ByPriority)?.priority?.sortOrder ?: Int.MAX_VALUE }

        else -> entries
    }
}

/** Stable key string for use in LazyColumn item keys. */
private val CompletedGroupKey.stableKey: String
    get() =
        when (this) {
            is CompletedGroupKey.ByDate -> "date_${date ?: "older"}"
            is CompletedGroupKey.ByPriority -> "priority_${priority?.name ?: "none"}"
            CompletedGroupKey.ByTitle -> "title"
            CompletedGroupKey.Flat -> "flat"
        }

@Composable
internal fun ActiveGroupHeader(
    label: String?,
    isFirst: Boolean,
    modifier: Modifier = Modifier,
) {
    if (label == null) return

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = if (isFirst) 0.dp else 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            thickness = 2.dp,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.groupLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, end = 8.dp),
        )
    }
}

/**
 * Resolves an [ActiveGroupKey] to its user-visible label string.
 */
@Composable
private fun resolveActiveGroupLabel(key: ActiveGroupKey?): String? {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val tomorrow = today.plus(1, DateTimeUnit.DAY)

    return when (key) {
        ActiveGroupKey.Overdue -> stringResource(R.string.active_overdue)

        is ActiveGroupKey.ByDate ->
            when (key.date) {
                null -> stringResource(R.string.active_no_date)
                today -> stringResource(R.string.completed_today)
                tomorrow -> stringResource(R.string.tomorrow)
                else -> DateTimeFormatter.formatDate(key.date)
            }

        is ActiveGroupKey.ByPriority ->
            when (key.priority) {
                Priority.HIGH -> stringResource(R.string.priority_high)
                Priority.MEDIUM -> stringResource(R.string.priority_medium)
                Priority.LOW -> stringResource(R.string.priority_low)
                null -> stringResource(R.string.priority_none)
            }

        ActiveGroupKey.ByTitle, ActiveGroupKey.Flat, null -> null
    }
}

/** Stable key string for use in LazyColumn item keys. */
private val ActiveGroupKey.stableKey: String
    get() =
        when (this) {
            ActiveGroupKey.Overdue -> "active_overdue"
            is ActiveGroupKey.ByDate -> "active_date_${date ?: "no_date"}"
            is ActiveGroupKey.ByPriority -> "active_priority_${priority?.name ?: "none"}"
            ActiveGroupKey.ByTitle -> "active_title"
            ActiveGroupKey.Flat -> "active_flat"
        }
