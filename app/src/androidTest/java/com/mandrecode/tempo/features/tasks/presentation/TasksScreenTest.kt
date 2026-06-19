package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.mandrecode.tempo.core.data.preferences.TasksScreenPreferencesRepository
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
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
import com.mandrecode.tempo.features.tasks.domain.usecase.SetDefaultCategoryUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.ToggleTaskCompletionUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.UpdateCategoryUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.UpdateTaskUseCase
import com.mandrecode.tempo.features.tasks.presentation.components.sections.QuickTaskEntryBar
import com.mandrecode.tempo.infrastructure.permissions.PermissionChecker
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TasksScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

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
    private lateinit var viewModel: TasksViewModel

    private val testTask =
        Task(
            id = 1,
            title = "Test Task",
            description = "Test Description",
            categoryId = 1L,
            priority = Priority.HIGH,
            isCompleted = false,
            sortOrder = 1,
        )

    private val inboxCategory = Category(id = -1, name = "Inbox", isDefault = true, sortOrder = -1)
    private val testCategory = Category(id = 1, name = "Work")

    @Before
    fun setup() {
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

        coEvery { taskRepository.getAllTasks() } returns flowOf(listOf(testTask))
        coEvery { categoryRepository.getAllCategories() } returns
            flowOf(listOf(inboxCategory, testCategory))
        every { tasksScreenPreferencesRepository.getSelectedCategoryId() } returns testCategory.id

        val testDispatcher = Dispatchers.Unconfined
        viewModel =
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
            )
    }

    @Test
    fun tasksScreen_displaysTaskTitle() {
        composeTestRule.setContent {
            TempoTheme {
                TasksScreen(viewModel = viewModel)
            }
        }

        composeTestRule.waitForIdle()

        // Check that the task title is displayed
        composeTestRule.onNodeWithText("Test Task").assertIsDisplayed()
    }

    @Test
    fun tasksScreen_displaysCategoryChip() {
        composeTestRule.setContent {
            TempoTheme {
                TasksScreen(viewModel = viewModel)
            }
        }

        composeTestRule.waitForIdle()

        // Check that the inbox category chip is displayed (Assuming it's added by default logic)
        // Wait, inbox is usually added in ViewModel logic
        composeTestRule.onNodeWithText("Inbox").assertIsDisplayed()
        // Check that the work category chip is displayed
        composeTestRule.onNodeWithText("Work").assertIsDisplayed()
    }

    @Test
    fun tasksScreen_displaysAddTaskButton() {
        composeTestRule.setContent {
            TempoTheme {
                val uiState = viewModel.uiState.collectAsState().value
                Box(modifier = Modifier.fillMaxSize()) {
                    TasksScreen(viewModel = viewModel)
                    QuickTaskEntryBar(
                        onAddTask = {},
                        sortOption = uiState.sortOption,
                        onSortClick = {},
                        showClearCompleted = false,
                        onClearCompletedClick = {},
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        // Check that the add task button is displayed
        composeTestRule.onNodeWithContentDescription("Add Task").assertIsDisplayed()
    }

    @Test
    fun tasksScreen_addTaskButton_canBeClicked() {
        // Mock insertTask to return a valid ID (e.g. 2L)
        coEvery { taskRepository.insertTask(any()) } returns 2L
        // Also mock getMaxSortOrder
        coEvery { taskRepository.getMaxSortOrder(any()) } returns 0

        composeTestRule.setContent {
            TempoTheme {
                val uiState = viewModel.uiState.collectAsState().value
                Box(modifier = Modifier.fillMaxSize()) {
                    TasksScreen(viewModel = viewModel)
                    QuickTaskEntryBar(
                        onAddTask = { title ->
                            viewModel.onEvent(
                                TasksContract.UiEvent.CreateOrUpdateTask(
                                    title = title,
                                    description = "",
                                    categoryId = uiState.selectedCategoryId,
                                ),
                            )
                        },
                        sortOption = uiState.sortOption,
                        onSortClick = {},
                        showClearCompleted = false,
                        onClearCompletedClick = {},
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        // Input text into the task field
        composeTestRule.onNodeWithText("New task\u2026").performTextInput("New Task Title")

        // Click the add task button
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()

        composeTestRule.waitForIdle()

        // Verify that the task creation method was called on the repository
        coVerify { createTaskUseCase.invoke(any()) }
    }

    @Test
    fun tasksScreen_displaysEmptyState_whenNoTasks() {
        coEvery { taskRepository.getAllTasks() } returns flowOf(emptyList())

        val testDispatcher = Dispatchers.Unconfined
        val emptyViewModel =
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
            )

        composeTestRule.setContent {
            TempoTheme {
                TasksScreen(viewModel = emptyViewModel)
            }
        }

        composeTestRule.waitForIdle()

        // Check that empty state is displayed
        composeTestRule.onNodeWithText("✨ Nothing here!").assertIsDisplayed()
    }

    @Test
    fun tasksScreen_subtaskCreation_usesNewStyle() {
        composeTestRule.setContent {
            TempoTheme {
                TasksScreen(viewModel = viewModel)
            }
        }

        composeTestRule.waitForIdle()

        // Click the add subtask button on the existing task
        composeTestRule.onNodeWithContentDescription("Add Subtask").performClick()

        composeTestRule.waitForIdle()

        // Verify that the New Style "Add details" placeholder is displayed
        // This placeholder is unique to the new style; old style used "Description" label
        composeTestRule.onNodeWithText("Add details").assertIsDisplayed()
    }
}
