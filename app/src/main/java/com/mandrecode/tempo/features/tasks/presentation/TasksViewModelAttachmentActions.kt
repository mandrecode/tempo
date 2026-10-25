package com.mandrecode.tempo.features.tasks.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.tasks.domain.model.AttachmentImportResult
import com.mandrecode.tempo.features.tasks.domain.model.TaskAttachment
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Keeps the open editor's attachments in step with the database while a saved task is being
 * edited, whichever way the editor was opened (list, notification, Focus). A new task has nothing
 * saved to follow; its attachments are the ones picked into the form.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun TasksViewModel.observeEditedTaskAttachments() {
    viewModelScope.launch {
        // Keyed on the editor session too: reopening the task already being edited (from its
        // notification, say) replaces the form with an empty list, and only a fresh subscription
        // fills it again — the rows themselves have not changed, so Room has nothing new to emit.
        uiState
            .map { it.taskForm.editorSessionId to it.taskForm.editingTask?.id }
            .distinctUntilChanged()
            .flatMapLatest { (sessionId, taskId) ->
                if (taskId == null) {
                    emptyFlow()
                } else {
                    taskAttachmentRepository.observeAttachments(taskId).map { Triple(sessionId, taskId, it) }
                }
            }.collect { (sessionId, taskId, attachments) ->
                mutableUiState.update { state ->
                    if (state.taskForm.editorSessionId != sessionId || state.taskForm.editingTask?.id != taskId) {
                        state
                    } else {
                        state.copy(taskForm = state.taskForm.copy(attachments = attachments.toPersistentList()))
                    }
                }
            }
    }
}

/**
 * Copies each picked file in. On a saved task it is attached at once, like every other edit made
 * there; on a new task it waits in the form until the task is saved.
 */
internal fun TasksViewModel.addAttachments(uris: List<String>) {
    if (uris.isEmpty()) return
    val editorSessionId = uiState.value.taskForm.editorSessionId
    clearAttachmentErrors()
    viewModelScope.launch {
        val imported = mutableListOf<TaskAttachment>()
        var tooLarge = 0
        var unreadable = 0
        uris.forEach { uri ->
            when (val result = importAttachment(uri)) {
                is AttachmentImportResult.Imported -> imported += result.attachment
                AttachmentImportResult.TooLarge -> tooLarge++
                AttachmentImportResult.Unreadable -> unreadable++
            }
        }

        val form = uiState.value.taskForm
        val editingTask = form.editingTask
        var attachFailed = false
        when {
            // The editor closed, or another one opened, while the files were copying.
            !form.isVisible || form.editorSessionId != editorSessionId ->
                imported.forEach { discardQuietly(it) }

            editingTask != null -> attachFailed = !attachPendingAttachments(imported, editingTask.id)

            else ->
                mutableUiState.update {
                    val attachments = (it.taskForm.attachments + imported).toPersistentList()
                    it.copy(taskForm = it.taskForm.copy(attachments = attachments))
                }
        }

        // Every kind of rejection is reported: a pick with an oversized file and an unreadable
        // one names both, rather than the second going unmentioned.
        val problems =
            buildList {
                if (tooLarge > 0) add(R.string.msg_attachment_too_large)
                if (unreadable > 0 || attachFailed) add(R.string.msg_attachment_failed)
            }
        reportAttachmentProblems(editorSessionId, problems)
    }
}

/**
 * Shows [problems] inside the editor that asked, when it is still open; otherwise — it closed while
 * files were copying — as snackbars, which by then are visible.
 */
private suspend fun TasksViewModel.reportAttachmentProblems(
    editorSessionId: Long,
    @StringRes problems: List<Int>,
) {
    if (problems.isEmpty()) return
    val form = uiState.value.taskForm
    if (form.isVisible && form.editorSessionId == editorSessionId) {
        mutableUiState.update {
            it.copy(taskForm = it.taskForm.copy(attachmentErrors = problems.toPersistentList()))
        }
    } else {
        problems.forEach { showSnackbar(it) }
    }
}

private fun TasksViewModel.clearAttachmentErrors() {
    mutableUiState.update {
        if (it.taskForm.attachmentErrors.isEmpty()) {
            it
        } else {
            it.copy(taskForm = it.taskForm.copy(attachmentErrors = persistentListOf()))
        }
    }
}

private suspend fun TasksViewModel.importAttachment(uri: String): AttachmentImportResult =
    try {
        taskAttachmentRepository.importFile(uri)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        AttachmentImportResult.Unreadable
    }

internal fun TasksViewModel.removeAttachment(attachment: TaskAttachment) {
    val editorSessionId = uiState.value.taskForm.editorSessionId
    clearAttachmentErrors()
    viewModelScope.launch {
        if (attachment.isSaved) {
            try {
                taskAttachmentRepository.remove(attachment)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                reportAttachmentProblems(editorSessionId, listOf(R.string.msg_attachment_remove_failed))
            }
        } else {
            mutableUiState.update {
                it.copy(
                    taskForm =
                        it.taskForm.copy(
                            attachments =
                                it.taskForm.attachments
                                    .filterNot { a -> a == attachment }
                                    .toPersistentList(),
                        ),
                )
            }
            discardQuietly(attachment)
        }
    }
}

/** An unsaved copy that cannot be deleted now is an orphan, and the next start prunes it. */
private suspend fun TasksViewModel.discardQuietly(attachment: TaskAttachment) {
    try {
        taskAttachmentRepository.discard(attachment)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        // Nothing for the user to do about it.
    }
}

/**
 * Attaches files to a saved task: picked into a new task's form before it existed, or straight
 * into one being edited. A failed insert is never thrown at the user; it returns `false` for the
 * caller to report where the user is looking.
 */
internal suspend fun TasksViewModel.attachPendingAttachments(
    pending: List<TaskAttachment>,
    taskId: Long,
): Boolean {
    if (pending.isEmpty()) return true
    return try {
        taskAttachmentRepository.attach(pending, taskId)
        true
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        false
    }
}

internal fun TasksViewModel.onAttachmentOpenFailed() {
    val editorSessionId = uiState.value.taskForm.editorSessionId
    clearAttachmentErrors()
    viewModelScope.launch { reportAttachmentProblems(editorSessionId, listOf(R.string.msg_attachment_no_app)) }
}
