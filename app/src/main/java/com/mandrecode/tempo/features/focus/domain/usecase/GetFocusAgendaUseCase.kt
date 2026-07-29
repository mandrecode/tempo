package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.features.focus.domain.model.FocusAgenda
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.util.CompletionHistoryUtil
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
 * than a second Tasks tab.
 */
class GetFocusAgendaUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val habitRepository: HabitRepository,
        private val habitChainRepository: HabitChainRepository,
        private val getUpNextItem: GetUpNextItemUseCase,
    ) {
        operator fun invoke(today: LocalDate): Flow<FocusAgenda> =
            combine(
                taskRepository.getAllTasks(),
                habitRepository.getAllHabits(),
                habitChainRepository.getAllHabitChains(),
            ) { tasks, habits, chains ->
                build(today, tasks, habits, chains)
            }

        private fun build(
            today: LocalDate,
            tasks: List<Task>,
            habits: List<Habit>,
            chains: List<HabitChain>,
        ): FocusAgenda {
            val subtasksByParent = tasks.filter { it.parentTaskId != null }.groupBy { it.parentTaskId }
            val topLevelTasks = tasks.filter { it.parentTaskId == null }

            val overdueTasks =
                topLevelTasks
                    .filter { task ->
                        val dueDate = task.reminderDate?.date
                        dueDate != null && dueDate < today && !task.isCompleted
                    }.map { FocusAgendaItem.TaskEntry(it, subtasksByParent[it.id].orEmpty()) }

            val todayTasks =
                topLevelTasks
                    .filter { it.reminderDate?.date == today }
                    .map { FocusAgendaItem.TaskEntry(it, subtasksByParent[it.id].orEmpty()) }

            // Habits inside a chain are shown by the chain's own card, not as separate rows.
            val chainedHabitIds = chains.flatMap { it.habitIds }.toSet()
            val todayHabits =
                habits
                    .filter { it.id !in chainedHabitIds }
                    .filter { CompletionHistoryUtil.isScheduledOn(today, it.repeatDays) }
                    .map { habit ->
                        FocusAgendaItem.HabitEntry(
                            habit = habit,
                            isCompleted = habit.wasCompletedOn(today),
                        )
                    }

            val todayChains =
                chains
                    .filter { CompletionHistoryUtil.isScheduledOn(today, it.repeatDays) }
                    .map { chain ->
                        FocusAgendaItem.ChainEntry(
                            chain = chain,
                            habits = habits.filter { it.id in chain.habitIds },
                            isCompleted = chain.wasCompletedOn(today),
                        )
                    }

            val overdue = overdueTasks.sortedByAgendaOrder()
            val todayItems = (todayTasks + todayHabits + todayChains).sortedByAgendaOrder()

            return FocusAgenda(
                upNext = getUpNextItem(overdue + todayItems),
                overdue = overdue,
                today = todayItems,
                undatedTaskCount = topLevelTasks.count { it.reminderDate == null && !it.isCompleted },
            )
        }

        /** Timed items first in clock order, then untimed ones, with completed work sinking last. */
        private fun List<FocusAgendaItem>.sortedByAgendaOrder(): List<FocusAgendaItem> =
            sortedWith(
                compareBy<FocusAgendaItem> { it.isCompleted }
                    .thenBy { it.dueTime == null }
                    .thenBy { it.dueTime }
                    .thenBy { it.id },
            )

        private fun Habit.wasCompletedOn(date: LocalDate): Boolean =
            CompletionHistoryUtil.isDateInHistory(completionHistory, date.toString())

        private fun HabitChain.wasCompletedOn(date: LocalDate): Boolean =
            CompletionHistoryUtil.isDateInHistory(completionHistory, date.toString())
    }
