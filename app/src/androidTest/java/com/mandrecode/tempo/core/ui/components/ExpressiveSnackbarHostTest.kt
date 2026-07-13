package com.mandrecode.tempo.core.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import org.junit.Rule
import org.junit.Test

class ExpressiveSnackbarHostTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun actionableSnackbar_displaysAccessibleUndoAndReportsAction() {
        var result: SnackbarResult? = null
        composeRule.setContent {
            val hostState = remember { SnackbarHostState() }
            TempoTheme {
                ExpressiveSnackbarHost(hostState)
            }
            LaunchedEffect(hostState) {
                result =
                    hostState.showSnackbar(
                        message = "Task deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Indefinite,
                    )
            }
        }

        composeRule.onNodeWithText("Task deleted").assertIsDisplayed()
        composeRule
            .onNodeWithText("Undo")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeRule.waitForIdle()

        assertThat(result).isEqualTo(SnackbarResult.ActionPerformed)
    }

    @Test
    fun messageSnackbar_programmaticDismissReportsDismissed() {
        lateinit var hostState: SnackbarHostState
        var result: SnackbarResult? = null
        composeRule.setContent {
            hostState = remember { SnackbarHostState() }
            TempoTheme {
                ExpressiveSnackbarHost(hostState)
            }
            LaunchedEffect(hostState) {
                result = hostState.showSnackbar("Saved", duration = SnackbarDuration.Indefinite)
            }
        }

        composeRule.onNodeWithText("Saved").assertIsDisplayed()
        composeRule.runOnIdle { hostState.currentSnackbarData?.dismiss() }
        composeRule.waitForIdle()

        assertThat(result).isEqualTo(SnackbarResult.Dismissed)
    }
}
