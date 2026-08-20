package com.mandrecode.tempo.core.ui.components

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TempoModalSheetImeTest {
    @Test
    fun givenImeVisible_whenRoutingBack_thenSheetPredictiveBackIsDisabled() {
        assertThat(shouldHandleSheetPredictiveBack(isImeVisible = true)).isFalse()
    }

    @Test
    fun givenImeHidden_whenRoutingBack_thenSheetPredictiveBackIsEnabled() {
        assertThat(shouldHandleSheetPredictiveBack(isImeVisible = false)).isTrue()
    }

    @Test
    fun givenImeFocusHandoff_whenResolvingBottomInset_thenPreviousInsetIsRetained() {
        assertThat(stableImeInset(current = 0, animationSource = 800, animationTarget = 800)).isEqualTo(800)
    }

    @Test
    fun givenImeFocusHandoffToShorterKeyboard_whenResolvingInset_thenSharedSpaceIsRetained() {
        assertThat(stableImeInset(current = 0, animationSource = 800, animationTarget = 600)).isEqualTo(600)
    }

    @Test
    fun givenImeHiding_whenResolvingBottomInset_thenCurrentAnimatedInsetIsUsed() {
        assertThat(stableImeInset(current = 400, animationSource = 800, animationTarget = 0)).isEqualTo(400)
    }

    @Test
    fun givenImeShowing_whenResolvingBottomInset_thenCurrentAnimatedInsetIsUsed() {
        assertThat(stableImeInset(current = 400, animationSource = 0, animationTarget = 800)).isEqualTo(400)
    }
}
