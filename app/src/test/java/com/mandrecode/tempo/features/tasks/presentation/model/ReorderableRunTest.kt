package com.mandrecode.tempo.features.tasks.presentation.model

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class ReorderableRunTest {
    @Test
    fun `undated unprioritised tasks form a single run under by priority`() {
        val tasks = listOf(task(id = 1), task(id = 2), task(id = 3))

        val runs = buildReorderableRuns(listOf(tasks), SortOption.BY_PRIORITY)

        assertThat(runs.keys).containsExactly(1L, 2L, 3L)
        assertThat(runs.values.map { it.key }.distinct()).hasSize(1)
        assertThat(runs.getValue(1L).indexInRun).isEqualTo(0)
        assertThat(runs.getValue(3L).indexInRun).isEqualTo(2)
        assertThat(runs.getValue(2L).tasks.map { it.id }).containsExactly(1L, 2L, 3L).inOrder()
    }

    @Test
    fun `a task the sort can distinguish is not reorderable`() {
        val sameMoment = LocalDateTime(2025, 6, 1, 9, 0)
        val tasks =
            listOf(
                task(id = 1, priority = Priority.HIGH, reminderDate = sameMoment),
                task(id = 2, priority = Priority.HIGH, reminderDate = sameMoment),
                task(id = 3, priority = Priority.HIGH, reminderDate = LocalDateTime(2025, 6, 2, 9, 0)),
            )

        val runs = buildReorderableRuns(listOf(tasks), SortOption.BY_PRIORITY)

        assertThat(runs.keys).containsExactly(1L, 2L)
    }

    @Test
    fun `one group can hold several separate runs`() {
        val tasks =
            listOf(
                task(id = 1, title = "alpha"),
                task(id = 2, title = "alpha"),
                task(id = 3, title = "beta"),
                task(id = 4, title = "gamma"),
                task(id = 5, title = "gamma"),
            )

        val runs = buildReorderableRuns(listOf(tasks), SortOption.BY_TITLE)

        assertThat(runs.keys).containsExactly(1L, 2L, 4L, 5L)
        assertThat(runs.getValue(1L).key).isEqualTo(runs.getValue(2L).key)
        assertThat(runs.getValue(4L).key).isEqualTo(runs.getValue(5L).key)
        assertThat(runs.getValue(1L).key).isNotEqualTo(runs.getValue(4L).key)
        assertThat(runs.getValue(4L).indexInRun).isEqualTo(0)
    }

    @Test
    fun `a run never straddles two groups`() {
        val first = listOf(task(id = 1), task(id = 2))
        val second = listOf(task(id = 3), task(id = 4))

        val runs = buildReorderableRuns(listOf(first, second), SortOption.MANUAL)

        assertThat(runs.getValue(1L).key).isEqualTo(runs.getValue(2L).key)
        assertThat(runs.getValue(3L).key).isEqualTo(runs.getValue(4L).key)
        assertThat(runs.getValue(2L).key).isNotEqualTo(runs.getValue(3L).key)
        assertThat(runs.getValue(1L).tasks.map { it.id }).containsExactly(1L, 2L).inOrder()
    }

    @Test
    fun `manual ties the whole group regardless of priority or date`() {
        val tasks =
            listOf(
                task(id = 1, priority = Priority.HIGH),
                task(id = 2, priority = Priority.LOW, reminderDate = LocalDateTime(2025, 6, 1, 9, 0)),
                task(id = 3),
            )

        val runs = buildReorderableRuns(listOf(tasks), SortOption.MANUAL)

        assertThat(runs.keys).containsExactly(1L, 2L, 3L)
        assertThat(runs.values.map { it.key }.distinct()).hasSize(1)
    }

    @Test
    fun `a lone task is not reorderable`() {
        val runs = buildReorderableRuns(listOf(listOf(task(id = 1))), SortOption.BY_PRIORITY)

        assertThat(runs).isEmpty()
    }

    @Test
    fun `no groups means nothing to reorder`() {
        assertThat(buildReorderableRuns(emptyList(), SortOption.BY_DATE)).isEmpty()
    }

    private fun task(
        id: Long,
        title: String = "Task",
        priority: Priority? = null,
        reminderDate: LocalDateTime? = null,
    ) = Task(
        id = id,
        title = title,
        description = "",
        priority = priority,
        reminderDate = reminderDate,
        sortOrder = id.toInt(),
    )
}
