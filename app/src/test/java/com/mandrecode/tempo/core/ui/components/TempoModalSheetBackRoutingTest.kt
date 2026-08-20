package com.mandrecode.tempo.core.ui.components

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TempoModalSheetBackRoutingTest {
    @Test
    fun givenImeVisible_whenRoutingBack_thenSheetPredictiveBackIsDisabled() {
        assertThat(shouldHandleSheetPredictiveBack(isImeVisible = true)).isFalse()
    }

    @Test
    fun givenImeHidden_whenRoutingBack_thenSheetPredictiveBackIsEnabled() {
        assertThat(shouldHandleSheetPredictiveBack(isImeVisible = false)).isTrue()
    }
}
