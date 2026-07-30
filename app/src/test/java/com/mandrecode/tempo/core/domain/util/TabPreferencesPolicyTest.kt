package com.mandrecode.tempo.core.domain.util

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.TempoTab
import org.junit.Test

class TabPreferencesPolicyTest {
    @Test
    fun `resolveEnabledTabsKeepsANonEmptySelection`() {
        val selection = setOf(TempoTab.FOCUS, TempoTab.TASKS)

        assertThat(TabPreferencesPolicy.resolveEnabledTabs(selection)).isEqualTo(selection)
    }

    @Test
    fun `resolveEnabledTabsFallsBackWhenEmpty`() {
        assertThat(TabPreferencesPolicy.resolveEnabledTabs(emptySet())).containsExactly(TempoTab.DEFAULT)
    }

    @Test
    fun `canDisableAnyTabWhileAnotherRemains`() {
        val enabled = setOf(TempoTab.FOCUS, TempoTab.ROUTINES)

        assertThat(TabPreferencesPolicy.canDisable(enabled, TempoTab.FOCUS)).isTrue()
        assertThat(TabPreferencesPolicy.canDisable(enabled, TempoTab.ROUTINES)).isTrue()
    }

    @Test
    fun `cannotDisableTheLastEnabledTab`() {
        assertThat(TabPreferencesPolicy.canDisable(setOf(TempoTab.TASKS), TempoTab.TASKS)).isFalse()
    }

    @Test
    fun `disablingAnAlreadyDisabledTabIsAllowed`() {
        assertThat(TabPreferencesPolicy.canDisable(setOf(TempoTab.TASKS), TempoTab.FOCUS)).isTrue()
    }

    @Test
    fun `withTabEnabledAddsAndRemoves`() {
        val enabled = setOf(TempoTab.FOCUS, TempoTab.TASKS)

        assertThat(TabPreferencesPolicy.withTabEnabled(enabled, TempoTab.ROUTINES, enabled = true))
            .containsExactly(TempoTab.FOCUS, TempoTab.ROUTINES, TempoTab.TASKS)
        assertThat(TabPreferencesPolicy.withTabEnabled(enabled, TempoTab.TASKS, enabled = false))
            .containsExactly(TempoTab.FOCUS)
    }

    @Test
    fun `withTabEnabledRefusesToEmptyTheSelection`() {
        val enabled = setOf(TempoTab.FOCUS)

        assertThat(TabPreferencesPolicy.withTabEnabled(enabled, TempoTab.FOCUS, enabled = false))
            .containsExactly(TempoTab.FOCUS)
    }

    @Test
    fun `defaultTabIsKeptWhenStillEnabled`() {
        val resolved =
            TabPreferencesPolicy.resolveDefaultTab(
                defaultTab = TempoTab.TASKS,
                enabledTabs = setOf(TempoTab.FOCUS, TempoTab.TASKS),
            )

        assertThat(resolved).isEqualTo(TempoTab.TASKS)
    }

    @Test
    fun `disabledDefaultTabFallsBackToFirstEnabledInDisplayOrder`() {
        val resolved =
            TabPreferencesPolicy.resolveDefaultTab(
                defaultTab = TempoTab.TASKS,
                enabledTabs = setOf(TempoTab.ROUTINES, TempoTab.FOCUS),
            )

        assertThat(resolved).isEqualTo(TempoTab.FOCUS)
    }

    @Test
    fun `defaultTabResolvesEvenWithNoEnabledTabs`() {
        val resolved = TabPreferencesPolicy.resolveDefaultTab(TempoTab.TASKS, emptySet())

        assertThat(resolved).isEqualTo(TempoTab.DEFAULT)
    }
}
