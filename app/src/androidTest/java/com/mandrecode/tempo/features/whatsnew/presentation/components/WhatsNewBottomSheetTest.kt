package com.mandrecode.tempo.features.whatsnew.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.whatsnew.presentation.model.WhatsNewEntry
import org.junit.Rule
import org.junit.Test

class WhatsNewBottomSheetTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun targetContext() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun entry() =
        WhatsNewEntry(
            versionCode = 1_001_000,
            versionName = "1.1.0",
            titleRes = R.string.whats_new_210_title,
            descriptionRes = R.string.whats_new_210_description,
        )

    @Test
    fun givenEntry_whenRendered_thenLegendAndDescriptionAreDisplayed() {
        composeTestRule.setContent {
            TempoTheme {
                WhatsNewBottomSheet(entry = entry(), onDismissRequest = {})
            }
        }

        val legend =
            targetContext().getString(
                R.string.whats_new_legend,
                "1.1.0",
                targetContext().getString(R.string.whats_new_210_title),
            )
        composeTestRule.onNodeWithText(legend).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(targetContext().getString(R.string.whats_new_210_description))
            .assertIsDisplayed()
    }

    @Test
    fun givenSheet_whenGotItClicked_thenDismissRequestIsInvoked() {
        var dismissed = false
        composeTestRule.setContent {
            TempoTheme {
                WhatsNewBottomSheet(entry = entry(), onDismissRequest = { dismissed = true })
            }
        }

        composeTestRule.onNodeWithText(targetContext().getString(R.string.whats_new_got_it)).performClick()

        // Wait for the sheet hide animation to complete and trigger onDismissRequest.
        composeTestRule.waitUntil(timeoutMillis = 5000) { dismissed }
        assertThat(dismissed).isTrue()
    }
}
