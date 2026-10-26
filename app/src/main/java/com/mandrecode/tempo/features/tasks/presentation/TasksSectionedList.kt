package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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

/** Where a section being named will go: last in the category, or right after a section. */
internal sealed interface NewSectionDraft {
    data object AtEnd : NewSectionDraft

    data class After(
        val sectionId: Long,
    ) : NewSectionDraft
}

/** The Tasks list's transient section editing state (#61): what is being named or reordered. */
@Stable
internal class SectionEditState {
    var renamingSectionId by mutableStateOf<Long?>(null)
    var newSectionDraft by mutableStateOf<NewSectionDraft?>(null)
    var isReorderSheetVisible by mutableStateOf(false)
}

internal const val SECTION_HEADER_KEY_PREFIX = "section_header_"
private const val SECTION_EMPTY_KEY_PREFIX = "section_empty_"

/** The lazy-list key of a section's header, also what the shortcut group scrolls to. */
internal fun sectionHeaderKey(sectionId: Long): String = "$SECTION_HEADER_KEY_PREFIX$sectionId"

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

    list.sections.forEach { sectionUi ->
        val section = sectionUi.section
        val isCollapsed = section.id in collapsedSectionIds
        val header: @Composable LazyItemScope.() -> Unit = {
            SectionHeaderItem(sectionUi, isCollapsed, editState, onEvent, dragState, Modifier.animateItem())
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
                modifier = Modifier.animateItem(),
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

/** The section a one-column list item key belongs to, for the shortcut group's scroll spy. */
internal fun listKeySection(
    key: Any?,
    list: SectionedTaskList,
): SectionKey =
    when {
        key is Long && list.unsectioned.any { it.id == key } -> SectionKey.Of(null)
        key is Long ->
            list.sections.firstOrNull { ui -> ui.tasks.any { it.id == key } }?.let { SectionKey.Of(it.section.id) }
                ?: SectionKey.Unknown
        key is String && key.startsWith(SECTION_HEADER_KEY_PREFIX) ->
            SectionKey.Of(key.removePrefix(SECTION_HEADER_KEY_PREFIX).toLongOrNull())
        key is String && key.startsWith(SECTION_EMPTY_KEY_PREFIX) ->
            SectionKey.Of(key.removePrefix(SECTION_EMPTY_KEY_PREFIX).toLongOrNull())
        else -> SectionKey.Unknown
    }

/**
 * Where [sectionId]'s header sits in the one-column list, mirroring [sectionedTaskItems]'s order:
 * unsectioned tasks, then per section its header, its tasks or placeholder unless collapsed, and a
 * new-section field placed after it.
 */
internal fun sectionHeaderListIndex(
    list: SectionedTaskList,
    collapsedSectionIds: Set<Long>,
    newSectionDraft: NewSectionDraft?,
    sectionId: Long,
): Int {
    var index = list.unsectioned.size
    for (sectionUi in list.sections) {
        val id = sectionUi.section.id
        if (id == sectionId) return index
        index += 1 + if (id in collapsedSectionIds) 0 else sectionUi.tasks.size.coerceAtLeast(1)
        if ((newSectionDraft as? NewSectionDraft.After)?.sectionId == id) index++
    }
    return 0
}
