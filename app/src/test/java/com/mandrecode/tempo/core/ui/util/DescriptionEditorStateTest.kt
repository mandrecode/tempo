package com.mandrecode.tempo.core.ui.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DescriptionEditorStateTest {
    @Test
    fun givenUpdatedValue_whenReadingEditorState_thenPreservesTextAndSelection() {
        val state = DescriptionEditorState(TextFieldValue("Initial"))
        val updated = TextFieldValue(text = "Updated", selection = TextRange(4))

        state.update(updated)

        assertThat(state.value).isEqualTo(updated)
    }
}
