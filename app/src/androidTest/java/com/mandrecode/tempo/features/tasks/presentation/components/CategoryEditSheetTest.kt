package com.mandrecode.tempo.features.tasks.presentation.components

import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import org.junit.Rule
import org.junit.Test

class CategoryEditSheetTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun givenFocusedCategoryName_whenDoneIsPerformed_thenFocusIsClearedWithoutSaving() {
        var saveCalls = 0
        composeTestRule.setContent {
            TempoTheme {
                CategoryEditSheet(
                    category = null,
                    categories = emptyList(),
                    nameError = null,
                    onDismiss = {},
                    onSave = { _, _, _, _ -> saveCalls++ },
                    onDelete = null,
                    onClearError = {},
                )
            }
        }

        val nameField = composeTestRule.onNodeWithTag(CATEGORY_EDIT_SHEET_NAME_FIELD_TEST_TAG)
        nameField.performClick()
        nameField.assertIsFocused()
        nameField.performImeAction()
        nameField.assertIsNotFocused()

        assertThat(saveCalls).isEqualTo(0)
    }
}
