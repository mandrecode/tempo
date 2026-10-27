package com.mandrecode.tempo.features.tasks.presentation

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.components.CardGroupPosition
import com.mandrecode.tempo.features.tasks.domain.model.Task
import org.junit.Test

class CardGroupPositionsTest {
    @Test
    fun `headed groups each form their own run`() {
        val groups = listOf(listOf(task(1), task(2)), listOf(task(3)))

        val positions = cardGroupPositions(groups, separateRuns = true)

        assertThat(positions).containsExactly(
            1L,
            CardGroupPosition.FIRST,
            2L,
            CardGroupPosition.LAST,
            3L,
            CardGroupPosition.SINGLE,
        )
    }

    @Test
    fun `unheaded groups join into one run`() {
        val groups = listOf(listOf(task(1), task(2)), listOf(task(3)))

        val positions = cardGroupPositions(groups, separateRuns = false)

        assertThat(positions).containsExactly(
            1L,
            CardGroupPosition.FIRST,
            2L,
            CardGroupPosition.MIDDLE,
            3L,
            CardGroupPosition.LAST,
        )
    }

    @Test
    fun `no tasks give no positions`() {
        assertThat(cardGroupPositions(emptyList(), separateRuns = false)).isEmpty()
    }

    private fun task(id: Long) =
        Task(
            id = id,
            title = "Task",
            description = "",
            sortOrder = id.toInt(),
        )
}
