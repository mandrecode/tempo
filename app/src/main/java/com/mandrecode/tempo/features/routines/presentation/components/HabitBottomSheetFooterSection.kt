package com.mandrecode.tempo.features.routines.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.editor.EditorBottomSheetFooter
import com.mandrecode.tempo.features.routines.presentation.RoutinesContract.HabitSheetTab

@Composable
internal fun HabitBottomSheetFooter(
    selectedTab: HabitSheetTab,
    editingHabit: Boolean,
    editingHabitChain: Boolean,
    selectedHabitIds: List<Long>,
    title: String,
    hasUnsavedChanges: Boolean,
    autoSaveEnabled: Boolean,
    onDeleteHabit: (() -> Unit)?,
    onDeleteHabitChain: (() -> Unit)?,
    onRequestDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    val hasDeleteAction =
        when (selectedTab) {
            HabitSheetTab.HABIT -> editingHabit && onDeleteHabit != null
            HabitSheetTab.HABIT_CHAIN -> editingHabitChain && onDeleteHabitChain != null
        }
    val confirmEnabled =
        when (selectedTab) {
            HabitSheetTab.HABIT -> hasUnsavedChanges && title.isNotBlank()
            HabitSheetTab.HABIT_CHAIN -> {
                (editingHabitChain || selectedHabitIds.isNotEmpty()) &&
                    hasUnsavedChanges &&
                    title.isNotBlank()
            }
        }

    EditorBottomSheetFooter(
        hasDeleteAction = hasDeleteAction,
        deleteLabel =
            stringResource(
                if (selectedTab == HabitSheetTab.HABIT) {
                    R.string.delete_habit
                } else {
                    R.string.delete_habit_chain
                },
            ),
        onDelete = {
            if (selectedTab == HabitSheetTab.HABIT) {
                onDeleteHabit?.invoke()
            } else {
                onDeleteHabitChain?.invoke()
            }
        },
        autoSaveEnabled = autoSaveEnabled,
        confirmEnabled = confirmEnabled,
        confirmLabel =
            if (editingHabit || editingHabitChain) {
                stringResource(R.string.update)
            } else {
                stringResource(R.string.add_habit)
            },
        onRequestDismiss = onRequestDismiss,
        onConfirmClick = onConfirmClick,
    )
}
