package com.mandrecode.tempo.core.ui.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * The rule for whether the running-session slot shows its countdown or only its icon.
 *
 * Worth its own test because the shape of the bar decides it, not the route: the narrow rail is one
 * icon wide, and a countdown there pushed the chip out of the rail and over the content behind it.
 */
class SessionChipCompactnessTest {
    @Test
    fun `a narrow rail is always compact`() {
        assertThat(compact(isRail = true)).isTrue()
        assertThat(compact(isRail = true, hasActions = true, roomy = true)).isTrue()
    }

    @Test
    fun `an expanded rail always shows the countdown`() {
        // Its rows are wide and labelled, so a bare icon would be the one row saying nothing.
        assertThat(compact(isRail = true, isExpandedRail = true)).isFalse()
        assertThat(compact(isRail = true, isExpandedRail = true, hasActions = true)).isFalse()
    }

    @Test
    fun `the bottom bar shows the countdown when it fits`() {
        assertThat(compact()).isFalse()
        assertThat(compact(roomy = true)).isFalse()
    }

    @Test
    fun `the bottom bar shows the countdown beside a tab's actions on a wide screen`() {
        assertThat(compact(hasActions = true, roomy = true)).isFalse()
    }

    @Test
    fun `the bottom bar stays compact beside a tab's actions on a phone`() {
        // The case the bar actually runs out of width in.
        assertThat(compact(hasActions = true)).isTrue()
    }

    private fun compact(
        isRail: Boolean = false,
        isExpandedRail: Boolean = false,
        hasActions: Boolean = false,
        roomy: Boolean = false,
    ) = isSessionChipCompact(
        isRailLayout = isRail,
        isExpandedRail = isExpandedRail,
        hasContextualActions = hasActions,
        hasRoomForCountdown = roomy,
    )
}
