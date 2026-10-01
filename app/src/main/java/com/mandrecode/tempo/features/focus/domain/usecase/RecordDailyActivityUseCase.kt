package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.core.domain.repository.DailyFocusActivityRepository
import com.mandrecode.tempo.core.domain.usecase.DailyActivityRecorder
import com.mandrecode.tempo.features.focus.domain.model.isOnFocusDay
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Recomputes today's scheduled and completed counts from the current task state.
 *
 * Recomputed rather than incremented because the denominator moves: work gets added, rescheduled
 * and deleted during the day, so `scheduledCount` is only final once the day ends. Past days are
 * never revisited — they keep whatever was last written while they were current.
 */
class RecordDailyActivityUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val activityRepository: DailyFocusActivityRepository,
        private val clock: Clock,
    ) : DailyActivityRecorder {
        override suspend fun recordToday() {
            invoke(clock.todayIn(TimeZone.currentSystemDefault()))
        }

        suspend operator fun invoke(today: LocalDate) {
            val tasks = taskRepository.getAllTasks().first()

            val tasksById = tasks.associateBy { it.id }
            val countedTasks = tasks.filter { it.isOnFocusDay(today, tasksById) }
            activityRepository.recordCounts(
                date = today,
                scheduledCount = countedTasks.size,
                completedCount =
                    countedTasks.count { it.isCompleted },
            )
        }
    }
