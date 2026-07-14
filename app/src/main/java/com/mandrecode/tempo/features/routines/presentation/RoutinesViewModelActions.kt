package com.mandrecode.tempo.features.routines.presentation

import android.database.SQLException
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.core.domain.util.ValidationResult
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.core.ui.model.PermissionInfo
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import com.mandrecode.tempo.features.routines.domain.usecase.CreateHabitUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.CreateOrUpdateHabitChainUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.UpdateHabitUseCase
import com.mandrecode.tempo.features.routines.domain.util.HabitReminderDateUtil
import com.mandrecode.tempo.features.routines.presentation.RoutinesContract.HabitFormState
import com.mandrecode.tempo.features.routines.presentation.RoutinesContract.HabitSheetTab
import com.mandrecode.tempo.features.routines.presentation.RoutinesContract.PendingHabitChainData
import com.mandrecode.tempo.util.CompletionHistoryUtil
import com.mandrecode.tempo.util.findChainForHabit
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.io.IOException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val ROUTINES_VIEW_MODEL_TAG = "RoutinesViewModel"

internal fun Throwable.toUserFacingMessage(): String =
    localizedMessage?.takeIf { it.isNotBlank() }
        ?: message?.takeIf { it.isNotBlank() }
        ?: javaClass.simpleName.takeIf { it.isNotBlank() }
        ?: javaClass.name

private suspend fun RoutinesViewModel.showErrorSnackbar(
    @StringRes messageResId: Int,
    throwable: Throwable,
) {
    showSnackbar(messageResId, listOf(throwable.toUserFacingMessage()))
}

private fun logRecoverableFailure(
    message: String,
    throwable: Throwable,
) {
    Log.w(ROUTINES_VIEW_MODEL_TAG, message, throwable)
}

private fun markChainLinkFailed(throwable: Throwable): Boolean {
    logRecoverableFailure("Unable to link habit to chain", throwable)
    return true
}

internal fun RoutinesViewModel.loadData() {
    viewModelScope.launch {
        combine(
            habitRepository.getAllHabits(),
            habitChainRepository.getAllHabitChains(),
        ) { habits, habitChains ->
            Pair(habits, habitChains)
        }.catch { exception ->
            if (exception is CancellationException) {
                throw exception
            }
            mutableUiState.update { it.copy(isLoading = false) }
            showSnackbar(
                R.string.msg_error_loading_data,
                listOf(exception.toUserFacingMessage()),
            )
        }.collect { (habits, habitChains) ->
            mutableUiState.update { currentState ->
                val (scheduled, unscheduled) =
                    computeTimelineItems(habits, habitChains, currentState.selectedDate)
                currentState.copy(
                    habits = habits.toPersistentList(),
                    habitChains = habitChains.toPersistentList(),
                    isLoading = false,
                    scheduledTimelineItems =
                        if (scheduled == currentState.scheduledTimelineItems) {
                            currentState.scheduledTimelineItems
                        } else {
                            scheduled
                        },
                    unscheduledTimelineItems =
                        if (unscheduled == currentState.unscheduledTimelineItems) {
                            currentState.unscheduledTimelineItems
                        } else {
                            unscheduled
                        },
                )
            }
        }
    }
}

internal fun RoutinesViewModel.showHabitBottomSheet(
    habit: Habit? = null,
    tab: HabitSheetTab = HabitSheetTab.HABIT,
    chainId: Long? = null,
) {
    val chain =
        chainId?.let { id ->
            mutableUiState.value.habitChains.find { it.id == id }
        }
    mutableUiState.update {
        it.copy(
            habitForm =
                HabitFormState(
                    isVisible = true,
                    editingHabit = habit,
                    targetChainId = chainId,
                    reminderDate = habit?.reminderDate,
                    selectedColorKey = chain?.colorKey ?: habit?.colorKey,
                    selectedIcon = habit?.icon,
                    selectedTab = tab,
                    selectedHabitType = habit?.habitType ?: HabitType.BUILD,
                    shouldAutoSelectColor = habit == null && chainId == null,
                    selectedRepeatDays = habit?.repeatDays?.toPersistentSet(),
                    shouldAutoSelectIcon = habit == null,
                ),
        )
    }
}

internal fun RoutinesViewModel.showHabitChainBottomSheet(habitChain: HabitChain? = null) {
    mutableUiState.update {
        it.copy(
            habitForm =
                HabitFormState(
                    isVisible = true,
                    editingHabitChain = habitChain,
                    reminderDate = habitChain?.periodicReminder,
                    selectedColorKey = habitChain?.colorKey,
                    selectedIcon = habitChain?.icon,
                    selectedTab = HabitSheetTab.HABIT_CHAIN,
                    shouldAutoSelectColor = habitChain == null,
                    selectedRepeatDays = habitChain?.repeatDays?.toPersistentSet(),
                    shouldAutoSelectIcon = habitChain == null,
                ),
        )
    }
}

