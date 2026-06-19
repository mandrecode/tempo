package com.mandrecode.tempo.features.tasks.presentation.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.core.ui.components.TASK_COMPLETION_CHECKBOX_TEST_TAG
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.DEFAULT_INBOX_CATEGORY
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.presentation.TasksContract
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private val markTaskNotCompleted: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.mark_as_not_completed)

private val taskTitlePlaceholder: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.task_title_placeholder)

private val addTaskLabel: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.add_task)

private val addDetailsPlaceholder: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.add_details)

private val descriptionLabel: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.description)

private val cancelLabel: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.cancel)

private val discardChangesLabel: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.discard_changes)

private val discardLabel: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.discard)

private fun defaultFormState() = TasksContract.TaskFormState()

class TaskBottomSheetTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysTaskTitleField() {
        composeTestRule.setContent {
            TempoTheme {
                TaskBottomSheet(
                    task = null,
                    categories = listOf(DEFAULT_INBOX_CATEGORY),
                    selectedCategoryIdFromFilter = null,
                    formState = defaultFormState(),
                    onSetPriority = {},
                    onClearPriority = {},
                    onSetReminder = { _, _, _, _, _ -> },
                    onClearReminder = {},
                    onSetPeriodicity = {},
                    onClearPeriodicity = {},
                    onSetPeriodicityInterval = {},
                    onSetRepeatDays = {},
                    onSetMonthDayOption = {},
                    onDismiss = {},
                    onClearErrors = {},
                    onConfirm = { _, _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithTag(TASK_BOTTOM_SHEET_TITLE_FIELD_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun displaysDiscardDialog_whenUnsavedChangesExist_andCancelIsClicked() {
        var dismissed = false
        composeTestRule.setContent {
            TempoTheme {
                TaskBottomSheet(
                    task = null,
                    categories = listOf(DEFAULT_INBOX_CATEGORY),
                    selectedCategoryIdFromFilter = null,
                    formState = defaultFormState(),
                    onSetPriority = {},
                    onClearPriority = {},
                    onSetReminder = { _, _, _, _, _ -> },
                    onClearReminder = {},
                    onSetPeriodicity = {},
                    onClearPeriodicity = {},
                    onSetPeriodicityInterval = {},
                    onSetRepeatDays = {},
                    onSetMonthDayOption = {},
                    onDismiss = { dismissed = true },
                    onClearErrors = {},
                    onConfirm = { _, _, _ -> },
                )
            }
        }

        // Enter some text
        composeTestRule
            .onNodeWithTag(TASK_BOTTOM_SHEET_TITLE_FIELD_TEST_TAG)
            .performTextInput("My Task")

        // Click Cancel
        composeTestRule.onNodeWithText(cancelLabel).performScrollTo().performClick()

        // Verify dialog is shown
        composeTestRule.onNodeWithText(discardChangesLabel).assertIsDisplayed()

        // Ensure not dismissed yet
        assertFalse(dismissed)

        // Click Discard
        composeTestRule.onNodeWithText(discardLabel).performClick()

        // Wait for the sheet hide animation to complete and trigger onDismiss
        composeTestRule.waitUntil(timeoutMillis = 5000) { dismissed }

        // Verify dismissed
        assertTrue(dismissed)
    }

    @Test
    fun displaysSaveButton() {
        composeTestRule.setContent {
            TempoTheme {
                TaskBottomSheet(
                    task = null,
                    categories = listOf(DEFAULT_INBOX_CATEGORY),
                    selectedCategoryIdFromFilter = null,
                    formState = defaultFormState(),
                    onSetPriority = {},
                    onClearPriority = {},
                    onSetReminder = { _, _, _, _, _ -> },
                    onClearReminder = {},
                    onSetPeriodicity = {},
                    onClearPeriodicity = {},
                    onSetPeriodicityInterval = {},
                    onSetRepeatDays = {},
                    onSetMonthDayOption = {},
                    onDismiss = {},
                    onClearErrors = {},
                    onConfirm = { _, _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithText(addTaskLabel).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun displaysCategorySelector() {
        val categories =
            listOf(
                DEFAULT_INBOX_CATEGORY,
                Category(id = 2, name = "Work"),
            )

        composeTestRule.setContent {
            TempoTheme {
                TaskBottomSheet(
                    task = null,
                    categories = categories,
                    selectedCategoryIdFromFilter = null,
                    formState = defaultFormState(),
                    onSetPriority = {},
                    onClearPriority = {},
                    onSetReminder = { _, _, _, _, _ -> },
                    onClearReminder = {},
                    onSetPeriodicity = {},
                    onClearPeriodicity = {},
                    onSetPeriodicityInterval = {},
                    onSetRepeatDays = {},
                    onSetMonthDayOption = {},
                    onDismiss = {},
                    onClearErrors = {},
                    onConfirm = { _, _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithText("Inbox").assertIsDisplayed()
    }

    @Test
    fun displaysDescriptionPlaceholder() {
        composeTestRule.setContent {
            TempoTheme {
                TaskBottomSheet(
                    task = null,
                    categories = listOf(DEFAULT_INBOX_CATEGORY),
                    selectedCategoryIdFromFilter = null,
                    formState = defaultFormState(),
                    onSetPriority = {},
                    onClearPriority = {},
                    onSetReminder = { _, _, _, _, _ -> },
                    onClearReminder = {},
                    onSetPeriodicity = {},
                    onClearPeriodicity = {},
                    onSetPeriodicityInterval = {},
                    onSetRepeatDays = {},
                    onSetMonthDayOption = {},
                    onDismiss = {},
                    onClearErrors = {},
                    onConfirm = { _, _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithText(addDetailsPlaceholder, substring = true).performScrollTo().assertIsDisplayed()
    }

    // --- Regression tests for #398, #424: title overflow and long text ---

    private fun renderSheet() {
        composeTestRule.setContent {
            TempoTheme {
                TaskBottomSheet(
                    task = null,
                    categories = listOf(DEFAULT_INBOX_CATEGORY),
                    selectedCategoryIdFromFilter = null,
                    formState = defaultFormState(),
                    onSetPriority = {},
                    onClearPriority = {},
                    onSetReminder = { _, _, _, _, _ -> },
                    onClearReminder = {},
                    onSetPeriodicity = {},
                    onClearPeriodicity = {},
                    onSetPeriodicityInterval = {},
                    onSetRepeatDays = {},
                    onSetMonthDayOption = {},
                    onDismiss = {},
                    onClearErrors = {},
                    onConfirm = { _, _, _ -> },
                )
            }
        }
    }

    @Test
    fun titleOverflow_at65Chars_movesExcessTextToDescription() {
        renderSheet()

        val titlePart = "A".repeat(65)
        val overflowPart = "OVERFLOW"
        composeTestRule
            .onNodeWithTag(TASK_BOTTOM_SHEET_TITLE_FIELD_TEST_TAG)
            .performTextInput(titlePart + overflowPart)

        // The overflow text should now appear in the description field
        composeTestRule
            .onNodeWithText(overflowPart, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun longTitle_nearLimit_displaysCorrectly() {
        renderSheet()

        val longTitle = "B".repeat(60)
        composeTestRule
            .onNodeWithTag(TASK_BOTTOM_SHEET_TITLE_FIELD_TEST_TAG)
            .performTextInput(longTitle)

        composeTestRule
            .onNodeWithTag(TASK_BOTTOM_SHEET_TITLE_FIELD_TEST_TAG)
            .assertTextContains(longTitle)
    }

    @Test
    fun titleAtExactLimit_doesNotOverflowToDescription() {
        renderSheet()

        val exactTitle = "C".repeat(65)
        composeTestRule
            .onNodeWithTag(TASK_BOTTOM_SHEET_TITLE_FIELD_TEST_TAG)
            .performTextInput(exactTitle)

        // Title should contain all 65 chars
        composeTestRule
            .onNodeWithText(exactTitle, substring = true)
            .assertIsDisplayed()

        // Description placeholder should still be visible (no overflow text)
        composeTestRule
            .onNodeWithText(addDetailsPlaceholder, substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun descriptionIconRow_displaysDescriptionIcon() {
        renderSheet()

        // Verify the description icon is present (top-aligned row from #374 fix)
        composeTestRule
            .onNodeWithContentDescription(descriptionLabel)
            .assertIsDisplayed()
    }

    // --- Tests for #656: completion checkbox in the task bottom sheet ---

    private fun sampleTask(isCompleted: Boolean = false) =
        Task(
            id = 42L,
            title = "Sample task",
            description = "",
            isCompleted = isCompleted,
        )

    @Test
    fun completionCheckbox_notDisplayed_whenCreatingNewTask() {
        composeTestRule.setContent {
            TempoTheme {
                TaskBottomSheet(
                    task = null,
                    categories = listOf(DEFAULT_INBOX_CATEGORY),
                    selectedCategoryIdFromFilter = null,
                    formState = defaultFormState(),
                    onSetPriority = {},
                    onClearPriority = {},
                    onSetReminder = { _, _, _, _, _ -> },
                    onClearReminder = {},
                    onSetPeriodicity = {},
                    onClearPeriodicity = {},
                    onSetPeriodicityInterval = {},
                    onSetRepeatDays = {},
                    onSetMonthDayOption = {},
                    onDismiss = {},
                    onClearErrors = {},
                    onConfirm = { _, _, _ -> },
                    onToggleCompletion = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TASK_COMPLETION_CHECKBOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun completionCheckbox_displayed_whenEditingTask() {
        composeTestRule.setContent {
            TempoTheme {
                TaskBottomSheet(
                    task = sampleTask(),
                    categories = listOf(DEFAULT_INBOX_CATEGORY),
                    selectedCategoryIdFromFilter = null,
                    formState = defaultFormState(),
                    onSetPriority = {},
                    onClearPriority = {},
                    onSetReminder = { _, _, _, _, _ -> },
                    onClearReminder = {},
                    onSetPeriodicity = {},
                    onClearPeriodicity = {},
                    onSetPeriodicityInterval = {},
                    onSetRepeatDays = {},
                    onSetMonthDayOption = {},
                    onDismiss = {},
                    onClearErrors = {},
                    onConfirm = { _, _, _ -> },
                    onToggleCompletion = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TASK_COMPLETION_CHECKBOX_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun completionCheckbox_clicking_invokesOnToggleCompletion() {
        var toggledTask: Task? = null
        val task = sampleTask()
        composeTestRule.setContent {
            TempoTheme {
                TaskBottomSheet(
                    task = task,
                    categories = listOf(DEFAULT_INBOX_CATEGORY),
                    selectedCategoryIdFromFilter = null,
                    formState = defaultFormState(),
                    onSetPriority = {},
                    onClearPriority = {},
                    onSetReminder = { _, _, _, _, _ -> },
                    onClearReminder = {},
                    onSetPeriodicity = {},
                    onClearPeriodicity = {},
                    onSetPeriodicityInterval = {},
                    onSetRepeatDays = {},
                    onSetMonthDayOption = {},
                    onDismiss = {},
                    onClearErrors = {},
                    onConfirm = { _, _, _ -> },
                    onToggleCompletion = { toggledTask = it },
                )
            }
        }

        composeTestRule.onNodeWithTag(TASK_COMPLETION_CHECKBOX_TEST_TAG).performClick()

        assertTrue(toggledTask == task)
    }

    @Test
    fun completionCheckbox_completedTask_showsCompletedContentDescription() {
        composeTestRule.setContent {
            TempoTheme {
                TaskBottomSheet(
                    task = sampleTask(isCompleted = true),
                    categories = listOf(DEFAULT_INBOX_CATEGORY),
                    selectedCategoryIdFromFilter = null,
                    formState = defaultFormState(),
                    onSetPriority = {},
                    onClearPriority = {},
                    onSetReminder = { _, _, _, _, _ -> },
                    onClearReminder = {},
                    onSetPeriodicity = {},
                    onClearPeriodicity = {},
                    onSetPeriodicityInterval = {},
                    onSetRepeatDays = {},
                    onSetMonthDayOption = {},
                    onDismiss = {},
                    onClearErrors = {},
                    onConfirm = { _, _, _ -> },
                    onToggleCompletion = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(markTaskNotCompleted).assertIsDisplayed()
    }

    // Regression test for #655: when the upstream task updates after a toggle (i.e. the
    // ViewModel refreshes the editing snapshot from the live repo), the sheet's checkbox
    // must reflect the new completion state — not stay stuck on the original snapshot.
    @Test
    fun completionCheckbox_reflectsUpstreamStateChange_afterToggle() {
        composeTestRule.setContent {
            TempoTheme {
                var task by remember { mutableStateOf(sampleTask(isCompleted = false)) }
                TaskBottomSheet(
                    task = task,
                    categories = listOf(DEFAULT_INBOX_CATEGORY),
                    selectedCategoryIdFromFilter = null,
                    formState = defaultFormState(),
                    onSetPriority = {},
                    onClearPriority = {},
                    onSetReminder = { _, _, _, _, _ -> },
                    onClearReminder = {},
                    onSetPeriodicity = {},
                    onClearPeriodicity = {},
                    onSetPeriodicityInterval = {},
                    onSetRepeatDays = {},
                    onSetMonthDayOption = {},
                    onDismiss = {},
                    onClearErrors = {},
                    onConfirm = { _, _, _ -> },
                    onToggleCompletion = { task = it.copy(isCompleted = !it.isCompleted) },
                )
            }
        }

        // Initially uncompleted: the checked-state action label is not present yet.
        composeTestRule.onNodeWithContentDescription(markTaskNotCompleted).assertDoesNotExist()

        composeTestRule.onNodeWithTag(TASK_COMPLETION_CHECKBOX_TEST_TAG).performClick()

        // After the upstream prop flips, the checkbox should render the completed state.
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithContentDescription(markTaskNotCompleted)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription(markTaskNotCompleted).assertIsDisplayed()
    }

    // Regression test for #655 follow-up: the periodicity section must remain visible when
    // the task is completed (so users can still see the configured repetition), and its
    // chips must be disabled to prevent edits.
    @Test
    fun completedTask_periodicitySection_remainsVisibleAndDisabled() {
        composeTestRule.setContent {
            TempoTheme {
                TaskBottomSheet(
                    task = sampleTask(isCompleted = true).copy(periodicity = Periodicity.WEEKLY),
                    categories = listOf(DEFAULT_INBOX_CATEGORY),
                    selectedCategoryIdFromFilter = null,
                    formState =
                        defaultFormState().copy(
                            reminderDate = LocalDateTime(2026, 4, 25, 9, 0),
                            periodicity = Periodicity.WEEKLY,
                        ),
                    onSetPriority = {},
                    onClearPriority = {},
                    onSetReminder = { _, _, _, _, _ -> },
                    onClearReminder = {},
                    onSetPeriodicity = {},
                    onClearPeriodicity = {},
                    onSetPeriodicityInterval = {},
                    onSetRepeatDays = {},
                    onSetMonthDayOption = {},
                    onDismiss = {},
                    onClearErrors = {},
                    onConfirm = { _, _, _ -> },
                    onToggleCompletion = {},
                )
            }
        }

        // The selected WEEKLY chip is rendered (proves the periodicity section is still composed)
        // and is disabled (proves it is read-only).
        composeTestRule
            .onNodeWithTag("taskPeriodicityChip_WEEKLY")
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    // Regression test for #655 follow-up: completing a periodic task strips its recurrence
    // fields on the archived copy (see ToggleTaskCompletionUseCase). The sheet snapshots
    // the form-relevant task fields once when it opens and uses that snapshot for its
    // dirty-check, so a refreshed task with stripped recurrence does NOT make the form
    // look dirty — and toggling completion does NOT wipe any in-progress user edits.
    // This test simulates that flow at the UI layer: toggling a periodic task complete
    // must NOT trigger the discard dialog on Cancel, because no user-driven changes have
    // actually been made.
    @Test
    fun completionCheckbox_togglingPeriodicTask_doesNotShowDiscardDialog_onCancel() {
        composeTestRule.setContent {
            TempoTheme {
                var task by remember {
                    mutableStateOf(
                        sampleTask(isCompleted = false).copy(periodicity = Periodicity.WEEKLY),
                    )
                }
                val formState by remember {
                    mutableStateOf(defaultFormState().copy(periodicity = Periodicity.WEEKLY))
                }
                TaskBottomSheet(
                    task = task,
                    categories = listOf(DEFAULT_INBOX_CATEGORY),
                    selectedCategoryIdFromFilter = null,
                    formState = formState,
                    onSetPriority = {},
                    onClearPriority = {},
                    onSetReminder = { _, _, _, _, _ -> },
                    onClearReminder = {},
                    onSetPeriodicity = {},
                    onClearPeriodicity = {},
                    onSetPeriodicityInterval = {},
                    onSetRepeatDays = {},
                    onSetMonthDayOption = {},
                    onDismiss = {},
                    onClearErrors = {},
                    onConfirm = { _, _, _ -> },
                    onToggleCompletion = {
                        // Mirror ToggleTaskCompletionUseCase: archived copy strips recurrence.
                        // Crucially, the form state is NOT re-seeded — the new snapshot-based
                        // dirty-check is what makes this work without wiping user edits.
                        task =
                            it.copy(
                                isCompleted = true,
                                periodicity = null,
                                periodicityInterval = 1,
                                repeatDays = null,
                                monthDayOption = null,
                            )
                    },
                )
            }
        }

        // Toggle the completion checkbox.
        composeTestRule.onNodeWithTag(TASK_COMPLETION_CHECKBOX_TEST_TAG).performClick()

        // Wait for the toggle to be reflected in the UI.
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithContentDescription(markTaskNotCompleted)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Cancel: must NOT show the discard dialog because the form is in sync with
        // the refreshed task — the only "change" was the toggle itself, not user edits.
        composeTestRule.onNodeWithText(cancelLabel).performScrollTo().performClick()

        composeTestRule.onNodeWithText(discardChangesLabel).assertDoesNotExist()
    }
}
