package com.mandrecode.tempo.core.ui.util

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue

@Stable
internal class DescriptionEditorState(
    initialValue: TextFieldValue,
) {
    var value by mutableStateOf(initialValue)
        private set

    fun update(value: TextFieldValue) {
        this.value = value
    }
}
