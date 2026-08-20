package com.mandrecode.tempo.core.ui.components

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TempoModalSheetImeTest {
    @Test
    fun givenImeVisible_whenRoutingBack_thenSheetPredictiveBackIsDisabled() {
        assertThat(
            shouldHandleSheetPredictiveBack(
                isImeVisible = true,
                wasImeVisible = true,
            ),
        ).isFalse()
    }

    @Test
    fun givenImeHidden_whenRoutingBack_thenSheetPredictiveBackIsEnabled() {
        assertThat(
            shouldHandleSheetPredictiveBack(
                isImeVisible = false,
                wasImeVisible = false,
            ),
        ).isTrue()
    }

    @Test
    fun givenImeJustBecameHidden_whenRoutingBack_thenSheetPredictiveBackRemainsDisabled() {
        assertThat(
            shouldHandleSheetPredictiveBack(
                isImeVisible = false,
                wasImeVisible = true,
            ),
        ).isFalse()
    }

    @Test
    fun givenImeDismissalReachedZeroTarget_whenResolvingFocus_thenFocusIsCleared() {
        assertThat(
            shouldClearSheetFocusAfterImeChange(
                wasImeVisible = true,
                isImeVisible = false,
                animationTarget = 0,
            ),
        ).isTrue()
    }

    @Test
    fun givenImeFocusHandoffHasTarget_whenResolvingFocus_thenFocusIsRetained() {
        assertThat(
            shouldClearSheetFocusAfterImeChange(
                wasImeVisible = true,
                isImeVisible = false,
                animationTarget = 800,
            ),
        ).isFalse()
    }

    @Test
    fun givenImeWasAlreadyHidden_whenResolvingFocus_thenFocusIsRetained() {
        assertThat(
            shouldClearSheetFocusAfterImeChange(
                wasImeVisible = false,
                isImeVisible = false,
                animationTarget = 0,
            ),
        ).isFalse()
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
