package com.mandrecode.tempo.features.routines.presentation

import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

internal fun RoutinesViewModel.storePendingDeletion(snapshot: PendingRoutineDeletion): Long {
    val token = ++nextDeletionToken
    pendingDeletionSnapshots[token] = snapshot
    return token
}

internal fun RoutinesViewModel.dismissDeletionUndo(token: Long) {
    pendingDeletionSnapshots.remove(token)
}

internal fun RoutinesViewModel.undoDeletion(token: Long) {
    val pending = pendingDeletionSnapshots[token] ?: return
    viewModelScope.launch {
        try {
            val result =
                when (pending) {
                    is PendingRoutineDeletion.Habit -> restoreDeletedHabitUseCase(pending.snapshot)
                    is PendingRoutineDeletion.HabitChain ->
                        restoreDeletedHabitChainUseCase(pending.snapshot)
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
