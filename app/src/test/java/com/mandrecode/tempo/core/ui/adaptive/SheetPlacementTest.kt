package com.mandrecode.tempo.core.ui.adaptive

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SheetPlacementTest {
    @Test
    fun givenPortraitPhone_whenResolvingPlacement_thenUsesVerticalSheet() {
        assertThat(sheetPlacement(windowWidthDp = 411))
            .isEqualTo(SheetPlacement.BottomSheet)
    }

    @Test
    fun givenLandscapePhone_whenResolvingPlacement_thenUsesBottomSheet() {
        assertThat(sheetPlacement(windowWidthDp = 780))
            .isEqualTo(SheetPlacement.BottomSheet)
    }

    @Test
    fun givenUnfoldedFoldablePortrait_whenResolvingPlacement_thenUsesVerticalSheet() {
        assertThat(sheetPlacement(windowWidthDp = 673))
            .isEqualTo(SheetPlacement.BottomSheet)
    }

    @Test
    fun givenExpandedWidth_whenResolvingPlacement_thenUsesBottomSheet() {
        assertThat(sheetPlacement(windowWidthDp = 840))
            .isEqualTo(SheetPlacement.BottomSheet)
        assertThat(sheetPlacement(windowWidthDp = 1199))
            .isEqualTo(SheetPlacement.BottomSheet)
    }

    @Test
    fun givenMediumTabletPortrait_whenResolvingPlacement_thenUsesVerticalSheet() {
        assertThat(sheetPlacement(windowWidthDp = 800))
            .isEqualTo(SheetPlacement.BottomSheet)
    }

    @Test
    fun givenDockedPaneBreakpoint_whenResolvingPlacement_thenDoesNotRoundUp() {
        assertThat(sheetPlacement(windowWidthDp = 1199))
            .isEqualTo(SheetPlacement.BottomSheet)
        assertThat(sheetPlacement(windowWidthDp = 1200))
            .isEqualTo(SheetPlacement.DockedPane)
    }

    @Test
    fun givenLargeWindow_whenResolvingPlacement_thenUsesDockedPane() {
        assertThat(sheetPlacement(windowWidthDp = 1200))
            .isEqualTo(SheetPlacement.DockedPane)
        assertThat(sheetPlacement(windowWidthDp = 1600))
            .isEqualTo(SheetPlacement.DockedPane)
    }
}
