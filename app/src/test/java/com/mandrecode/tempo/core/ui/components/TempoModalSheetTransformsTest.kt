package com.mandrecode.tempo.core.ui.components

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TempoModalSheetTransformsTest {
    @Test
    fun alignment_usesDirectionSpecificEdges() {
        assertThat(TempoModalSheetDirection.Top.alignment).isEqualTo(Alignment.TopCenter)
        assertThat(TempoModalSheetDirection.Bottom.alignment).isEqualTo(Alignment.BottomCenter)
    }

    @Test
    fun maxHeight_reservesTopAirOnlyForBottomSheets() {
        val screenHeight = 800.dp

        assertThat(TempoModalSheetDirection.Top.maxHeight(screenHeight)).isEqualTo(screenHeight)
        assertThat(TempoModalSheetDirection.Bottom.maxHeight(screenHeight)).isEqualTo(752.dp)
    }

    @Test
    fun maxHeight_usesLargerTopInsetForBottomSheets() {
        val screenHeight = 800.dp

        assertThat(TempoModalSheetDirection.Bottom.maxHeight(screenHeight, topInset = 72.dp)).isEqualTo(728.dp)
    }

    @Test
    fun hiddenOffset_movesTopAndBottomSheetsInOppositeDirections() {
        val screenHeightPx = 1_000f

        assertThat(TempoModalSheetDirection.Top.hiddenOffset(screenHeightPx)).isEqualTo(-screenHeightPx)
        assertThat(TempoModalSheetDirection.Bottom.hiddenOffset(screenHeightPx)).isEqualTo(screenHeightPx)
    }

    @Test
    fun dismissOffsetForProgress_movesTopAndBottomSheetsInOppositeDirections() {
        val screenHeightPx = 1_000f
        val progress = 0.35f

        assertThat(TempoModalSheetDirection.Top.dismissOffsetForProgress(screenHeightPx, progress))
            .isEqualTo(-350f)
        assertThat(TempoModalSheetDirection.Bottom.dismissOffsetForProgress(screenHeightPx, progress))
            .isEqualTo(350f)
    }

    @Test
    fun coerceDragOffset_clampsToDismissDirectionOnly() {
        val screenHeightPx = 1_000f

        assertThat(TempoModalSheetDirection.Top.coerceDragOffset(120f, screenHeightPx)).isEqualTo(0f)
        assertThat(TempoModalSheetDirection.Top.coerceDragOffset(-1_200f, screenHeightPx)).isEqualTo(-1_000f)
        assertThat(TempoModalSheetDirection.Bottom.coerceDragOffset(-120f, screenHeightPx)).isEqualTo(0f)
        assertThat(TempoModalSheetDirection.Bottom.coerceDragOffset(1_200f, screenHeightPx)).isEqualTo(1_000f)
    }

    @Test
    fun shouldDismiss_appliesThresholdInDismissDirectionOnly() {
        val screenHeightPx = 1_000f

        assertThat(TempoModalSheetDirection.Top.shouldDismiss(-310f, screenHeightPx)).isTrue()
        assertThat(TempoModalSheetDirection.Top.shouldDismiss(-290f, screenHeightPx)).isFalse()
        assertThat(TempoModalSheetDirection.Bottom.shouldDismiss(310f, screenHeightPx)).isTrue()
        assertThat(TempoModalSheetDirection.Bottom.shouldDismiss(290f, screenHeightPx)).isFalse()
    }

    @Test
    fun endDirection_alignsToEndAndRunsHorizontally() {
        assertThat(TempoModalSheetDirection.End.alignment).isEqualTo(Alignment.CenterEnd)
        assertThat(TempoModalSheetDirection.End.isHorizontal).isTrue()
        assertThat(TempoModalSheetDirection.Top.isHorizontal).isFalse()
        assertThat(TempoModalSheetDirection.Bottom.isHorizontal).isFalse()
    }

    @Test
    fun endDirection_mirrorsBottomSemanticsOnItsAxis() {
        val axisSizePx = 1_000f

        assertThat(TempoModalSheetDirection.End.hiddenOffset(axisSizePx)).isEqualTo(axisSizePx)
        assertThat(TempoModalSheetDirection.End.dismissOffsetForProgress(axisSizePx, 0.35f)).isEqualTo(350f)
        assertThat(TempoModalSheetDirection.End.coerceDragOffset(-120f, axisSizePx)).isEqualTo(0f)
        assertThat(TempoModalSheetDirection.End.coerceDragOffset(1_200f, axisSizePx)).isEqualTo(1_000f)
        assertThat(TempoModalSheetDirection.End.shouldDismiss(310f, axisSizePx)).isTrue()
        assertThat(TempoModalSheetDirection.End.shouldDismiss(290f, axisSizePx)).isFalse()
    }

    @Test
    fun endDirection_usesFullHeightAndStartRoundedShape() {
        val screenHeight = 800.dp

        assertThat(TempoModalSheetDirection.End.maxHeight(screenHeight)).isEqualTo(screenHeight)
    }

    @Test
    fun translationOffset_mapsAxisToTranslationWithLayoutFactor() {
        assertThat(TempoModalSheetDirection.Bottom.translationOffset(40, layoutFactor = 1))
            .isEqualTo(IntOffset(0, 40))
        assertThat(TempoModalSheetDirection.Top.translationOffset(-40, layoutFactor = 1))
            .isEqualTo(IntOffset(0, -40))
        assertThat(TempoModalSheetDirection.End.translationOffset(40, layoutFactor = 1))
            .isEqualTo(IntOffset(40, 0))
        assertThat(TempoModalSheetDirection.End.translationOffset(40, layoutFactor = -1))
            .isEqualTo(IntOffset(-40, 0))
    }
}
