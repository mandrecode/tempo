package com.mandrecode.tempo.features.routines.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.usecase.ClearAllHabitRemindersUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.CreateHabitUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.CreateOrUpdateHabitChainUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.DeleteHabitChainUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.DeleteHabitUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.RestoreDeletedHabitChainUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.RestoreDeletedHabitUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.ToggleHabitCompletionUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.UpdateHabitUseCase
import com.mandrecode.tempo.infrastructure.permissions.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class RoutinesViewModel
    @Inject
    constructor(
        internal val habitRepository: HabitRepository,
        internal val habitChainRepository: HabitChainRepository,
        internal val createHabitUseCase: CreateHabitUseCase,
        internal val updateHabitUseCase: UpdateHabitUseCase,
        internal val deleteHabitUseCase: DeleteHabitUseCase,
        internal val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
        internal val createOrUpdateHabitChainUseCase: CreateOrUpdateHabitChainUseCase,
        internal val deleteHabitChainUseCase: DeleteHabitChainUseCase,
        internal val clearAllHabitRemindersUseCase: ClearAllHabitRemindersUseCase,
        internal val permissionChecker: PermissionChecker,
        internal val restoreDeletedHabitUseCase: RestoreDeletedHabitUseCase,
        internal val restoreDeletedHabitChainUseCase: RestoreDeletedHabitChainUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(RoutinesContract.UiState())
        internal val mutableUiState: MutableStateFlow<RoutinesContract.UiState>
            get() = _uiState
        val uiState: StateFlow<RoutinesContract.UiState> = _uiState.asStateFlow()

        private val _uiEffect = Channel<RoutinesContract.UiEffect>(Channel.BUFFERED)
        val uiEffect = _uiEffect.receiveAsFlow()
        internal val pendingDeletionSnapshots = mutableMapOf<Long, PendingRoutineDeletion>()
        internal var nextDeletionToken = 0L
        internal var nextEditorSessionId = 0L

        init {
            loadData()
        }

        fun onEvent(event: RoutinesContract.UiEvent) {
            when (event) {
                is RoutinesContract.UiEvent.ShowHabitBottomSheet ->
                    showHabitBottomSheet(event.habit, event.tab, event.chainId)

                is RoutinesContract.UiEvent.ShowHabitChainBottomSheet ->
                    showHabitChainBottomSheet(event.habitChain)

                is RoutinesContract.UiEvent.HideHabitBottomSheet -> hideHabitBottomSheet()
                is RoutinesContract.UiEvent.SetSelectedTab -> setSelectedTab(event.tab)
                is RoutinesContract.UiEvent.SetHabitType -> setHabitType(event.habitType)
                is RoutinesContract.UiEvent.CreateOrUpdateHabit ->
                    createOrUpdateHabit(event.title, event.description, event.autoSave)

                is RoutinesContract.UiEvent.CreateOrUpdateHabitChain ->
                    createOrUpdateHabitChain(event.title, event.description, event.habitIds, event.autoSave)

                is RoutinesContract.UiEvent.ToggleHabitCompletion ->
                    toggleHabitCompletion(event.habitId, event.isCompleted)

                is RoutinesContract.UiEvent.ShowDeleteHabitConfirmation ->
                    showDeleteHabitConfirmation(event.habit)

                is RoutinesContract.UiEvent.HideDeleteHabitConfirmation -> hideDeleteHabitConfirmation()
                is RoutinesContract.UiEvent.DeleteHabit -> deleteHabit()
                is RoutinesContract.UiEvent.ShowDeleteHabitChainConfirmation ->
                    showDeleteHabitChainConfirmation(event.habitChain)

                is RoutinesContract.UiEvent.HideDeleteHabitChainConfirmation ->
                    hideDeleteHabitChainConfirmation()

                is RoutinesContract.UiEvent.DeleteHabitChain -> deleteHabitChain(event.deleteHabits)
                is RoutinesContract.UiEvent.HideEmptyHabitChainConfirmation ->
                    hideEmptyHabitChainConfirmation()

                is RoutinesContract.UiEvent.ConfirmDeleteEmptyHabitChain ->
                    confirmDeleteEmptyHabitChain()

                is RoutinesContract.UiEvent.HideClearRemindersConfirmation ->
                    hideClearRemindersConfirmation()

                is RoutinesContract.UiEvent.ConfirmClearRemindersAndProceed ->
                    confirmClearRemindersAndProceed()

                is RoutinesContract.UiEvent.SetReminder ->
                    setReminder(event.year, event.month, event.day, event.hour, event.minute)

                is RoutinesContract.UiEvent.ClearReminder -> clearReminder()
                is RoutinesContract.UiEvent.SetColorKey -> setColorKey(event.colorKey)
                is RoutinesContract.UiEvent.ClearColor -> clearColor()
                is RoutinesContract.UiEvent.SetIcon -> setIcon(event.icon)
                is RoutinesContract.UiEvent.ClearIcon -> clearIcon()
                is RoutinesContract.UiEvent.SetRepeatDays -> setRepeatDays(event.days)
                is RoutinesContract.UiEvent.ClearHabitErrors -> clearHabitErrors()
                is RoutinesContract.UiEvent.SelectDate -> selectDate(event.date)
                is RoutinesContract.UiEvent.ToggleChainExpanded -> toggleChainExpanded(event.chainId)
                is RoutinesContract.UiEvent.DismissPermissionRequestDialog ->
                    dismissPermissionRequestDialog()

                is RoutinesContract.UiEvent.DismissPermissionRevokedDialog ->
                    dismissPermissionRevokedDialog()

                is RoutinesContract.UiEvent.ConfirmClearAllHabitReminders ->
                    confirmClearAllHabitReminders()

                is RoutinesContract.UiEvent.OnPermissionsGranted -> onPermissionsGranted()
                is RoutinesContract.UiEvent.UndoDeletion -> undoDeletion(event.token)
                is RoutinesContract.UiEvent.DismissDeletionUndo -> dismissDeletionUndo(event.token)
            }
        }

        internal suspend fun showSnackbar(
            @StringRes messageResId: Int,
            formatArgs: List<Any> = emptyList(),
            @StringRes actionResId: Int? = null,
            deletionToken: Long? = null,
        ) {
            _uiEffect.send(
                RoutinesContract.UiEffect.ShowSnackbar(
                    messageResId = messageResId,
                    formatArgs = formatArgs,
                    actionResId = actionResId,
                    deletionToken = deletionToken,
                ),
            )
        }

        fun openHabitFromNotification(habitId: Long) {
            openHabitFromNotificationInternal(habitId)
        }

        fun openHabitChainFromNotification(
            chainId: Long,
            scheduledDate: LocalDate? = null,
        ) {
            openHabitChainFromNotificationInternal(chainId, scheduledDate)
        }

        fun checkPermissionsAndSyncReminders() {
            checkPermissionsAndSyncRemindersInternal()
        }
    }
