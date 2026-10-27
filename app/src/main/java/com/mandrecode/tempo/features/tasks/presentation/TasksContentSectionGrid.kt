package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.components.ConnectedCardGap
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.presentation.components.sections.AddSectionButton
import com.mandrecode.tempo.features.tasks.presentation.components.sections.EmptySectionPlaceholder
import com.mandrecode.tempo.features.tasks.presentation.components.sections.SectionNameField
import com.mandrecode.tempo.features.tasks.presentation.model.DropEntry
import com.mandrecode.tempo.features.tasks.presentation.model.SectionUi
import com.mandrecode.tempo.features.tasks.presentation.model.SectionedTaskList
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption

/** The list pane width from which a sectioned category flows into two columns (#61). */
internal val TwoColumnSectionsMinWidth: Dp = 900.dp

private const val GRID_COLUMNS = 2

/**
 * A wide pane's sectioned list (#61): the unsectioned block across both columns, then each section
 * whole in one column, in order — a staggered grid puts each in the shorter column — and the
 * Completed block across both columns below. Cards are not lazy inside a section, which keeps a
 * section in one piece and lets drag-and-drop see every card's bounds.
 */
@Suppress("LongParameterList")
@Composable
internal fun SectionedTaskGrid(
    list: SectionedTaskList,
    collapsedSectionIds: Set<Long>,
    editState: SectionEditState,
    dragState: SectionDragState?,
    gridState: LazyStaggeredGridState,
    contentPadding: PaddingValues,
    onEvent: (TasksContract.UiEvent) -> Unit,
    taskCard: @Composable (Task, gestures: Modifier?) -> Unit,
    completedBlock: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(GRID_COLUMNS),
        state = gridState,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalItemSpacing = 8.dp,
        modifier = modifier.fillMaxSize(),
    ) {
        if (list.unsectioned.isNotEmpty() || dragState?.draggedTaskId != null) {
            item(key = GRID_UNSECTIONED_KEY, span = StaggeredGridItemSpan.FullLine) {
                SectionCards(list.unsectioned, sectionId = null, dragState, onEvent, taskCard)
            }
        }
        list.sections.forEach { sectionUi ->
            item(key = gridSectionKey(sectionUi.section.id)) {
                val isCollapsed = sectionUi.section.id in collapsedSectionIds
                GridSection(sectionUi, isCollapsed, editState, dragState, onEvent, taskCard)
            }
        }
        item(key = "grid_section_add", span = StaggeredGridItemSpan.FullLine) {
            if (editState.newSectionDraft == null) {
                AddSectionButton(onClick = { editState.newSectionDraft = NewSectionDraft.AtEnd })
            } else {
                val after = (editState.newSectionDraft as? NewSectionDraft.After)?.sectionId
                SectionNameField(
                    initialName = "",
                    onConfirm = { name ->
                        onEvent(TasksContract.UiEvent.AddSection(name, after))
                        editState.newSectionDraft = null
                    },
                    onDismiss = { editState.newSectionDraft = null },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        item(key = "grid_completed", span = StaggeredGridItemSpan.FullLine) { completedBlock() }
    }
}

@Suppress("LongParameterList")
@Composable
private fun GridSection(
    sectionUi: SectionUi,
    isCollapsed: Boolean,
    editState: SectionEditState,
    dragState: SectionDragState?,
    onEvent: (TasksContract.UiEvent) -> Unit,
    taskCard: @Composable (Task, gestures: Modifier?) -> Unit,
) {
    val section = sectionUi.section
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        SectionHeaderItem(sectionUi, isCollapsed, editState, onEvent, dragState)
        when {
            isCollapsed -> Unit
            sectionUi.tasks.isEmpty() ->
                EmptySectionPlaceholder(
                    isDropTarget = dragState?.isTargeting(DropEntry.Kind.Placeholder, section.id) == true,
                    modifier =
                        dragState?.let {
                            Modifier.dropEntry(it, "placeholder_${section.id}", DropEntry.Kind.Placeholder, section.id)
                        } ?: Modifier,
                )
            else -> SectionCards(sectionUi.tasks, section.id, dragState, onEvent, taskCard)
        }
    }
}

@Composable
private fun SectionCards(
    tasks: List<Task>,
    sectionId: Long?,
    dragState: SectionDragState?,
    onEvent: (TasksContract.UiEvent) -> Unit,
    taskCard: @Composable (Task, gestures: Modifier?) -> Unit,
) {
    // The section's cards as one connected run.
    Column(verticalArrangement = Arrangement.spacedBy(ConnectedCardGap), modifier = Modifier.fillMaxWidth()) {
        if (tasks.isEmpty() && dragState != null) {
            EmptySectionPlaceholder(
                isDropTarget = dragState.isTargeting(DropEntry.Kind.Placeholder, sectionId),
                modifier = Modifier.dropEntry(dragState, "placeholder_none", DropEntry.Kind.Placeholder, sectionId),
            )
        }
        tasks.forEachIndexed { index, task ->
            val gestures =
                dragState?.let { state ->
                    Modifier.sectionDragGestures(state, task.id, sectionId, index) { action ->
                        onEvent(sectionDropEvent(action, task, tasks))
                    }
                }
            taskCard(task, gestures)
        }
    }
}

/**
 * The Completed block as one piece, for the two-column grid: the same separator, groups and clear
 * action the one-column list emits item by item.
 */
@Composable
internal fun CompletedBlock(
    uiState: TasksContract.UiState,
    onEvent: (TasksContract.UiEvent) -> Unit,
    taskCard: @Composable (Task) -> Unit,
) {
    if (uiState.completedTaskGroups.isEmpty()) return
    val showDivider = uiState.sortOption != SortOption.MANUAL
    val entries = sortedCompletedEntries(groups = uiState.completedTaskGroups, sortOption = uiState.sortOption)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        CompletedTasksSeparator(
            isExpanded = uiState.showCompletedTasks,
            onToggle = { onEvent(TasksContract.UiEvent.ToggleCompletedTasksVisibility) },
            showDivider = showDivider,
            firstGroupLabel = if (showDivider) resolveGroupLabel(entries.firstOrNull()?.key) else null,
        )
        if (uiState.showCompletedTasks) {
            entries.forEachIndexed { index, (key, tasks) ->
                if (index > 0 && showDivider) CompletedGroupHeader(label = resolveGroupLabel(key))
                // Each completed group as one connected run.
                Column(verticalArrangement = Arrangement.spacedBy(ConnectedCardGap)) { tasks.forEach { taskCard(it) } }
            }
            ClearCompletedFooter(onClick = { onEvent(TasksContract.UiEvent.RequestDeleteCompletedTasks) })
        }
    }
}
