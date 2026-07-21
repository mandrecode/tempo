package com.mandrecode.tempo.core.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.OnboardingPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.data.preferences.WhatsNewPreferencesRepository
import com.mandrecode.tempo.core.ui.model.MainUiState
import com.mandrecode.tempo.core.ui.navigation.PendingNotificationAction
import com.mandrecode.tempo.features.whatsnew.presentation.WhatsNewRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        navigationPreferencesRepository: NavigationPreferencesRepository,
        themePreferencesRepository: ThemePreferencesRepository,
        onboardingPreferencesRepository: OnboardingPreferencesRepository,
        private val whatsNewPreferencesRepository: WhatsNewPreferencesRepository,
    ) : ViewModel() {
        private val _pendingNotificationAction = MutableStateFlow(readPendingNotificationAction())
        val pendingNotificationAction: StateFlow<PendingNotificationAction?> = _pendingNotificationAction.asStateFlow()

        private val mainPreferences =
            combine(
                themePreferencesRepository.getThemeMode(),
                themePreferencesRepository.getUseTempoColors(),
                navigationPreferencesRepository.getDefaultTab(),
                navigationPreferencesRepository.isRoutinesTabEnabled(),
                navigationPreferencesRepository.isTasksTabEnabled(),
            ) { themeMode, useTempoColors, defaultTab, isRoutinesTabEnabled, isTasksTabEnabled ->
                MainUiState.Success(
                    themeMode = themeMode,
                    useTempoColors = useTempoColors,
                    defaultTab = defaultTab,
                    isRoutinesTabEnabled = isRoutinesTabEnabled,
                    isTasksTabEnabled = isTasksTabEnabled,
                    isOnboardingCompleted = false,
                )
            }

        val uiState: StateFlow<MainUiState> =
            combine(
                mainPreferences,
                onboardingPreferencesRepository.isCompleted,
                whatsNewPreferencesRepository.lastSeenVersionCode,
            ) { state, isOnboardingCompleted, lastSeenVersionCode ->
                val latestWhatsNewEntry = WhatsNewRegistry.latest
                state.copy(
                    isOnboardingCompleted = isOnboardingCompleted,
                    whatsNewEntry =
                        latestWhatsNewEntry.takeIf {
                            isOnboardingCompleted && it.versionCode > lastSeenVersionCode
                        },
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MainUiState.Loading,
            )

        fun onWhatsNewDismissed() {
            whatsNewPreferencesRepository.setLastSeenVersionCode(WhatsNewRegistry.latest.versionCode)
        }

        fun setPendingNotificationAction(action: PendingNotificationAction) {
            when (action) {
                is PendingNotificationAction.OpenTask -> {
                    savedStateHandle[KEY_PENDING_ACTION_TYPE] = ACTION_TYPE_TASK
                    savedStateHandle[KEY_PENDING_ACTION_ID] = action.taskId
                    savedStateHandle[KEY_PENDING_ORIGINAL_REMINDER_DATE] = action.originalReminderDate?.toString()
                    savedStateHandle.remove<String>(KEY_PENDING_SCHEDULED_DATE)
                }

                is PendingNotificationAction.OpenHabit -> {
                    savedStateHandle[KEY_PENDING_ACTION_TYPE] = ACTION_TYPE_HABIT
                    savedStateHandle[KEY_PENDING_ACTION_ID] = action.habitId
                    savedStateHandle.remove<String>(KEY_PENDING_ORIGINAL_REMINDER_DATE)
                    savedStateHandle.remove<String>(KEY_PENDING_SCHEDULED_DATE)
                }

                is PendingNotificationAction.OpenHabitChain -> {
                    savedStateHandle[KEY_PENDING_ACTION_TYPE] = ACTION_TYPE_HABIT_CHAIN
                    savedStateHandle[KEY_PENDING_ACTION_ID] = action.chainId
                    savedStateHandle.remove<String>(KEY_PENDING_ORIGINAL_REMINDER_DATE)
                    if (action.scheduledDate != null) {
                        savedStateHandle[KEY_PENDING_SCHEDULED_DATE] = action.scheduledDate.toString()
                    } else {
                        savedStateHandle.remove<String>(KEY_PENDING_SCHEDULED_DATE)
                    }
                }
            }
            _pendingNotificationAction.value = action
        }

        fun consumePendingNotificationAction() {
            clearPendingNotificationAction()
            _pendingNotificationAction.value = null
        }

        private fun clearPendingNotificationAction() {
            savedStateHandle.remove<String>(KEY_PENDING_ACTION_TYPE)
            savedStateHandle.remove<Long>(KEY_PENDING_ACTION_ID)
            savedStateHandle.remove<String>(KEY_PENDING_ORIGINAL_REMINDER_DATE)
            savedStateHandle.remove<String>(KEY_PENDING_SCHEDULED_DATE)
        }

        private fun readPendingNotificationAction(): PendingNotificationAction? {
            val type = savedStateHandle.get<String>(KEY_PENDING_ACTION_TYPE)
            val id = savedStateHandle.get<Long>(KEY_PENDING_ACTION_ID)
            val action =
                if (type != null && id != null) {
                    when (type) {
                        ACTION_TYPE_TASK ->
                            PendingNotificationAction.OpenTask(
                                taskId = id,
                                originalReminderDate = readPendingOriginalReminderDate(),
                            )

                        ACTION_TYPE_HABIT -> PendingNotificationAction.OpenHabit(id)
                        ACTION_TYPE_HABIT_CHAIN ->
                            PendingNotificationAction.OpenHabitChain(
                                chainId = id,
                                scheduledDate = readPendingScheduledDate(),
                            )

                        else -> null
                    }
                } else {
                    null
                }

            if (action == null && (type != null || id != null)) {
                clearPendingNotificationAction()
            }
            return action
        }

        private fun readPendingOriginalReminderDate(): LocalDateTime? {
            val reminderDate = savedStateHandle.get<String>(KEY_PENDING_ORIGINAL_REMINDER_DATE)
            return reminderDate?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
        }

        private fun readPendingScheduledDate(): LocalDate? {
            val scheduledDate = savedStateHandle.get<String>(KEY_PENDING_SCHEDULED_DATE)
            return scheduledDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        }

        private companion object {
            const val KEY_PENDING_ACTION_TYPE = "pending_notification_action_type"
            const val KEY_PENDING_ACTION_ID = "pending_notification_action_id"
            const val KEY_PENDING_ORIGINAL_REMINDER_DATE = "pending_original_reminder_date"
            const val KEY_PENDING_SCHEDULED_DATE = "pending_scheduled_date"

            const val ACTION_TYPE_TASK = "task"
            const val ACTION_TYPE_HABIT = "habit"
            const val ACTION_TYPE_HABIT_CHAIN = "habit_chain"
        }
    }
