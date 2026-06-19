package com.mandrecode.tempo.features.tasks.presentation.components.sections

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private val addTaskContentDescription: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.add_task)

private val deleteAllCompletedTasksContentDescription: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.delete_all_completed_tasks)

class QuickTaskEntryBarTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun renderBar(
        onAddTask: (String) -> Unit = {},
        sortOption: SortOption = SortOption.MANUAL,
        onSortClick: () -> Unit = {},
        showClearCompleted: Boolean = false,
        onClearCompletedClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            TempoTheme {
                QuickTaskEntryBar(
                    onAddTask = onAddTask,
                    sortOption = sortOption,
                    onSortClick = onSortClick,
                    showClearCompleted = showClearCompleted,
                    onClearCompletedClick = onClearCompletedClick,
                )
            }
        }
    }

    @Test
    fun displaysPlaceholderText() {
        renderBar()

        composeTestRule
            .onNodeWithTag(QUICK_TASK_ENTRY_TITLE_FIELD_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun displaysAddTaskButton() {
        renderBar()

        composeTestRule
            .onNodeWithContentDescription(addTaskContentDescription)
            .assertIsDisplayed()
    }

    @Test
    fun titleEnforces65CharLimit() {
        renderBar()

        val longText = "X".repeat(70)
        composeTestRule
            .onNodeWithTag(QUICK_TASK_ENTRY_TITLE_FIELD_TEST_TAG)
            .performTextInput(longText)

        // Only 65 chars should be accepted
        composeTestRule
            .onNodeWithText("X".repeat(65), substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun titleAtExactLimit_isAccepted() {
        renderBar()

        val exactText = "Y".repeat(65)
        composeTestRule
            .onNodeWithTag(QUICK_TASK_ENTRY_TITLE_FIELD_TEST_TAG)
            .performTextInput(exactText)

        composeTestRule
            .onNodeWithText(exactText, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun submitWithText_callsOnAddTask() {
        var submittedTitle = ""
        renderBar(onAddTask = { submittedTitle = it })

        composeTestRule
            .onNodeWithTag(QUICK_TASK_ENTRY_TITLE_FIELD_TEST_TAG)
            .performTextInput("Buy groceries")

        composeTestRule
            .onNodeWithContentDescription(addTaskContentDescription)
            .performClick()

        assertEquals("Buy groceries", submittedTitle)
    }

    @Test
    fun submitWithText_clearsInputAfterSubmit() {
        renderBar(onAddTask = {})

        composeTestRule
            .onNodeWithTag(QUICK_TASK_ENTRY_TITLE_FIELD_TEST_TAG)
            .performTextInput("Buy groceries")

        composeTestRule
            .onNodeWithContentDescription(addTaskContentDescription)
            .performClick()

        // After submit, the placeholder should be visible again
        composeTestRule
            .onNodeWithTag(QUICK_TASK_ENTRY_TITLE_FIELD_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun submitWithEmptyText_doesNotCallOnAddTask() {
        var callCount = 0
        renderBar(onAddTask = { callCount++ })

        // Click submit without entering text
        composeTestRule
            .onNodeWithContentDescription(addTaskContentDescription)
            .performClick()

        assertEquals(0, callCount)
    }

    @Test
    fun clearCompletedButton_visibleWhenEnabled() {
        renderBar(showClearCompleted = true)

        composeTestRule
            .onNodeWithContentDescription(deleteAllCompletedTasksContentDescription)
            .assertIsDisplayed()
    }

    @Test
    fun clearCompletedButton_hiddenWhenDisabled() {
        renderBar(showClearCompleted = false)

        composeTestRule
            .onNodeWithContentDescription(deleteAllCompletedTasksContentDescription)
            .assertDoesNotExist()
    }

    @Test
    fun clearCompletedButton_callsCallback() {
        var clicked = false
        renderBar(
            showClearCompleted = true,
            onClearCompletedClick = { clicked = true },
        )

        composeTestRule
            .onNodeWithContentDescription(deleteAllCompletedTasksContentDescription)
            .performClick()

        assertTrue(clicked)
    }
}
