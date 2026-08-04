package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.mandrecode.tempo.core.ui.components.TempoLoadingIndicator
import com.mandrecode.tempo.core.ui.components.WavyDivider
import com.mandrecode.tempo.core.ui.navigation.floatingNavigationBottomClearancePadding
import com.mandrecode.tempo.core.ui.theme.groupLabel
import com.mandrecode.tempo.core.ui.theme.sectionHeader
import com.mandrecode.tempo.core.ui.util.rememberViewportHold
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.presentation.components.cards.TaskItem
import com.mandrecode.tempo.features.tasks.presentation.components.sections.CategoryChipRow
import com.mandrecode.tempo.features.tasks.presentation.components.sections.EmptyStateContent
import com.mandrecode.tempo.features.tasks.presentation.model.ActiveGroupKey
import com.mandrecode.tempo.features.tasks.presentation.model.CompletedGroupKey
import com.mandrecode.tempo.features.tasks.presentation.model.ReorderableRun
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption
import com.mandrecode.tempo.util.DateTimeFormatter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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
private const val DRAGGED_ITEM_ALPHA = 0.8f
private const val TARGET_ITEM_ALPHA = 0.5f
private val ContentBlockTopCornerRadius = 28.dp

// Rough per-card height used to translate a drag distance into a number of slots. The list is
// lazy, so measured heights aren't available for cards that haven't been composed yet.
private val DragItemHeightEstimate = 96.dp

