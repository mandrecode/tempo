package com.mandrecode.tempo.features.tasks.presentation.model

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class TaskSortCriteriaTest {
    @Test
    fun `by date sorts by reminder date with undated last`() {
        val later = task(id = 1, reminderDate = LocalDateTime(2025, 6, 15, 10, 0))
        val undated = task(id = 2, reminderDate = null)
        val earlier = task(id = 3, reminderDate = LocalDateTime(2025, 1, 1, 10, 0))

        val sorted = listOf(later, undated, earlier).sortedWith(TaskSortCriteria.comparator(SortOption.BY_DATE))

        assertThat(sorted.map { it.id }).containsExactly(3L, 1L, 2L).inOrder()
    }

    @Test
    fun `by priority sorts by priority with unprioritised last`() {
        val low = task(id = 1, priority = Priority.LOW)
        val high = task(id = 2, priority = Priority.HIGH)
        val medium = task(id = 3, priority = Priority.MEDIUM)
        val none = task(id = 4, priority = null)

        val sorted = listOf(low, high, medium, none).sortedWith(TaskSortCriteria.comparator(SortOption.BY_PRIORITY))

        assertThat(sorted.map { it.id }).containsExactly(2L, 3L, 1L, 4L).inOrder()
    }

    @Test
    fun `by priority breaks a priority tie with the closer reminder`() {
        val nextMonth =
            task(id = 1, priority = Priority.HIGH, reminderDate = LocalDateTime(2025, 7, 1, 9, 0))
        val today =
            task(id = 2, priority = Priority.HIGH, reminderDate = LocalDateTime(2025, 6, 1, 9, 0))

        val sorted = listOf(nextMonth, today).sortedWith(TaskSortCriteria.comparator(SortOption.BY_PRIORITY))

        assertThat(sorted.map { it.id }).containsExactly(2L, 1L).inOrder()
    }

    @Test
    fun `by priority sorts a dated task above an undated one of the same priority`() {
        val undated = task(id = 1, priority = Priority.HIGH, reminderDate = null)
        val dated =
            task(id = 2, priority = Priority.HIGH, reminderDate = LocalDateTime(2025, 6, 1, 9, 0))

        val sorted = listOf(undated, dated).sortedWith(TaskSortCriteria.comparator(SortOption.BY_PRIORITY))

        assertThat(sorted.map { it.id }).containsExactly(2L, 1L).inOrder()
    }

    @Test
    fun `by date breaks a datetime tie with priority`() {
        val sameMoment = LocalDateTime(2025, 6, 1, 9, 0)
        val low = task(id = 1, priority = Priority.LOW, reminderDate = sameMoment)
        val high = task(id = 2, priority = Priority.HIGH, reminderDate = sameMoment)

        val sorted = listOf(low, high).sortedWith(TaskSortCriteria.comparator(SortOption.BY_DATE))

        assertThat(sorted.map { it.id }).containsExactly(2L, 1L).inOrder()
    }

    @Test
    fun `fully tied tasks fall back to manual order`() {
        val sameMoment = LocalDateTime(2025, 6, 1, 9, 0)
        val second =
            task(id = 1, priority = Priority.HIGH, reminderDate = sameMoment, sortOrder = 9)
        val first =
            task(id = 2, priority = Priority.HIGH, reminderDate = sameMoment, sortOrder = 4)

        val sorted = listOf(second, first).sortedWith(TaskSortCriteria.comparator(SortOption.BY_PRIORITY))

        assertThat(sorted.map { it.id }).containsExactly(2L, 1L).inOrder()
    }

    @Test
    fun `by title sorts case-insensitively then by manual order`() {
        val beta = task(id = 1, title = "beta")
        val alphaSecond = task(id = 2, title = "Alpha", sortOrder = 7)
        val alphaFirst = task(id = 3, title = "alpha", sortOrder = 3)

        val sorted = listOf(beta, alphaSecond, alphaFirst).sortedWith(TaskSortCriteria.comparator(SortOption.BY_TITLE))

        assertThat(sorted.map { it.id }).containsExactly(3L, 2L, 1L).inOrder()
    }

    @Test
    fun `manual sorts by manual order alone`() {
        val third = task(id = 1, priority = Priority.HIGH, sortOrder = 8)
        val first = task(id = 2, priority = null, sortOrder = 1)
        val second = task(id = 3, priority = Priority.LOW, sortOrder = 5)

        val sorted = listOf(third, first, second).sortedWith(TaskSortCriteria.comparator(SortOption.MANUAL))

        assertThat(sorted.map { it.id }).containsExactly(2L, 3L, 1L).inOrder()
    }

    @Test
    fun `tasks equal in every respect are ordered by id so reloads agree`() {
        val second = task(id = 7, sortOrder = 3)
        val first = task(id = 2, sortOrder = 3)

        val sorted = listOf(second, first).sortedWith(TaskSortCriteria.comparator(SortOption.MANUAL))

        assertThat(sorted.map { it.id }).containsExactly(2L, 7L).inOrder()
    }

    @Test
    fun `areTied is true only when every criterion of the sort compares equal`() {
        val sameMoment = LocalDateTime(2025, 6, 1, 9, 0)
        val high = task(id = 1, priority = Priority.HIGH, reminderDate = sameMoment)
        val highTwin = task(id = 2, priority = Priority.HIGH, reminderDate = sameMoment, sortOrder = 5)
        val highLater =
            task(id = 3, priority = Priority.HIGH, reminderDate = LocalDateTime(2025, 6, 2, 9, 0))

        assertThat(TaskSortCriteria.areTied(high, highTwin, SortOption.BY_PRIORITY)).isTrue()
        assertThat(TaskSortCriteria.areTied(high, highLater, SortOption.BY_PRIORITY)).isFalse()
        // BY_TITLE ignores priority and date, so the same three tasks tie on their shared title.
        assertThat(TaskSortCriteria.areTied(high, highLater, SortOption.BY_TITLE)).isTrue()
    }

    @Test
    fun `areTied treats two unset values as equal`() {
        val first = task(id = 1, priority = null, reminderDate = null)
        val second = task(id = 2, priority = null, reminderDate = null)

        assertThat(TaskSortCriteria.areTied(first, second, SortOption.BY_PRIORITY)).isTrue()
        assertThat(TaskSortCriteria.areTied(first, second, SortOption.BY_DATE)).isTrue()
    }

    @Test
    fun `manual ties every task, so the whole list is the user's to order`() {
        val high = task(id = 1, priority = Priority.HIGH)
        val low = task(id = 2, priority = Priority.LOW, reminderDate = LocalDateTime(2025, 6, 1, 9, 0))

        assertThat(TaskSortCriteria.areTied(high, low, SortOption.MANUAL)).isTrue()
    }

    private fun task(
        id: Long,
        title: String = "Task",
        priority: Priority? = null,
        reminderDate: LocalDateTime? = null,
        sortOrder: Int = 0,
    ) = Task(
        id = id,
        title = title,
        description = "",
        priority = priority,
        reminderDate = reminderDate,
        sortOrder = sortOrder,
    )
}
