package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.core.domain.repository.DailyFocusActivityRepository
import com.mandrecode.tempo.core.domain.usecase.DailyActivityRecorder
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.tasks.domain.model.Task
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
        private val activityRepository: DailyFocusActivityRepository,
        private val clock: Clock,
    ) : DailyActivityRecorder {
        override suspend fun recordToday() {
            invoke(clock.todayIn(TimeZone.currentSystemDefault()))
        }

        suspend operator fun invoke(today: LocalDate) {
            val tasks = taskRepository.getAllTasks().first()
            val habits = habitRepository.getAllHabits().first()

            val countedTasks = tasks.filter { it.countsTowards(today) }
            val countedHabits = habits.filter { CompletionHistoryUtil.isScheduledOn(today, it.repeatDays) }

            activityRepository.recordCounts(
                date = today,
                scheduledCount = countedTasks.size + countedHabits.size,
                completedCount =
                    countedTasks.count { it.isCompleted } +
                        countedHabits.count { it.wasCompletedOn(today) },
            )
        }

        /**
         * Subtasks are excluded: they are counted through their parent, so including both would
         * make a task with five subtasks weigh six times a task with none.
         *
         * A task counts for today when it is due today, or when it is overdue and still open —
         * matching what the Focus agenda puts in front of the user.
         */
        private fun Task.countsTowards(today: LocalDate): Boolean {
            val dueDate = reminderDate?.date
            return parentTaskId == null &&
                dueDate != null &&
                (dueDate == today || (dueDate < today && !isCompleted))
        }

        private fun Habit.wasCompletedOn(today: LocalDate): Boolean =
            CompletionHistoryUtil.isDateInHistory(completionHistory, today.toString())
    }