internal fun RoutinesViewModel.hideHabitBottomSheet() {
    mutableUiState.update {
        it.copy(habitForm = HabitFormState())
    }
}

internal fun RoutinesViewModel.setSelectedTab(tab: HabitSheetTab) {
    mutableUiState.update { it.copy(habitForm = it.habitForm.copy(selectedTab = tab)) }
}

internal fun RoutinesViewModel.setHabitType(habitType: HabitType) {
    if (mutableUiState.value.habitForm.selectedHabitType == habitType) return
    mutableUiState.update {
        val updatedForm = it.habitForm.copy(selectedHabitType = habitType)
        val formWithDefaults =
            if (habitType == HabitType.QUIT) {
                val reminderWasMissing = updatedForm.reminderDate == null
                val withReminder =
                    if (reminderWasMissing) {
                        val eveningReminder =
                            HabitReminderDateUtil.nextUpcomingTime(
                                hour = HabitReminderDateUtil.QUIT_DEFAULT_REMINDER_HOUR,
                            )
                        updatedForm.copy(
                            reminderDate = eveningReminder,
                            quitDefaultReminderApplied = true,
                        )
                    } else {
                        updatedForm
                    }
                val withRepeatSnapshot =
                    if (!withReminder.quitRepeatDaysCleared) {
                        withReminder.copy(
                            quitClearedRepeatDays = withReminder.selectedRepeatDays,
                            quitRepeatDaysCleared = true,
                        )
                    } else {
                        withReminder
                    }
                withRepeatSnapshot.copy(
                    selectedRepeatDays = null,
                )
            } else {
                var revertedForm = updatedForm
                if (revertedForm.quitDefaultReminderApplied) {
                    revertedForm = revertedForm.copy(reminderDate = null)
                }
                if (revertedForm.quitRepeatDaysCleared) {
                    revertedForm =
                        revertedForm.copy(
                            selectedRepeatDays = revertedForm.quitClearedRepeatDays,
                        )
                }
                revertedForm.copy(
                    quitDefaultReminderApplied = false,
                    quitClearedRepeatDays = null,
                    quitRepeatDaysCleared = false,
                )
            }
        it.copy(habitForm = formWithDefaults)
    }
}

internal fun RoutinesViewModel.setReminder(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
) {
    val reminderDate = LocalDateTime(year, month, day, hour, minute)
    mutableUiState.update {
        it.copy(
            habitForm =
                it.habitForm.copy(
                    reminderDate = reminderDate,
                    quitDefaultReminderApplied = false,
                ),
        )
    }
}

internal fun RoutinesViewModel.clearReminder() {
    mutableUiState.update {
        it.copy(
            habitForm =
                it.habitForm.copy(
                    reminderDate = null,
                    quitDefaultReminderApplied = false,
                ),
        )
    }
}

internal fun RoutinesViewModel.setColorKey(colorKey: String) {
    mutableUiState.update { it.copy(habitForm = it.habitForm.copy(selectedColorKey = colorKey)) }
}

internal fun RoutinesViewModel.clearColor() {
    mutableUiState.update { it.copy(habitForm = it.habitForm.copy(selectedColorKey = null)) }
}

internal fun RoutinesViewModel.setIcon(icon: String) {
    mutableUiState.update {
        it.copy(
            habitForm =
                it.habitForm.copy(
                    selectedIcon = icon,
                ),
        )
    }
}

internal fun RoutinesViewModel.clearIcon() {
    mutableUiState.update {
        it.copy(
            habitForm =
                it.habitForm.copy(
                    selectedIcon = null,
                ),
        )
    }
}

