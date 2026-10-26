package com.mandrecode.tempo.features.tasks.presentation.model

import androidx.compose.runtime.Immutable
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.TaskSection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate

/** One section of the selected category (#61) with its active tasks, already in sort order. */
@Immutable
data class SectionUi(
    val section: TaskSection,
    val tasks: ImmutableList<Task>,
    /** Active tasks whose reminder day has passed, surfaced on the header while collapsed. */
    val overdueCount: Int,
)

/**
 * The active list of a category that has sections (#61): unsectioned tasks first, without a
 * header, then every section in its order — empty ones included, since a section is not a filter.
 */
@Immutable
data class SectionedTaskList(
    val unsectioned: ImmutableList<Task>,
    val sections: ImmutableList<SectionUi>,
)

/**
 * Splits [activeTasks] (top-level, already sorted) by section. Returns null when the category has
 * no sections, so the list keeps today's grouping exactly. A task pointing at a section that is
 * not one of [sections] reads as unsectioned rather than disappearing.
 */
internal fun buildSectionedList(
    activeTasks: List<Task>,
    sections: List<TaskSection>,
    today: LocalDate,
): SectionedTaskList? {
    if (sections.isEmpty()) return null
    val sectionIds = sections.mapTo(mutableSetOf()) { it.id }
    val bySection = activeTasks.groupBy { task -> task.sectionId?.takeIf { it in sectionIds } }
    return SectionedTaskList(
        unsectioned = bySection[null].orEmpty().toPersistentList(),
        sections =
            sections
                .map { section ->
                    val tasks = bySection[section.id].orEmpty()
                    SectionUi(
                        section = section,
                        tasks = tasks.toPersistentList(),
                        overdueCount = tasks.count { task -> task.reminderDate?.let { it.date < today } == true },
                    )
                }.toPersistentList(),
    )
}
