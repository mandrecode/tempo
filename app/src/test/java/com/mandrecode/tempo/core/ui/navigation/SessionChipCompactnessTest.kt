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
    fun `a narrow rail is always compact, wherever you are`() {
        assertThat(compact(isRail = true, isExpandedRail = false, onFocus = false)).isTrue()
        assertThat(compact(isRail = true, isExpandedRail = false, onFocus = true)).isTrue()
        assertThat(
            compact(isRail = true, isExpandedRail = false, onFocus = false, hasActions = true),
        ).isTrue()
    }

    @Test
    fun `an expanded rail always shows the countdown`() {
        // Its rows are wide and labelled, so a bare icon would be the one row saying nothing.
        assertThat(compact(isRail = true, isExpandedRail = true, onFocus = false)).isFalse()
        assertThat(compact(isRail = true, isExpandedRail = true, onFocus = true)).isFalse()
        assertThat(
            compact(isRail = true, isExpandedRail = true, onFocus = true, hasActions = true),
        ).isFalse()
    }

    @Test
    fun `the bottom bar shows the countdown away from Focus`() {
        assertThat(compact(isRail = false, isExpandedRail = false, onFocus = false)).isFalse()
    }

    @Test
    fun `the bottom bar stays compact on Focus, where the session card is already open`() {
        assertThat(compact(isRail = false, isExpandedRail = false, onFocus = true)).isTrue()
    }

    @Test
    fun `the bottom bar stays compact when a tab brings its own actions`() {
        // The case the bar actually runs out of width in.
        assertThat(
            compact(isRail = false, isExpandedRail = false, onFocus = false, hasActions = true),
        ).isTrue()
    }

    private fun compact(
        isRail: Boolean,
        isExpandedRail: Boolean,
        onFocus: Boolean,
        hasActions: Boolean = false,
    ) = isSessionChipCompact(
        isRailLayout = isRail,
        isExpandedRail = isExpandedRail,
        currentRoute = if (onFocus) FocusRoute else RoutinesRoute,
        hasContextualActions = hasActions,
    )
}