@OptIn(ExperimentalTime::class)
internal fun RoutinesViewModel.createOrUpdateHabit(
    title: String,
    description: String,
    autoSave: Boolean = false,
) {
    if (ValidationUtils.validateIcon(mutableUiState.value.habitForm.selectedIcon) is ValidationResult.TooLong ||
        ValidationUtils.validateColorKey(mutableUiState.value.habitForm.selectedColorKey) is ValidationResult.TooLong
    ) {
        return
    }

    val habitToEdit = mutableUiState.value.habitForm.editingHabit
    val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val editingHabitChain =
        habitToEdit?.let { findChainForHabit(it, mutableUiState.value.habitChains) }
    val selectedHabitType =
        if (editingHabitChain != null) {
            HabitType.BUILD
        } else {
            mutableUiState.value.habitForm.selectedHabitType
        }
    val effectiveRepeatDays =
        if (selectedHabitType == HabitType.QUIT) null else mutableUiState.value.habitForm.selectedRepeatDays

    if (habitToEdit != null) {
        val habitChain = editingHabitChain
        val colorKeyToUse =
            if (habitChain != null) {
                habitChain.colorKey
            } else {
                mutableUiState.value.habitForm.selectedColorKey
            }
        val iconToUse = mutableUiState.value.habitForm.selectedIcon
        val reminderToUse =
            if (habitChain != null) null else mutableUiState.value.habitForm.reminderDate

        val updatedHabit =
            habitToEdit.copy(
                title = title,
                description = description,
                colorKey = colorKeyToUse,
                icon = iconToUse,
                reminderDate = reminderToUse,
                habitType = selectedHabitType,
                repeatDays = effectiveRepeatDays,
            )
        updateHabit(updatedHabit, autoSave)
    } else {
        val targetChainId = mutableUiState.value.habitForm.targetChainId
        val newHabit =
            Habit(
                title = title,
                description = description,
                colorKey = mutableUiState.value.habitForm.selectedColorKey,
                icon = mutableUiState.value.habitForm.selectedIcon,
                reminderDate = if (targetChainId != null) null else mutableUiState.value.habitForm.reminderDate,
                habitType = selectedHabitType,
                createdDate = currentTime,
                repeatDays = if (targetChainId != null) null else effectiveRepeatDays,
            )
        addHabit(newHabit)
    }
}

internal fun RoutinesViewModel.createOrUpdateHabitChain(
    title: String,
    description: String,
    habitIds: ImmutableList<Long>,
    autoSave: Boolean = false,
) {
    if (ValidationUtils.validateIcon(mutableUiState.value.habitForm.selectedIcon) is ValidationResult.TooLong ||
        ValidationUtils.validateColorKey(mutableUiState.value.habitForm.selectedColorKey) is ValidationResult.TooLong
    ) {
        return
    }

    if (habitIds.isEmpty()) {
        val editingChain = mutableUiState.value.habitForm.editingHabitChain
        if (editingChain != null) {
            mutableUiState.update {
                it.copy(showEmptyHabitChainConfirmationDialog = true)
            }
        } else {
            viewModelScope.launch {
                showSnackbar(R.string.msg_error_empty_habit_chain)
            }
        }
        return
    }

    val habitsWithReminders =
        mutableUiState.value.habits.filter { habit ->
            habitIds.contains(habit.id) && habit.reminderDate != null
        }

    if (habitsWithReminders.isNotEmpty()) {
        mutableUiState.update {
            it.copy(
                showClearRemindersConfirmationDialog = true,
                habitsWithRemindersToBeCleared = habitsWithReminders.toPersistentList(),
                pendingHabitChainData =
                    PendingHabitChainData(
                        title = title,
                        description = description,
                        habitIds = habitIds,
                        colorKey = mutableUiState.value.habitForm.selectedColorKey,
                        icon = mutableUiState.value.habitForm.selectedIcon,
                        autoSave = autoSave,
                    ),
            )
        }
    } else {
        proceedWithHabitChainCreation(
            title,
            description,
            habitIds,
            mutableUiState.value.habitForm.selectedColorKey,
            mutableUiState.value.habitForm.selectedIcon,
            autoSave,
        )
    }
}

