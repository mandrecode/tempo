package com.mandrecode.tempo.features.whatsnew.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.whatsnew.presentation.WhatsNewRegistry
import org.junit.Rule
import org.junit.Test

class WhatsNewBottomSheetTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun targetContext() = InstrumentationRegistry.getInstrumentation().targetContext

    // References the registry's actual latest entry rather than a hardcoded copy, so this test
    // keeps validating real rendered content as WhatsNewRegistry.latest evolves.
    private fun entry() = WhatsNewRegistry.latest

    @Test
    fun givenEntry_whenRendered_thenLegendAndDescriptionAreDisplayed() {
        val latestEntry = entry()
        composeTestRule.setContent {
            TempoTheme {
                WhatsNewBottomSheet(entry = latestEntry, onDismissRequest = {})
            }
        }

        val legend =
            targetContext().getString(
                R.string.whats_new_legend,
                latestEntry.versionName,
                targetContext().getString(latestEntry.titleRes),
            )
        composeTestRule.onNodeWithText(legend).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(targetContext().getString(latestEntry.descriptionRes))
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
