package com.mandrecode.tempo.core.ui.adaptive

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SheetPlacementTest {
    @Test
    fun givenPortraitPhone_whenResolvingPlacement_thenUsesVerticalSheet() {
        assertThat(sheetPlacement(windowWidthDp = 411, windowHeightDp = 891))
            .isEqualTo(SheetPlacement.BottomSheet)
    }

    @Test
    fun givenLandscapePhone_whenResolvingPlacement_thenUsesSideSheet() {
        // Height-compact windows collide with the keyboard when using bottom sheets.
        assertThat(sheetPlacement(windowWidthDp = 780, windowHeightDp = 360))
            .isEqualTo(SheetPlacement.SideSheet)
    }

    @Test
    fun givenUnfoldedFoldablePortrait_whenResolvingPlacement_thenUsesVerticalSheet() {
        assertThat(sheetPlacement(windowWidthDp = 673, windowHeightDp = 841))
            .isEqualTo(SheetPlacement.BottomSheet)
    }

    @Test
    fun givenExpandedWidth_whenResolvingPlacement_thenUsesSideSheet() {
        assertThat(sheetPlacement(windowWidthDp = 840, windowHeightDp = 800))
            .isEqualTo(SheetPlacement.SideSheet)
        assertThat(sheetPlacement(windowWidthDp = 1199, windowHeightDp = 800))
            .isEqualTo(SheetPlacement.SideSheet)
    }

    @Test
    fun givenMediumTabletPortrait_whenResolvingPlacement_thenUsesVerticalSheet() {
        assertThat(sheetPlacement(windowWidthDp = 800, windowHeightDp = 1280))
            .isEqualTo(SheetPlacement.BottomSheet)
    }

    @Test
    fun givenDimensionsJustBelowBreakpoints_whenResolvingPlacement_thenDoesNotRoundUp() {
        assertThat(sheetPlacement(windowWidthDp = 839, windowHeightDp = 480))
            .isEqualTo(SheetPlacement.BottomSheet)
        assertThat(sheetPlacement(windowWidthDp = 839, windowHeightDp = 479))
            .isEqualTo(SheetPlacement.SideSheet)
    }

    @Test
    fun givenLargeWindow_whenResolvingPlacement_thenUsesDockedPane() {
        assertThat(sheetPlacement(windowWidthDp = 1200, windowHeightDp = 700))
            .isEqualTo(SheetPlacement.DockedPane)
        assertThat(sheetPlacement(windowWidthDp = 1600, windowHeightDp = 1000))
            .isEqualTo(SheetPlacement.DockedPane)
    }
}
