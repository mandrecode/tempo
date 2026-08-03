package com.mandrecode.tempo.features.focus.domain.model

import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.datetime.LocalDate

/**
 * Whether [this] task belongs to [today]'s Focus agenda.
 *
 * The whole of the day's membership rule, in one place: the agenda and the day's scheduled/completed
 * counts both ask this, so the hero can never disagree with the list underneath it.
 *
 * A task is on the day when it is due that day, or overdue and still open. Undated work never is —
 * it is reported as a count and left to the Tasks tab.
 *
 * A subtask is on the day on those same terms, *and* only when its parent is not. A parent already
 * shows its steps inside its own card, so promoting them to rows as well would list the same work
 * twice and let one task fill the Up next row on its own. When the parent is absent from the day —
 * undated, dated later, or an overdue task already ticked off — nothing is showing the subtask, and
 * the subtask is the thing with a time on it, so it stands on its own.
 *
 * [tasksById] is the same snapshot the caller is already working from; no lookup leaves it.
 */
fun Task.isOnFocusDay(
    today: LocalDate,
    tasksById: Map<Long, Task>,
): Boolean = isOnFocusDay(today, tasksById, mutableSetOf())

private fun Task.isOnFocusDay(
    today: LocalDate,
    tasksById: Map<Long, Task>,
    visiting: MutableSet<Long>,
): Boolean {
    // Parent links are a tree in every path that writes them, but an imported snapshot is only
    // validated for parents that exist — not for parents that lead back here. A cycle has no
    // top-level task to hang off, so it is not on anyone's day rather than a stack overflow.
    if (!visiting.add(id)) return false

    val dueDate = reminderDate?.date
    // Due today, or overdue and still open. Undated and future-dated work is not the day.
    val isDatedForTheDay =
        dueDate != null && (dueDate == today || (dueDate < today && !isCompleted))
    val parent = parentTaskId?.let(tasksById::get)

    return isDatedForTheDay && (parent == null || !parent.isOnFocusDay(today, tasksById, visiting))
}
