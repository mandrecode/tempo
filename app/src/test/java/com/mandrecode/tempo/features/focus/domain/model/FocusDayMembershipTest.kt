package com.mandrecode.tempo.features.focus.domain.model

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.Test

class FocusDayMembershipTest {
    private val today = LocalDate(2026, 7, 29)
    private val yesterday = today.minus(1, DateTimeUnit.DAY)
    private val nextWeek = today.plus(7, DateTimeUnit.DAY)

    private fun task(
        id: Long,
        date: LocalDate?,
        isCompleted: Boolean = false,
        parentTaskId: Long? = null,
    ) = Task(
        id = id,
        title = "Task $id",
        description = "",
        isCompleted = isCompleted,
        parentTaskId = parentTaskId,
        reminderDate = date?.let { LocalDateTime(it, LocalTime(9, 0)) },
    )

    private fun List<Task>.membership(task: Task) = task.isOnFocusDay(today, associateBy { it.id })

    @Test
    fun `a task due today is on the day`() {
        val task = task(id = 1, date = today)

        assertThat(listOf(task).membership(task)).isTrue()
    }

    @Test
    fun `a completed task due today is still on the day, so it can be read as done`() {
        val task = task(id = 1, date = today, isCompleted = true)

        assertThat(listOf(task).membership(task)).isTrue()
    }

    @Test
    fun `an open overdue task is on the day`() {
        val task = task(id = 1, date = yesterday)

        assertThat(listOf(task).membership(task)).isTrue()
    }

    @Test
    fun `a completed overdue task has left the day`() {
        val task = task(id = 1, date = yesterday, isCompleted = true)

        assertThat(listOf(task).membership(task)).isFalse()
    }

    @Test
    fun `future work is not on the day`() {
        val task = task(id = 1, date = nextWeek)

        assertThat(listOf(task).membership(task)).isFalse()
    }

    @Test
    fun `an undated task is never on the day`() {
        val task = task(id = 1, date = null)

        assertThat(listOf(task).membership(task)).isFalse()
    }

    @Test
    fun `a dated subtask stands on its own when its parent has no date`() {
        val parent = task(id = 1, date = null)
        val subtask = task(id = 2, date = today, parentTaskId = 1)

        assertThat(listOf(parent, subtask).membership(subtask)).isTrue()
    }

    @Test
    fun `a subtask stays inside a parent that is itself on the day`() {
        val parent = task(id = 1, date = today)
        val subtask = task(id = 2, date = today, parentTaskId = 1)

        assertThat(listOf(parent, subtask).membership(subtask)).isFalse()
        assertThat(listOf(parent, subtask).membership(parent)).isTrue()
    }

    @Test
    fun `an overdue subtask under a future-dated parent stands on its own`() {
        // The parent is nowhere on this day, so nothing else is showing the step that is late.
        val parent = task(id = 1, date = nextWeek)
        val subtask = task(id = 2, date = yesterday, parentTaskId = 1)

        assertThat(listOf(parent, subtask).membership(subtask)).isTrue()
    }

    @Test
    fun `a subtask under a completed overdue parent stands on its own`() {
        val parent = task(id = 1, date = yesterday, isCompleted = true)
        val subtask = task(id = 2, date = today, parentTaskId = 1)

        assertThat(listOf(parent, subtask).membership(subtask)).isTrue()
    }

    @Test
    fun `an undated subtask is not on the day whatever its parent does`() {
        val parent = task(id = 1, date = null)
        val subtask = task(id = 2, date = null, parentTaskId = 1)

        assertThat(listOf(parent, subtask).membership(subtask)).isFalse()
    }

    @Test
    fun `the rule reaches up the whole tree, not just one level`() {
        // Grandparent undated, so the parent is promoted — which in turn keeps its own step nested.
        val grandparent = task(id = 1, date = null)
        val parent = task(id = 2, date = today, parentTaskId = 1)
        val child = task(id = 3, date = today, parentTaskId = 2)
        val tasks = listOf(grandparent, parent, child)

        assertThat(tasks.membership(parent)).isTrue()
        assertThat(tasks.membership(child)).isFalse()
    }

    @Test
    fun `a parent that is not in the snapshot leaves its subtask standing on its own`() {
        val subtask = task(id = 2, date = today, parentTaskId = 404)

        assertThat(listOf(subtask).membership(subtask)).isTrue()
    }

    @Test
    fun `a cycle in parent links resolves to nothing rather than a stack overflow`() {
        // Only an imported snapshot can produce this; the day is the wrong place to discover it.
        val first = task(id = 1, date = today, parentTaskId = 2)
        val second = task(id = 2, date = today, parentTaskId = 1)
        val tasks = listOf(first, second)

        // Both sides, not just one: a cycle has no top-level task to hang off, so nothing in it is
        // on anyone's day. Asserting one side would miss the other being promoted in its place.
        assertThat(tasks.membership(first)).isFalse()
        assertThat(tasks.membership(second)).isFalse()
    }

    @Test
    fun `an odd-length cycle resolves to nothing too`() {
        // The length matters: walking up a cycle alternates "on the day" at each step, so an
        // odd-length one lands back on its start with the opposite answer to an even-length one.
        val first = task(id = 1, date = today, parentTaskId = 2)
        val second = task(id = 2, date = today, parentTaskId = 3)
        val third = task(id = 3, date = today, parentTaskId = 1)
        val tasks = listOf(first, second, third)

        assertThat(tasks.membership(first)).isFalse()
        assertThat(tasks.membership(second)).isFalse()
        assertThat(tasks.membership(third)).isFalse()
    }

    @Test
    fun `a task hanging off a cycle is not on the day either`() {
        // It is not in the cycle, but every answer about it depends on one, so there is no answer.
        val first = task(id = 1, date = today, parentTaskId = 2)
        val second = task(id = 2, date = today, parentTaskId = 1)
        val child = task(id = 3, date = today, parentTaskId = 1)
        val tasks = listOf(first, second, child)

        assertThat(tasks.membership(child)).isFalse()
    }
}
