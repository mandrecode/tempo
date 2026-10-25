package com.mandrecode.tempo.core.ui.adaptive

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WindowWidthClassTest {
    @Test
    fun `each Material breakpoint starts its class and the dp below it belongs to the previous one`() {
        val expected =
            listOf(
                0 to WindowWidthClass.COMPACT,
                599 to WindowWidthClass.COMPACT,
                600 to WindowWidthClass.MEDIUM,
                839 to WindowWidthClass.MEDIUM,
                840 to WindowWidthClass.EXPANDED,
                1199 to WindowWidthClass.EXPANDED,
                1200 to WindowWidthClass.LARGE,
                1599 to WindowWidthClass.LARGE,
                1600 to WindowWidthClass.EXTRA_LARGE,
                2560 to WindowWidthClass.EXTRA_LARGE,
            )

        expected.forEach { (width, widthClass) ->
            assertThat(windowWidthClass(width)).isEqualTo(widthClass)
        }
    }

    @Test
    fun `editors dock from the Large class up, Extra-large included`() {
        assertThat(sheetPlacement(WindowWidthClass.EXPANDED)).isEqualTo(SheetPlacement.BottomSheet)
        assertThat(sheetPlacement(WindowWidthClass.LARGE)).isEqualTo(SheetPlacement.DockedPane)
        assertThat(sheetPlacement(WindowWidthClass.EXTRA_LARGE)).isEqualTo(SheetPlacement.DockedPane)
    }

    @Test
    fun `classes are ordered by width`() {
        assertThat(WindowWidthClass.LARGE.isAtLeast(WindowWidthClass.MEDIUM)).isTrue()
        assertThat(WindowWidthClass.COMPACT.isAtLeast(WindowWidthClass.MEDIUM)).isFalse()
    }
}
