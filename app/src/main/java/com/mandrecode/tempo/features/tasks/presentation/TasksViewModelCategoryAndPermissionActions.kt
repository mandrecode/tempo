package com.mandrecode.tempo.features.tasks.presentation

import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.model.PermissionInfo
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.usecase.CreateCategoryUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.DeleteCategoryUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.UpdateCategoryUseCase
import com.mandrecode.tempo.features.tasks.presentation.TasksContract.CategoryFormState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal fun TasksViewModel.showCategoryDialog(category: Category? = null) {
    mutableUiState.update {
        it.copy(
            categoryForm =
                CategoryFormState(
                    isVisible = true,
                    editingCategory = category,
                ),
        )
    }
}

internal fun TasksViewModel.hideCategoryDialog() {
    mutableUiState.update {
        it.copy(categoryForm = CategoryFormState())
    }
}

internal fun TasksViewModel.addCategory(
    name: String,
    color: String?,
    icon: String?,
) {
    viewModelScope.launch {
        try {
            when (val result = createCategoryUseCase(name, color, icon)) {
                is CreateCategoryUseCase.Result.EmptyName -> {
                    mutableUiState.update { it.copy(categoryForm = it.categoryForm.copy(nameError = R.string.msg_category_name_empty)) }
                }

                is CreateCategoryUseCase.Result.TooLong -> {
                    mutableUiState.update {
                        it.copy(
                            categoryForm = it.categoryForm.copy(nameError = R.string.error_category_name_too_long),
                        )
                    }
                }

                is CreateCategoryUseCase.Result.AlreadyExists -> {
                    mutableUiState.update { it.copy(categoryForm = CategoryFormState()) }
                    showSnackbar(R.string.msg_category_exists, listOf(result.name))
                }

                is CreateCategoryUseCase.Result.Success -> {
                    mutableUiState.update { it.copy(categoryForm = CategoryFormState()) }
                    showSnackbar(R.string.msg_category_added_success, listOf(result.name))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            showSnackbar(
                R.string.msg_category_add_failed,
                listOf(e.toUserFacingMessage()),
            )
        }
    }
}

internal fun TasksViewModel.updateCategory(category: Category) {
    viewModelScope.launch {
        try {
            when (val result = updateCategoryUseCase(category)) {
                is UpdateCategoryUseCase.Result.EmptyName -> {
                    mutableUiState.update { it.copy(categoryForm = it.categoryForm.copy(nameError = R.string.msg_category_name_empty)) }
                }

                is UpdateCategoryUseCase.Result.TooLong -> {
                    mutableUiState.update {
                        it.copy(
                            categoryForm = it.categoryForm.copy(nameError = R.string.error_category_name_too_long),
                        )
                    }
                }

                is UpdateCategoryUseCase.Result.AlreadyExists -> {
                    mutableUiState.update { it.copy(categoryForm = CategoryFormState()) }
                    showSnackbar(R.string.msg_category_exists, listOf(result.name))
                }

                is UpdateCategoryUseCase.Result.Success -> {
                    if (category.isDefault) {
                        setDefaultCategoryUseCase(category.id)
                    }
                    mutableUiState.update {
                        it.copy(categoryForm = CategoryFormState())
                    }
                    showSnackbar(R.string.msg_category_updated_success)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            showSnackbar(
                R.string.msg_category_update_failed,
                listOf(e.toUserFacingMessage()),
            )
        }
    }
}

internal fun TasksViewModel.requestDeleteCategory(category: Category) {
    mutableUiState.update {
        it.copy(
            showDeleteCategoryConfirmationDialog = true,
            categoryToDelete = category,
        )
    }
}

internal fun TasksViewModel.cancelDeleteCategory() {
    mutableUiState.update {
        it.copy(
            showDeleteCategoryConfirmationDialog = false,
            categoryToDelete = null,
        )
    }
}

internal fun TasksViewModel.deleteCategory(category: Category) {
    val selectedCategoryIdBeforeDeletion = mutableUiState.value.selectedCategoryId
    viewModelScope.launch {
        try {
            when (val result = deleteCategoryUseCase(category)) {
                is DeleteCategoryUseCase.Result.CannotDeleteDefault -> {
                    mutableUiState.update {
                        it.copy(
                            showDeleteCategoryConfirmationDialog = false,
                            categoryToDelete = null,
                        )
                    }
                    showSnackbar(R.string.msg_cannot_delete_default_category)
                }

                is DeleteCategoryUseCase.Result.LastCategory -> {
                    mutableUiState.update {
                        it.copy(
                            showDeleteCategoryConfirmationDialog = false,
                            categoryToDelete = null,
                        )
                    }
                    showSnackbar(R.string.msg_cannot_delete_last_category)
                }

                is DeleteCategoryUseCase.Result.Success -> {
                    val fallbackId =
                        if (selectedCategoryIdBeforeDeletion != category.id) {
                            selectedCategoryIdBeforeDeletion
                        } else {
                            mutableUiState.value.categories
                                .firstOrNull { it.id != category.id }
                                ?.id
                                ?: 0L
                        }
                    val sortOption =
                        tasksScreenPreferencesRepository.getSortOption(fallbackId)
                    mutableUiState.update {
                        it.copy(
                            showDeleteCategoryConfirmationDialog = false,
                            categoryToDelete = null,
                            categoryForm = CategoryFormState(),
                            selectedCategoryId = fallbackId,
                            sortOption = sortOption,
                        )
                    }
                    val token =
                        storePendingDeletion(
                            PendingTaskDeletion.Category(
                                snapshot = result.snapshot,
                                selectedCategoryIdBeforeDeletion = selectedCategoryIdBeforeDeletion,
                            ),
                        )
                    showSnackbar(
                        messageResId = R.string.msg_category_deleted_success,
                        formatArgs = listOf(category.name),
                        actionResId = R.string.undo,
                        deletionToken = token,
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            mutableUiState.update {
                it.copy(
                    showDeleteCategoryConfirmationDialog = false,
                    categoryToDelete = null,
                )
            }
            showSnackbar(
                R.string.msg_category_delete_failed,
                listOf(e.toUserFacingMessage()),
            )
        }
    }
}

internal fun TasksViewModel.clearCategoryError() {
    mutableUiState.update { it.copy(categoryForm = it.categoryForm.copy(nameError = null)) }
}

internal fun TasksViewModel.setDefaultCategory(categoryId: Long) {
    viewModelScope.launch {
        try {
            setDefaultCategoryUseCase(categoryId)
            mutableUiState.update { state ->
                val updatedCategory =
                    state.categoryForm.editingCategory?.copy(isDefault = true)
                state.copy(
                    categoryForm =
                        state.categoryForm.copy(editingCategory = updatedCategory),
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            showSnackbar(
                R.string.msg_category_update_failed,
                listOf(e.toUserFacingMessage()),
            )
        }
    }
}

internal fun TasksViewModel.reorderCategories(
    fromIndex: Int,
    toIndex: Int,
    categories: List<Category>,
) {
    viewModelScope.launch {
        try {
            reorderCategoriesUseCase(fromIndex, toIndex, categories)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            showSnackbar(
                R.string.msg_category_update_failed,
                listOf(e.toUserFacingMessage()),
            )
        }
    }
}

internal fun TasksViewModel.toggleCompletedTasksVisibility() {
    val newValue = !mutableUiState.value.showCompletedTasks
    tasksScreenPreferencesRepository.setShowCompletedTasks(newValue)
    mutableUiState.update { it.copy(showCompletedTasks = newValue) }
}

internal fun TasksViewModel.requestDeleteCompletedTasks() {
    mutableUiState.update {
        it.copy(showDeleteCompletedConfirmationDialog = true)
    }
}

internal fun TasksViewModel.cancelDeleteCompletedTasks() {
    mutableUiState.update {
        it.copy(showDeleteCompletedConfirmationDialog = false)
    }
}

internal fun TasksViewModel.confirmDeleteCompletedTasks() {
    viewModelScope.launch {
        try {
            val categoryId = mutableUiState.value.selectedCategoryId
            val snapshot = deleteCompletedTasksUseCase(categoryId)
            mutableUiState.update {
                it.copy(showDeleteCompletedConfirmationDialog = false)
            }
            val token = storePendingDeletion(PendingTaskDeletion.Tasks(snapshot))
            showSnackbar(
                messageResId = R.string.msg_completed_tasks_deleted_success,
                actionResId = R.string.undo,
                deletionToken = token,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            mutableUiState.update {
                it.copy(showDeleteCompletedConfirmationDialog = false)
            }
            showSnackbar(
                R.string.msg_completed_tasks_delete_failed,
                listOf(e.toUserFacingMessage()),
            )
        }
    }
}

internal fun TasksViewModel.showSortMenu() {
    mutableUiState.update { it.copy(showSortBottomSheet = true) }
}

internal fun TasksViewModel.hideSortMenu() {
    mutableUiState.update { it.copy(showSortBottomSheet = false) }
}

internal fun TasksViewModel.dismissPermissionRequestDialog() {
    mutableUiState.update { it.copy(showPermissionRequestDialog = false) }
}

internal fun TasksViewModel.onPermissionsGranted() {
    viewModelScope.launch {
        showSnackbar(R.string.msg_all_set_reminders_active)
    }
}

internal fun TasksViewModel.checkPermissionsAndSyncRemindersInternal() {
    val wasGrantedBefore = uiState.value.permissionInfo.areAllGranted

    val hasNotifications = permissionChecker.hasNotificationPermissions()
    val canScheduleAlarms = permissionChecker.canScheduleExactAlarms()
    val permissionInfo = PermissionInfo(hasNotifications, canScheduleAlarms)

    mutableUiState.update { it.copy(permissionInfo = permissionInfo) }

    if (!permissionInfo.areAllGranted) {
        viewModelScope.launch {
            val tasksWithReminders = taskRepository.getTasksWithReminders()
            if (tasksWithReminders.isNotEmpty()) {
                mutableUiState.update {
                    it.copy(showPermissionRevokedDialog = true)
                }
            }
        }
    } else {
        if (!wasGrantedBefore) {
            onPermissionsGranted()
        }
    }
}

internal fun TasksViewModel.confirmClearAllReminders() {
    viewModelScope.launch {
        clearAllRemindersUseCase()
        mutableUiState.update {
            it.copy(showPermissionRevokedDialog = false)
        }
        showSnackbar(R.string.msg_reminders_cleared)
    }
}

internal fun TasksViewModel.dismissPermissionRevokedDialog() {
    mutableUiState.update { it.copy(showPermissionRevokedDialog = false) }
}
