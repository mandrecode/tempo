package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FloatingNavigationLayoutTest {
    @Test
    fun givenPortraitLayoutWithoutNavigationInset_whenCalculatingClearance_thenIncludesFloatingBar() {
        val clearance =
            calculateFloatingNavigationBottomClearancePadding(
                defaultPadding = 16.dp,
                navigationBarPadding = 0.dp,
                isRailLayout = false,
            )

        assertThat(clearance).isEqualTo(96.dp)
    }

    @Test
    fun givenPortraitLayoutWithNavigationInset_whenCalculatingClearance_thenIncludesBothInsets() {
        val clearance =
            calculateFloatingNavigationBottomClearancePadding(
                defaultPadding = 16.dp,
                navigationBarPadding = 24.dp,
                isRailLayout = false,
            )

        assertThat(clearance).isEqualTo(120.dp)
    }

    @Test
    fun givenRailLayoutWithoutNavigationInset_whenCalculatingClearance_thenUsesContentPadding() {
        val clearance =
            calculateFloatingNavigationBottomClearancePadding(
                defaultPadding = 16.dp,
                navigationBarPadding = 0.dp,
                isRailLayout = true,
            )

        assertThat(clearance).isEqualTo(16.dp)
    }

    @Test
    fun givenRailLayoutWithNavigationInset_whenCalculatingClearance_thenIncludesSystemInset() {
        val clearance =
            calculateFloatingNavigationBottomClearancePadding(
                defaultPadding = 16.dp,
                navigationBarPadding = 24.dp,
                isRailLayout = true,
            )

        assertThat(clearance).isEqualTo(40.dp)
    }

    @Test
    fun givenRailMetrics_whenDerivingContentClearance_thenContentClearsTheRailFootprint() {
        val railFootprint = FloatingRailStartPadding + FloatingRailSurfaceWidth

        assertThat(FloatingRailContentStartPadding.value)
            .isAtLeast((railFootprint + 8.dp).value)
    }

    @Test
    fun givenExpandedRailMetrics_whenDerivingContentClearance_thenContentClearsTheExpandedFootprint() {
        val expandedRailFootprint = FloatingRailStartPadding + FloatingRailExpandedSurfaceWidth

        assertThat(FloatingRailExpandedContentStartPadding.value)
            .isAtLeast((expandedRailFootprint + 8.dp).value)
    }
}
