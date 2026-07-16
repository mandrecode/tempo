package com.mandrecode.tempo.core.ui.components

import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.IconCategory
import org.junit.Test

class IconPickerTest {
    @Test
    fun givenNarrowContainer_whenCalculatingItemsPerRow_thenFloorsToDefault() {
        val itemsPerRow = calculateItemsPerRow(availableWidth = 312.dp)

        assertThat(itemsPerRow).isEqualTo(6)
    }

    @Test
    fun givenMediumContainer_whenCalculatingItemsPerRow_thenGrowsBeyondDefault() {
        val itemsPerRow = calculateItemsPerRow(availableWidth = 400.dp)

        assertThat(itemsPerRow).isEqualTo(7)
    }

    @Test
    fun givenVeryWideContainer_whenCalculatingItemsPerRow_thenCapsAtOneSlotPerCategory() {
        val itemsPerRow = calculateItemsPerRow(availableWidth = 1000.dp)

        assertThat(itemsPerRow).isEqualTo(IconCategory.entries.size + 1)
    }

    @Test
    fun givenZeroWidthContainer_whenCalculatingItemsPerRow_thenStillFloorsToDefault() {
        val itemsPerRow = calculateItemsPerRow(availableWidth = 0.dp)

        assertThat(itemsPerRow).isEqualTo(6)
    }
}
