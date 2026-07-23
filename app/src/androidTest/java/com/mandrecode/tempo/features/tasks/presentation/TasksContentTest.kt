package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.DEFAULT_INBOX_CATEGORY
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.presentation.model.ActiveGroupKey
import com.mandrecode.tempo.features.tasks.presentation.model.CompletedGroupKey
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TasksContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCategory = Category(id = 1, name = "Work")

    @Test
    fun showsLoadingIndicator_whenLoading() {
        composeTestRule.setContent {
            TempoTheme {
                TasksContent(
                    uiState = TasksContract.UiState(isLoading = true),
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Loading tasks", substring = true).assertIsDisplayed()
    }

    @Test
    fun showsEmptyState_whenNoTasks() {
        composeTestRule.setContent {
            TempoTheme {
                TasksContent(
                    uiState =
                        TasksContract.UiState(
                            isLoading = false,
                            tasks = persistentListOf(),
                            categories = persistentListOf(DEFAULT_INBOX_CATEGORY),
                        ),
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Nothing here", substring = true).assertIsDisplayed()
    }

    @Test
    fun showsTaskTitle_whenTasksExist() {
        val task =
            Task(
                id = 1,
                title = "Buy groceries",
                description = "",
                categoryId = DEFAULT_INBOX_CATEGORY.id,
                sortOrder = 0,
            )

        composeTestRule.setContent {
            TempoTheme {
                TasksContent(
                    uiState =
                        TasksContract.UiState(
                            isLoading = false,
                            tasks = persistentListOf(task),
                            activeTasks =
                                persistentMapOf(
                                    ActiveGroupKey.Flat to persistentListOf(task),
                                ),
                            categories = persistentListOf(DEFAULT_INBOX_CATEGORY),
                        ),
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Buy groceries").assertIsDisplayed()
    }

    @Test
    fun showsCategoryChips() {
        composeTestRule.setContent {
            TempoTheme {
                TasksContent(
                    uiState =
                        TasksContract.UiState(
                            isLoading = false,
                            tasks = persistentListOf(),
                            categories = persistentListOf(DEFAULT_INBOX_CATEGORY, testCategory),
                        ),
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Inbox").assertIsDisplayed()
        composeTestRule.onNodeWithText("Work").assertIsDisplayed()
    }

    @Test
    fun showsCompletedSection_whenCompletedTasksExist() {
        val completedTask =
            Task(
                id = 1,
                title = "Done task",
                description = "",
                isCompleted = true,
                categoryId = DEFAULT_INBOX_CATEGORY.id,
                sortOrder = 0,
            )

        composeTestRule.setContent {
            TempoTheme {
                TasksContent(
                    uiState =
                        TasksContract.UiState(
                            isLoading = false,
                            tasks = persistentListOf(completedTask),
                            completedTaskGroups =
                                persistentMapOf(
                                    CompletedGroupKey.Flat to persistentListOf(completedTask),
                                ),
                            categories = persistentListOf(DEFAULT_INBOX_CATEGORY),
                            showCompletedTasks = true,
                        ),
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Completed", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Done task").assertIsDisplayed()
    }

    @Test
    fun completedSection_manualMode_showsNoDividerOrLabel() {
        val completedTask =
            Task(
                id = 1,
                title = "Done task",
                description = "",
                isCompleted = true,
                categoryId = DEFAULT_INBOX_CATEGORY.id,
                sortOrder = 0,
            )

        composeTestRule.setContent {
            TempoTheme {
                TasksContent(
                    uiState =
                        TasksContract.UiState(
                            isLoading = false,
                            tasks = persistentListOf(completedTask),
                            completedTaskGroups =
                                persistentMapOf(
                                    CompletedGroupKey.Flat to persistentListOf(completedTask),
                                ),
                            categories = persistentListOf(DEFAULT_INBOX_CATEGORY),
                            showCompletedTasks = true,
                            sortOption = SortOption.MANUAL,
                        ),
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Completed", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Done task").assertIsDisplayed()
        // No group labels should appear in manual mode
        composeTestRule.onNodeWithText("High").assertDoesNotExist()
        composeTestRule.onNodeWithText("A → Z").assertDoesNotExist()
    }

    @Test
    fun completedSection_priorityMode_showsDividerWithPriorityLabel() {
        val highTask =
            Task(
                id = 1,
                title = "Important task",
                description = "",
                isCompleted = true,
                priority = Priority.HIGH,
                categoryId = DEFAULT_INBOX_CATEGORY.id,
                sortOrder = 0,
            )
        val lowTask =
            Task(
                id = 2,
                title = "Low priority task",
                description = "",
                isCompleted = true,
                priority = Priority.LOW,
                categoryId = DEFAULT_INBOX_CATEGORY.id,
                sortOrder = 1,
            )

        composeTestRule.setContent {
            TempoTheme {
                TasksContent(
                    uiState =
                        TasksContract.UiState(
                            isLoading = false,
                            tasks = persistentListOf(highTask, lowTask),
                            completedTaskGroups =
                                persistentMapOf(
                                    CompletedGroupKey.ByPriority(Priority.HIGH) to
                                        persistentListOf(highTask),
                                    CompletedGroupKey.ByPriority(Priority.LOW) to
                                        persistentListOf(lowTask),
                                ),
                            categories = persistentListOf(DEFAULT_INBOX_CATEGORY),
                            showCompletedTasks = true,
                            sortOption = SortOption.BY_PRIORITY,
                        ),
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Completed", substring = true).assertIsDisplayed()
        // First group label "High" shown in separator, second group "Low" as header
        composeTestRule.onNodeWithText("High").assertIsDisplayed()
        composeTestRule.onNodeWithText("Low").assertIsDisplayed()
    }

    @Test
    fun completedSection_titleMode_showsDividerWithAlphabeticalLabel() {
        val completedTask =
            Task(
                id = 1,
                title = "Alpha task",
                description = "",
                isCompleted = true,
                categoryId = DEFAULT_INBOX_CATEGORY.id,
                sortOrder = 0,
            )

        composeTestRule.setContent {
            TempoTheme {
                TasksContent(
                    uiState =
                        TasksContract.UiState(
                            isLoading = false,
                            tasks = persistentListOf(completedTask),
                            completedTaskGroups =
                                persistentMapOf(
                                    CompletedGroupKey.ByTitle to persistentListOf(completedTask),
                                ),
                            categories = persistentListOf(DEFAULT_INBOX_CATEGORY),
                            showCompletedTasks = true,
                            sortOption = SortOption.BY_TITLE,
                        ),
                    onEvent = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Completed", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("A → Z").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alpha task").assertIsDisplayed()
    }

    // Regression test for the isFirstVisibleItem fix: CompletedTasksSeparator used to keep its
    // 28dp top padding even when it was the only thing in the list (a category with no active
    // tasks), sitting noticeably lower than the top-of-list gap every other first item gets.
    @Test
    fun completedSection_asOnlyContent_hasSameTopInsetAsFirstActiveTask() {
        val completedTask =
            Task(
                id = 1,
                title = "Done task",
                description = "",
                isCompleted = true,
                categoryId = DEFAULT_INBOX_CATEGORY.id,
                sortOrder = 0,
            )
        val completedOnlyState =
            TasksContract.UiState(
                isLoading = false,
                tasks = persistentListOf(completedTask),
                completedTaskGroups =
                    persistentMapOf(
                        CompletedGroupKey.Flat to persistentListOf(completedTask),
                    ),
                categories = persistentListOf(DEFAULT_INBOX_CATEGORY),
                showCompletedTasks = true,
                sortOption = SortOption.MANUAL,
            )

        val activeTask =
            Task(
                id = 2,
                title = "Active task",
                description = "",
                categoryId = DEFAULT_INBOX_CATEGORY.id,
                sortOrder = 0,
            )
        val activeOnlyState =
            TasksContract.UiState(
                isLoading = false,
                tasks = persistentListOf(activeTask),
                activeTasks =
                    persistentMapOf(
                        ActiveGroupKey.Flat to persistentListOf(activeTask),
                    ),
                categories = persistentListOf(DEFAULT_INBOX_CATEGORY),
                sortOption = SortOption.MANUAL,
            )

        // A single setContent with mutable state (rather than two setContent calls) — the test
        // rule's underlying Activity only accepts one setContent call per test.
        var uiState by mutableStateOf(completedOnlyState)
        composeTestRule.setContent {
            TempoTheme {
                TasksContent(uiState = uiState, onEvent = {})
            }
        }
        composeTestRule.waitForIdle()
        val completedSeparatorTop =
            composeTestRule
                .onNodeWithText("Completed", substring = true)
                .fetchSemanticsNode()
                .boundsInRoot.top

        uiState = activeOnlyState
        composeTestRule.waitForIdle()
        val activeTaskTop =
            composeTestRule
                .onNodeWithText("Active task")
                .fetchSemanticsNode()
                .boundsInRoot.top

        assertEquals(activeTaskTop, completedSeparatorTop, 1f)
    }
}
