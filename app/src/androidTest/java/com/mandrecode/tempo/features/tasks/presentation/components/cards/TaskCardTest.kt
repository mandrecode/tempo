package com.mandrecode.tempo.features.tasks.presentation.components.cards

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test

private val priorityLabel: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.priority_label)

private val periodicityLabel: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.periodicity_label)

private val completedLabel: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.completed)

private val addSubtaskLabel: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.add_subtask)

private val expandLabel: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.expand)

class TaskCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysTaskTitle() {
        val task = Task(id = 1, title = "Test Task", description = "")

        composeTestRule.setContent {
            TempoTheme {
                TaskItem(
                    task = task,
                    onToggleCompletion = {},
                    onEdit = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Test Task").assertIsDisplayed()
    }

    @Test
    fun displaysTaskDescription() {
        val task = Task(id = 1, title = "Task", description = "Some description here")

        composeTestRule.setContent {
            TempoTheme {
                TaskItem(
                    task = task,
                    onToggleCompletion = {},
                    onEdit = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Some description here").assertIsDisplayed()
    }

    @Test
    fun showsPriorityBadge_whenPrioritySet() {
        val task = Task(id = 1, title = "High Priority", description = "", priority = Priority.HIGH)

        composeTestRule.setContent {
            TempoTheme {
                TaskItem(
                    task = task,
                    onToggleCompletion = {},
                    onEdit = {},
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(priorityLabel, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun showsPeriodicityBadge_whenPeriodicitySet() {
        val task =
            Task(
                id = 1,
                title = "Daily Task",
                description = "",
                periodicity = Periodicity.DAILY,
            )

        composeTestRule.setContent {
            TempoTheme {
                TaskItem(
                    task = task,
                    onToggleCompletion = {},
                    onEdit = {},
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(periodicityLabel, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun showsSubtaskCount_whenSubtasksExist() {
        val task = Task(id = 1, title = "Parent", description = "")
        val subtasks =
            listOf(
                Task(id = 2, title = "Sub 1", description = "", parentTaskId = 1),
                Task(id = 3, title = "Sub 2", description = "", parentTaskId = 1),
            )

        composeTestRule.setContent {
            TempoTheme {
                TaskItem(
                    task = task,
                    onToggleCompletion = {},
                    onEdit = {},
                    subtasks = subtasks,
                    isSubtasksExpanded = true,
                )
            }
        }

        composeTestRule.onNodeWithText("Sub 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sub 2").assertIsDisplayed()
    }

    @Test
    fun showsCompletionCheckbox() {
        val task = Task(id = 1, title = "Checkable", description = "", isCompleted = true)

        composeTestRule.setContent {
            TempoTheme {
                TaskItem(
                    task = task,
                    onToggleCompletion = {},
                    onEdit = {},
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(completedLabel, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun showsAddSubtaskButton_whenNoSubtasks() {
        val task = Task(id = 1, title = "Simple Task", description = "")

        composeTestRule.setContent {
            TempoTheme {
                TaskItem(
                    task = task,
                    onToggleCompletion = {},
                    onEdit = {},
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(addSubtaskLabel, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun showsExpandAndAddSubtaskButtons_whenSubtasksExist() {
        val task = Task(id = 1, title = "Parent", description = "")
        val subtasks =
            listOf(
                Task(id = 2, title = "Sub 1", description = "", parentTaskId = 1),
            )

        composeTestRule.setContent {
            TempoTheme {
                TaskItem(
                    task = task,
                    onToggleCompletion = {},
                    onEdit = {},
                    subtasks = subtasks,
                    isSubtasksExpanded = false,
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(expandLabel, substring = true)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(addSubtaskLabel, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun topLevelCompletedMetadataDate_staysSingleLine_inConstrainedWidth() {
        val task =
            Task(
                id = 1,
                title = "Finalize rollout checklist",
                description = "",
                priority = Priority.HIGH,
                periodicity = Periodicity.WEEKLY,
                isCompleted = true,
                completedAt = LocalDateTime(2026, 6, 15, 9, 45),
            )

        composeTestRule.setContent {
            TempoTheme {
                TaskItem(
                    task = task,
                    onToggleCompletion = {},
                    onEdit = {},
                    modifier = Modifier.width(280.dp),
                )
            }
        }

        assertSingleLineText(TASK_METADATA_COMPLETED_DATE_TAG)
    }

    @Test
    fun topLevelReminderMetadataDate_staysSingleLine_inConstrainedWidth() {
        val task =
            Task(
                id = 1,
                title = "Finalize rollout checklist",
                description = "",
                priority = Priority.HIGH,
                periodicity = Periodicity.WEEKLY,
                reminderDate = LocalDateTime(2026, 6, 15, 9, 45),
            )

        composeTestRule.setContent {
            TempoTheme {
                TaskItem(
                    task = task,
                    onToggleCompletion = {},
                    onEdit = {},
                    modifier = Modifier.width(280.dp),
                )
            }
        }

        assertSingleLineText(TASK_METADATA_REMINDER_DATE_TAG)
    }

    @Test
    fun denseMetadata_showsDateBadge_inConstrainedWidth() {
        val parentTaskId = 10L
        val task =
            Task(
                id = parentTaskId,
                title = "Launch release",
                description = "",
                priority = Priority.HIGH,
                periodicity = Periodicity.WEEKLY,
                reminderDate = LocalDateTime(2026, 6, 15, 9, 45),
            )
        val subtasks =
            listOf(
                Task(id = 101, title = "A", description = "", parentTaskId = parentTaskId, isCompleted = true),
                Task(id = 102, title = "B", description = "", parentTaskId = parentTaskId, isCompleted = false),
            )

        composeTestRule.setContent {
            TempoTheme {
                TaskItem(
                    task = task,
                    onToggleCompletion = {},
                    onEdit = {},
                    subtasks = subtasks,
                    isSubtasksExpanded = false,
                    modifier = Modifier.width(320.dp),
                )
            }
        }

        composeTestRule.onNodeWithTag(TASK_METADATA_REMINDER_DATE_TAG, useUnmergedTree = true).assertIsDisplayed()
    }

    private fun assertSingleLineText(tag: String) {
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) {
            it(textLayoutResults)
        }
        assertThat(textLayoutResults).isNotEmpty()
        assertThat(textLayoutResults.first().lineCount).isEqualTo(1)
    }
}
