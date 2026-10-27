package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.presentation.components.sections.AddSectionButton
import com.mandrecode.tempo.features.tasks.presentation.components.sections.EmptySectionPlaceholder
import com.mandrecode.tempo.features.tasks.presentation.components.sections.SectionHeader
import com.mandrecode.tempo.features.tasks.presentation.components.sections.SectionNameField
import com.mandrecode.tempo.features.tasks.presentation.model.DropEntry
import com.mandrecode.tempo.features.tasks.presentation.model.SectionUi
import com.mandrecode.tempo.features.tasks.presentation.model.SectionedTaskList

/**
 * Emits a category's sectioned active list (#61): unsectioned tasks first, without a header, then
 * every section — header, tasks unless collapsed, or a placeholder when it has none — and finally
 * the field that names a new section, or the button that starts one.
 */
@Suppress("LongParameterList")
internal fun LazyListScope.sectionedTaskItems(
    list: SectionedTaskList,
    collapsedSectionIds: Set<Long>,
    useStickyHeaders: Boolean,
    editState: SectionEditState,
    onEvent: (TasksContract.UiEvent) -> Unit,
    taskRow: @Composable LazyItemScope.(Task, gestures: Modifier?) -> Unit,
    dragState: SectionDragState? = null,
) {
    if (dragState?.draggedTaskId != null && list.unsectioned.isEmpty()) {
        // While dragging, the unsectioned area needs somewhere to drop when it holds no task.
        item(key = "section_empty_none", contentType = "section_empty") {
            EmptySectionPlaceholder(
                isDropTarget = dragState.isTargeting(DropEntry.Kind.Placeholder, null),
                modifier = Modifier.dropEntry(dragState, "placeholder_none", DropEntry.Kind.Placeholder, null),
            )
        }
    }
    sectionTasks(list.unsectioned, sectionId = null, dragState, onEvent, taskRow)

    list.sections.forEachIndexed { index, sectionUi ->
        val section = sectionUi.section
        val isCollapsed = section.id in collapsedSectionIds
        // Room above a header that follows cards; the first item keeps the list's top inset.
        val separatedAbove = index > 0 || list.unsectioned.isNotEmpty()
        val header: @Composable LazyItemScope.() -> Unit = {
            val spaced = Modifier.animateItem().groupSeparation(above = separatedAbove)
            SectionHeaderItem(sectionUi, isCollapsed, editState, onEvent, dragState, spaced)
        }
        if (useStickyHeaders) {
            stickyHeader(key = sectionHeaderKey(section.id), contentType = "section_header") { header() }
        } else {
            item(key = sectionHeaderKey(section.id), contentType = "section_header") { header() }
        }

        if (!isCollapsed && sectionUi.tasks.isEmpty()) {
            item(key = "$SECTION_EMPTY_KEY_PREFIX${section.id}", contentType = "section_empty") {
                EmptySectionPlaceholder(
                    isDropTarget = dragState?.isTargeting(DropEntry.Kind.Placeholder, section.id) == true,
                    modifier =
                        Modifier.animateItem().then(
                            dragState?.let {
                                Modifier.dropEntry(
                                    state = it,
                                    key = "placeholder_${section.id}",
                                    kind = DropEntry.Kind.Placeholder,
                                    sectionId = section.id,
                                )
                            } ?: Modifier,
                        ),
                )
            }
        } else if (!isCollapsed) {
            sectionTasks(sectionUi.tasks, section.id, dragState, onEvent, taskRow)
        }

        if ((editState.newSectionDraft as? NewSectionDraft.After)?.sectionId == section.id) {
            newSectionField(afterSectionId = section.id, editState = editState, onEvent = onEvent)
        }
    }

    if (editState.newSectionDraft == NewSectionDraft.AtEnd) {
        newSectionField(afterSectionId = null, editState = editState, onEvent = onEvent)
    } else {
        item(key = "section_add", contentType = "section_add") {
            AddSectionButton(
                onClick = { editState.newSectionDraft = NewSectionDraft.AtEnd },
                modifier = Modifier.animateItem().padding(top = GroupSeparation),
            )
        }
    }
}

