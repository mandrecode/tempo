package com.mandrecode.tempo.core.ui.components

import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.IconCategory
import com.mandrecode.tempo.core.ui.theme.TempoIcon
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

    // --- resolveRowIcons TESTS ---

    @Test
    fun givenNoSelection_whenResolvingRowIcons_thenReturnsSampledIconsUnchanged() {
        val sampled = listOf(TempoIcon.RUN, TempoIcon.HEALTH, TempoIcon.COFFEE)

        val result = resolveRowIcons(sampled, TempoIcon.getAllIcons(), null, slotCount = 3)

        assertThat(result).isEqualTo(sampled)
    }

    @Test
    fun givenSelectionAlreadySampled_whenResolvingRowIcons_thenReturnsSampledIconsUnchanged() {
        val sampled = listOf(TempoIcon.RUN, TempoIcon.HEALTH, TempoIcon.COFFEE)

        val result = resolveRowIcons(sampled, TempoIcon.getAllIcons(), "run", slotCount = 3)

        assertThat(result).isEqualTo(sampled)
    }

    @Test
    fun givenSelectionOutsideSampleFromARepresentedCategory_whenResolvingRowIcons_thenDropsOldRepresentative() {
        // RUN and FITNESS are both FITNESS_SPORTS - the sample already represents that
        // category via RUN, and the manual selection is the unsampled FITNESS instead.
        val sampled = listOf(TempoIcon.RUN, TempoIcon.HEALTH, TempoIcon.COFFEE, TempoIcon.HOME, TempoIcon.CALL)

        val result = resolveRowIcons(sampled, TempoIcon.getAllIcons(), "fitness", slotCount = 5)

        assertThat(result).containsExactly(
            TempoIcon.FITNESS,
            TempoIcon.HEALTH,
            TempoIcon.COFFEE,
            TempoIcon.HOME,
            TempoIcon.CALL,
        )
        assertThat(result.map { it.category }.toSet()).hasSize(5)
    }

    @Test
    fun givenSelectionOutsideSampleFromANewCategory_whenResolvingRowIcons_thenPinsItAndBackfillsFromTheSample() {
        val sampled = listOf(TempoIcon.RUN, TempoIcon.HEALTH, TempoIcon.COFFEE, TempoIcon.HOME)

        val result = resolveRowIcons(sampled, TempoIcon.getAllIcons(), "call", slotCount = 4)

        assertThat(result).containsExactly(
            TempoIcon.CALL,
            TempoIcon.RUN,
            TempoIcon.HEALTH,
            TempoIcon.COFFEE,
        )
    }
}
