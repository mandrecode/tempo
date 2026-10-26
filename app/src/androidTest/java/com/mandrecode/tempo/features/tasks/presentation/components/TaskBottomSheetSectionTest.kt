package com.mandrecode.tempo.features.tasks.presentation.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.TaskSection
import com.mandrecode.tempo.features.tasks.presentation.TasksContract
import org.junit.Rule
import org.junit.Test

/** The editor's Section options (#61). */
class TaskBottomSheetSectionTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val sv = Category(id = 3, name = "S&V", isDefault = true)
    private val work = Category(id = 4, name = "Work")
    private val cat = TaskSection(id = 10, categoryId = 3, name = "Cat", sortOrder = 0)
    private val shopping = TaskSection(id = 11, categoryId = 3, name = "Shopping", sortOrder = 1)

    private fun setSheet(task: Task) {
        composeTestRule.setContent {
            // The ViewModel's role: the selection lives in the form state.
            var formState by mutableStateOf(TasksContract.TaskFormState(editingTask = task, sectionId = task.sectionId))
            TempoTheme {
                TaskBottomSheet(
                    task = task,
                    categories = listOf(sv, work),
                    selectedCategoryIdFromFilter = null,
                    formState = formState,
                    sectionsByCategory = mapOf(sv.id to listOf(cat, shopping)),
                    onSelectSection = { formState = formState.copy(sectionId = it) },
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
    fun pickSection_selectsIt() {
        setSheet(Task(id = 1, title = "Buy litter", description = "", categoryId = sv.id))

        composeTestRule.onNodeWithTag("taskSectionChip_none").performScrollTo().assertIsOn()
        composeTestRule.onNodeWithTag("taskSectionChip_${cat.id}").performClick()

        composeTestRule.onNodeWithTag("taskSectionChip_${cat.id}").assertIsOn()
    }

    @Test
    fun categoryWithoutSections_showsNoSectionOptions() {
        setSheet(Task(id = 1, title = "Standup", description = "", categoryId = work.id))

        composeTestRule.onNodeWithTag("taskSectionChip_none").assertDoesNotExist()
    }

    @Test
    fun changingCategory_resetsTheSection() {
        setSheet(Task(id = 1, title = "Buy litter", description = "", categoryId = sv.id, sectionId = cat.id))
        composeTestRule.onNodeWithTag("taskSectionChip_${cat.id}").performScrollTo().assertIsOn()

        composeTestRule.onNodeWithText("Work").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("taskSectionChip_none").assertDoesNotExist()
        composeTestRule.onNodeWithText("S&V").performScrollTo().performClick()

        composeTestRule
            .onNodeWithTag("taskSectionChip_none")
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsOn()
    }

    @Test
    fun subtask_showsNoSectionOptions() {
        setSheet(Task(id = 2, title = "Sub", description = "", categoryId = sv.id, parentTaskId = 1))

        composeTestRule.onNodeWithTag("taskSectionChip_none").assertDoesNotExist()
    }
}
