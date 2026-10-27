package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.components.CardGroupPosition
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.presentation.model.SectionedTaskList
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption

/**
 * Each task's place in its connected run of cards, by id: one run per group when the
 * groups are headed ([separateRuns]), or the whole list as a single run when they are not, since
 * nothing would then mark where one group's cards stop and the next one's start.
 */
internal fun cardGroupPositions(
    groups: List<List<Task>>,
    separateRuns: Boolean,
): Map<Long, CardGroupPosition> {
    val runs = if (separateRuns) groups else listOf(groups.flatten())
    return buildMap {
        runs.forEach { run ->
            run.forEachIndexed { index, task -> put(task.id, CardGroupPosition.of(index, run.size)) }
        }
    }
}

/** Whether the active groups are told apart by a header, and so form runs of their own. */
internal val SortOption.hasActiveGroupHeaders: Boolean
    get() = this == SortOption.BY_DATE || this == SortOption.BY_PRIORITY

/** Whether the completed groups are told apart by a header, and so form runs of their own. */
internal val SortOption.hasCompletedGroupHeaders: Boolean
    get() = this != SortOption.MANUAL

/**
 * Every listed task's place in its connected run: in a sectioned category the unsectioned tasks and
 * each section are runs of their own; otherwise [cardGroupPositions] over the active groups. The
 * completed groups follow the same rule after them.
 */
internal fun listCardGroupPositions(
    sectioned: SectionedTaskList?,
    activeRuns: List<List<Task>>,
    separateActiveRuns: Boolean,
    completedRuns: List<List<Task>>,
    separateCompletedRuns: Boolean,
): Map<Long, CardGroupPosition> =
    buildMap {
        if (sectioned != null) {
            putAll(
                cardGroupPositions(
                    listOf(sectioned.unsectioned) + sectioned.sections.map { it.tasks },
                    separateRuns = true,
                ),
            )
        } else {
            putAll(cardGroupPositions(activeRuns, separateActiveRuns))
        }
        putAll(cardGroupPositions(completedRuns, separateCompletedRuns))
    }

/**
 * What the list keeps between a run of cards and a header, separator or footer beside it, on top
 * of the connected-card gap every item gets: together, the 8dp the cards used to stand apart by.
 */
internal val GroupSeparation = 5.dp

/**
 * [GroupSeparation] around a header between runs: below it always, above it only when a run comes
 * before it, so the first item in the list starts at the same inset whatever it is.
 */
internal fun Modifier.groupSeparation(above: Boolean): Modifier =
    padding(top = if (above) GroupSeparation else 0.dp, bottom = GroupSeparation)
