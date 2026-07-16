package com.mandrecode.tempo.core.ui.components

import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AdaptiveGridSizingTest {
    @Test
    fun givenNarrowContainer_whenCalculatingItemCount_thenFloorsToMinCount() {
        val count =
            calculateAdaptiveItemCount(
                availableWidth = 312.dp,
                itemSize = 48.dp,
                minGap = 4.dp,
                horizontalPadding = 8.dp,
                minCount = 6,
                maxCount = 11,
            )

        assertThat(count).isEqualTo(6)
    }

    @Test
    fun givenWideContainer_whenCalculatingItemCount_thenGrowsBeyondMinCount() {
        val count =
            calculateAdaptiveItemCount(
                availableWidth = 400.dp,
                itemSize = 48.dp,
                minGap = 4.dp,
                horizontalPadding = 8.dp,
                minCount = 6,
                maxCount = 11,
            )

        assertThat(count).isEqualTo(7)
    }

    @Test
    fun givenVeryWideContainer_whenCalculatingItemCount_thenCapsAtMaxCount() {
        val count =
            calculateAdaptiveItemCount(
                availableWidth = 1000.dp,
                itemSize = 48.dp,
                minGap = 4.dp,
                horizontalPadding = 8.dp,
                minCount = 6,
                maxCount = 11,
            )

        assertThat(count).isEqualTo(11)
    }

    @Test
    fun givenDifferentMaxCount_whenCalculatingItemCount_thenCapsAtThatIconPickerMax() {
        val count =
            calculateAdaptiveItemCount(
                availableWidth = 1000.dp,
                itemSize = 48.dp,
                minGap = 4.dp,
                horizontalPadding = 8.dp,
                minCount = 6,
                maxCount = 10,
            )

        assertThat(count).isEqualTo(10)
    }
}
