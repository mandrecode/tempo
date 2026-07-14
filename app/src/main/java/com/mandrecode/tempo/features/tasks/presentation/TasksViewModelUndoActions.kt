package com.mandrecode.tempo.features.tasks.presentation

import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

internal fun TasksViewModel.storePendingDeletion(snapshot: PendingTaskDeletion): Long {
    val token = ++nextDeletionToken
    pendingDeletionSnapshots[token] = snapshot
    return token
}

internal fun TasksViewModel.dismissDeletionUndo(token: Long) {
    pendingDeletionSnapshots.remove(token)
}

internal fun TasksViewModel.undoDeletion(token: Long) {
    val pending = pendingDeletionSnapshots[token] ?: return
    viewModelScope.launch {
        try {
            val result =
                when (pending) {
                    is PendingTaskDeletion.Tasks -> restoreDeletedTasksUseCase(pending.snapshot)
                    is PendingTaskDeletion.Category -> {
                        restoreDeletedCategoryUseCase(pending.snapshot).also {
                            val categoryId = pending.selectedCategoryIdBeforeDeletion
                            val categoryIsValid =
                                categoryId == pending.snapshot.category.id ||
                                    mutableUiState.value.categories.any { it.id == categoryId }
                            if (categoryIsValid) {
                                selectCategory(categoryId)
                            }
                        }
                    }
                }
            pendingDeletionSnapshots.remove(token)
            showSnackbar(
                if (result.hasSchedulingFailure) {
                    R.string.msg_undo_restored_reminder_warning
                } else {
                    R.string.msg_undo_success
                },
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            showSnackbar(
                messageResId = R.string.msg_undo_failed,
                deletionToken = token,
            )
        }
    }
}
