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
 * Because that rule defers to the parent, the answer is settled by walking the whole line of
 * ancestors and resolving it from the root down — the root's answer is its date alone, and each
 * step below it flips on whether the one above claimed the day.
 *
 * [tasksById] is the same snapshot the caller is already working from; no lookup leaves it.
 */
fun Task.isOnFocusDay(
    today: LocalDate,
    tasksById: Map<Long, Task>,
): Boolean {
    val ancestry = ancestryFrom(tasksById) ?: return false

    // Settled from the root down, because "is this one on the day?" is only answerable once its
    // parent's answer is known. The root has no parent to defer to, so it is simply its own date.
    var onTheDay = false
    for (task in ancestry.asReversed()) {
        onTheDay = task.isDatedFor(today) && !onTheDay
    }
    return onTheDay
}

/**
 * [this] and every task above it, nearest first — or `null` when the line runs into a cycle.
 *
 * Parent links are a tree in every path that writes them, and a restore rejects a snapshot whose
 * links are not, so this is a second line of defence rather than the first.
 *
 * It matters that a cycle answers `null` rather than "not on the day" at the point it is found: the
 * answers alternate on the way up, so a guard that returned a plain `false` mid-walk handed the
 * caller above it a real answer, and an odd-length cycle came back out of the walk claiming every
 * task in it was on the day.
 *
 * Iterative rather than recursive so a long line of ancestors cannot end the process either.
 */
private fun Task.ancestryFrom(tasksById: Map<Long, Task>): List<Task>? {
    val ancestry = mutableListOf<Task>()
    val seen = mutableSetOf<Long>()
    var current: Task? = this
    while (current != null) {
        if (!seen.add(current.id)) return null
        ancestry += current
        current = current.parentTaskId?.let(tasksById::get)
    }
    return ancestry
}

/** Due today, or overdue and still open. Undated and future-dated work is not the day. */
private fun Task.isDatedFor(today: LocalDate): Boolean {
    val dueDate = reminderDate?.date ?: return false
    return dueDate == today || (dueDate < today && !isCompleted)
}
