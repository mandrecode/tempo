package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
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
    taskRow: @Composable LazyItemScope.(Task) -> Unit,
) {
    items(items = list.unsectioned, key = { task -> task.id }, contentType = { "task" }) { task -> taskRow(task) }

    list.sections.forEach { sectionUi ->
        val section = sectionUi.section
        val isCollapsed = section.id in collapsedSectionIds
        val header: @Composable LazyItemScope.() -> Unit = {
            SectionHeaderItem(sectionUi, isCollapsed, editState, onEvent, Modifier.animateItem())
        }
        if (useStickyHeaders) {
            stickyHeader(key = sectionHeaderKey(section.id), contentType = "section_header") { header() }
        } else {
            item(key = sectionHeaderKey(section.id), contentType = "section_header") { header() }
        }

        if (!isCollapsed) {
            if (sectionUi.tasks.isEmpty()) {
                item(key = "section_empty_${section.id}", contentType = "section_empty") {
                    EmptySectionPlaceholder(modifier = Modifier.animateItem())
                }
            } else {
                items(items = sectionUi.tasks, key = { task -> task.id }, contentType = { "task" }) { task ->
                    taskRow(task)
                }
            }
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
private fun SectionHeaderItem(
    sectionUi: SectionUi,
    isCollapsed: Boolean,
    editState: SectionEditState,
    onEvent: (TasksContract.UiEvent) -> Unit,
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
            modifier = modifier.padding(top = 8.dp),
        )
    }
}
