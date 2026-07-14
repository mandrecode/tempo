package com.mandrecode.tempo.core.ui.components

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class TempoDatePickerDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun alternativeConfirmInvokesDateOnlyAction() {
        var selectedDate: LocalDate? = null
        val initialDate = LocalDate(2030, 1, 2)

        composeTestRule.setContent {
            TempoTheme {
                TempoDatePickerDialog(
                    initialDate = initialDate,
                    onConfirm = {},
                    onDismiss = {},
                    confirmLabel = "Choose time",
                    alternativeConfirmLabel = "Use 09:00",
                    onAlternativeConfirm = { selectedDate = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Use 09:00").performClick()

        assertThat(selectedDate).isEqualTo(initialDate)
    }

    @Test
    fun primaryConfirmPreservesExactTimePath() {
        var selectedDate: LocalDate? = null
        val initialDate = LocalDate(2030, 1, 2)

        composeTestRule.setContent {
            TempoTheme {
                TempoDatePickerDialog(
                    initialDate = initialDate,
                    onConfirm = { selectedDate = it },
                    onDismiss = {},
                    confirmLabel = "Choose time",
                    alternativeConfirmLabel = "Use 09:00",
                    onAlternativeConfirm = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Choose time").performClick()

        assertThat(selectedDate).isEqualTo(initialDate)
    }

    @Test
    fun alternativeConfirmIsDisabledWhenCombinationIsNotAllowed() {
        composeTestRule.setContent {
            TempoTheme {
                TempoDatePickerDialog(
                    initialDate = LocalDate(2030, 1, 2),
                    onConfirm = {},
                    onDismiss = {},
                    confirmLabel = "Choose time",
                    alternativeConfirmLabel = "Use 09:00",
                    onAlternativeConfirm = {},
                    isAlternativeConfirmEnabled = { false },
                )
            }
        }

        composeTestRule.onNodeWithText("Use 09:00").assertIsNotEnabled()
    }
}
