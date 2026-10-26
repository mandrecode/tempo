package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.TaskSection
import com.mandrecode.tempo.features.tasks.presentation.model.ActiveGroupKey
import com.mandrecode.tempo.features.tasks.presentation.model.SectionUi
import com.mandrecode.tempo.features.tasks.presentation.model.SectionedTaskList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Rule
import org.junit.Test

/** The Tasks list of a category with sections (#61). */
class TasksSectionedContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val sv = Category(id = 3, name = "S&V", isDefault = true)
    private val cat = TaskSection(id = 10, categoryId = 3, name = "Cat", sortOrder = 0)
    private val chores = TaskSection(id = 11, categoryId = 3, name = "Home chores", sortOrder = 1)
    private val garden = TaskSection(id = 12, categoryId = 3, name = "Garden", sortOrder = 2)
    private val events = mutableListOf<TasksContract.UiEvent>()

    private fun task(
        id: Long,
        title: String,
        sectionId: Long?,
    ) = Task(id = id, title = title, description = "", categoryId = sv.id, sectionId = sectionId)

    private fun setContent(
        collapsed: PersistentSet<Long> = persistentSetOf(),
        sectioned: Boolean = true,
    ) {
        val unsectioned = task(1, "Pay the internet bill", null)
        val list =
            SectionedTaskList(
                unsectioned = persistentListOf(unsectioned),
                sections =
                    persistentListOf(
                        SectionUi(cat, persistentListOf(task(2, "Buy litter", cat.id)), overdueCount = 0),
                        SectionUi(chores, persistentListOf(task(3, "Descale the kettle", chores.id)), overdueCount = 1),
                        SectionUi(garden, persistentListOf(), overdueCount = 0),
                    ),
            )
        composeTestRule.setContent {
            TempoTheme {
                TasksContent(
                    uiState =
                        TasksContract.UiState(
                            isLoading = false,
                            categories = persistentListOf(sv),
                            selectedCategoryId = sv.id,
                            activeTasks = persistentMapOf(ActiveGroupKey.Flat to persistentListOf(unsectioned)),
                            sectionedList = if (sectioned) list else null,
                            collapsedSectionIds = collapsed,
                        ),
                    onEvent = { events += it },
                )
            }
        }
    }

    @Test
    fun unsectionedTasksComeFirst_thenSectionsInOrder() {
        setContent()

        val unsectioned = composeTestRule.onNodeWithText("Pay the internet bill").getUnclippedBoundsInRoot().top
        val catHeader = composeTestRule.onNodeWithText("Cat").getUnclippedBoundsInRoot().top
        val catTask = composeTestRule.onNodeWithText("Buy litter").getUnclippedBoundsInRoot().top
        val choresHeader = composeTestRule.onNodeWithText("Home chores").getUnclippedBoundsInRoot().top

        assertThat(unsectioned).isLessThan(catHeader)
        assertThat(catHeader).isLessThan(catTask)
        assertThat(catTask).isLessThan(choresHeader)
    }

    @Test
    fun collapsedSectionHidesTasksAndShowsOverdue() {
        setContent(collapsed = persistentSetOf(chores.id))

        composeTestRule.onNodeWithText("Descale the kettle").assertDoesNotExist()
        composeTestRule.onNodeWithText("1 overdue").assertIsDisplayed()

        composeTestRule.onNodeWithText("Home chores").performClick()
        assertThat(events).contains(TasksContract.UiEvent.ToggleSectionCollapsed(chores.id))
    }

    @Test
    fun emptySectionShowsPlaceholder() {
        setContent()

        composeTestRule.onNodeWithText("No tasks yet", substring = true).assertIsDisplayed()
    }

    @Test
    fun categoryWithoutSectionsShowsNoSectionUi() {
        setContent(sectioned = false)

        composeTestRule.onNodeWithText("Pay the internet bill").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add section").assertDoesNotExist()
        composeTestRule.onNodeWithText("Cat").assertDoesNotExist()
    }

    @Test
    fun headerAddActionOpensEditorInThatSection() {
        setContent()

        composeTestRule.onNodeWithContentDescription("Add task to Cat").performClick()

        assertThat(events).contains(TasksContract.UiEvent.ShowTaskDialog(sectionId = cat.id))
    }

    @Test
    fun renameHappensInPlace() {
        setContent()

        composeTestRule.onNodeWithContentDescription("Cat section options").performClick()
        composeTestRule.onNodeWithText("Rename").performClick()
        composeTestRule.onNodeWithText("Cat").performTextReplacement("Cats")
        composeTestRule.onNodeWithContentDescription("Save section name").performClick()

        assertThat(events).contains(TasksContract.UiEvent.RenameSection(cat.id, "Cats"))
    }

    @Test
    fun deleteFromMenuEmitsDelete() {
        setContent()

        composeTestRule.onNodeWithContentDescription("Garden section options").performClick()
        composeTestRule.onNodeWithText("Delete section").performClick()

        assertThat(events).contains(TasksContract.UiEvent.DeleteSection(garden))
    }

    @Test
    fun addSectionAtEnd() {
        setContent()

        composeTestRule.onNodeWithText("Add section").performClick()
        composeTestRule.onNodeWithText("Section name").performTextReplacement("Bills")
        composeTestRule.onNodeWithContentDescription("Save section name").performClick()

        assertThat(events).contains(TasksContract.UiEvent.AddSection("Bills", null))
    }

    @Test
    fun reorderSectionsOpensItsOwnSheet() {
        setContent()

        composeTestRule.onNodeWithContentDescription("Cat section options").performClick()
        composeTestRule.onNodeWithText("Reorder sections").performClick()

        composeTestRule.onNodeWithText("Done").assertIsDisplayed()
    }
}
