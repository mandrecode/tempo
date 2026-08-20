package com.mandrecode.tempo.core.ui.components

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TempoModalSheetImeInsetsTest {
    @Test
    fun givenImeFocusHandoff_whenResolvingBottomInset_thenPreviousInsetIsRetained() {
        assertThat(stableImeBottomInset(current = 0, animationSource = 800, animationTarget = 800)).isEqualTo(800)
    }

    @Test
    fun givenImeHiding_whenResolvingBottomInset_thenCurrentAnimatedInsetIsUsed() {
        assertThat(stableImeBottomInset(current = 400, animationSource = 800, animationTarget = 0)).isEqualTo(400)
    }

    @Test
    fun givenImeShowing_whenResolvingBottomInset_thenCurrentAnimatedInsetIsUsed() {
        assertThat(stableImeBottomInset(current = 400, animationSource = 0, animationTarget = 800)).isEqualTo(400)
    }
}
