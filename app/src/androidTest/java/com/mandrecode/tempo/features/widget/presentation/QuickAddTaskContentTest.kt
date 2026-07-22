package com.mandrecode.tempo.features.widget.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.Category
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test

class QuickAddTaskContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val categories = persistentListOf(Category(id = 1L, name = "Inbox", isDefault = true))

    private fun setContent(
        uiState: QuickAddTaskContract.UiState,
        onEvent: (QuickAddTaskContract.UiEvent) -> Unit = {},
    ) {
        composeTestRule.setContent {
            TempoTheme {
                QuickAddTaskContent(uiState = uiState, onEvent = onEvent)
            }
        }
    }

    @Test
    fun whenTitleIsTyped_thenTitleChangedEventIsEmitted() {
        var emittedEvent: QuickAddTaskContract.UiEvent? = null
        setContent(
            uiState = QuickAddTaskContract.UiState(categories = categories, selectedCategoryId = 1L),
            onEvent = { emittedEvent = it },
        )

        composeTestRule
            .onNodeWithText(context.getString(R.string.widget_quick_add_task_title_hint))
            .performTextInput("Buy groceries")

        assertThat(emittedEvent).isEqualTo(QuickAddTaskContract.UiEvent.TitleChanged("Buy groceries"))
    }

    @Test
    fun givenValidationError_whenRendered_thenErrorMessageIsDisplayed() {
        setContent(
            uiState =
                QuickAddTaskContract.UiState(
                    categories = categories,
                    selectedCategoryId = 1L,
                    titleErrorRes = R.string.task_title_required,
                ),
        )

        composeTestRule
            .onNodeWithText(context.getString(R.string.task_title_required))
            .assertIsDisplayed()
    }

    @Test
    fun whenCategoryChipClicked_thenCategorySelectedEventIsEmitted() {
        var emittedEvent: QuickAddTaskContract.UiEvent? = null
        setContent(
            uiState = QuickAddTaskContract.UiState(categories = categories, selectedCategoryId = 0L),
            onEvent = { emittedEvent = it },
        )

        composeTestRule.onNodeWithText("Inbox").performClick()

        assertThat(emittedEvent).isEqualTo(QuickAddTaskContract.UiEvent.CategorySelected(1L))
    }

    @Test
    fun whenSaveClicked_thenSaveClickedEventIsEmitted() {
        var emittedEvent: QuickAddTaskContract.UiEvent? = null
        setContent(
            uiState = QuickAddTaskContract.UiState(categories = categories, selectedCategoryId = 1L),
            onEvent = { emittedEvent = it },
        )

        composeTestRule.onNodeWithText(context.getString(R.string.save)).performClick()

        assertThat(emittedEvent).isEqualTo(QuickAddTaskContract.UiEvent.SaveClicked)
    }

    @Test
    fun whenCancelClicked_thenCancelClickedEventIsEmitted() {
        var emittedEvent: QuickAddTaskContract.UiEvent? = null
        setContent(
            uiState = QuickAddTaskContract.UiState(categories = categories, selectedCategoryId = 1L),
            onEvent = { emittedEvent = it },
        )

        composeTestRule.onNodeWithText(context.getString(R.string.cancel)).performClick()

        assertThat(emittedEvent).isEqualTo(QuickAddTaskContract.UiEvent.CancelClicked)
    }
}
