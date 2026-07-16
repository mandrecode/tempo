package com.mandrecode.tempo.core.ui.adaptive

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SheetPlacementTest {
    @Test
    fun givenPortraitPhone_whenResolvingPlacement_thenUsesVerticalSheet() {
        assertThat(sheetPlacement(windowWidthDp = 411f, windowHeightDp = 891f))
            .isEqualTo(SheetPlacement.Vertical)
    }

    @Test
    fun givenLandscapePhone_whenResolvingPlacement_thenUsesSideSheet() {
        // Height-compact windows collide with the keyboard when using bottom sheets.
        assertThat(sheetPlacement(windowWidthDp = 780f, windowHeightDp = 360f))
            .isEqualTo(SheetPlacement.Side)
    }

    @Test
    fun givenUnfoldedFoldablePortrait_whenResolvingPlacement_thenUsesVerticalSheet() {
        assertThat(sheetPlacement(windowWidthDp = 673f, windowHeightDp = 841f))
            .isEqualTo(SheetPlacement.Vertical)
    }

    @Test
    fun givenExpandedWidth_whenResolvingPlacement_thenUsesSideSheet() {
        assertThat(sheetPlacement(windowWidthDp = 840f, windowHeightDp = 800f))
            .isEqualTo(SheetPlacement.Side)
        assertThat(sheetPlacement(windowWidthDp = 1280f, windowHeightDp = 800f))
            .isEqualTo(SheetPlacement.Side)
    }

    @Test
    fun givenMediumTabletPortrait_whenResolvingPlacement_thenUsesVerticalSheet() {
        assertThat(sheetPlacement(windowWidthDp = 800f, windowHeightDp = 1280f))
            .isEqualTo(SheetPlacement.Vertical)
    }
}
