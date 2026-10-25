package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import org.junit.Rule
import org.junit.Test

class SelectorButtonGroupTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val options = listOf("High", "Medium", "Low")
    private var selected by mutableStateOf<String?>(null)

    private fun setGroup() {
        composeTestRule.setContent {
            TempoTheme {
                // The width a 360 dp phone's task editor leaves the priority row.
                Box(modifier = Modifier.width(260.dp)) {
                    SelectorButtonGroup(
                        options = options,
                        isChecked = { it == selected },
                        onClick = { selected = if (selected == it) null else it },
                        label = { it },
                    )
                }
            }
        }
    }

    @Test
    fun everyOptionFits_asAFullTarget() {
        setGroup()

        options.forEach { label ->
            composeTestRule.onNodeWithText(label).assertIsDisplayed()
        }
        // The toggles themselves, not just their labels, carry the 48 dp height.
        val height = composeTestRule.onNodeWithText("Medium", useUnmergedTree = false).getUnclippedBoundsInRoot().height
        assertThat(height.value).isAtLeast(48f)
    }

    @Test
    fun tappingSelects_andTappingAgainClears() {
        setGroup()

        composeTestRule.onNodeWithText("Medium").performClick()
        composeTestRule.onNodeWithText("Medium").assertIsOn()
        composeTestRule.onNodeWithText("High").assertIsOff()

        composeTestRule.onNodeWithText("Medium").performClick()
        composeTestRule.onNodeWithText("Medium").assertIsOff()
        assertThat(selected).isNull()
    }

    /**
     * The monthly options in Spanish at twice the font size, in the width a phone's task editor
     * leaves them: the group scrolls instead, and every label is shown whole.
     */
    @Test
    fun longLabels_scrollInsteadOfBeingCut() {
        val labels = listOf("El mismo día", "Primer día del mes", "Último día del mes")
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                TempoTheme {
                    Box(modifier = Modifier.width(260.dp)) {
                        SelectorButtonGroup(
                            options = labels,
                            isChecked = { it == labels.first() },
                            onClick = {},
                            label = { it },
                        )
                    }
                }
            }
        }

        labels.forEach { label ->
            // The button (merged node) must hold its whole label (unmerged text node).
            val button = composeTestRule.onNodeWithText(label).performScrollTo().getUnclippedBoundsInRoot()
            val text = composeTestRule.onNodeWithText(label, useUnmergedTree = true).getUnclippedBoundsInRoot()
            assertThat(text.left.value).isAtLeast(button.left.value)
            assertThat(text.right.value).isAtMost(button.right.value)
            composeTestRule.onNodeWithText(label).assertIsDisplayed()
        }
    }
}
