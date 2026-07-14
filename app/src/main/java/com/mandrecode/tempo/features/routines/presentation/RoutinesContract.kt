package com.mandrecode.tempo.features.routines.presentation

import androidx.annotation.StringRes
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.ui.model.PermissionInfo
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Contract for Routines screen following MVI pattern.
 */
object RoutinesContract {
    /**
     * UI State for Routines screen.
     */
    data class UiState(
        val habits: ImmutableList<Habit> = persistentListOf(),
        val habitChains: ImmutableList<HabitChain> = persistentListOf(),
        val scheduledTimelineItems: ImmutableList<TimelineItem> = persistentListOf(),
        val unscheduledTimelineItems: ImmutableList<TimelineItem> = persistentListOf(),
        val isLoading: Boolean = true,
        val habitForm: HabitFormState = HabitFormState(),
        val habitToDelete: Habit? = null,
        val habitChainToDelete: HabitChain? = null,
        val permissionInfo: PermissionInfo = PermissionInfo(),
        val showDeleteHabitConfirmationDialog: Boolean = false,
        val showDeleteHabitChainConfirmationDialog: Boolean = false,
        val showClearRemindersConfirmationDialog: Boolean = false,
        val showEmptyHabitChainConfirmationDialog: Boolean = false,
        val showPermissionRequestDialog: Boolean = false,
        val showPermissionRevokedDialog: Boolean = false,
        val habitsWithRemindersToBeCleared: ImmutableList<Habit> = persistentListOf(),
        val pendingHabitChainData: PendingHabitChainData? = null,
        val expandedChainIds: PersistentSet<Long> = persistentSetOf(),
        val selectedDate: LocalDate =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date,
    )

    data class HabitFormState(
        val isVisible: Boolean = false,
        val editingHabit: Habit? = null,
        val editingHabitChain: HabitChain? = null,
        val targetChainId: Long? = null,
        val reminderDate: LocalDateTime? = null,
        val selectedColorKey: String? = null,
        val selectedIcon: String? = null,
        val selectedTab: HabitSheetTab = HabitSheetTab.HABIT,
        val selectedHabitType: HabitType = HabitType.BUILD,
        val shouldAutoSelectColor: Boolean = false,
        val selectedRepeatDays: PersistentSet<DayOfWeek>? = null,
        val shouldAutoSelectIcon: Boolean = false,
        val quitDefaultReminderApplied: Boolean = false,
        val quitClearedRepeatDays: PersistentSet<DayOfWeek>? = null,
        val quitRepeatDaysCleared: Boolean = false,
        @StringRes val titleError: Int? = null,
        @StringRes val descriptionError: Int? = null,
    )

    /**
     * UI Events that can be triggered from the Routines screen.
     */
    sealed interface UiEvent {
        // Habit CRUD
        data class ShowHabitBottomSheet(
            val habit: Habit? = null,
            val tab: HabitSheetTab = HabitSheetTab.HABIT,
            val chainId: Long? = null,
        ) : UiEvent

        data class ShowHabitChainBottomSheet(
            val habitChain: HabitChain? = null,
        ) : UiEvent

        data object HideHabitBottomSheet : UiEvent

        data class SetSelectedTab(
            val tab: HabitSheetTab,
        ) : UiEvent

        data class SetHabitType(
            val habitType: HabitType,
        ) : UiEvent

        data class CreateOrUpdateHabit(
            val title: String,
            val description: String,
            val autoSave: Boolean = false,
        ) : UiEvent

        data class CreateOrUpdateHabitChain(
            val title: String,
            val description: String,
            val habitIds: ImmutableList<Long>,
            val autoSave: Boolean = false,
        ) : UiEvent {
            constructor(
                title: String,
                description: String,
                habitIds: List<Long>,
                autoSave: Boolean = false,
            ) : this(title, description, habitIds.toPersistentList(), autoSave)
        }

        data class ToggleHabitCompletion(
            val habitId: Long,
            val isCompleted: Boolean,
        ) : UiEvent

        // Delete
        data class ShowDeleteHabitConfirmation(
            val habit: Habit,
        ) : UiEvent

        data object HideDeleteHabitConfirmation : UiEvent

        data object DeleteHabit : UiEvent

        data class ShowDeleteHabitChainConfirmation(
            val habitChain: HabitChain,
        ) : UiEvent

        data object HideDeleteHabitChainConfirmation : UiEvent

        data class DeleteHabitChain(
            val deleteHabits: Boolean = false,
        ) : UiEvent

        data object HideEmptyHabitChainConfirmation : UiEvent

        data object ConfirmDeleteEmptyHabitChain : UiEvent

        // Clear reminders
        data object HideClearRemindersConfirmation : UiEvent

        data object ConfirmClearRemindersAndProceed : UiEvent

        // Options
        data class SetReminder(
            val year: Int,
            val month: Int,
            val day: Int,
            val hour: Int,
            val minute: Int,
        ) : UiEvent

        data object ClearReminder : UiEvent

        data class SetColorKey(
            val colorKey: String,
        ) : UiEvent

        data object ClearColor : UiEvent

        data class SetIcon(
            val icon: String,
        ) : UiEvent

        data object ClearIcon : UiEvent

        data class SetRepeatDays(
            val days: Set<DayOfWeek>?,
        ) : UiEvent

        data object ClearHabitErrors : UiEvent

        // Navigation / date
        data class SelectDate(
            val date: LocalDate,
        ) : UiEvent

        data class ToggleChainExpanded(
            val chainId: Long,
        ) : UiEvent

        // Permissions
        data object DismissPermissionRequestDialog : UiEvent

        data object DismissPermissionRevokedDialog : UiEvent

        data object ConfirmClearAllHabitReminders : UiEvent

        data object OnPermissionsGranted : UiEvent

        data class UndoDeletion(
            val token: Long,
        ) : UiEvent

        data class DismissDeletionUndo(
            val token: Long,
        ) : UiEvent
    }

    /**
     * One-time UI Effects for Routines screen.
     */
    sealed interface UiEffect {
        data class ShowSnackbar(
            @StringRes val messageResId: Int,
            val formatArgs: List<Any> = emptyList(),
            @StringRes val actionResId: Int? = null,
            val deletionToken: Long? = null,
        ) : UiEffect
    }

    data class PendingHabitChainData(
        val title: String,
        val description: String,
        val habitIds: ImmutableList<Long>,
        val colorKey: String? = null,
        val icon: String? = null,
        val autoSave: Boolean = false,
    ) {
        constructor(
            title: String,
            description: String,
            habitIds: List<Long>,
            colorKey: String? = null,
            icon: String? = null,
            autoSave: Boolean = false,
        ) : this(
            title = title,
            description = description,
            habitIds = habitIds.toPersistentList(),
            colorKey = colorKey,
            icon = icon,
            autoSave = autoSave,
        )
    }

    enum class HabitSheetTab {
        HABIT,
        HABIT_CHAIN,
    }

    /**
     * Represents an item in the timeline. Stores only structural info (IDs + scheduling)
     * so that the list stays stable during data-only changes like completion toggles.
     */
    data class TimelineItem(
        val time: Int?,
        val isChain: Boolean,
        val chainId: Long? = null,
        val habitId: Long? = null,
    ) {
        init {
            require(
                if (isChain) {
                    chainId != null && habitId == null
                } else {
                    habitId != null && chainId == null
                },
            ) { "Invalid TimelineItem: isChain=$isChain, chainId=$chainId, habitId=$habitId" }
        }
    }
}