@Composable
fun TasksContent(
    uiState: TasksContract.UiState,
    onEvent: (TasksContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onScrolledFromTopChange: (Boolean) -> Unit = {},
    selectedTaskId: Long? = null,
) {
    val listState = rememberLazyListState()
    val currentOnScrolledFromTopChange by rememberUpdatedState(onScrolledFromTopChange)
    val listBottomPadding = floatingNavigationBottomClearancePadding(defaultPadding = 16.dp)

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

    // Checking a task off sends it to the completed section, which is below — and the list would
    // otherwise go after it, because the row checked is usually the first one visible. Held here
    // rather than inside the row so that both sections get it from one place, and un-checking, which
    // makes the same trip in reverse, is covered by the same line.
    // Counts, not the sections themselves: the key has to change when a row crosses, and only then.
    val activeTaskCount = activeTaskGroups.values.sumOf { it.size }
    val completedTaskCount = completedTaskGroups.values.sumOf { it.size }
    val holdViewport = rememberViewportHold(listState, activeTaskCount, completedTaskCount)
    val onRowEvent: (TasksContract.UiEvent) -> Unit = { event ->
        if (event is TasksContract.UiEvent.ToggleTaskCompletion) holdViewport()
        onEvent(event)
    }

    Box(
        // Matches the Scaffold containerColor this is normally hosted in (TasksScreen) — kept
        // here too so the rounded content block's corner cutouts (see below) still resolve to
        // the correct tinted color when this composable is previewed/tested standalone.
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        if (uiState.isLoading) {
            TempoLoadingIndicator(message = stringResource(R.string.loading_tasks))
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
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            // clip before background: background() alone doesn't clip children,
                            // so list overscroll/ripple effects would otherwise draw past the
                            // rounded top corners instead of respecting the seam.
                            .clip(
                                RoundedCornerShape(
                                    topStart = ContentBlockTopCornerRadius,
                                    topEnd = ContentBlockTopCornerRadius,
                                ),
                            ).background(MaterialTheme.colorScheme.surface),
                ) {
                    if (!hasActiveTasks && completedTaskGroups.isEmpty()) {
                        EmptyStateContent()
                    } else {
                        val dragState = remember { TaskDragState() }

                        LazyColumn(
                            state = listState,
                            contentPadding =
                                PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 8.dp,
                                    bottom = listBottomPadding,
                                ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            val activeEntries =
                                sortedActiveEntries(
                                    groups = activeTaskGroups,
                                    sortOption = uiState.sortOption,
                                )
                            val showActiveHeaders =
                                uiState.sortOption == SortOption.BY_DATE ||
                                    uiState.sortOption == SortOption.BY_PRIORITY

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

                                items(
                                    items = tasksForGroup,
                                    key = { task -> task.id },
                                    contentType = { "task" },
                                ) { task ->
                                    val taskSubtasks = subtasksMap[task.id] ?: persistentListOf()

                                    TaskListItem(
                                        task = task,
                                        subtasks = taskSubtasks,
                                        isSubtasksExpanded =
                                            task.id in uiState.expandedTaskIds || taskSubtasks.isEmpty(),
                                        isSelected = task.id == selectedTaskId,
                                        onEvent = onRowEvent,
                                        modifier =
                                            Modifier
                                                .animateItem()
                                                .taskReorderGestures(
                                                    reorderableRun = uiState.reorderableRuns[task.id],
                                                    taskId = task.id,
                                                    dragState = dragState,
                                                    onReorder = { from, to, runTasks ->
                                                        onEvent(
                                                            TasksContract.UiEvent.ReorderTasks(
                                                                from,
                                                                to,
                                                                runTasks,
                                                            ),
                                                        )
                                                    },
                                                ),
                                    )
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
                                        isFirstVisibleItem = !hasActiveTasks,
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

                                        items(
                                            items = tasksForGroup,
                                            key = { task -> task.id },
                                            contentType = { "task" },
                                        ) { task ->
                                            val taskSubtasks = subtasksMap[task.id] ?: persistentListOf()

                                            TaskListItem(
                                                task = task,
                                                subtasks = taskSubtasks,
                                                isSubtasksExpanded =
                                                    task.id in uiState.expandedTaskIds || taskSubtasks.isEmpty(),
                                                isSelected = task.id == selectedTaskId,
                                                onEvent = onRowEvent,
                                                modifier = Modifier.animateItem(),
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
private fun TaskListItem(
    task: Task,
    subtasks: ImmutableList<Task>,
    isSubtasksExpanded: Boolean,
    isSelected: Boolean,
    onEvent: (TasksContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onToggleSubtasksExpansion =
        remember(task.id) {
            { _: Boolean -> onEvent(TasksContract.UiEvent.ToggleTaskExpanded(task.id)) }
        }
    val onAddSubtask =
        remember {
            { parentId: Long ->
                onEvent(TasksContract.UiEvent.ShowTaskDialog(parentTaskId = parentId))
            }
        }

    TaskItem(
        task = task,
        isSelected = isSelected,
        onToggleCompletion = { onEvent(TasksContract.UiEvent.ToggleTaskCompletion(it)) },
        onToggleSubtasksExpansion = onToggleSubtasksExpansion,
        onEdit = { onEvent(TasksContract.UiEvent.ShowTaskDialog(task = it)) },
        onAddSubtask = onAddSubtask,
        onReorderSubtasks = { from, to, subs ->
            onEvent(TasksContract.UiEvent.ReorderSubtasks(from, to, subs))
        },
        subtasks = subtasks,
        isSubtasksExpanded = isSubtasksExpanded,
        modifier = modifier,
    )
}

/**
 * Drag state for the task list.
 *
 * The drop target is tracked as (run key, index within run) rather than a list index: once tasks
 * are grouped under headers the same index exists in every group, so a bare index would light up
 * the wrong card.
 */
@Stable
private class TaskDragState {
    var draggedTaskId by mutableStateOf<Long?>(null)
    var draggedRunKey by mutableStateOf<String?>(null)
    var targetIndex by mutableIntStateOf(-1)
    var offsetY by mutableFloatStateOf(0f)

    fun onDragStart(
        run: ReorderableRun,
        taskId: Long,
    ) {
        draggedTaskId = taskId
        draggedRunKey = run.key
        targetIndex = run.indexInRun
        offsetY = 0f
    }

    /** Returns true when the drop target moved, so the caller can fire the tick haptic. */
    fun onDrag(
        run: ReorderableRun,
        dragAmountY: Float,
        itemHeightPx: Float,
    ): Boolean {
        offsetY += dragAmountY
        val newTargetIndex =
            (run.indexInRun + (offsetY / itemHeightPx).roundToInt())
                .coerceIn(0, run.tasks.lastIndex)
        val moved = newTargetIndex != targetIndex
        targetIndex = newTargetIndex
        return moved
    }

    fun reset() {
        draggedTaskId = null
        draggedRunKey = null
        targetIndex = -1
        offsetY = 0f
    }
}

/**
 * Long-press drag-and-drop for a task, confined to [reorderableRun] — the run of tasks the
 * current sort cannot tell apart. A `null` run means the sort distinguishes this task from its
 * neighbours, so its position is not the user's to set and the modifier is a no-op.
 */
@Composable
private fun Modifier.taskReorderGestures(
    reorderableRun: ReorderableRun?,
    taskId: Long,
    dragState: TaskDragState,
    onReorder: (fromIndex: Int, toIndex: Int, tasks: List<Task>) -> Unit,
): Modifier {
    if (reorderableRun == null) return this

    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val isDragging = dragState.draggedTaskId == taskId
    val isTarget =
        !isDragging &&
            dragState.draggedRunKey == reorderableRun.key &&
            dragState.targetIndex == reorderableRun.indexInRun

    return this
        .zIndex(if (isDragging) 1f else 0f)
        .graphicsLayer {
            if (isDragging) {
                translationY = dragState.offsetY
                alpha = DRAGGED_ITEM_ALPHA
            } else if (isTarget) {
                alpha = TARGET_ITEM_ALPHA
            }
        }.pointerInput(reorderableRun) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    dragState.onDragStart(reorderableRun, taskId)
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    val itemHeight = with(density) { DragItemHeightEstimate.toPx() }
                    if (dragState.onDrag(reorderableRun, dragAmount.y, itemHeight)) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                },
                onDragEnd = {
                    val target = dragState.targetIndex
                    if (target >= 0 && target != reorderableRun.indexInRun) {
                        onReorder(reorderableRun.indexInRun, target, reorderableRun.tasks)
                    }
                    dragState.reset()
                },
                onDragCancel = { dragState.reset() },
            )
        }
}

@Composable
internal fun CompletedTasksSeparator(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    firstGroupLabel: String? = null,
    // Whether this separator is the first *visible* content in the list, which is a question about
    // whether there are any active tasks above it rather than about its index.
    isFirstVisibleItem: Boolean = false,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation",
    )

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = if (isFirstVisibleItem) 0.dp else 28.dp, bottom = 12.dp),
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
            WavyDivider(modifier = Modifier.weight(1f))
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
        WavyDivider(modifier = Modifier.weight(1f))
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
        WavyDivider(modifier = Modifier.weight(1f))
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
