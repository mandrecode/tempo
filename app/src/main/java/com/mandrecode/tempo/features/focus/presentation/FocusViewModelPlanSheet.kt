package com.mandrecode.tempo.features.focus.presentation

import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.features.tasks.domain.util.PlanReminderTimeUtil
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Planning undated work, from opening the sheet to taking the whole batch back.
 *
 * The plan sheet's half of [FocusViewModel], split off the way Tasks splits its own view model:
 * the day's behaviour and planning what has no day are two subjects, together only because one
 * screen hosts both.
 */
internal fun FocusViewModel.onPlanSheetEvent(event: FocusContract.UiEvent) {
    when (event) {
        FocusContract.UiEvent.UndatedTasksClicked -> openPlanSheet()

        is FocusContract.UiEvent.PlanTask ->
            viewModelScope.launch { planTask(event.taskId, event.date) }

        is FocusContract.UiEvent.UnplanTask ->
            viewModelScope.launch { setReminder(event.taskId, reminderDate = null) }

        FocusContract.UiEvent.ClosePlanSheet -> {
            val sheet = mutableUiState.value.planSheet ?: return
            val changed = sheet.changedTaskIds
            // Only what moved, and only as it was: a task the sheet never touched has no
            // business being reset by an undo the sheet offered.
            undoableBatch =
                changed.associateWith { id -> sheet.originalReminders[id] }.takeIf { it.isNotEmpty() }
            closePlanSheet()
            // Nothing happened, nothing to say. A sheet opened and closed without a change should
            // not leave a notice behind claiming otherwise.
            if (changed.isNotEmpty()) {
                sendEffect(FocusContract.UiEffect.PlanBatchConfirmed(changed.size))
            }
        }

        FocusContract.UiEvent.UndoPlanBatch ->
            viewModelScope.launch { undoPlanBatch() }

        else -> onEditorEvent(event)
    }
}

private fun FocusViewModel.openPlanSheet() {
    planSheetJob?.cancel()
    mutableUiState.update { it.copy(planSheet = FocusContract.PlanSheetState()) }
    planSheetJob =
        viewModelScope.launch {
            // The ids are taken once, from what is undated right now; the rows that follow
            // are held to them. Re-reading the undated set on every emission would empty
            // the sheet as fast as the user filled it.
            val opened = getUndatedTasks().first()
            val originalReminders =
                opened.associate { it.task.id to it.task.reminderDate }.toPersistentMap()
            mutableUiState.update { state ->
                state.planSheet?.let { sheet ->
                    state.copy(
                        planSheet =
                            sheet.copy(
                                rows = opened.toPersistentList(),
                                originalReminders = originalReminders,
                                isLoading = false,
                            ),
                    )
                } ?: state
            }
            getUndatedTasks.pinnedTo(originalReminders.keys).collect { rows ->
                mutableUiState.update { state ->
                    state.planSheet?.let { sheet ->
                        state.copy(planSheet = sheet.copy(rows = rows.toPersistentList(), isLoading = false))
                    } ?: state
                }
            }
        }
}

private fun FocusViewModel.closePlanSheet() {
    planSheetJob?.cancel()
    planSheetJob = null
    mutableUiState.update { it.copy(planSheet = null) }
}

private suspend fun FocusViewModel.planTask(
    taskId: Long,
    date: LocalDate,
) {
    val now = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
    setReminder(taskId, PlanReminderTimeUtil.resolve(date, now))
}

/**
 * Writes a row's reminder, or takes it away when [reminderDate] is null.
 *
 * Planning and unplanning are the same write with a different value, and both go through the use
 * case the editor writes with — which is what cancels the alarm on the way out, as well as arming
 * it on the way in.
 */
private suspend fun FocusViewModel.setReminder(
    taskId: Long,
    reminderDate: LocalDateTime?,
) {
    val task =
        mutableUiState.value.planSheet
            ?.rows
            ?.firstOrNull { it.task.id == taskId }
            ?.task ?: return
    updateTask(task.copy(reminderDate = reminderDate))
}

/**
 * Puts every task the sheet moved back to the reminder it had when the sheet opened.
 *
 * The batch is consumed on the way: an undo already taken has nothing left to undo, and a
 * second one would silently reset dates the user has since chosen on purpose.
 */
private suspend fun FocusViewModel.undoPlanBatch() {
    val batch = undoableBatch ?: return
    undoableBatch = null
    restoreTaskReminders(batch)
}
