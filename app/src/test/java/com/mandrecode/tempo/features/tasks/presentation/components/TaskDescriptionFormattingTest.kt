package com.mandrecode.tempo.features.tasks.presentation.components

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TaskDescriptionFormattingTest {
    @Test
    fun givenDashedItem_whenNewlineInserted_thenContinuesDashList() {
        val result =
            applyTaskDescriptionDashFormatting(
                previousValue = textFieldValue("- Buy milk"),
                proposedValue = textFieldValue("- Buy milk\n"),
            )

        assertThat(result.text).isEqualTo("- Buy milk\n- ")
        assertThat(result.selection).isEqualTo(TextRange(result.text.length))
    }

    @Test
    fun givenIndentedDashedItem_whenNewlineInserted_thenPreservesIndentation() {
        val result =
            applyTaskDescriptionDashFormatting(
                previousValue = textFieldValue("  - Buy milk"),
                proposedValue = textFieldValue("  - Buy milk\n"),
            )

        assertThat(result.text).isEqualTo("  - Buy milk\n  - ")
        assertThat(result.selection).isEqualTo(TextRange(result.text.length))
    }

    @Test
    fun givenCursorInsideDashedItem_whenNewlineInserted_thenPlacesPrefixBeforeRemainingText() {
        val previous = textFieldValue(text = "- Buy milk today", cursor = 10)
        val proposed = textFieldValue(text = "- Buy milk\n today", cursor = 11)

        val result = applyTaskDescriptionDashFormatting(previous, proposed)

        assertThat(result.text).isEqualTo("- Buy milk\n-  today")
        assertThat(result.selection).isEqualTo(TextRange(13))
    }

    @Test
    fun givenCursorAfterDashPrefix_whenItemTextRemains_thenPreservesListFormatting() {
        val previous = textFieldValue(text = "- Buy milk", cursor = 2)
        val proposed = textFieldValue(text = "- \nBuy milk", cursor = 3)

        val result = applyTaskDescriptionDashFormatting(previous, proposed)

        assertThat(result.text).isEqualTo("- \n- Buy milk")
        assertThat(result.selection).isEqualTo(TextRange(5))
    }

    @Test
    fun givenEmptyDashedItem_whenNewlineInserted_thenExitsDashList() {
        val previous = textFieldValue("- Buy milk\n- ")
        val proposed = textFieldValue("- Buy milk\n- \n")

        val result = applyTaskDescriptionDashFormatting(previous, proposed)

        assertThat(result.text).isEqualTo("- Buy milk\n")
        assertThat(result.selection).isEqualTo(TextRange(result.text.length))
    }

    @Test
    fun givenPlainText_whenNewlineInserted_thenLeavesEditUnchanged() {
        val proposed = textFieldValue("Buy milk\n")

        val result =
            applyTaskDescriptionDashFormatting(
                previousValue = textFieldValue("Buy milk"),
                proposedValue = proposed,
            )

        assertThat(result).isEqualTo(proposed)
    }

    @Test
    fun givenMultipleCharactersInserted_whenValueChanges_thenLeavesEditUnchanged() {
        val proposed = textFieldValue("- Buy milk\n- Buy bread")

        val result =
            applyTaskDescriptionDashFormatting(
                previousValue = textFieldValue("- Buy milk"),
                proposedValue = proposed,
            )

        assertThat(result).isEqualTo(proposed)
    }

    @Test
    fun givenSelectedText_whenReplacedWithNewline_thenLeavesEditUnchanged() {
        val proposed = textFieldValue("- Buy\n", cursor = 6)

        val result =
            applyTaskDescriptionDashFormatting(
                previousValue =
                    TextFieldValue(
                        text = "- Buy milk",
                        selection = TextRange(5, 10),
                    ),
                proposedValue = proposed,
            )

        assertThat(result).isEqualTo(proposed)
    }

    private fun textFieldValue(
        text: String,
        cursor: Int = text.length,
    ) = TextFieldValue(text = text, selection = TextRange(cursor))
}
