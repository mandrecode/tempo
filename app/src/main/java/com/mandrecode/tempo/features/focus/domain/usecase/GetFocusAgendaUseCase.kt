package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.features.focus.domain.model.FocusAgenda
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.focus.domain.model.TaskFocusToday
import com.mandrecode.tempo.features.focus.domain.model.isOnFocusDay
import com.mandrecode.tempo.features.focus.domain.repository.FocusSessionRepository
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

/**
 * Assembles what Focus shows for a given day: what is overdue, what is due today, and the single
 * item to spotlight.
 *
 * Membership follows issue #42's today-only rule — future work is hidden, and tasks with no date
 * are excluded entirely and reported only as a count, so the screen stays a view of the day rather
 * than a second Tasks tab. Which tasks that leaves is [isOnFocusDay]'s answer, shared with the day's
 * counts so the two can never drift.
 */
class GetFocusAgendaUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val categoryRepository: CategoryRepository,
        private val getUpNextItem: GetUpNextItemUseCase,
        private val sessionRepository: FocusSessionRepository,
    ) {
        operator fun invoke(today: LocalDate): Flow<FocusAgenda> =
            combine(
                taskRepository.getAllTasks(),
                categoryRepository.getAllCategories(),
                sessionRepository.focusToday,
            ) { tasks, categories, focusByTask ->
                build(
                    today = today,
                    tasks = tasks,
                    categoriesById = categories.associateBy { it.id },
                    focusByTask = focusByTask,
                )
            }

        private fun build(
            today: LocalDate,
            tasks: List<Task>,
            categoriesById: Map<Long, Category>,
            focusByTask: Map<Long, TaskFocusToday>,
        ): FocusAgenda {
            // Steps read in the order they were given, the way the Tasks tab lists them. The task
            // query behind this orders by descending id and nothing else, so without this a task
            // showed its steps backwards here and forwards there.
            val subtasksByParent =
                tasks
                    .filter { it.parentTaskId != null }
                    .groupBy { it.parentTaskId }
                    .mapValues { (_, subtasks) ->
                        subtasks.sortedWith(compareBy<Task> { it.sortOrder }.thenBy { it.id })
                    }
            val tasksById = tasks.associateBy { it.id }

            // Subtasks are here too, not only top-level tasks: a step with a time on it under a
            // parent that has none is the only thing on the day that says when, so it stands as its
            // own row. [isOnFocusDay] is what decides which ones those are.
            val datedTasks = tasks.filter { it.isOnFocusDay(today, tasksById) }

            val overdueTasks =
                datedTasks
                    .filter { it.reminderDate?.date != today }
                    .map { it.toEntry(subtasksByParent, categoriesById, focusByTask) }

            val todayTasks =
                datedTasks
                    .filter { it.reminderDate?.date == today }
                    .map { it.toEntry(subtasksByParent, categoriesById, focusByTask) }

            val overdue = overdueTasks.sortedByAgendaOrder()
            val todayItems = todayTasks.sortedByAgendaOrder()

            // A shortlist over the day rather than a slice taken out of it: the row is somewhere
            // to start from, not a fourth section, so the work still appears where it belongs.
            //
            // Today first, then what is left over from before it. The row offering last week's
            // errand ahead of this morning's meeting was Focus arguing with its own headline — and
            // the sections below read the same way round, so the two never disagree.
            return FocusAgenda(
                upNext = getUpNextItem(todayItems + overdue),
                overdue = overdue,
                today = todayItems,
                // Undated *top-level* work only. A step with no time of its own is not a loose end
                // waiting in Tasks; it is part of whatever its parent is.
                undatedTaskCount =
                    tasks.count {
                        it.parentTaskId == null && it.reminderDate == null && !it.isCompleted
                    },
            )
        }

        private fun Task.toEntry(
            subtasksByParent: Map<Long?, List<Task>>,
            categoriesById: Map<Long, Category>,
            focusByTask: Map<Long, TaskFocusToday>,
        ) = FocusAgendaItem.TaskEntry(
            task = this,
            subtasks = subtasksByParent[id].orEmpty(),
            categoryName = categoriesById[categoryId]?.name,
            focusToday = focusByTask[id] ?: TaskFocusToday(),
        )

        /** Timed items first in clock order, then untimed ones, with completed work sinking last. */
        private fun List<FocusAgendaItem>.sortedByAgendaOrder(): List<FocusAgendaItem> =
            sortedWith(
                compareBy<FocusAgendaItem> { it.isCompleted }
                    .thenBy { it.dueTime == null }
                    .thenBy { it.dueTime }
                    .thenBy { it.id },
            )
    }