internal fun RoutinesViewModel.proceedWithHabitChainCreation(
    title: String,
    description: String,
    habitIds: ImmutableList<Long>,
    colorKey: String?,
    icon: String?,
    autoSave: Boolean = false,
) {
    viewModelScope.launch {
        try {
            val params =
                CreateOrUpdateHabitChainUseCase.Params(
                    title = title,
                    description = description,
                    habitIds = habitIds,
                    colorKey = colorKey,
                    icon = icon,
                    reminderDate = mutableUiState.value.habitForm.reminderDate,
                    repeatDays = mutableUiState.value.habitForm.selectedRepeatDays,
                    editingHabitChain = mutableUiState.value.habitForm.editingHabitChain,
                )
            val result = createOrUpdateHabitChainUseCase(params)
            when (result) {
                is CreateOrUpdateHabitChainUseCase.Result.ValidationError -> {
                    handleHabitChainValidationError(result.type)
                    return@launch
                }

                is CreateOrUpdateHabitChainUseCase.Result.Success -> {
                    val isGenericSuccess =
                        !result.reminderAdvanced &&
                            (
                                result.messageResId == R.string.msg_habit_chain_updated_success ||
                                    result.messageResId == R.string.msg_habit_chain_created_success
                            )
                    if (!autoSave || !isGenericSuccess) {
                        showSnackbar(result.messageResId)
                    }
                }
            }
            if (!autoSave) {
                hideHabitBottomSheet()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            showErrorSnackbar(R.string.msg_habit_chain_save_error, e)
        } catch (e: SQLException) {
            showErrorSnackbar(R.string.msg_habit_chain_save_error, e)
        } catch (e: IllegalArgumentException) {
            showErrorSnackbar(R.string.msg_habit_chain_save_error, e)
        } catch (e: IllegalStateException) {
            showErrorSnackbar(R.string.msg_habit_chain_save_error, e)
        }
    }
}

internal fun RoutinesViewModel.confirmClearRemindersAndProceed() {
    val pendingData = mutableUiState.value.pendingHabitChainData
    if (pendingData != null) {
        proceedWithHabitChainCreation(
            pendingData.title,
            pendingData.description,
            pendingData.habitIds,
            pendingData.colorKey,
            pendingData.icon,
            pendingData.autoSave,
        )
    }
    hideClearRemindersConfirmation()
}

internal fun RoutinesViewModel.hideClearRemindersConfirmation() {
    mutableUiState.update {
        it.copy(
            showClearRemindersConfirmationDialog = false,
            habitsWithRemindersToBeCleared = persistentListOf(),
            pendingHabitChainData = null,
        )
    }
}

internal fun RoutinesViewModel.addHabit(habit: Habit) {
    val targetChainId = mutableUiState.value.habitForm.targetChainId
    viewModelScope.launch {
        try {
            when (val result = createHabitUseCase(habit)) {
                is CreateHabitUseCase.Result.ValidationError -> {
                    handleHabitValidationError(result.type)
                }

                is CreateHabitUseCase.Result.Success -> {
                    var chainLinkFailed = false
                    if (targetChainId != null) {
                        try {
                            val linked = addHabitToChain(result.habitId, targetChainId)
                            chainLinkFailed = !linked
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: IOException) {
                            chainLinkFailed = markChainLinkFailed(e)
                        } catch (e: SQLException) {
                            chainLinkFailed = markChainLinkFailed(e)
                        } catch (e: IllegalArgumentException) {
                            chainLinkFailed = markChainLinkFailed(e)
                        } catch (e: IllegalStateException) {
                            chainLinkFailed = markChainLinkFailed(e)
                        }
                    }
                    val messageResId =
                        when {
                            chainLinkFailed ->
                                R.string.msg_habit_created_chain_link_failed

                            result.scheduleResult is ScheduleResult.PermissionError -> {
                                mutableUiState.update { it.copy(showPermissionRequestDialog = true) }
                                R.string.msg_permission_needed
                            }

                            result.scheduleResult is ScheduleResult.Failure ->
                                R.string.msg_habit_create_failed_scheduling

                            result.reminderAdvanced ->
                                R.string.msg_habit_created_reminder_advanced

                            else -> R.string.msg_habit_created_success
                        }
                    hideHabitBottomSheet()
                    showSnackbar(messageResId)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            showErrorSnackbar(R.string.msg_habit_create_error, e)
        } catch (e: SQLException) {
            showErrorSnackbar(R.string.msg_habit_create_error, e)
        } catch (e: IllegalArgumentException) {
            showErrorSnackbar(R.string.msg_habit_create_error, e)
        } catch (e: IllegalStateException) {
            showErrorSnackbar(R.string.msg_habit_create_error, e)
        }
    }
}

internal suspend fun RoutinesViewModel.addHabitToChain(
    habitId: Long,
    chainId: Long,
): Boolean {
    val chain = habitChainRepository.getHabitChainById(chainId) ?: return false
    if (habitId in chain.habitIds) return true
    val newHabitIds = chain.habitIds + habitId
    if (ValidationUtils.validateHabitChainSize(newHabitIds.size) != ValidationResult.Valid) {
        return false
    }
    val updatedChain = chain.copy(habitIds = newHabitIds)
    habitChainRepository.updateHabitChain(updatedChain)
    return true
}

internal fun RoutinesViewModel.updateHabit(
    habit: Habit,
    autoSave: Boolean = false,
) {
    viewModelScope.launch {
        try {
            when (val result = updateHabitUseCase(habit)) {
                is UpdateHabitUseCase.Result.ValidationError -> {
                    handleHabitValidationError(result.type)
                }

                is UpdateHabitUseCase.Result.Success -> {
                    val messageResId =
                        when {
                            result.scheduleResult is ScheduleResult.PermissionError -> {
                                mutableUiState.update { it.copy(showPermissionRequestDialog = true) }
                                R.string.msg_permission_needed
                            }

                            result.scheduleResult is ScheduleResult.Failure ->
                                R.string.msg_habit_update_failed_scheduling

                            result.reminderAdvanced ->
                                R.string.msg_habit_updated_reminder_advanced

                            autoSave -> null

                            else -> R.string.msg_habit_updated_success
                        }
                    if (!autoSave) {
                        hideHabitBottomSheet()
                    }
                    if (messageResId != null) {
                        showSnackbar(messageResId)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            showErrorSnackbar(R.string.msg_habit_update_error, e)
        } catch (e: SQLException) {
            showErrorSnackbar(R.string.msg_habit_update_error, e)
        } catch (e: IllegalArgumentException) {
            showErrorSnackbar(R.string.msg_habit_update_error, e)
        } catch (e: IllegalStateException) {
            showErrorSnackbar(R.string.msg_habit_update_error, e)
        }
    }
}

internal fun RoutinesViewModel.handleHabitValidationError(type: CreateHabitUseCase.ValidationErrorType) {
    when (type) {
        CreateHabitUseCase.ValidationErrorType.TITLE_EMPTY ->
            mutableUiState.update { it.copy(habitForm = it.habitForm.copy(titleError = R.string.task_title_required)) }

        CreateHabitUseCase.ValidationErrorType.TITLE_TOO_LONG ->
            mutableUiState.update { it.copy(habitForm = it.habitForm.copy(titleError = R.string.error_habit_title_too_long)) }

        CreateHabitUseCase.ValidationErrorType.DESCRIPTION_TOO_LONG ->
            mutableUiState.update { it.copy(habitForm = it.habitForm.copy(descriptionError = R.string.error_habit_description_too_long)) }
    }
}

internal fun RoutinesViewModel.handleHabitChainValidationError(type: CreateOrUpdateHabitChainUseCase.ValidationErrorType) {
    when (type) {
        CreateOrUpdateHabitChainUseCase.ValidationErrorType.TITLE_EMPTY ->
            mutableUiState.update { it.copy(habitForm = it.habitForm.copy(titleError = R.string.task_title_required)) }

        CreateOrUpdateHabitChainUseCase.ValidationErrorType.TITLE_TOO_LONG ->
            mutableUiState.update { it.copy(habitForm = it.habitForm.copy(titleError = R.string.error_habit_title_too_long)) }

        CreateOrUpdateHabitChainUseCase.ValidationErrorType.DESCRIPTION_TOO_LONG ->
            mutableUiState.update { it.copy(habitForm = it.habitForm.copy(descriptionError = R.string.error_habit_description_too_long)) }

        CreateOrUpdateHabitChainUseCase.ValidationErrorType.TOO_MANY_HABITS ->
            viewModelScope.launch { showSnackbar(R.string.error_habit_chain_too_long) }

        CreateOrUpdateHabitChainUseCase.ValidationErrorType.QUIT_HABITS_NOT_ALLOWED ->
            viewModelScope.launch { showSnackbar(R.string.error_habit_chain_quit_not_allowed) }
    }
}

internal fun RoutinesViewModel.showDeleteHabitConfirmation(habit: Habit) {
    mutableUiState.update {
        it.copy(
            habitToDelete = habit,
            showDeleteHabitConfirmationDialog = true,
        )
    }
}

internal fun RoutinesViewModel.hideDeleteHabitConfirmation() {
    mutableUiState.update {
        it.copy(
            habitToDelete = null,
            showDeleteHabitConfirmationDialog = false,
        )
    }
}

internal fun RoutinesViewModel.deleteHabit() {
    val habit = mutableUiState.value.habitToDelete ?: return
    viewModelScope.launch {
        try {
            val snapshot = deleteHabitUseCase(habit)
            hideDeleteHabitConfirmation()
            hideHabitBottomSheet()
            val token = storePendingDeletion(PendingRoutineDeletion.Habit(snapshot))
            showSnackbar(
                messageResId = R.string.msg_habit_deleted_success,
                actionResId = R.string.undo,
                deletionToken = token,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            showErrorSnackbar(R.string.msg_habit_delete_error, e)
        } catch (e: SQLException) {
            showErrorSnackbar(R.string.msg_habit_delete_error, e)
        } catch (e: IllegalArgumentException) {
            showErrorSnackbar(R.string.msg_habit_delete_error, e)
        } catch (e: IllegalStateException) {
            showErrorSnackbar(R.string.msg_habit_delete_error, e)
        }
    }
}

internal fun RoutinesViewModel.showDeleteHabitChainConfirmation(habitChain: HabitChain) {
    mutableUiState.update {
        it.copy(
            habitChainToDelete = habitChain,
            showDeleteHabitChainConfirmationDialog = true,
        )
    }
}

internal fun RoutinesViewModel.hideDeleteHabitChainConfirmation() {
    mutableUiState.update {
        it.copy(
            habitChainToDelete = null,
            showDeleteHabitChainConfirmationDialog = false,
        )
    }
}

internal fun RoutinesViewModel.deleteHabitChain(deleteHabits: Boolean = false) {
    val habitChain = mutableUiState.value.habitChainToDelete ?: return
    viewModelScope.launch {
        try {
            val snapshot = deleteHabitChainUseCase(habitChain, deleteHabits)
            hideDeleteHabitChainConfirmation()
            hideHabitBottomSheet()
            val token = storePendingDeletion(PendingRoutineDeletion.HabitChain(snapshot))
            showSnackbar(
                messageResId = R.string.msg_habit_chain_deleted_success,
                actionResId = R.string.undo,
                deletionToken = token,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            showErrorSnackbar(R.string.msg_habit_chain_delete_error, e)
        } catch (e: SQLException) {
            showErrorSnackbar(R.string.msg_habit_chain_delete_error, e)
        } catch (e: IllegalArgumentException) {
            showErrorSnackbar(R.string.msg_habit_chain_delete_error, e)
        } catch (e: IllegalStateException) {
            showErrorSnackbar(R.string.msg_habit_chain_delete_error, e)
        }
    }
}

internal fun RoutinesViewModel.showEmptyHabitChainConfirmation() {
    mutableUiState.update {
        it.copy(showEmptyHabitChainConfirmationDialog = true)
    }
}

internal fun RoutinesViewModel.hideEmptyHabitChainConfirmation() {
    mutableUiState.update {
        it.copy(showEmptyHabitChainConfirmationDialog = false)
    }
}

internal fun RoutinesViewModel.confirmDeleteEmptyHabitChain() {
    val habitChain = mutableUiState.value.habitForm.editingHabitChain
    if (habitChain != null) {
        viewModelScope.launch {
            try {
                val snapshot = deleteHabitChainUseCase(habitChain, deleteHabits = false)
                hideEmptyHabitChainConfirmation()
                hideHabitBottomSheet()
                val token = storePendingDeletion(PendingRoutineDeletion.HabitChain(snapshot))
                showSnackbar(
                    messageResId = R.string.msg_habit_chain_deleted_success,
                    actionResId = R.string.undo,
                    deletionToken = token,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                showErrorSnackbar(R.string.msg_habit_chain_delete_error, e)
            } catch (e: SQLException) {
                showErrorSnackbar(R.string.msg_habit_chain_delete_error, e)
            } catch (e: IllegalArgumentException) {
                showErrorSnackbar(R.string.msg_habit_chain_delete_error, e)
            } catch (e: IllegalStateException) {
                showErrorSnackbar(R.string.msg_habit_chain_delete_error, e)
            }
        }
    } else {
        hideEmptyHabitChainConfirmation()
    }
}

internal fun RoutinesViewModel.toggleHabitCompletion(
    habitId: Long,
    isCompleted: Boolean,
) {
    viewModelScope.launch {
        try {
            val selectedDate = mutableUiState.value.selectedDate
            toggleHabitCompletionUseCase(habitId, isCompleted, selectedDate)

            if (isCompleted) {
                val state = mutableUiState.value
                val dateStr = selectedDate.toString()

                val chainsToCollapse =
                    state.habitChains
                        .filter { habitId in it.habitIds && state.expandedChainIds.contains(it.id) }
                        .filter { chain ->
                            val chainHabits = state.habits.filter { it.id in chain.habitIds }
                            chainHabits.isNotEmpty() &&
                                chainHabits.all { habit ->
                                    if (habit.id == habitId) {
                                        true
                                    } else {
                                        CompletionHistoryUtil.isDateInHistory(
                                            habit.completionHistory,
                                            dateStr,
                                        )
                                    }
                                }
                        }

                if (chainsToCollapse.isNotEmpty()) {
                    mutableUiState.update { current ->
                        val updatedIds =
                            chainsToCollapse.fold(current.expandedChainIds) { acc, chain ->
                                acc.remove(chain.id)
                            }
                        current.copy(expandedChainIds = updatedIds)
                    }
                }
            }
            refreshEditingHabitStateIfNeeded(habitId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            showErrorSnackbar(R.string.msg_habit_update_error, e)
        } catch (e: SQLException) {
            showErrorSnackbar(R.string.msg_habit_update_error, e)
        } catch (e: IllegalArgumentException) {
            showErrorSnackbar(R.string.msg_habit_update_error, e)
        } catch (e: IllegalStateException) {
            showErrorSnackbar(R.string.msg_habit_update_error, e)
        }
    }
}

internal suspend fun RoutinesViewModel.refreshEditingHabitStateIfNeeded(toggledHabitId: Long) {
    val form = mutableUiState.value.habitForm
    if (form.editingHabit?.id == toggledHabitId) {
        refreshHabitForForm(toggledHabitId)?.let { refreshed ->
            mutableUiState.update {
                it.copy(habitForm = it.habitForm.copy(editingHabit = refreshed))
            }
        }
    }
    val editingChain = mutableUiState.value.habitForm.editingHabitChain
    if (editingChain != null && toggledHabitId in editingChain.habitIds) {
        refreshHabitChainForForm(editingChain.id)?.let { refreshedChain ->
            mutableUiState.update {
                it.copy(habitForm = it.habitForm.copy(editingHabitChain = refreshedChain))
            }
        }
    }
}

private suspend fun RoutinesViewModel.refreshHabitForForm(habitId: Long): Habit? =
    try {
        habitRepository.getHabitById(habitId)
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        logRecoverableFailure("Unable to refresh edited habit", e)
        null
    } catch (e: SQLException) {
        logRecoverableFailure("Unable to refresh edited habit", e)
        null
    } catch (e: IllegalArgumentException) {
        logRecoverableFailure("Unable to refresh edited habit", e)
        null
    } catch (e: IllegalStateException) {
        logRecoverableFailure("Unable to refresh edited habit", e)
        null
    }

private suspend fun RoutinesViewModel.refreshHabitChainForForm(chainId: Long): HabitChain? =
    try {
        habitChainRepository.getHabitChainById(chainId)
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        logRecoverableFailure("Unable to refresh edited habit chain", e)
        null
    } catch (e: SQLException) {
        logRecoverableFailure("Unable to refresh edited habit chain", e)
        null
    } catch (e: IllegalArgumentException) {
        logRecoverableFailure("Unable to refresh edited habit chain", e)
        null
    } catch (e: IllegalStateException) {
        logRecoverableFailure("Unable to refresh edited habit chain", e)
        null
    }

internal fun RoutinesViewModel.toggleChainExpanded(chainId: Long) {
    mutableUiState.update { currentState ->
        val expandedIds = currentState.expandedChainIds
        val newExpandedIds =
            if (expandedIds.contains(chainId)) {
                expandedIds.remove(chainId)
            } else {
                expandedIds.add(chainId)
            }
        currentState.copy(expandedChainIds = newExpandedIds)
    }
}

internal fun RoutinesViewModel.openHabitFromNotificationInternal(habitId: Long) {
    viewModelScope.launch {
        val habit = habitRepository.getHabitById(habitId)
        if (habit != null) {
            showHabitBottomSheet(habit)
        }
    }
}

internal fun RoutinesViewModel.openHabitChainFromNotificationInternal(
    chainId: Long,
    scheduledDate: LocalDate?,
) {
    viewModelScope.launch {
        val habitChain = habitChainRepository.getHabitChainById(chainId)
        if (habitChain != null) {
            if (scheduledDate != null) {
                selectDate(scheduledDate)
            }
            showHabitChainBottomSheet(habitChain)
        }
    }
}

internal fun RoutinesViewModel.selectDate(date: LocalDate) {
    mutableUiState.update { currentState ->
        val (scheduled, unscheduled) =
            computeTimelineItems(currentState.habits, currentState.habitChains, date)
        currentState.copy(
            selectedDate = date,
            scheduledTimelineItems = scheduled,
            unscheduledTimelineItems = unscheduled,
        )
    }
}

internal fun RoutinesViewModel.selectPreviousDay() {
    mutableUiState.update { currentState ->
        val previousDay = currentState.selectedDate.minus(DatePeriod(days = 1))
        val (scheduled, unscheduled) =
            computeTimelineItems(currentState.habits, currentState.habitChains, previousDay)
        currentState.copy(
            selectedDate = previousDay,
            scheduledTimelineItems = scheduled,
            unscheduledTimelineItems = unscheduled,
        )
    }
}

internal fun RoutinesViewModel.selectNextDay() {
    mutableUiState.update { currentState ->
        val nextDay = currentState.selectedDate.plus(DatePeriod(days = 1))
        val (scheduled, unscheduled) =
            computeTimelineItems(currentState.habits, currentState.habitChains, nextDay)
        currentState.copy(
            selectedDate = nextDay,
            scheduledTimelineItems = scheduled,
            unscheduledTimelineItems = unscheduled,
        )
    }
}

@OptIn(ExperimentalTime::class)
internal fun RoutinesViewModel.selectToday() {
    val today =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    mutableUiState.update { currentState ->
        val (scheduled, unscheduled) =
            computeTimelineItems(currentState.habits, currentState.habitChains, today)
        currentState.copy(
            selectedDate = today,
            scheduledTimelineItems = scheduled,
            unscheduledTimelineItems = unscheduled,
        )
    }
}

internal fun RoutinesViewModel.setRepeatDays(days: Set<DayOfWeek>?) {
    mutableUiState.update {
        it.copy(
            habitForm =
                it.habitForm.copy(
                    selectedRepeatDays = days?.toPersistentSet(),
                    quitClearedRepeatDays = null,
                    quitRepeatDaysCleared = false,
                ),
        )
    }
}

internal fun RoutinesViewModel.clearHabitErrors() {
    mutableUiState.update { state ->
        if (state.habitForm.titleError == null && state.habitForm.descriptionError == null) {
            state
        } else {
            state.copy(
                habitForm =
                    state.habitForm.copy(
                        titleError = null,
                        descriptionError = null,
                    ),
            )
        }
    }
}

internal fun RoutinesViewModel.checkPermissionsAndSyncRemindersInternal() {
    val wasGrantedBefore = uiState.value.permissionInfo.areAllGranted

    val hasNotifications = permissionChecker.hasNotificationPermissions()
    val canScheduleAlarms = permissionChecker.canScheduleExactAlarms()
    val permissionInfo = PermissionInfo(hasNotifications, canScheduleAlarms)

    mutableUiState.update { it.copy(permissionInfo = permissionInfo) }

    if (!permissionInfo.areAllGranted) {
        viewModelScope.launch {
            val habitsWithReminders = habitRepository.getHabitsWithReminders()
            if (habitsWithReminders.isNotEmpty()) {
                mutableUiState.update { it.copy(showPermissionRevokedDialog = true) }
            }
        }
    } else {
        if (!wasGrantedBefore) {
            onPermissionsGranted()
        }
    }
}

internal fun RoutinesViewModel.onPermissionsGranted() {
    viewModelScope.launch {
        showSnackbar(R.string.msg_all_set_reminders_active)
    }
}

internal fun RoutinesViewModel.dismissPermissionRequestDialog() {
    mutableUiState.update { it.copy(showPermissionRequestDialog = false) }
}

internal fun RoutinesViewModel.dismissPermissionRevokedDialog() {
    mutableUiState.update { it.copy(showPermissionRevokedDialog = false) }
}

internal fun RoutinesViewModel.confirmClearAllHabitReminders() {
    viewModelScope.launch {
        clearAllHabitRemindersUseCase()
        mutableUiState.update { it.copy(showPermissionRevokedDialog = false) }
        showSnackbar(R.string.msg_reminders_cleared)
    }
}

internal fun computeTimelineItems(
    habits: List<Habit>,
    habitChains: List<HabitChain>,
    selectedDate: LocalDate,
): Pair<ImmutableList<RoutinesContract.TimelineItem>, ImmutableList<RoutinesContract.TimelineItem>> {
    val selectedDayOfWeek = DayOfWeek.fromKotlinDayOfWeek(selectedDate.dayOfWeek)
    val habitIdsInChains = habitChains.flatMap { it.habitIds }.toSet()

    val filteredChains =
        habitChains.filter { chain ->
            chain.repeatDays == null ||
                chain.repeatDays.isEmpty() ||
                chain.repeatDays.contains(selectedDayOfWeek)
        }

    val filteredStandaloneHabits =
        habits
            .filter { habit -> !habitIdsInChains.contains(habit.id) }
            .filter { habit -> habit.habitType != HabitType.QUIT }
            .filter { habit ->
                habit.repeatDays == null ||
                    habit.repeatDays.isEmpty() ||
                    habit.repeatDays.contains(selectedDayOfWeek)
            }

    val items =
        buildList {
            filteredChains.forEach { chain ->
                add(
                    RoutinesContract.TimelineItem(
                        time =
                            chain.periodicReminder?.let { reminder ->
                                reminder.hour * 60 + reminder.minute
                            },
                        isChain = true,
                        chainId = chain.id,
                    ),
                )
            }
            filteredStandaloneHabits.forEach { habit ->
                add(
                    RoutinesContract.TimelineItem(
                        time =
                            habit.reminderDate?.let { reminder ->
                                reminder.hour * 60 + reminder.minute
                            },
                        isChain = false,
                        habitId = habit.id,
                    ),
                )
            }
        }

    return items
        .sortedWith(
            compareBy<RoutinesContract.TimelineItem> { it.time == null }
                .thenBy(nullsLast()) { it.time },
        ).partition { it.time != null }
        .let { (scheduled, unscheduled) ->
            scheduled.toPersistentList() to unscheduled.toPersistentList()
        }
}
