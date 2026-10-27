package com.mandrecode.tempo.features.tasks.presentation.components

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TaskEditorFocusStateTest {
    @Test
    fun givenNoFieldFocused_thenBackIsNotOwned() {
        val state = TaskEditorFocusState()

        assertThat(state.focusedField).isEqualTo(TaskEditorField.None)
        assertThat(state.ownsBack).isFalse()
    }

    @Test
    fun givenTitleFocused_thenTitleOwnsBack() {
        val state = keyboardOpenState()

        state.onTitleFocusChanged(true)

        assertThat(state.focusedField).isEqualTo(TaskEditorField.Title)
        assertThat(state.ownsBack).isTrue()
    }

    @Test
    fun givenTitleToDescriptionHandoff_whenGainArrivesBeforeLoss_thenNeverPassesThroughNone() {
        val state = keyboardOpenState()
        state.onTitleFocusChanged(true)

        state.onDescriptionFocusChanged(true)
        assertThat(state.ownsBack).isTrue()
        state.onTitleFocusChanged(false)

        assertThat(state.focusedField).isEqualTo(TaskEditorField.Description)
    }

    @Test
    fun givenDescriptionToTitleHandoff_whenLossArrivesBeforeGain_thenEndsOnTitle() {
        val state = keyboardOpenState()
        state.onDescriptionFocusChanged(true)

        state.onDescriptionFocusChanged(false)
        state.onTitleFocusChanged(true)

        assertThat(state.focusedField).isEqualTo(TaskEditorField.Title)
        assertThat(state.ownsBack).isTrue()
    }

    @Test
    fun givenFocusedField_whenFocusIsCleared_thenBackIsReleased() {
        val state = keyboardOpenState()
        state.onDescriptionFocusChanged(true)

        state.onDescriptionFocusChanged(false)

        assertThat(state.focusedField).isEqualTo(TaskEditorField.None)
        assertThat(state.ownsBack).isFalse()
    }

    @Test
    fun givenFocusedField_whenKeyboardClosesOnItsOwn_thenBackIsReleasedButFocusIsKept() {
        val state = keyboardOpenState()
        state.onDescriptionFocusChanged(true)

        state.onKeyboardVisibilityChanged(false)

        assertThat(state.focusedField).isEqualTo(TaskEditorField.Description)
        assertThat(state.ownsBack).isFalse()
    }

    @Test
    fun givenFocusedField_whenKeyboardReopens_thenBackIsOwnedAgain() {
        val state = TaskEditorFocusState()
        state.onTitleFocusChanged(true)
        assertThat(state.ownsBack).isFalse()

        state.onKeyboardVisibilityChanged(true)

        assertThat(state.ownsBack).isTrue()
    }

    private fun keyboardOpenState() = TaskEditorFocusState().apply { onKeyboardVisibilityChanged(true) }
}
