package com.mandrecode.tempo.features.settings.presentation

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.components.CardGroupPosition
import org.junit.Test

class SettingsGroupPositionsTest {
    @Test
    fun `all items visible form one run`() {
        assertThat(settingsGroupPositions(listOf(true, true, true)))
            .containsExactly(CardGroupPosition.FIRST, CardGroupPosition.MIDDLE, CardGroupPosition.LAST)
            .inOrder()
    }

    @Test
    fun `a hidden option leaves its switch rounded at both ends`() {
        assertThat(settingsGroupPositions(listOf(true, false)))
            .containsExactly(CardGroupPosition.SINGLE, null)
            .inOrder()
    }

    @Test
    fun `hidden items do not count towards the run`() {
        assertThat(settingsGroupPositions(listOf(false, true, false, true)))
            .containsExactly(null, CardGroupPosition.FIRST, null, CardGroupPosition.LAST)
            .inOrder()
    }
}
