package com.mandrecode.tempo.features.tasks.presentation

import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.util.toUserFacingMessage
import com.mandrecode.tempo.features.tasks.domain.model.TaskSection
import com.mandrecode.tempo.features.tasks.domain.usecase.CreateTaskSectionUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.RenameTaskSectionUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Section actions for the Tasks screen (#61). Every write goes through a use case; the list
// itself refreshes from the sections flow in [loadData].

internal fun TasksViewModel.addSection(
    name: String,
    afterSectionId: Long? = null,
) {
    val categoryId = mutableUiState.value.selectedCategoryId
    launchSectionAction {
        when (val result = sectionUseCases.create(categoryId, name, afterSectionId)) {
            is CreateTaskSectionUseCase.Result.Success -> Unit
            is CreateTaskSectionUseCase.Result.AlreadyExists ->
                showSnackbar(R.string.msg_section_name_exists, listOf(result.name))
            CreateTaskSectionUseCase.Result.EmptyName -> showSnackbar(R.string.msg_section_name_empty)
            CreateTaskSectionUseCase.Result.TooLong -> showSnackbar(R.string.msg_section_name_too_long)
        }
    }
}

internal fun TasksViewModel.renameSection(
    sectionId: Long,
    name: String,
) {
    launchSectionAction {
        when (val result = sectionUseCases.rename(sectionId, name)) {
            RenameTaskSectionUseCase.Result.EmptyName -> showSnackbar(R.string.msg_section_name_empty)
            RenameTaskSectionUseCase.Result.TooLong -> showSnackbar(R.string.msg_section_name_too_long)
            is RenameTaskSectionUseCase.Result.AlreadyExists ->
                showSnackbar(R.string.msg_section_name_exists, listOf(result.name))
            RenameTaskSectionUseCase.Result.Success, RenameTaskSectionUseCase.Result.NotFound -> Unit
        }
    }
}

internal fun TasksViewModel.reorderSections(
    fromIndex: Int,
    toIndex: Int,
    sections: List<TaskSection>,
) {
    launchSectionAction { sectionUseCases.reorder(fromIndex, toIndex, sections) }
}

internal fun TasksViewModel.deleteSection(section: TaskSection) {
    val wasCollapsed = section.id in mutableUiState.value.collapsedSectionIds
    launchSectionAction {
        val snapshot = sectionUseCases.delete(section.id) ?: return@launchSectionAction
        setSectionCollapsed(section.id, collapsed = false)
        val token = storePendingDeletion(PendingTaskDeletion.Section(snapshot, wasCollapsed))
        showBoldSnackbar(
            prefixResId = R.string.msg_section_deleted_prefix,
            word = snapshot.section.name,
            suffixResId = R.string.msg_section_deleted_suffix,
            actionResId = R.string.undo,
            deletionToken = token,
        )
    }
}

internal suspend fun TasksViewModel.restoreDeletedSection(pending: PendingTaskDeletion.Section) {
    sectionUseCases.restore(pending.snapshot)
    if (pending.wasCollapsed) setSectionCollapsed(pending.snapshot.section.id, collapsed = true)
}

internal fun TasksViewModel.toggleSectionCollapsed(sectionId: Long) {
    setSectionCollapsed(sectionId, collapsed = sectionId !in mutableUiState.value.collapsedSectionIds)
}

internal fun TasksViewModel.moveTaskToSection(
    taskId: Long,
    sectionId: Long?,
    index: Int,
) {
    launchSectionAction { sectionUseCases.moveTask(taskId, sectionId, index) }
}

internal fun TasksViewModel.setTaskFormSection(sectionId: Long?) {
    mutableUiState.update { it.copy(taskForm = it.taskForm.copy(sectionId = sectionId)) }
}

/**
 * The section a saved task should land in: the editor's choice when it is a section of
 * [categoryId], otherwise none — so a category change in the editor can never carry a section
 * across. Subtasks are never sectioned on their own.
 */
internal fun TasksViewModel.resolvedFormSectionId(
    categoryId: Long,
    parentTaskId: Long?,
): Long? {
    val state = mutableUiState.value
    val sectionId = state.taskForm.sectionId.takeIf { parentTaskId == null }
    return sectionId?.takeIf { id -> state.sectionsByCategory[categoryId].orEmpty().any { it.id == id } }
}

private fun TasksViewModel.setSectionCollapsed(
    sectionId: Long,
    collapsed: Boolean,
) {
    tasksScreenPreferencesRepository.setSectionCollapsed(sectionId, collapsed)
    mutableUiState.update {
        it.copy(
            collapsedSectionIds =
                if (collapsed) it.collapsedSectionIds.add(sectionId) else it.collapsedSectionIds.remove(sectionId),
        )
    }
}

private fun TasksViewModel.launchSectionAction(block: suspend () -> Unit) {
    viewModelScope.launch {
        runCatching { block() }.onFailure { error ->
            if (error is CancellationException) throw error
            showSnackbar(R.string.msg_error_loading_data, listOf(error.toUserFacingMessage()))
        }
    }
}
