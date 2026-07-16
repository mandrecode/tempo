package com.mandrecode.tempo.core.ui.adaptive

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SheetPlacementTest {
    @Test
    fun givenPortraitPhone_whenResolvingPlacement_thenUsesVerticalSheet() {
        assertThat(sheetPlacement(windowWidthDp = 411, windowHeightDp = 891))
            .isEqualTo(SheetPlacement.Vertical)
    }

    @Test
    fun givenLandscapePhone_whenResolvingPlacement_thenUsesSideSheet() {
        // Height-compact windows collide with the keyboard when using bottom sheets.
        assertThat(sheetPlacement(windowWidthDp = 780, windowHeightDp = 360))
            .isEqualTo(SheetPlacement.Side)
    }

    @Test
    fun givenUnfoldedFoldablePortrait_whenResolvingPlacement_thenUsesVerticalSheet() {
        assertThat(sheetPlacement(windowWidthDp = 673, windowHeightDp = 841))
            .isEqualTo(SheetPlacement.Vertical)
    }

    @Test
    fun givenExpandedWidth_whenResolvingPlacement_thenUsesSideSheet() {
        assertThat(sheetPlacement(windowWidthDp = 840, windowHeightDp = 800))
            .isEqualTo(SheetPlacement.Side)
        assertThat(sheetPlacement(windowWidthDp = 1280, windowHeightDp = 800))
            .isEqualTo(SheetPlacement.Side)
    }

    @Test
    fun givenMediumTabletPortrait_whenResolvingPlacement_thenUsesVerticalSheet() {
        assertThat(sheetPlacement(windowWidthDp = 800, windowHeightDp = 1280))
            .isEqualTo(SheetPlacement.Vertical)
    }

    @Test
    fun givenDimensionsJustBelowBreakpoints_whenResolvingPlacement_thenDoesNotRoundUp() {
        assertThat(sheetPlacement(windowWidthDp = 839, windowHeightDp = 480))
            .isEqualTo(SheetPlacement.Vertical)
        assertThat(sheetPlacement(windowWidthDp = 839, windowHeightDp = 479))
            .isEqualTo(SheetPlacement.Side)
    }
}
