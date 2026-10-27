package com.mandrecode.tempo.features.tasks.presentation

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.components.CardGroupPosition
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.TaskSection
import com.mandrecode.tempo.features.tasks.presentation.model.SectionUi
import com.mandrecode.tempo.features.tasks.presentation.model.SectionedTaskList
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption
import kotlinx.collections.immutable.toPersistentList
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

    @Test
    fun `only date and priority sorting head the active groups`() {
        assertThat(SortOption.entries.filter { it.hasActiveGroupHeaders })
            .containsExactly(SortOption.BY_DATE, SortOption.BY_PRIORITY)
    }

    @Test
    fun `every sorting but manual heads the completed groups`() {
        assertThat(SortOption.entries.filter { !it.hasCompletedGroupHeaders }).containsExactly(SortOption.MANUAL)
    }

    @Test
    fun `without sections active and completed tasks form their own runs`() {
        val positions =
            listCardGroupPositions(
                sectioned = null,
                activeRuns = listOf(listOf(task(1), task(2))),
                separateActiveRuns = false,
                completedRuns = listOf(listOf(task(3), task(4)), listOf(task(5))),
                separateCompletedRuns = true,
            )

        assertThat(positions).containsExactly(
            1L,
            CardGroupPosition.FIRST,
            2L,
            CardGroupPosition.LAST,
            3L,
            CardGroupPosition.FIRST,
            4L,
            CardGroupPosition.LAST,
            5L,
            CardGroupPosition.SINGLE,
        )
    }

    @Test
    fun `a sectioned list makes the unsectioned tasks and each section a run`() {
        val sectioned = sectionedList(unsectioned = listOf(task(1), task(2)), sections = listOf(listOf(task(3))))

        val positions =
            listCardGroupPositions(
                sectioned = sectioned,
                activeRuns = emptyList(),
                separateActiveRuns = false,
                completedRuns = listOf(listOf(task(4))),
                separateCompletedRuns = false,
            )

        assertThat(positions).containsExactly(
            1L,
            CardGroupPosition.FIRST,
            2L,
            CardGroupPosition.LAST,
            3L,
            CardGroupPosition.SINGLE,
            4L,
            CardGroupPosition.SINGLE,
        )
    }

    private fun task(id: Long) =
        Task(
            id = id,
            title = "Task",
            description = "",
            sortOrder = id.toInt(),
        )

    private fun sectionedList(
        unsectioned: List<Task>,
        sections: List<List<Task>>,
    ) = SectionedTaskList(
        unsectioned = unsectioned.toPersistentList(),
        sections =
            sections
                .mapIndexed { index, tasks ->
                    SectionUi(
                        section = TaskSection(id = index + 1L, categoryId = 1L, name = "S$index"),
                        tasks = tasks.toPersistentList(),
                        overdueCount = 0,
                    )
                }.toPersistentList(),
    )
}
