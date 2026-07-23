package com.mandrecode.tempo.features.tasks.presentation

import androidx.annotation.StringRes
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.MonthDayOption
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.components.SnackbarBoldSegment
import com.mandrecode.tempo.core.ui.model.PermissionInfo
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.presentation.model.ActiveGroupKey
import com.mandrecode.tempo.features.tasks.presentation.model.CompletedGroupKey
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.LocalDateTime

/**
 * Contract for Tasks screen following MVI pattern.
 */
object TasksContract {
    /**
     * UI State for Tasks screen.
     */
    data class UiState(
        val tasks: ImmutableList<Task> = persistentListOf(),
        val categories: ImmutableList<Category> = persistentListOf(),
        val isLoading: Boolean = true,
        val selectedCategoryId: Long = 0L,
        val sortOption: SortOption = SortOption.MANUAL,
        val taskForm: TaskFormState = TaskFormState(),
        val categoryForm: CategoryFormState = CategoryFormState(),
        val taskToDelete: Task? = null,
        val taskToDeleteSubtasksCount: Int = 0,
        val categoryToDelete: Category? = null,
        val permissionInfo: PermissionInfo = PermissionInfo(),
        val showDeleteCategoryConfirmationDialog: Boolean = false,
        val showDeleteTaskConfirmationDialog: Boolean = false,
        val showDeleteCompletedConfirmationDialog: Boolean = false,
        val showPermissionRequestDialog: Boolean = false,
        val showPermissionRevokedDialog: Boolean = false,
        val expandedTaskIds: PersistentSet<Long> = persistentSetOf(),
        val showCompletedTasks: Boolean = true,
        val showSortBottomSheet: Boolean = false,
        val uncompletedTasksCounts: ImmutableMap<Long, Int> = persistentMapOf(),
        val activeTasks: ImmutableMap<ActiveGroupKey, ImmutableList<Task>> = persistentMapOf(),
        val completedTaskGroups: ImmutableMap<CompletedGroupKey, ImmutableList<Task>> = persistentMapOf(),
        val subtasksMap: ImmutableMap<Long, ImmutableList<Task>> = persistentMapOf(),
    )

    data class TaskFormState(
        val isVisible: Boolean = false,
        val editorSessionId: Long = 0L,
        val editingTask: Task? = null,
        val priority: Priority? = null,
        val reminderDate: LocalDateTime? = null,
        val periodicity: Periodicity? = null,
        val periodicityInterval: Int = 1,
        val repeatDays: PersistentSet<DayOfWeek>? = null,
        val monthDayOption: MonthDayOption? = null,
        val parentTaskId: Long? = null,
        val parentTask: Task? = null,
        @StringRes val titleError: Int? = null,
        @StringRes val descriptionError: Int? = null,
    )

    data class CategoryFormState(
        val isVisible: Boolean = false,
        val editingCategory: Category? = null,
        @StringRes val nameError: Int? = null,
    )

    /**
     * UI Events that can be triggered from the Tasks screen.
     */
    sealed interface UiEvent {
        // Category events
        data class CategorySelected(
            val categoryId: Long,
        ) : UiEvent

        data class ShowCategoryDialog(
            val category: Category? = null,
        ) : UiEvent

        data object HideCategoryDialog : UiEvent

        data class AddCategory(
            val name: String,
            val color: String? = null,
            val icon: String? = null,
        ) : UiEvent

        data class UpdateCategory(
            val category: Category,
        ) : UiEvent

        data class RequestDeleteCategory(
            val category: Category,
        ) : UiEvent

        data object CancelDeleteCategory : UiEvent

        data class DeleteCategory(
            val category: Category,
        ) : UiEvent

        data object ClearCategoryError : UiEvent

        data class SetDefaultCategory(
            val categoryId: Long,
        ) : UiEvent

        data class ReorderCategories(
            val fromIndex: Int,
            val toIndex: Int,
            val categories: List<Category>,
        ) : UiEvent

        // Task CRUD events
        data class ShowTaskDialog(
            val task: Task? = null,
            val parentTaskId: Long? = null,
        ) : UiEvent

        data object HideTaskDialog : UiEvent

        data class CreateOrUpdateTask(
            val title: String,
            val description: String,
            val categoryId: Long,
            val parentTaskId: Long? = null,
            val autoSave: Boolean = false,
        ) : UiEvent

        data class ToggleTaskCompletion(
            val task: Task,
        ) : UiEvent

        data class RequestDeleteTask(
            val task: Task,
        ) : UiEvent

        data object CancelDeleteTask : UiEvent

        data class ConfirmDeleteTask(
            val task: Task,
        ) : UiEvent

        data object ClearTaskErrors : UiEvent

        // Task options
        data class SetPriority(
            val priority: Priority,
        ) : UiEvent

        data object ClearPriority : UiEvent

        data class SetReminder(
            val year: Int,
            val month: Int,
            val day: Int,
            val hour: Int,
            val minute: Int,
        ) : UiEvent

        data object ClearReminder : UiEvent

        data class SetPeriodicity(
            val periodicity: Periodicity,
        ) : UiEvent

        data object ClearPeriodicity : UiEvent

        data class SetPeriodicityInterval(
            val interval: Int,
        ) : UiEvent

        data class SetRepeatDays(
            val days: Set<DayOfWeek>?,
        ) : UiEvent

        data class SetMonthDayOption(
            val option: MonthDayOption?,
        ) : UiEvent

        // Sort & display
        data class SetSortOption(
            val sortOption: SortOption,
        ) : UiEvent

        data object ShowSortMenu : UiEvent

        data object HideSortMenu : UiEvent

        data object ToggleCompletedTasksVisibility : UiEvent

        data class ToggleTaskExpanded(
            val taskId: Long,
        ) : UiEvent

        data class ReorderTasks(
            val fromIndex: Int,
            val toIndex: Int,
            val tasks: List<Task>,
        ) : UiEvent

        data class ReorderSubtasks(
            val fromIndex: Int,
            val toIndex: Int,
            val subtasks: List<Task>,
        ) : UiEvent

        // Completed tasks bulk delete
        data object RequestDeleteCompletedTasks : UiEvent

        data object CancelDeleteCompletedTasks : UiEvent

        data object ConfirmDeleteCompletedTasks : UiEvent

        // Permissions
        data object DismissPermissionRequestDialog : UiEvent

        data object DismissPermissionRevokedDialog : UiEvent

        data object ConfirmClearAllReminders : UiEvent

        data object OnPermissionsGranted : UiEvent

        data class UndoDeletion(
            val token: Long,
        ) : UiEvent

        data class DismissDeletionUndo(
            val token: Long,
        ) : UiEvent
    }

    /**
     * One-time UI Effects for Tasks screen.
     */
    sealed interface UiEffect {
        data class ShowSnackbar(
            @StringRes val messageResId: Int? = null,
            val formatArgs: List<Any> = emptyList(),
            val boldSegment: SnackbarBoldSegment? = null,
            @StringRes val actionResId: Int? = null,
            val deletionToken: Long? = null,
        ) : UiEffect {
            init {
                require((messageResId != null) != (boldSegment != null)) {
                    "ShowSnackbar requires exactly one of messageResId or boldSegment"
                }
                require(boldSegment == null || formatArgs.isEmpty()) {
                    "ShowSnackbar.formatArgs is ignored when boldSegment is set and must be empty"
                }
            }
        }
    }
}