/**
 * One section's cards. With [dragState] (Manual sort) each card drags across sections; without it
 * the row keeps the list's usual within-run reordering.
 */
private fun LazyListScope.sectionTasks(
    tasks: List<Task>,
    sectionId: Long?,
    dragState: SectionDragState?,
    onEvent: (TasksContract.UiEvent) -> Unit,
    taskRow: @Composable LazyItemScope.(Task, gestures: Modifier?) -> Unit,
) {
    itemsIndexed(items = tasks, key = { _, task -> task.id }, contentType = { _, _ -> "task" }) { index, task ->
        val gestures =
            dragState?.let { state ->
                Modifier.sectionDragGestures(state, task.id, sectionId, index) { action ->
                    onEvent(sectionDropEvent(action, task, tasks))
                }
            }
        taskRow(task, gestures)
    }
}

/** The field that names a new section; also used in a category that has no section yet. */
internal fun LazyListScope.newSectionField(
    afterSectionId: Long?,
    editState: SectionEditState,
    onEvent: (TasksContract.UiEvent) -> Unit,
) {
    item(key = "section_new_${afterSectionId ?: "end"}", contentType = "section_new") {
        SectionNameField(
            initialName = "",
            onConfirm = { name ->
                onEvent(TasksContract.UiEvent.AddSection(name, afterSectionId))
                editState.newSectionDraft = null
            },
            onDismiss = { editState.newSectionDraft = null },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
    }
}

/** Short windows (phones in landscape) pin the current section's header while scrolling (#61). */
@Composable
internal fun isShortWindow(): Boolean =
    currentWindowAdaptiveInfoV2().windowSizeClass.minHeightDp < WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND

/** A section's header, or the field that renames it in place. */
@Composable
internal fun SectionHeaderItem(
    sectionUi: SectionUi,
    isCollapsed: Boolean,
    editState: SectionEditState,
    onEvent: (TasksContract.UiEvent) -> Unit,
    dragState: SectionDragState?,
    modifier: Modifier = Modifier,
) {
    val section = sectionUi.section
    if (editState.renamingSectionId == section.id) {
        SectionNameField(
            initialName = section.name,
            onConfirm = { name ->
                onEvent(TasksContract.UiEvent.RenameSection(section.id, name))
                editState.renamingSectionId = null
            },
            onDismiss = { editState.renamingSectionId = null },
            modifier = modifier.padding(top = 8.dp),
        )
    } else {
        SectionHeader(
            name = section.name,
            taskCount = sectionUi.tasks.size,
            overdueCount = sectionUi.overdueCount,
            isCollapsed = isCollapsed,
            onToggleCollapse = { onEvent(TasksContract.UiEvent.ToggleSectionCollapsed(section.id)) },
            onAddTask = { onEvent(TasksContract.UiEvent.ShowTaskDialog(sectionId = section.id)) },
            onRename = { editState.renamingSectionId = section.id },
            onReorderSections = { editState.isReorderSheetVisible = true },
            onAddSectionBelow = { editState.newSectionDraft = NewSectionDraft.After(section.id) },
            onDelete = { onEvent(TasksContract.UiEvent.DeleteSection(section)) },
            isDropTarget = dragState?.isTargeting(DropEntry.Kind.Header, section.id) == true,
            modifier =
                modifier.padding(top = 8.dp).then(
                    dragState?.let {
                        Modifier.dropEntry(
                            state = it,
                            key = "header_${section.id}",
                            kind = DropEntry.Kind.Header,
                            sectionId = section.id,
                            taskCount = sectionUi.tasks.size,
                        )
                    } ?: Modifier,
                ),
        )
    }
}
