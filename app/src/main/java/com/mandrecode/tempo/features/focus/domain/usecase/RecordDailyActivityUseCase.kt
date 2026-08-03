package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.core.domain.repository.DailyFocusActivityRepository
import com.mandrecode.tempo.core.domain.usecase.DailyActivityRecorder
import com.mandrecode.tempo.features.focus.domain.model.isOnFocusDay
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.util.CompletionHistoryUtil
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Recomputes today's scheduled and completed counts from the current task and habit state.
 *
 * Recomputed rather than incremented because the denominator moves: work gets added, rescheduled
 * and deleted during the day, so `scheduledCount` is only final once the day ends. Past days are
 * never revisited — they keep whatever was last written while they were current.
 */
class RecordDailyActivityUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val habitRepository: HabitRepository,
        private val habitChainRepository: HabitChainRepository,
        private val activityRepository: DailyFocusActivityRepository,
        private val clock: Clock,
    ) : DailyActivityRecorder {
        override suspend fun recordToday() {
            invoke(clock.todayIn(TimeZone.currentSystemDefault()))
        }

        suspend operator fun invoke(today: LocalDate) {
            val tasks = taskRepository.getAllTasks().first()
            val habits = habitRepository.getAllHabits().first()
            val chains = habitChainRepository.getAllHabitChains().first()

            // Counted the way the Focus agenda shows the day, or the heatmap and the hero would
            // disagree about the same date: a chain is one thing on screen, so it is one thing
            // here, and the habits inside it are not counted again on their own. Tasks are counted
            // by the agenda's own membership rule for the same reason — a subtask counts when, and
            // only when, it is the thing showing on the day rather than a step inside a parent.
            val chainedHabitIds = chains.flatMap { it.habitIds }.toSet()

            val tasksById = tasks.associateBy { it.id }
            val countedTasks = tasks.filter { it.isOnFocusDay(today, tasksById) }
            val countedHabits =
                habits.filter {
                    it.id !in chainedHabitIds &&
                        CompletionHistoryUtil.isScheduledOn(today, it.repeatDays)
                }
            val countedChains =
                chains.filter { CompletionHistoryUtil.isScheduledOn(today, it.repeatDays) }

            activityRepository.recordCounts(
                date = today,
                scheduledCount = countedTasks.size + countedHabits.size + countedChains.size,
                completedCount =
                    countedTasks.count { it.isCompleted } +
                        countedHabits.count { it.wasCompletedOn(today) } +
                        countedChains.count { it.wasCompletedOn(today) },
            )
        }

        private fun Habit.wasCompletedOn(today: LocalDate): Boolean =
            CompletionHistoryUtil.isDateInHistory(completionHistory, today.toString())

        private fun HabitChain.wasCompletedOn(today: LocalDate): Boolean =
            CompletionHistoryUtil.isDateInHistory(completionHistory, today.toString())
    }
