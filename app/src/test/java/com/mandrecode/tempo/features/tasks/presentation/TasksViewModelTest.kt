package com.mandrecode.tempo.features.tasks.presentation

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.data.preferences.TasksScreenPreferencesRepository
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.MonthDayOption
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.domain.model.RestoreResult
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.DEFAULT_INBOX_CATEGORY
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.TaskDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.usecase.ClearAllTaskRemindersUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.CreateCategoryUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.CreateTaskUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.DeleteCategoryUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.DeleteCompletedTasksUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.DeleteTaskUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.ReorderCategoriesUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.ReorderTasksUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.RestoreDeletedCategoryUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.RestoreDeletedTasksUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.SetDefaultCategoryUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.ToggleTaskCompletionUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.UpdateCategoryUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.UpdateTaskUseCase
import com.mandrecode.tempo.features.tasks.presentation.model.ActiveGroupKey
import com.mandrecode.tempo.features.tasks.presentation.model.CompletedGroupKey
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption
import com.mandrecode.tempo.infrastructure.permissions.PermissionChecker
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {
    private lateinit var viewModel: TasksViewModel
    private lateinit var taskRepository: TaskRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var createTaskUseCase: CreateTaskUseCase
    private lateinit var updateTaskUseCase: UpdateTaskUseCase
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase
    private lateinit var toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase
    private lateinit var createCategoryUseCase: CreateCategoryUseCase
    private lateinit var updateCategoryUseCase: UpdateCategoryUseCase
    private lateinit var deleteCategoryUseCase: DeleteCategoryUseCase
    private lateinit var setDefaultCategoryUseCase: SetDefaultCategoryUseCase
    private lateinit var reorderCategoriesUseCase: ReorderCategoriesUseCase
    private lateinit var deleteCompletedTasksUseCase: DeleteCompletedTasksUseCase
    private lateinit var clearAllRemindersUseCase: ClearAllTaskRemindersUseCase
    private lateinit var reorderTasksUseCase: ReorderTasksUseCase
    private lateinit var permissionChecker: PermissionChecker
    private lateinit var tasksScreenPreferencesRepository: TasksScreenPreferencesRepository
    private lateinit var restoreDeletedTasksUseCase: RestoreDeletedTasksUseCase
    private lateinit var restoreDeletedCategoryUseCase: RestoreDeletedCategoryUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        taskRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        createTaskUseCase = mockk(relaxed = true)
        updateTaskUseCase = mockk(relaxed = true)
        deleteTaskUseCase = mockk(relaxed = true)
        toggleTaskCompletionUseCase = mockk(relaxed = true)
        createCategoryUseCase = mockk(relaxed = true)
        updateCategoryUseCase = mockk(relaxed = true)
        deleteCategoryUseCase = mockk(relaxed = true)
        setDefaultCategoryUseCase = mockk(relaxed = true)
        reorderCategoriesUseCase = mockk(relaxed = true)
        deleteCompletedTasksUseCase = mockk(relaxed = true)
        clearAllRemindersUseCase = mockk(relaxed = true)
        reorderTasksUseCase = mockk(relaxed = true)
        permissionChecker = mockk(relaxed = true)
        tasksScreenPreferencesRepository = mockk(relaxed = true)
        restoreDeletedTasksUseCase = mockk(relaxed = true)
        restoreDeletedCategoryUseCase = mockk(relaxed = true)

        coEvery { taskRepository.getAllTasks() } returns flowOf(emptyList())
        coEvery { categoryRepository.getAllCategories() } returns flowOf(emptyList())
        every { tasksScreenPreferencesRepository.getSortOption(any()) } returns SortOption.MANUAL
        every { tasksScreenPreferencesRepository.getShowCompletedTasks() } returns true

        viewModel =
            createViewModel()
    }

    private fun createViewModel() =
        TasksViewModel(
            taskRepository,
            categoryRepository,
            createTaskUseCase,
            updateTaskUseCase,
            deleteTaskUseCase,
            toggleTaskCompletionUseCase,
            createCategoryUseCase,
            updateCategoryUseCase,
            deleteCategoryUseCase,
            setDefaultCategoryUseCase,
            reorderCategoriesUseCase,
            deleteCompletedTasksUseCase,
            clearAllRemindersUseCase,
            reorderTasksUseCase,
            permissionChecker,
            tasksScreenPreferencesRepository,
            testDispatcher,
            restoreDeletedTasksUseCase,
            restoreDeletedCategoryUseCase,
        )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selectCategory updates selectedCategoryId`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.CategorySelected(5L))
            assertThat(viewModel.uiState.value.selectedCategoryId).isEqualTo(5L)
        }

    @Test
    fun `delete task stores tokenized snapshot and undo restores matching deletion`() =
        runTest {
            val task = Task(id = 9, title = "Delete", description = "")
            val snapshot = TaskDeletionSnapshot.TaskTree(task.id, listOf(task))
            coEvery { deleteTaskUseCase(task) } returns snapshot
            coEvery { restoreDeletedTasksUseCase(snapshot) } returns RestoreResult(emptyList())

            viewModel.onEvent(TasksContract.UiEvent.ConfirmDeleteTask(task))
            advanceUntilIdle()
            val token = viewModel.pendingDeletionSnapshots.keys.single()

            viewModel.onEvent(TasksContract.UiEvent.UndoDeletion(token))
            advanceUntilIdle()

            coVerify { restoreDeletedTasksUseCase(snapshot) }
            assertThat(viewModel.pendingDeletionSnapshots).isEmpty()
        }

    @Test
    fun `failed task undo retains snapshot until failure snackbar is dismissed`() =
        runTest {
            val snapshot =
                TaskDeletionSnapshot.TaskTree(
                    rootTaskId = 9L,
                    tasks = listOf(Task(id = 9L, title = "Delete", description = "")),
                )
            val token = viewModel.storePendingDeletion(PendingTaskDeletion.Tasks(snapshot))
            coEvery { restoreDeletedTasksUseCase(snapshot) } throws IllegalStateException("Restore failed")
            val effects = mutableListOf<TasksContract.UiEffect>()
            backgroundScope.launch { viewModel.uiEffect.toList(effects) }

            viewModel.onEvent(TasksContract.UiEvent.UndoDeletion(token))
            advanceUntilIdle()

            assertThat(viewModel.pendingDeletionSnapshots).containsKey(token)
            assertThat(effects)
                .contains(
                    TasksContract.UiEffect.ShowSnackbar(
                        messageResId = R.string.msg_undo_failed,
                        deletionToken = token,
                    ),
                )

            viewModel.onEvent(TasksContract.UiEvent.DismissDeletionUndo(token))

            assertThat(viewModel.pendingDeletionSnapshots).doesNotContainKey(token)
        }

    @Test
    fun `dismiss undo removes only matching deletion token`() =
        runTest {
            val first = TaskDeletionSnapshot.TaskTree(1, listOf(Task(id = 1, title = "One", description = "")))
            val second = TaskDeletionSnapshot.TaskTree(2, listOf(Task(id = 2, title = "Two", description = "")))
            val firstToken = viewModel.storePendingDeletion(PendingTaskDeletion.Tasks(first))
            val secondToken = viewModel.storePendingDeletion(PendingTaskDeletion.Tasks(second))

            viewModel.onEvent(TasksContract.UiEvent.DismissDeletionUndo(firstToken))

            assertThat(viewModel.pendingDeletionSnapshots).containsKey(secondToken)
            assertThat(viewModel.pendingDeletionSnapshots).doesNotContainKey(firstToken)
        }

    @Test
    fun `selectCategory persists selected category id`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.CategorySelected(5L))
            verify { tasksScreenPreferencesRepository.setSelectedCategoryId(5L) }
        }

    @Test
    fun `falls back to Inbox when selected category is removed`() =
        runTest {
            val category = Category(id = 10L, name = "Work")
            coEvery { categoryRepository.getAllCategories() } returns
                flowOf(listOf(category))
            every {
                tasksScreenPreferencesRepository.getSelectedCategoryId()
            } returns 10L

            val vm =
                createViewModel()
            advanceUntilIdle()
            assertThat(vm.uiState.value.selectedCategoryId).isEqualTo(10L)

            // Category removed
            coEvery { categoryRepository.getAllCategories() } returns flowOf(emptyList())
            val vm2 =
                createViewModel()
            advanceUntilIdle()
            assertThat(vm2.uiState.value.selectedCategoryId).isEqualTo(0L)
            verify { tasksScreenPreferencesRepository.setSelectedCategoryId(0L) }
        }

    @Test
    fun `setSortOption updates sortOption`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.SetSortOption(SortOption.BY_DATE))
            assertThat(viewModel.uiState.value.sortOption).isEqualTo(SortOption.BY_DATE)
        }

    @Test
    fun `createOrUpdateTask shows error when title is too long`() =
        runTest {
            val longTitle = "a".repeat(ValidationUtils.MAX_TITLE_LENGTH + 1)
            viewModel.onEvent(TasksContract.UiEvent.CreateOrUpdateTask(longTitle, "desc", 1L))

            assertThat(viewModel.uiState.value.taskForm.titleError).isEqualTo(R.string.error_task_title_too_long)
            coVerify(exactly = 0) { createTaskUseCase.invoke(any()) }
        }

    @Test
    fun `createOrUpdateTask shows error when title is empty`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.CreateOrUpdateTask("", "desc", 1L))

            assertThat(viewModel.uiState.value.taskForm.titleError).isEqualTo(R.string.task_title_required)
            coVerify(exactly = 0) { createTaskUseCase.invoke(any()) }
        }

    @Test
    fun `addTask saves task and schedules reminder`() =
        runTest {
            coEvery { createTaskUseCase.invoke(any()) } returns
                CreateTaskUseCase.Result.Success(
                    100L,
                    ScheduleResult.Success(LocalDateTime(2030, 1, 1, 10, 0)),
                )

            // Set up reminder in state first
            viewModel.onEvent(TasksContract.UiEvent.SetReminder(2030, 1, 1, 10, 0))
            viewModel.onEvent(TasksContract.UiEvent.CreateOrUpdateTask("New Task", "Desc", 1L))
            advanceUntilIdle()

            coVerify { createTaskUseCase.invoke(any()) }
        }

    @Test
    fun `updateTask updates repository and scheduler`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Updated Task",
                    description = "Desc",
                    categoryId = 1L,
                    reminderDate = LocalDateTime(2030, 1, 1, 10, 0),
                )

            coEvery { updateTaskUseCase.invoke(any()) } returns
                UpdateTaskUseCase.Result.Success(
                    ScheduleResult.Success(task.reminderDate!!),
                )

            // Show dialog with existing task, then save
            viewModel.onEvent(TasksContract.UiEvent.ShowTaskDialog(task = task))
            advanceUntilIdle()
            viewModel.onEvent(TasksContract.UiEvent.SetReminder(2030, 1, 1, 10, 0))
            viewModel.onEvent(TasksContract.UiEvent.CreateOrUpdateTask("Updated Task", "Desc", 1L))
            advanceUntilIdle()

            coVerify { updateTaskUseCase.invoke(any()) }
        }

    @Test
    fun `createOrUpdateTask with autoSave keeps dialog open and emits no success snackbar`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Updated Task",
                    description = "Desc",
                    categoryId = 1L,
                )

            coEvery { updateTaskUseCase.invoke(any()) } returns
                UpdateTaskUseCase.Result.Success(ScheduleResult.Skipped)

            val effects = mutableListOf<TasksContract.UiEffect>()
            backgroundScope.launch { viewModel.uiEffect.toList(effects) }

            viewModel.onEvent(TasksContract.UiEvent.ShowTaskDialog(task = task))
            advanceUntilIdle()
            viewModel.onEvent(
                TasksContract.UiEvent.CreateOrUpdateTask("Updated Task", "Desc", 1L, autoSave = true),
            )
            advanceUntilIdle()

            coVerify { updateTaskUseCase.invoke(any()) }
            assertThat(viewModel.uiState.value.taskForm.isVisible).isTrue()
            assertThat(
                effects
                    .filterIsInstance<TasksContract.UiEffect.ShowSnackbar>()
                    .map { it.messageResId },
            ).doesNotContain(R.string.msg_task_updated_success)
        }

    @Test
    fun `updating completed task preserves stripped recurrence fields`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Archived periodic",
                    description = "Desc",
                    categoryId = 1L,
                    isCompleted = true,
                    reminderDate = null,
                    periodicity = null,
                    periodicityInterval = 1,
                    repeatDays = null,
                    monthDayOption = null,
                )
            val updatedTaskSlot = slot<Task>()
            coEvery { updateTaskUseCase.invoke(capture(updatedTaskSlot)) } returns
                UpdateTaskUseCase.Result.Success(ScheduleResult.Skipped)

            viewModel.onEvent(TasksContract.UiEvent.ShowTaskDialog(task = task))
            advanceUntilIdle()
            viewModel.onEvent(TasksContract.UiEvent.SetReminder(2030, 1, 1, 10, 0))
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.DAILY))
            viewModel.onEvent(TasksContract.UiEvent.CreateOrUpdateTask("Renamed", "Updated desc", 1L))
            advanceUntilIdle()

            assertThat(updatedTaskSlot.captured.title).isEqualTo("Renamed")
            assertThat(updatedTaskSlot.captured.description).isEqualTo("Updated desc")
            assertThat(updatedTaskSlot.captured.isCompleted).isTrue()
            assertThat(updatedTaskSlot.captured.reminderDate).isNull()
            assertThat(updatedTaskSlot.captured.periodicity).isNull()
            assertThat(updatedTaskSlot.captured.periodicityInterval).isEqualTo(1)
            assertThat(updatedTaskSlot.captured.repeatDays).isNull()
            assertThat(updatedTaskSlot.captured.monthDayOption).isNull()
        }

    @Test
    fun `deleteTask cancels reminder and deletes from repo`() =
        runTest {
            val task = Task(id = 1, title = "Delete me", description = "")

            viewModel.onEvent(TasksContract.UiEvent.ConfirmDeleteTask(task))
            advanceUntilIdle()

            coVerify { deleteTaskUseCase.invoke(task) }
        }

    @Test
    fun `reorderTasks performs batch update`() =
        runTest {
            val task1 = Task(id = 1, title = "Task 1", description = "", sortOrder = 1)
            val task2 = Task(id = 2, title = "Task 2", description = "", sortOrder = 2)
            val task3 = Task(id = 3, title = "Task 3", description = "", sortOrder = 3)
            val tasks = listOf(task1, task2, task3)

            // Move Task 1 (index 0) to end (index 2) -> 2, 3, 1
            viewModel.onEvent(TasksContract.UiEvent.ReorderTasks(0, 2, tasks))
            advanceUntilIdle()

            coVerify { reorderTasksUseCase.invoke(0, 2, any()) }
        }

    @Test
    fun `reorderSubtasks delegates to reorder use case`() =
        runTest {
            val subtask1 = Task(id = 10, title = "Sub 1", description = "", parentTaskId = 1, sortOrder = 0)
            val subtask2 = Task(id = 11, title = "Sub 2", description = "", parentTaskId = 1, sortOrder = 1)
            val subtask3 = Task(id = 12, title = "Sub 3", description = "", parentTaskId = 1, sortOrder = 2)
            val subtasks = listOf(subtask1, subtask2, subtask3)

            viewModel.onEvent(TasksContract.UiEvent.ReorderSubtasks(0, 2, subtasks))
            advanceUntilIdle()

            coVerify { reorderTasksUseCase.invoke(0, 2, any()) }
        }

    @Test
    fun `toggleTaskCompletion for recurring task creates new instance`() =
        runTest {
            val reminderDate = LocalDateTime(2024, 1, 1, 10, 0)
            val task =
                Task(
                    id = 1,
                    title = "Recurring",
                    description = "",
                    isCompleted = false,
                    reminderDate = reminderDate,
                    periodicity = Periodicity.DAILY,
                )

            coEvery { toggleTaskCompletionUseCase.invoke(task) } returns
                ToggleTaskCompletionUseCase.Result.PeriodicCompleted(
                    UpdateTaskUseCase.Result.Success(ScheduleResult.Skipped),
                )

            viewModel.onEvent(TasksContract.UiEvent.ToggleTaskCompletion(task))
            advanceUntilIdle()

            coVerify { toggleTaskCompletionUseCase.invoke(task) }
        }

    @Test
    fun `showTaskDialog with parentTaskId loads parent task`() =
        runTest {
            val parentTask = Task(id = 10L, title = "Parent", description = "")
            coEvery { taskRepository.getTaskById(10L) } returns parentTask

            viewModel.onEvent(TasksContract.UiEvent.ShowTaskDialog(parentTaskId = 10L))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.taskForm.isVisible).isTrue()
            assertThat(viewModel.uiState.value.taskForm.parentTaskId).isEqualTo(10L)
            assertThat(viewModel.uiState.value.taskForm.parentTask).isEqualTo(parentTask)
        }

    @Test
    fun `onSetPriority updates uiState`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.SetPriority(Priority.HIGH))
            assertThat(viewModel.uiState.value.taskForm.priority).isEqualTo(Priority.HIGH)
        }

    @Test
    fun `onClearPriority resets priority in uiState`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.SetPriority(Priority.HIGH))
            viewModel.onEvent(TasksContract.UiEvent.ClearPriority)
            assertThat(viewModel.uiState.value.taskForm.priority).isNull()
        }

    @Test
    fun `onSetPeriodicity updates uiState`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.WEEKLY))
            assertThat(viewModel.uiState.value.taskForm.periodicity).isEqualTo(Periodicity.WEEKLY)
        }

    @Test
    fun `onSetPeriodicity preserves existing sub-fields in form state`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.WEEKLY))
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicityInterval(3))
            viewModel.onEvent(
                TasksContract.UiEvent.SetRepeatDays(setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)),
            )

            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.MONTHLY))

            val form = viewModel.uiState.value.taskForm
            assertThat(form.periodicity).isEqualTo(Periodicity.MONTHLY)
            assertThat(form.periodicityInterval).isEqualTo(3)
            assertThat(form.repeatDays).isEqualTo(setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY))
        }

    @Test
    fun `onSetPeriodicity weekly does not overwrite existing repeatDays`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.SetReminder(2026, 4, 10, 10, 0))
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.WEEKLY))
            viewModel.onEvent(
                TasksContract.UiEvent.SetRepeatDays(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)),
            )

            // Switch away and back
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.MONTHLY))
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.WEEKLY))

            val form = viewModel.uiState.value.taskForm
            assertThat(form.repeatDays).isEqualTo(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        }

    @Test
    fun `onSetPeriodicity weekly pre-selects reminder day of week`() =
        runTest {
            // Friday 10 Apr 2026
            viewModel.onEvent(TasksContract.UiEvent.SetReminder(2026, 4, 10, 10, 0))
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.WEEKLY))

            val form = viewModel.uiState.value.taskForm
            assertThat(form.repeatDays).isEqualTo(setOf(DayOfWeek.FRIDAY))
        }

    @Test
    fun `onSetPeriodicity weekly without reminder leaves repeatDays null`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.WEEKLY))

            assertThat(viewModel.uiState.value.taskForm.repeatDays).isNull()
        }

    @Test
    fun `onSetPeriodicity hourly clears repeatDays and monthDayOption and resets interval`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.SetReminder(2026, 4, 10, 10, 0))
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.WEEKLY))
            viewModel.onEvent(
                TasksContract.UiEvent.SetRepeatDays(setOf(DayOfWeek.MONDAY)),
            )
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicityInterval(5))

            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.HOURLY))

            val form = viewModel.uiState.value.taskForm
            assertThat(form.periodicity).isEqualTo(Periodicity.HOURLY)
            assertThat(form.repeatDays).isNull()
            assertThat(form.monthDayOption).isNull()
            assertThat(form.periodicityInterval).isEqualTo(1)
        }

    @Test
    fun `onSetPeriodicity from hourly to daily resets interval to 1`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.HOURLY))
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicityInterval(20))

            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.DAILY))

            assertThat(viewModel.uiState.value.taskForm.periodicityInterval).isEqualTo(1)
        }

    @Test
    fun `onSetPeriodicityInterval clamps to 23 when periodicity is hourly`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.HOURLY))

            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicityInterval(99))

            assertThat(viewModel.uiState.value.taskForm.periodicityInterval).isEqualTo(23)
        }

    @Test
    fun `onSetPeriodicityInterval has no upper cap for non-hourly periodicity`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.DAILY))

            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicityInterval(99))

            assertThat(viewModel.uiState.value.taskForm.periodicityInterval).isEqualTo(99)
        }

    @Test
    fun `save sanitizes stale repeatDays when periodicity is monthly`() =
        runTest {
            coEvery { createTaskUseCase.invoke(any()) } returns
                CreateTaskUseCase.Result.Success(1L, ScheduleResult.Skipped)

            viewModel.onEvent(TasksContract.UiEvent.SetReminder(2026, 4, 10, 10, 0))
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.WEEKLY))
            viewModel.onEvent(
                TasksContract.UiEvent.SetRepeatDays(setOf(DayOfWeek.MONDAY)),
            )
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicityInterval(3))

            // Switch to monthly without touching sub-fields
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.MONTHLY))
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicityInterval(2))
            viewModel.onEvent(
                TasksContract.UiEvent.SetMonthDayOption(MonthDayOption.LAST_DAY),
            )

            viewModel.onEvent(
                TasksContract.UiEvent.CreateOrUpdateTask("Test", "Desc", 1L),
            )
            advanceUntilIdle()

            coVerify {
                createTaskUseCase.invoke(
                    match { task ->
                        task.periodicity == Periodicity.MONTHLY &&
                            task.periodicityInterval == 2 &&
                            task.repeatDays == null &&
                            task.monthDayOption == MonthDayOption.LAST_DAY
                    },
                )
            }
        }

    @Test
    fun `save sanitizes stale monthDayOption when periodicity is weekly`() =
        runTest {
            coEvery { createTaskUseCase.invoke(any()) } returns
                CreateTaskUseCase.Result.Success(1L, ScheduleResult.Skipped)

            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.MONTHLY))
            viewModel.onEvent(
                TasksContract.UiEvent.SetMonthDayOption(MonthDayOption.FIRST_DAY),
            )

            // Switch to weekly
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.WEEKLY))
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicityInterval(2))
            viewModel.onEvent(
                TasksContract.UiEvent.SetRepeatDays(setOf(DayOfWeek.TUESDAY)),
            )

            viewModel.onEvent(
                TasksContract.UiEvent.CreateOrUpdateTask("Test", "Desc", 1L),
            )
            advanceUntilIdle()

            coVerify {
                createTaskUseCase.invoke(
                    match { task ->
                        task.periodicity == Periodicity.WEEKLY &&
                            task.periodicityInterval == 2 &&
                            task.repeatDays == setOf(DayOfWeek.TUESDAY) &&
                            task.monthDayOption == null
                    },
                )
            }
        }

    @Test
    fun `save preserves interval for daily periodicity`() =
        runTest {
            coEvery { createTaskUseCase.invoke(any()) } returns
                CreateTaskUseCase.Result.Success(1L, ScheduleResult.Skipped)

            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.WEEKLY))
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicityInterval(5))

            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.DAILY))

            viewModel.onEvent(
                TasksContract.UiEvent.CreateOrUpdateTask("Test", "Desc", 1L),
            )
            advanceUntilIdle()

            coVerify {
                createTaskUseCase.invoke(
                    match { task ->
                        task.periodicity == Periodicity.DAILY &&
                            task.periodicityInterval == 5 &&
                            task.repeatDays == null &&
                            task.monthDayOption == null
                    },
                )
            }
        }

    @Test
    fun `toggleTaskExpanded updates expandedTaskIds set`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.ToggleTaskExpanded(1L))
            assertThat(viewModel.uiState.value.expandedTaskIds).contains(1L)

            viewModel.onEvent(TasksContract.UiEvent.ToggleTaskExpanded(1L))
            assertThat(viewModel.uiState.value.expandedTaskIds).doesNotContain(1L)
        }

    @Test
    fun `toggleCompletedTasksVisibility toggles showCompletedTasks`() =
        runTest {
            val initial = viewModel.uiState.value.showCompletedTasks
            viewModel.onEvent(TasksContract.UiEvent.ToggleCompletedTasksVisibility)
            assertThat(viewModel.uiState.value.showCompletedTasks).isEqualTo(!initial)
        }

    @Test
    fun `setSortOption persists sort option for current category`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.CategorySelected(5L))
            viewModel.onEvent(TasksContract.UiEvent.SetSortOption(SortOption.BY_PRIORITY))
            verify { tasksScreenPreferencesRepository.setSortOption(5L, SortOption.BY_PRIORITY) }
        }

    @Test
    fun `selectCategory restores persisted sort option`() =
        runTest {
            every { tasksScreenPreferencesRepository.getSortOption(7L) } returns SortOption.BY_TITLE
            viewModel.onEvent(TasksContract.UiEvent.CategorySelected(7L))
            assertThat(viewModel.uiState.value.sortOption).isEqualTo(SortOption.BY_TITLE)
        }

    @Test
    fun `toggleCompletedTasksVisibility persists value`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.ToggleCompletedTasksVisibility)
            verify { tasksScreenPreferencesRepository.setShowCompletedTasks(false) }
        }

    @Test
    fun `initial state reads persisted showCompletedTasks`() =
        runTest {
            every { tasksScreenPreferencesRepository.getShowCompletedTasks() } returns false
            val vm =
                createViewModel()
            assertThat(vm.uiState.value.showCompletedTasks).isFalse()
        }

    @Test
    fun `initial state reads persisted sort option for default category`() =
        runTest {
            every {
                tasksScreenPreferencesRepository.getSelectedCategoryId()
            } returns DEFAULT_INBOX_CATEGORY.id
            every {
                tasksScreenPreferencesRepository.getSortOption(DEFAULT_INBOX_CATEGORY.id)
            } returns SortOption.BY_DATE
            val vm =
                createViewModel()
            assertThat(vm.uiState.value.sortOption).isEqualTo(SortOption.BY_DATE)
        }

    @Test
    fun `clearTaskErrors removes error messages from uiState`() =
        runTest {
            // First set some errors (via a too long title)
            val longTitle = "a".repeat(ValidationUtils.MAX_TITLE_LENGTH + 1)
            viewModel.onEvent(TasksContract.UiEvent.CreateOrUpdateTask(longTitle, "", 1L))
            assertThat(viewModel.uiState.value.taskForm.titleError).isNotNull()

            viewModel.onEvent(TasksContract.UiEvent.ClearTaskErrors)
            assertThat(viewModel.uiState.value.taskForm.titleError).isNull()
        }

    @Test
    fun `deleteCategory removes tasks and category`() =
        runTest {
            val category = Category(id = 2L, name = "Temp")

            viewModel.onEvent(TasksContract.UiEvent.DeleteCategory(category))
            advanceUntilIdle()

            coVerify { deleteCategoryUseCase.invoke(category) }
            assertThat(viewModel.uiState.value.selectedCategoryId).isEqualTo(0L)
        }

    @Test
    fun `setDefaultCategory invokes use case`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.SetDefaultCategory(5L))
            advanceUntilIdle()

            coVerify { setDefaultCategoryUseCase.invoke(5L) }
        }

    @Test
    fun `reorderCategories invokes use case`() =
        runTest {
            val categories =
                listOf(
                    Category(id = 1L, name = "A"),
                    Category(id = 2L, name = "B"),
                )

            viewModel.onEvent(TasksContract.UiEvent.ReorderCategories(0, 1, categories))
            advanceUntilIdle()

            coVerify { reorderCategoriesUseCase.invoke(0, 1, categories) }
        }

    @Test
    fun `confirmDeleteCompletedTasks calls repository`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.CategorySelected(3L))

            viewModel.onEvent(TasksContract.UiEvent.ConfirmDeleteCompletedTasks)
            advanceUntilIdle()

            coVerify { deleteCompletedTasksUseCase.invoke(3L) }
        }

    @Test
    fun `openTaskFromNotification updates state with task details`() =
        runTest {
            val task = Task(id = 5L, title = "Notif Task", description = "D", categoryId = 2L)
            coEvery { taskRepository.getTaskById(5L) } returns task
            coEvery { categoryRepository.getAllCategories() } returns
                flowOf(listOf(Category(id = 2L, name = "Work")))
            every {
                tasksScreenPreferencesRepository.getSelectedCategoryId()
            } returns DEFAULT_INBOX_CATEGORY.id

            val vm =
                createViewModel()

            vm.openTaskFromNotification(5L)
            advanceUntilIdle()

            assertThat(vm.uiState.value.selectedCategoryId).isEqualTo(2L)
            assertThat(vm.uiState.value.taskForm.isVisible).isTrue()
            assertThat(vm.uiState.value.taskForm.editingTask).isEqualTo(task)
        }

    @Test
    fun `openTaskFromNotification with originalReminderDate overrides reminderDate on toggle`() =
        runTest {
            // The advanced reminderDate that the user sees in the sheet (already pre-advanced
            // by TaskReminderReceiver.rescheduleTask).
            val advancedReminderDate = LocalDateTime(2024, 1, 2, 10, 0)
            // The original reminderDate before pre-advance, plumbed via the notification
            // body PendingIntent. Toggle from the sheet must use this anchor so the use case
            // computes the next occurrence from the correct date.
            val originalReminderDate = LocalDateTime(2024, 1, 1, 10, 0)
            val task =
                Task(
                    id = 7L,
                    title = "Periodic",
                    description = "",
                    categoryId = 2L,
                    isCompleted = false,
                    reminderDate = advancedReminderDate,
                    periodicity = Periodicity.DAILY,
                )
            val expectedTaskOnToggle = task.copy(reminderDate = originalReminderDate)

            coEvery { taskRepository.getTaskById(7L) } returns task
            coEvery { categoryRepository.getAllCategories() } returns
                flowOf(listOf(Category(id = 2L, name = "Work")))
            every {
                tasksScreenPreferencesRepository.getSelectedCategoryId()
            } returns DEFAULT_INBOX_CATEGORY.id
            coEvery { toggleTaskCompletionUseCase.invoke(expectedTaskOnToggle) } returns
                ToggleTaskCompletionUseCase.Result.PeriodicCompleted(
                    UpdateTaskUseCase.Result.Success(ScheduleResult.Skipped),
                )

            val vm =
                createViewModel()

            vm.openTaskFromNotification(7L, originalReminderDate)
            advanceUntilIdle()

            // User toggles from the bottom sheet — VM must override the advanced reminderDate
            // with the original one before delegating to the use case.
            vm.onEvent(TasksContract.UiEvent.ToggleTaskCompletion(task))
            advanceUntilIdle()

            coVerify { toggleTaskCompletionUseCase.invoke(expectedTaskOnToggle) }
        }

    @Test
    fun `addCategory validates name length`() =
        runTest {
            val longName = "a".repeat(ValidationUtils.MAX_CATEGORY_NAME_LENGTH + 1)
            coEvery { createCategoryUseCase.invoke(longName) } returns CreateCategoryUseCase.Result.TooLong

            viewModel.onEvent(TasksContract.UiEvent.AddCategory(longName))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.categoryForm.nameError).isEqualTo(R.string.error_category_name_too_long)
            coVerify { createCategoryUseCase.invoke(longName) }
        }

    @Test
    fun `updateCategory validation fails for long name`() =
        runTest {
            val longName = "a".repeat(ValidationUtils.MAX_CATEGORY_NAME_LENGTH + 1)
            val category = Category(id = 1L, name = longName)
            coEvery { updateCategoryUseCase.invoke(category) } returns UpdateCategoryUseCase.Result.TooLong

            viewModel.onEvent(TasksContract.UiEvent.UpdateCategory(category))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.categoryForm.nameError).isEqualTo(R.string.error_category_name_too_long)
            coVerify { updateCategoryUseCase.invoke(category) }
        }

    @Test
    fun `setReminder updates reminderDate in state`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.SetReminder(2030, 12, 25, 10, 30))
            assertThat(viewModel.uiState.value.taskForm.reminderDate).isEqualTo(
                LocalDateTime(
                    2030,
                    12,
                    25,
                    10,
                    30,
                ),
            )
        }

    @Test
    fun `setReminder with past date still updates state`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.SetReminder(2020, 1, 1, 10, 0))
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.taskForm.reminderDate).isEqualTo(
                LocalDateTime(
                    2020,
                    1,
                    1,
                    10,
                    0,
                ),
            )
        }

    @Test
    fun `clearReminder resets reminder and periodicity`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(Periodicity.MONTHLY))
            viewModel.onEvent(TasksContract.UiEvent.SetReminder(2030, 12, 25, 10, 30))

            viewModel.onEvent(TasksContract.UiEvent.ClearReminder)

            assertThat(viewModel.uiState.value.taskForm.reminderDate).isNull()
            assertThat(viewModel.uiState.value.taskForm.periodicity).isNull()
        }

    @Test
    fun `confirmClearAllReminders cancels all task reminders`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.ConfirmClearAllReminders)
            advanceUntilIdle()

            coVerify { clearAllRemindersUseCase.invoke() }
        }

    @Test
    fun `requestDeleteTask updates state with subtasks count`() =
        runTest {
            val task = Task(id = 1, title = "Parent", description = "")
            val subtasks = listOf(Task(id = 2, title = "S1", description = "", parentTaskId = 1))
            coEvery { taskRepository.getSubtasksSync(1) } returns subtasks

            viewModel.onEvent(TasksContract.UiEvent.RequestDeleteTask(task))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.showDeleteTaskConfirmationDialog).isTrue()
            assertThat(viewModel.uiState.value.taskToDelete).isEqualTo(task)
            assertThat(viewModel.uiState.value.taskToDeleteSubtasksCount).isEqualTo(1)
        }

    @Test
    fun `addCategory success updates state and repo`() =
        runTest {
            coEvery { createCategoryUseCase.invoke("New") } returns
                CreateCategoryUseCase.Result.Success(
                    "New",
                )

            viewModel.onEvent(TasksContract.UiEvent.AddCategory("New"))
            advanceUntilIdle()

            coVerify { createCategoryUseCase.invoke("New") }
            assertThat(viewModel.uiState.value.categoryForm.isVisible).isFalse()
        }

    @Test
    fun `updateCategory success updates repo`() =
        runTest {
            val category = Category(id = 1L, name = "Old")
            val updatedCategory = category.copy(name = "Updated")
            coEvery { updateCategoryUseCase.invoke(any()) } returns UpdateCategoryUseCase.Result.Success

            viewModel.onEvent(TasksContract.UiEvent.UpdateCategory(updatedCategory))
            advanceUntilIdle()

            coVerify { updateCategoryUseCase.invoke(any()) }
            assertThat(viewModel.uiState.value.categoryForm.isVisible).isFalse()
        }

    @Test
    fun `toggleTaskCompletion for non-recurring parent toggles state and subtasks`() =
        runTest {
            val task = Task(id = 1, title = "Parent", description = "", isCompleted = false)
            coEvery { toggleTaskCompletionUseCase.invoke(task) } returns
                ToggleTaskCompletionUseCase.Result.ParentToggled(
                    true,
                    UpdateTaskUseCase.Result.Success(ScheduleResult.Skipped),
                )

            viewModel.onEvent(TasksContract.UiEvent.ToggleTaskCompletion(task))
            advanceUntilIdle()

            coVerify { toggleTaskCompletionUseCase.invoke(task) }
        }

    @Test
    fun `unchecking archived periodic occurrence expands task without snackbar`() =
        runTest {
            val task =
                Task(
                    id = 1,
                    title = "Archived periodic",
                    description = "",
                    isCompleted = true,
                    reminderDate = LocalDateTime(2024, 1, 1, 10, 0),
                    nextInstanceId = 42L,
                )
            coEvery { toggleTaskCompletionUseCase.invoke(task) } returns
                ToggleTaskCompletionUseCase.Result.ParentToggled(
                    isCompleted = false,
                    updateResult = UpdateTaskUseCase.Result.Success(ScheduleResult.Skipped),
                )
            val effects = mutableListOf<TasksContract.UiEffect>()
            backgroundScope.launch { viewModel.uiEffect.toList(effects) }

            viewModel.onEvent(TasksContract.UiEvent.ToggleTaskCompletion(task))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.expandedTaskIds).contains(task.id)
            assertThat(effects.filterIsInstance<TasksContract.UiEffect.ShowSnackbar>()).isEmpty()
        }

    @Test
    fun `hideTaskDialog resets task dialog state`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.ShowTaskDialog())
            viewModel.onEvent(TasksContract.UiEvent.HideTaskDialog)

            assertThat(viewModel.uiState.value.taskForm.isVisible).isFalse()
            assertThat(viewModel.uiState.value.taskForm.editingTask).isNull()
        }

    @Test
    fun `onPermissionsGranted shows success message`() =
        runTest {
            viewModel.onEvent(TasksContract.UiEvent.OnPermissionsGranted)
            advanceUntilIdle()

            // The ViewModel now sends a @StringRes Int via UiEffect, no context needed
        }

    @Test
    fun `loadData filters and partitions tasks correctly`() =
        runTest {
            val category1Id = 1L
            val category2Id = 2L

            val task1 =
                Task(
                    id = 1,
                    title = "Active C1",
                    description = "D1",
                    categoryId = category1Id,
                    isCompleted = false,
                )
            val task2 =
                Task(
                    id = 2,
                    title = "Completed C1",
                    description = "D2",
                    categoryId = category1Id,
                    isCompleted = true,
                )
            val task3 =
                Task(
                    id = 3,
                    title = "Active C2",
                    description = "D3",
                    categoryId = category2Id,
                    isCompleted = false,
                )
            val subtask =
                Task(
                    id = 4,
                    title = "Subtask",
                    description = "DS",
                    categoryId = category1Id,
                    parentTaskId = 1,
                )

            coEvery { taskRepository.getAllTasks() } returns
                flowOf(
                    listOf(
                        task1,
                        task2,
                        task3,
                        subtask,
                    ),
                )
            coEvery { categoryRepository.getAllCategories() } returns
                flowOf(
                    listOf(
                        Category(
                            category1Id,
                            "C1",
                        ),
                        Category(category2Id, "C2"),
                    ),
                )

            // Re-init viewModel to trigger loadData with mocked data
            viewModel =
                createViewModel()

            // Select Category 1
            viewModel.onEvent(TasksContract.UiEvent.CategorySelected(category1Id))
            advanceUntilIdle()

            val state = viewModel.uiState.value

            // Active tasks should only contain task1 (task3 is different category, subtask has parent)
            assertThat(state.activeTasks.values.flatten()).containsExactly(task1)

            // Completed tasks should only contain task2
            assertThat(state.completedTaskGroups.values.flatten()).containsExactly(task2)

            // Subtasks map should contain subtask for task1
            assertThat(state.subtasksMap[1L]).containsExactly(subtask)

            // Switch to Category 2
            viewModel.onEvent(TasksContract.UiEvent.CategorySelected(category2Id))
            advanceUntilIdle()

            assertThat(
                viewModel.uiState.value.activeTasks.values
                    .flatten(),
            ).containsExactly(task3)
        }

    @Test
    fun `completedTaskGroups groups tasks by completedAt date when sorted by date`() =
        runTest {
            val date1 = LocalDateTime(2024, 6, 15, 10, 0)
            val date2 = LocalDateTime(2024, 6, 15, 14, 30)
            val date3 = LocalDateTime(2024, 6, 14, 9, 0)

            val taskToday1 =
                Task(
                    id = 1,
                    title = "Today 1",
                    description = "",
                    isCompleted = true,
                    completedAt = date1,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                    sortOrder = 0,
                )
            val taskToday2 =
                Task(
                    id = 2,
                    title = "Today 2",
                    description = "",
                    isCompleted = true,
                    completedAt = date2,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                    sortOrder = 1,
                )
            val taskYesterday =
                Task(
                    id = 3,
                    title = "Yesterday",
                    description = "",
                    isCompleted = true,
                    completedAt = date3,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                    sortOrder = 2,
                )
            val taskNoDate =
                Task(
                    id = 4,
                    title = "Legacy",
                    description = "",
                    isCompleted = true,
                    completedAt = null,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                    sortOrder = 3,
                )

            coEvery { taskRepository.getAllTasks() } returns
                flowOf(listOf(taskToday1, taskToday2, taskYesterday, taskNoDate))
            coEvery { categoryRepository.getAllCategories() } returns
                flowOf(listOf(DEFAULT_INBOX_CATEGORY))
            every { tasksScreenPreferencesRepository.getSortOption(any()) } returns SortOption.BY_DATE

            viewModel =
                createViewModel()

            advanceUntilIdle()
            val grouped = viewModel.uiState.value.completedTaskGroups
            val june15 = CompletedGroupKey.ByDate(LocalDate(2024, 6, 15))
            assertThat(grouped[june15]).hasSize(2)
            val june14 = CompletedGroupKey.ByDate(LocalDate(2024, 6, 14))
            assertThat(grouped[june14]).hasSize(1)

            // null date key should have the legacy task
            val nullDate = CompletedGroupKey.ByDate(null)
            assertThat(grouped[nullDate]).hasSize(1)
            assertThat(grouped[nullDate]?.first()?.title).isEqualTo("Legacy")

            // Keys should be ordered: newest first, null last
            val keys = grouped.keys.toList()
            assertThat(keys.first()).isEqualTo(june15)
            assertThat(keys.last()).isEqualTo(nullDate)
        }

    @Test
    fun `completedTaskGroups groups tasks by priority when sorted by priority`() =
        runTest {
            val taskHigh =
                Task(
                    id = 1,
                    title = "High priority task",
                    description = "",
                    isCompleted = true,
                    priority = Priority.HIGH,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                    sortOrder = 0,
                )
            val taskMedium =
                Task(
                    id = 2,
                    title = "Medium priority task",
                    description = "",
                    isCompleted = true,
                    priority = Priority.MEDIUM,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                    sortOrder = 1,
                )
            val taskLow =
                Task(
                    id = 3,
                    title = "Low priority task",
                    description = "",
                    isCompleted = true,
                    priority = Priority.LOW,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                    sortOrder = 2,
                )
            val taskNoPriority =
                Task(
                    id = 4,
                    title = "No priority task",
                    description = "",
                    isCompleted = true,
                    priority = null,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                    sortOrder = 3,
                )

            coEvery { taskRepository.getAllTasks() } returns
                flowOf(listOf(taskHigh, taskMedium, taskLow, taskNoPriority))
            coEvery { categoryRepository.getAllCategories() } returns
                flowOf(listOf(DEFAULT_INBOX_CATEGORY))
            every { tasksScreenPreferencesRepository.getSortOption(any()) } returns SortOption.BY_PRIORITY

            viewModel =
                createViewModel()
            advanceUntilIdle()

            val grouped = viewModel.uiState.value.completedTaskGroups

            // Should have groups for each priority level
            assertThat(grouped[CompletedGroupKey.ByPriority(Priority.HIGH)]).hasSize(1)
            assertThat(grouped[CompletedGroupKey.ByPriority(Priority.MEDIUM)]).hasSize(1)
            assertThat(grouped[CompletedGroupKey.ByPriority(Priority.LOW)]).hasSize(1)
            assertThat(grouped[CompletedGroupKey.ByPriority(null)]).hasSize(1)

            // Keys should be ordered: HIGH first, null last
            val keys = grouped.keys.toList()
            assertThat(keys.first()).isEqualTo(CompletedGroupKey.ByPriority(Priority.HIGH))
            assertThat(keys.last()).isEqualTo(CompletedGroupKey.ByPriority(null))
        }

    @Test
    fun `completedTaskGroups uses flat grouping when sorted manually`() =
        runTest {
            val task1 =
                Task(
                    id = 1,
                    title = "Task A",
                    description = "",
                    isCompleted = true,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                    sortOrder = 0,
                )
            val task2 =
                Task(
                    id = 2,
                    title = "Task B",
                    description = "",
                    isCompleted = true,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                    sortOrder = 1,
                )

            coEvery { taskRepository.getAllTasks() } returns flowOf(listOf(task1, task2))
            coEvery { categoryRepository.getAllCategories() } returns
                flowOf(listOf(DEFAULT_INBOX_CATEGORY))
            every { tasksScreenPreferencesRepository.getSortOption(any()) } returns SortOption.MANUAL

            viewModel =
                createViewModel()
            advanceUntilIdle()

            val grouped = viewModel.uiState.value.completedTaskGroups

            // Should have a single Flat key
            assertThat(grouped.keys).hasSize(1)
            assertThat(grouped.keys.first()).isEqualTo(CompletedGroupKey.Flat)
            assertThat(grouped[CompletedGroupKey.Flat]).hasSize(2)
        }

    @Test
    fun `completedTaskGroups uses ByTitle grouping when sorted by title`() =
        runTest {
            val task1 =
                Task(
                    id = 1,
                    title = "Banana",
                    description = "",
                    isCompleted = true,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                    sortOrder = 0,
                )
            val task2 =
                Task(
                    id = 2,
                    title = "Apple",
                    description = "",
                    isCompleted = true,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                    sortOrder = 1,
                )

            coEvery { taskRepository.getAllTasks() } returns flowOf(listOf(task1, task2))
            coEvery { categoryRepository.getAllCategories() } returns
                flowOf(listOf(DEFAULT_INBOX_CATEGORY))
            every { tasksScreenPreferencesRepository.getSortOption(any()) } returns SortOption.BY_TITLE

            viewModel =
                createViewModel()
            advanceUntilIdle()

            val grouped = viewModel.uiState.value.completedTaskGroups

            assertThat(grouped.keys).hasSize(1)
            assertThat(grouped.keys.first()).isEqualTo(CompletedGroupKey.ByTitle)
            assertThat(grouped[CompletedGroupKey.ByTitle]).hasSize(2)
        }

    // region deleteCategory result handling

    @Test
    fun `deleteCategory CannotDeleteDefault clears dialog and shows snackbar`() =
        runTest {
            val category = Category(id = 1L, name = "Inbox", isDefault = true)
            coEvery { deleteCategoryUseCase.invoke(category) } returns
                DeleteCategoryUseCase.Result.CannotDeleteDefault

            viewModel.onEvent(TasksContract.UiEvent.DeleteCategory(category))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.showDeleteCategoryConfirmationDialog).isFalse()
            assertThat(viewModel.uiState.value.categoryToDelete).isNull()
        }

    @Test
    fun `deleteCategory LastCategory clears dialog`() =
        runTest {
            val category = Category(id = 1L, name = "Only")
            coEvery { deleteCategoryUseCase.invoke(category) } returns
                DeleteCategoryUseCase.Result.LastCategory

            viewModel.onEvent(TasksContract.UiEvent.DeleteCategory(category))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.showDeleteCategoryConfirmationDialog).isFalse()
            assertThat(viewModel.uiState.value.categoryToDelete).isNull()
        }

    @Test
    fun `delete and undo unselected category preserves selection`() =
        runTest {
            val catA = Category(id = 1L, name = "A")
            val catB = Category(id = 2L, name = "B")
            val snapshot =
                com.mandrecode.tempo.features.tasks.domain.model
                    .CategoryDeletionSnapshot(catB, emptyList())
            coEvery { categoryRepository.getAllCategories() } returns flowOf(listOf(catA, catB))
            every { tasksScreenPreferencesRepository.getSelectedCategoryId() } returns catA.id
            coEvery { deleteCategoryUseCase.invoke(catB) } returns
                DeleteCategoryUseCase.Result.Success(snapshot)
            coEvery { restoreDeletedCategoryUseCase(snapshot) } returns RestoreResult(emptyList())

            // Recreate ViewModel with categories available
            viewModel =
                createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(TasksContract.UiEvent.DeleteCategory(catB))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.categoryForm.isVisible).isFalse()
            assertThat(viewModel.uiState.value.showDeleteCategoryConfirmationDialog).isFalse()
            assertThat(viewModel.uiState.value.selectedCategoryId).isEqualTo(catA.id)

            val token = viewModel.pendingDeletionSnapshots.keys.single()
            viewModel.onEvent(TasksContract.UiEvent.UndoDeletion(token))
            advanceUntilIdle()

            coVerify { restoreDeletedCategoryUseCase(snapshot) }
            assertThat(viewModel.uiState.value.selectedCategoryId).isEqualTo(catA.id)
        }

    // endregion

    // region updateCategory with isDefault

    @Test
    fun `updateCategory with isDefault true invokes setDefaultCategoryUseCase`() =
        runTest {
            val category = Category(id = 2L, name = "Work", isDefault = true)
            coEvery { updateCategoryUseCase.invoke(any()) } returns
                UpdateCategoryUseCase.Result.Success

            viewModel.onEvent(TasksContract.UiEvent.UpdateCategory(category))
            advanceUntilIdle()

            coVerify { updateCategoryUseCase.invoke(any()) }
            coVerify { setDefaultCategoryUseCase.invoke(2L) }
            assertThat(viewModel.uiState.value.categoryForm.isVisible).isFalse()
        }

    @Test
    fun `updateCategory with isDefault false does not invoke setDefaultCategoryUseCase`() =
        runTest {
            val category = Category(id = 2L, name = "Work", isDefault = false)
            coEvery { updateCategoryUseCase.invoke(any()) } returns
                UpdateCategoryUseCase.Result.Success

            viewModel.onEvent(TasksContract.UiEvent.UpdateCategory(category))
            advanceUntilIdle()

            coVerify { updateCategoryUseCase.invoke(any()) }
            coVerify(exactly = 0) { setDefaultCategoryUseCase.invoke(any()) }
        }

    // endregion

    // region Active Task Grouping Tests

    @Test
    fun `activeTaskGroups groups tasks by reminderDate when sorted by date`() =
        runTest {
            val today =
                kotlinx.datetime.Clock.System.todayIn(
                    kotlinx.datetime.TimeZone.currentSystemDefault(),
                )
            val todayDateTime =
                LocalDateTime(
                    today.year,
                    today.monthNumber,
                    today.dayOfMonth,
                    9,
                    0,
                )
            val tomorrow = today.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
            val tomorrowDateTime =
                LocalDateTime(
                    tomorrow.year,
                    tomorrow.monthNumber,
                    tomorrow.dayOfMonth,
                    14,
                    0,
                )
            val nextWeek = today.plus(7, kotlinx.datetime.DateTimeUnit.DAY)
            val nextWeekDateTime =
                LocalDateTime(
                    nextWeek.year,
                    nextWeek.monthNumber,
                    nextWeek.dayOfMonth,
                    10,
                    0,
                )

            val taskTodayA =
                Task(
                    id = 1,
                    title = "Task A",
                    description = "",
                    reminderDate = todayDateTime,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                )
            val taskTodayB =
                Task(
                    id = 2,
                    title = "Task B",
                    description = "",
                    reminderDate =
                        LocalDateTime(
                            today.year,
                            today.monthNumber,
                            today.dayOfMonth,
                            14,
                            0,
                        ),
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                )
            val taskTomorrow =
                Task(
                    id = 3,
                    title = "Task C",
                    description = "",
                    reminderDate = tomorrowDateTime,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                )
            val taskNextWeek =
                Task(
                    id = 5,
                    title = "Task E",
                    description = "",
                    reminderDate = nextWeekDateTime,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                )
            val taskNoDate =
                Task(
                    id = 4,
                    title = "No date task",
                    description = "",
                    reminderDate = null,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                )

            coEvery { taskRepository.getAllTasks() } returns
                flowOf(listOf(taskTodayA, taskTodayB, taskTomorrow, taskNextWeek, taskNoDate))
            coEvery { categoryRepository.getAllCategories() } returns
                flowOf(listOf(DEFAULT_INBOX_CATEGORY))
            every { tasksScreenPreferencesRepository.getSortOption(any()) } returns SortOption.BY_DATE

            viewModel =
                createViewModel()

            advanceUntilIdle()
            val grouped = viewModel.uiState.value.activeTasks

            val todayKey = ActiveGroupKey.ByDate(today)
            assertThat(grouped[todayKey]).hasSize(2)

            val tomorrowKey = ActiveGroupKey.ByDate(tomorrow)
            assertThat(grouped[tomorrowKey]).hasSize(1)

            val nextWeekKey = ActiveGroupKey.ByDate(nextWeek)
            assertThat(grouped[nextWeekKey]).hasSize(1)

            val nullDate = ActiveGroupKey.ByDate(null)
            assertThat(grouped[nullDate]).hasSize(1)
            assertThat(grouped[nullDate]?.first()?.title).isEqualTo("No date task")

            // Keys should be ordered: today → tomorrow → next week → null
            val keys = grouped.keys.toList()
            assertThat(keys).containsExactly(todayKey, tomorrowKey, nextWeekKey, nullDate).inOrder()
        }

    @Test
    fun `activeTaskGroups buckets past dates into single Overdue group`() =
        runTest {
            val today =
                kotlinx.datetime.Clock.System.todayIn(
                    kotlinx.datetime.TimeZone.currentSystemDefault(),
                )
            val yesterday = today.minus(1, kotlinx.datetime.DateTimeUnit.DAY)
            val twoDaysAgo = today.minus(2, kotlinx.datetime.DateTimeUnit.DAY)
            val todayDateTime =
                LocalDateTime(
                    today.year,
                    today.monthNumber,
                    today.dayOfMonth,
                    9,
                    0,
                )

            val taskOverdueA =
                Task(
                    id = 1,
                    title = "Overdue A",
                    description = "",
                    reminderDate =
                        LocalDateTime(
                            yesterday.year,
                            yesterday.monthNumber,
                            yesterday.dayOfMonth,
                            10,
                            0,
                        ),
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                )
            val taskOverdueB =
                Task(
                    id = 2,
                    title = "Overdue B",
                    description = "",
                    reminderDate =
                        LocalDateTime(
                            twoDaysAgo.year,
                            twoDaysAgo.monthNumber,
                            twoDaysAgo.dayOfMonth,
                            8,
                            0,
                        ),
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                )
            val taskToday =
                Task(
                    id = 3,
                    title = "Today task",
                    description = "",
                    reminderDate = todayDateTime,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                )
            val taskNoDate =
                Task(
                    id = 4,
                    title = "No date task",
                    description = "",
                    reminderDate = null,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                )

            coEvery { taskRepository.getAllTasks() } returns
                flowOf(listOf(taskOverdueA, taskOverdueB, taskToday, taskNoDate))
            coEvery { categoryRepository.getAllCategories() } returns
                flowOf(listOf(DEFAULT_INBOX_CATEGORY))
            every { tasksScreenPreferencesRepository.getSortOption(any()) } returns SortOption.BY_DATE

            viewModel =
                createViewModel()

            advanceUntilIdle()
            val grouped = viewModel.uiState.value.activeTasks

            // Both past-date tasks are bucketed under a single Overdue key
            assertThat(grouped[ActiveGroupKey.Overdue]).hasSize(2)

            val todayKey = ActiveGroupKey.ByDate(today)
            assertThat(grouped[todayKey]).hasSize(1)

            val nullDate = ActiveGroupKey.ByDate(null)
            assertThat(grouped[nullDate]).hasSize(1)

            // Ordering: Overdue → Today → No date
            val keys = grouped.keys.toList()
            assertThat(keys)
                .containsExactly(
                    ActiveGroupKey.Overdue,
                    todayKey,
                    nullDate,
                ).inOrder()
        }

    @Test
    fun `activeTaskGroups groups tasks by priority when sorted by priority`() =
        runTest {
            val taskHigh =
                Task(
                    id = 1,
                    title = "High priority",
                    description = "",
                    priority = Priority.HIGH,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                )
            val taskMedium =
                Task(
                    id = 2,
                    title = "Medium priority",
                    description = "",
                    priority = Priority.MEDIUM,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                )
            val taskNoPriority =
                Task(
                    id = 3,
                    title = "No priority",
                    description = "",
                    priority = null,
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                )

            coEvery { taskRepository.getAllTasks() } returns
                flowOf(listOf(taskHigh, taskMedium, taskNoPriority))
            coEvery { categoryRepository.getAllCategories() } returns
                flowOf(listOf(DEFAULT_INBOX_CATEGORY))
            every { tasksScreenPreferencesRepository.getSortOption(any()) } returns SortOption.BY_PRIORITY

            viewModel =
                createViewModel()

            advanceUntilIdle()
            val grouped = viewModel.uiState.value.activeTasks

            assertThat(grouped[ActiveGroupKey.ByPriority(Priority.HIGH)]).hasSize(1)
            assertThat(grouped[ActiveGroupKey.ByPriority(Priority.MEDIUM)]).hasSize(1)
            assertThat(grouped[ActiveGroupKey.ByPriority(null)]).hasSize(1)

            // Keys should be ordered: HIGH, MEDIUM, null
            val keys = grouped.keys.toList()
            assertThat(keys.first()).isEqualTo(ActiveGroupKey.ByPriority(Priority.HIGH))
            assertThat(keys.last()).isEqualTo(ActiveGroupKey.ByPriority(null))
        }

    @Test
    fun `activeTaskGroups uses flat grouping when sorted manually`() =
        runTest {
            val task1 =
                Task(
                    id = 1,
                    title = "Task 1",
                    description = "",
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                    sortOrder = 0,
                )
            val task2 =
                Task(
                    id = 2,
                    title = "Task 2",
                    description = "",
                    categoryId = DEFAULT_INBOX_CATEGORY.id,
                    sortOrder = 1,
                )

            coEvery { taskRepository.getAllTasks() } returns
                flowOf(listOf(task1, task2))
            coEvery { categoryRepository.getAllCategories() } returns
                flowOf(listOf(DEFAULT_INBOX_CATEGORY))
            every { tasksScreenPreferencesRepository.getSortOption(any()) } returns SortOption.MANUAL

            viewModel =
                createViewModel()

            advanceUntilIdle()
            val grouped = viewModel.uiState.value.activeTasks

            assertThat(grouped).hasSize(1)
            assertThat(grouped.keys.first()).isEqualTo(ActiveGroupKey.Flat)
            assertThat(grouped[ActiveGroupKey.Flat]).hasSize(2)
        }

    // endregion
}
