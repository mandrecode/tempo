package com.mandrecode.tempo.features.tasks.presentation.model

import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap

private const val MIN_REORDERABLE_RUN_SIZE = 2

/**
 * A run of consecutive active tasks the current sort cannot tell apart, so their order is the
 * user's to set by hand.
 *
 * Looked up by task id, so [indexInRun] is the position of the task it was looked up by.
 */
data class ReorderableRun(
    val key: String,
    val indexInRun: Int,
    val tasks: ImmutableList<Task>,
)

/**
 * Indexes, by task id, the runs of mutually tied tasks worth dragging.
 *
 * Chunking each group separately rather than the flattened list keeps a run from straddling a
 * group header even if a group key ever stops being a function of the sort criteria.
 */
internal fun buildReorderableRuns(
    groupedTasks: Collection<List<Task>>,
    sortOption: SortOption,
): ImmutableMap<Long, ReorderableRun> =
    groupedTasks
        .flatMap { tasksInGroup -> tasksInGroup.tiedRuns(sortOption) }
        .flatMap { runTasks ->
            val key = "run_${runTasks.first().id}"
            runTasks.mapIndexed { index, task -> task.id to ReorderableRun(key, index, runTasks) }
        }.toMap()
        .toPersistentMap()

/** Splits an already-sorted group into its maximal runs of tied tasks, dropping the single ones. */
private fun List<Task>.tiedRuns(sortOption: SortOption): List<ImmutableList<Task>> {
    val runs = mutableListOf<ImmutableList<Task>>()
    var start = 0
    while (start < size) {
        var end = start + 1
        while (end < size && TaskSortCriteria.areTied(this[start], this[end], sortOption)) {
            end++
        }
        if (end - start >= MIN_REORDERABLE_RUN_SIZE) {
            runs += subList(start, end).toPersistentList()
        }
        start = end
    }
    return runs
}
