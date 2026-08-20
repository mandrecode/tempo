package com.mandrecode.tempo.core.ui.components

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TempoModalSheetBackRouteTest {
    @Test
    fun givenImeVisible_whenResolvingBackRoute_thenOnlyKeyboardCompletionClearsFocus() {
        val route = resolveTempoModalSheetBackRoute(isImeVisible = true)

        assertThat(route).isEqualTo(TempoModalSheetBackRoute.Keyboard)
        assertThat(route.forwardsProgressToSheet).isFalse()
        assertThat(route.clearsFocusOnCompletion).isTrue()
        assertThat(route.dismissesSheetOnCompletion).isFalse()
        assertThat(route.restoresSheetOnCancellation).isFalse()
    }

    @Test
    fun givenImeHidden_whenResolvingBackRoute_thenSheetHandlesEntireGesture() {
        val route = resolveTempoModalSheetBackRoute(isImeVisible = false)

        assertThat(route).isEqualTo(TempoModalSheetBackRoute.Sheet)
        assertThat(route.forwardsProgressToSheet).isTrue()
        assertThat(route.clearsFocusOnCompletion).isFalse()
        assertThat(route.dismissesSheetOnCompletion).isTrue()
        assertThat(route.restoresSheetOnCancellation).isTrue()
    }
}
