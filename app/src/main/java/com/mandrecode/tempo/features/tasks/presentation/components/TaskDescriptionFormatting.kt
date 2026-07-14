package com.mandrecode.tempo.features.tasks.presentation.components

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

private val DASH_LIST_LINE_PATTERN = Regex("^([ \\t]*)- (.*)$")

internal fun applyTaskDescriptionDashFormatting(
    previousValue: TextFieldValue,
    proposedValue: TextFieldValue,
): TextFieldValue {
    val insertionIndex = proposedValue.selection.start - 1
    val isSingleNewlineInsertion =
        previousValue.selection.collapsed &&
            proposedValue.selection.collapsed &&
            proposedValue.composition == null &&
            proposedValue.text.getOrNull(insertionIndex) == '\n' &&
            previousValue.selection.start == insertionIndex &&
            proposedValue.text.removeRange(insertionIndex, insertionIndex + 1) == previousValue.text
    val lineStart =
        if (isSingleNewlineInsertion) {
            proposedValue.text.lastIndexOf('\n', startIndex = insertionIndex - 1) + 1
        } else {
            0
        }
    val match =
        if (isSingleNewlineInsertion) {
            DASH_LIST_LINE_PATTERN.matchEntire(proposedValue.text.substring(lineStart, insertionIndex))
        } else {
            null
        }

    return when {
        match == null -> proposedValue
        match.groupValues[2].isBlank() ->
            TextFieldValue(
                text = proposedValue.text.removeRange(lineStart, insertionIndex + 1),
                selection = TextRange(lineStart),
            )
        else -> {
            val prefix = "${match.groupValues[1]}- "
            TextFieldValue(
                text =
                    proposedValue.text.substring(0, insertionIndex + 1) + prefix +
                        proposedValue.text.substring(insertionIndex + 1),
                selection = TextRange(proposedValue.selection.start + prefix.length),
            )
        }
    }
}
