package com.mandrecode.tempo.features.routines.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.ui.adaptive.SheetPlacement
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import com.mandrecode.tempo.features.routines.presentation.RoutinesContract
import com.mandrecode.tempo.features.routines.presentation.RoutinesContract.HabitSheetTab
import kotlinx.datetime.format.FormatStringsInDatetimeFormats

@OptIn(
    ExperimentalMaterial3Api::class,
    FormatStringsInDatetimeFormats::class,
)
@Composable
fun HabitBottomSheet(
    formState: RoutinesContract.HabitFormState,
    selectedDate: kotlinx.datetime.LocalDate,
    habits: List<Habit>,
    habitChains: List<HabitChain>,
    onSelectTab: (HabitSheetTab) -> Unit,
    onSetHabitType: (HabitType) -> Unit,
    onSetReminder: (Int, Int, Int, Int, Int) -> Unit,
    onClearReminder: () -> Unit,
    onSetColorKey: (String) -> Unit,
    onClearColor: () -> Unit,
    onSetIcon: (String) -> Unit,
    onClearIcon: () -> Unit,
    onDismiss: () -> Unit,
    onClearErrors: () -> Unit,
    onConfirmHabit: (title: String, description: String) -> Unit,
    onConfirmHabitChain: (title: String, description: String, habitIds: List<Long>) -> Unit,
    modifier: Modifier = Modifier,
    onAutoSaveHabit: ((title: String, description: String) -> Unit)? = null,
    onAutoSaveHabitChain: ((title: String, description: String, habitIds: List<Long>) -> Unit)? = null,
    onDeleteHabit: (() -> Unit)? = null,
    onDeleteHabitChain: (() -> Unit)? = null,
    onSetRepeatDays: ((Set<DayOfWeek>?) -> Unit)? = null,
    onToggleHabitCompletion: ((habitId: Long, isCompleted: Boolean) -> Unit)? = null,
    placement: SheetPlacement? = null,
) {
    HabitBottomSheetContent(
        formState = formState,
        selectedDate = selectedDate,
        habits = habits,
        habitChains = habitChains,
        onSelectTab = onSelectTab,
        onSetHabitType = onSetHabitType,
        onSetReminder = onSetReminder,
        onClearReminder = onClearReminder,
        onSetColorKey = onSetColorKey,
        onClearColor = onClearColor,
        onSetIcon = onSetIcon,
        onClearIcon = onClearIcon,
        onDismiss = onDismiss,
        onClearErrors = onClearErrors,
        onConfirmHabit = onConfirmHabit,
        onConfirmHabitChain = onConfirmHabitChain,
        modifier = modifier,
        onAutoSaveHabit = onAutoSaveHabit,
        onAutoSaveHabitChain = onAutoSaveHabitChain,
        onDeleteHabit = onDeleteHabit,
        onDeleteHabitChain = onDeleteHabitChain,
        onSetRepeatDays = onSetRepeatDays,
        onToggleHabitCompletion = onToggleHabitCompletion,
        placement = placement,
    )
}
