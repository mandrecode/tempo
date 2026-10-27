package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.preferences.TasksScreenPreferencesRepository
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.navigation.PendingNotificationAction
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
import com.mandrecode.tempo.features.tasks.domain.usecase.TaskSectionUseCases
import com.mandrecode.tempo.features.tasks.domain.usecase.ToggleTaskCompletionUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.UpdateCategoryUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.UpdateTaskUseCase
import com.mandrecode.tempo.features.tasks.presentation.components.TASK_BOTTOM_SHEET_DESCRIPTION_FIELD_TEST_TAG
import com.mandrecode.tempo.infrastructure.permissions.PermissionChecker
import io.mockk.coEvery
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
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk<TaskSectionUseCases>(relaxed = true) {
                    every { repository.getAllSections() } returns flowOf(emptyList())
                },
                kotlin.time.Clock.System,
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
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk<TaskSectionUseCases>(relaxed = true) {
                    every { repository.getAllSections() } returns flowOf(emptyList())
                },
                kotlin.time.Clock.System,
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

    @Test
    fun tasksScreen_opensTaskCreationSheet_whenPendingActionIsOpenNewTaskDialog() {
        var consumed = false
        composeTestRule.setContent {
            TempoTheme {
                TasksScreen(
                    viewModel = viewModel,
                    pendingNotificationAction = PendingNotificationAction.OpenNewTaskDialog,
                    onConsumePendingNotificationAction = { consumed = true },
                )
            }
        }

        composeTestRule.waitForIdle()

        // Same blank task-creation sheet the in-app "+" button opens (widget entry path)
        composeTestRule.onNodeWithText("New task…").assertIsDisplayed()
        assertThat(consumed).isTrue()
    }

    /**
     * Narrowing the window past the docked-pane breakpoint with the editor open moves the editor
     * into a bottom sheet. The docked pane plays its exit as the sheet appears, and both must not
     * hold the editor at once: a second copy would start empty and the draft would be lost (#163).
     */
    @Test
    fun shrinkingTheWindow_withTheEditorOpen_keepsOneEditorAndItsDraft() {
        var widthDp by mutableIntStateOf(1300)
        composeTestRule.setContent {
            val density = LocalDensity.current
            val window = LocalWindowInfo.current
            val resizable =
                remember(window, density) {
                    object : WindowInfo {
                        override val isWindowFocused: Boolean get() = window.isWindowFocused
                        override val containerSize: IntSize
                            get() = with(density) { IntSize(widthDp.dp.roundToPx(), 900.dp.roundToPx()) }
                    }
                }
            CompositionLocalProvider(LocalWindowInfo provides resizable) {
                TempoTheme { TasksScreen(viewModel = viewModel) }
            }
        }
        composeTestRule.runOnIdle { viewModel.onEvent(TasksContract.UiEvent.ShowTaskDialog()) }
        composeTestRule
            .onNodeWithTag(TASK_BOTTOM_SHEET_DESCRIPTION_FIELD_TEST_TAG)
            .performTextInput("Call the landlord first")

        widthDp = 400
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag(TASK_BOTTOM_SHEET_DESCRIPTION_FIELD_TEST_TAG).assertCountEquals(1)
        composeTestRule
            .onNodeWithTag(TASK_BOTTOM_SHEET_DESCRIPTION_FIELD_TEST_TAG)
            .assertTextContains("Call the landlord first")
    }
}
