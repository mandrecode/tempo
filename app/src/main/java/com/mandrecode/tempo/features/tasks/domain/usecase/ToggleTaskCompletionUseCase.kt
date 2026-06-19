package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import com.mandrecode.tempo.features.tasks.domain.util.TaskReminderDateUtil
import jakarta.inject.Inject
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ToggleTaskCompletionUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val taskReminderScheduler: TaskReminderScheduler,
        private val updateTaskUseCase: UpdateTaskUseCase,
    ) {
        sealed class Result {
            data class PeriodicCompleted(
                val updateResult: UpdateTaskUseCase.Result,
            ) : Result()

            /**
             * Returned when the user unchecks a parent task that previously archived a periodic
             * occurrence. The spawned next-instance was deleted and the original task's
             * recurrence was restored from it.
             */
            data class PeriodicRolledBack(
                val updateResult: UpdateTaskUseCase.Result,
            ) : Result()

            data class ParentToggled(
                val isCompleted: Boolean,
                val updateResult: UpdateTaskUseCase.Result,
            ) : Result()

            data class SubtaskToggled(
                val updateResult: UpdateTaskUseCase.Result,
            ) : Result()
        }

        suspend operator fun invoke(task: Task): Result {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            return when {
                task.periodicity != null && !task.isCompleted && task.parentTaskId == null ->
                    completePeriodic(task, now)
                task.parentTaskId == null && task.isCompleted && task.nextInstanceId != null ->
                    rollbackPeriodicCompletion(task, now)
                task.parentTaskId == null ->
                    toggleNonPeriodicParent(task, now)
                else ->
                    toggleSubtask(task, now)
            }
        }

        private data class PeriodicCommit(
            val archivedTask: Task,
            val nextInstance: Task?,
        )

        // ── Branch A: complete a periodic parent ────────────────────────────────────────
        private suspend fun completePeriodic(
            task: Task,
            now: LocalDateTime,
        ): Result {
            // Snapshot subtasks BEFORE the transaction so we don't keep the DB lock open while
            // computing reminder math, and so the post-commit scheduler calls have what they need.
            val subtasksBefore = taskRepository.getSubtasksSync(task.id)

            val baseDateTime = task.reminderDate ?: now
            // Periodicity is non-null at this branch (guarded by the dispatch in `invoke`).
            val periodicity = requireNotNull(task.periodicity)
            val nextReminderTime =
                TaskReminderDateUtil.calculateNextOccurrence(
                    from = baseDateTime,
                    periodicity = periodicity,
                    interval = task.periodicityInterval,
                    repeatDays = task.repeatDays,
                    monthDayOption = task.monthDayOption,
                )

            val commit =
                taskRepository.runInTransaction {
                    performPeriodicCompletionTxn(task, now, nextReminderTime, subtasksBefore)
                }

            taskReminderScheduler.cancel(commit.archivedTask)
            subtasksBefore.forEach { subtask ->
                if (subtask.reminderDate != null) taskReminderScheduler.cancel(subtask)
            }
            val scheduleResult = commit.nextInstance?.let { scheduleOrCancel(it) } ?: ScheduleResult.Skipped
            return Result.PeriodicCompleted(UpdateTaskUseCase.Result.Success(scheduleResult))
        }

        private suspend fun performPeriodicCompletionTxn(
            task: Task,
            now: LocalDateTime,
            nextReminderTime: LocalDateTime?,
            subtasksBefore: List<Task>,
        ): PeriodicCommit {
            val currentTask = taskRepository.getTaskById(task.id) ?: task

            // 1. Persist archived copy with recurrence stripped. We bypass UpdateTaskUseCase
            //    so the scheduler isn't called inside the transaction — we run it after commit.
            val archivedTask =
                currentTask.copy(
                    isCompleted = true,
                    periodicity = null,
                    periodicityInterval = 1,
                    repeatDays = null,
                    monthDayOption = null,
                    completedAt = now,
                    nextInstanceId = null,
                )
            taskRepository.updateTask(archivedTask)

            // 2. Mark only incomplete subtasks completed so rollback can keep prior completions.
            taskRepository.completeIncompleteSubtasks(task.id, now)

            var nextInstance =
                currentTask.nextInstanceId?.let { nextInstanceId ->
                    val existingNextInstance = taskRepository.getTaskById(nextInstanceId)
                    val reusableNextInstance =
                        existingNextInstance?.takeIf {
                            !it.isCompleted && it.parentTaskId == null && it.periodicity != null
                        }
                    if (reusableNextInstance != null) {
                        taskRepository.updateTaskNextInstanceId(currentTask.id, reusableNextInstance.id)
                        reusableNextInstance
                    } else {
                        taskRepository.updateTaskNextInstanceId(currentTask.id, null)
                        null
                    }
                }

            if (nextInstance == null) {
                val nextTaskInstance =
                    TaskReminderDateUtil.advanceReminderIfNeeded(
                        currentTask.copy(
                            id = 0,
                            isCompleted = false,
                            completedAt = null,
                            reminderDate = nextReminderTime,
                            nextInstanceId = null,
                        ),
                        now,
                    )
                val newParentId = taskRepository.insertTask(nextTaskInstance)
                if (newParentId > 0) {
                    taskRepository.updateTaskNextInstanceId(currentTask.id, newParentId)
                    cloneSubtasksUnder(newParentId, subtasksBefore)
                    nextInstance = nextTaskInstance.copy(id = newParentId)
                }
            }

            return PeriodicCommit(
                archivedTask = archivedTask.copy(nextInstanceId = nextInstance?.id),
                nextInstance = nextInstance,
            )
        }

        private suspend fun cloneSubtasksUnder(
            newParentId: Long,
            subtasksBefore: List<Task>,
        ) {
            if (subtasksBefore.isEmpty()) return
            val maxSortOrder = taskRepository.getMaxSubtaskSortOrder(newParentId)
            val clones =
                subtasksBefore.mapIndexed { index, subtask ->
                    subtask.copy(
                        id = 0,
                        isCompleted = false,
                        completedAt = null,
                        parentTaskId = newParentId,
                        reminderDate = null,
                        periodicity = null,
                        sortOrder = maxSortOrder + index + 1,
                        nextInstanceId = null,
                    )
                }
            taskRepository.insertTasks(clones)
        }

        private fun scheduleOrCancel(task: Task): ScheduleResult {
            if (task.reminderDate == null) {
                taskReminderScheduler.cancel(task)
                return ScheduleResult.Skipped
            }
            taskReminderScheduler.dismissNotification(task.id)
            val r = taskReminderScheduler.schedule(task)
            if (r is ScheduleResult.Skipped) taskReminderScheduler.cancel(task)
            return r
        }

        private data class RollbackCommit(
            val restoredTask: Task,
            val restoredSubtasks: List<Task>,
        )

        // ── Branch A-rollback: uncheck an archived task to discard the spawned occurrence ──
        private suspend fun rollbackPeriodicCompletion(
            archivedTask: Task,
            now: LocalDateTime,
        ): Result {
            val nextInstanceId = archivedTask.nextInstanceId ?: error("rollback called without nextInstanceId")

            // Snapshot before transaction. If the next instance was deleted by the user already,
            // we still proceed to restore the archived task — just nothing to delete.
            val nextInstance = taskRepository.getTaskById(nextInstanceId)
            val archivedSubtasks = taskRepository.getSubtasksSync(archivedTask.id)

            val commit =
                taskRepository.runInTransaction {
                    performRollbackTxn(archivedTask, nextInstance, archivedSubtasks, now)
                }

            applyRollbackSchedulerEffects(nextInstance, commit)
            val scheduleResult = scheduleOrCancel(commit.restoredTask)
            return Result.PeriodicRolledBack(UpdateTaskUseCase.Result.Success(scheduleResult))
        }

        private suspend fun performRollbackTxn(
            archivedTask: Task,
            nextInstance: Task?,
            archivedSubtasks: List<Task>,
            now: LocalDateTime,
        ): RollbackCommit {
            // 1. Delete the spawned next instance and its cloned subtasks.
            if (nextInstance != null) {
                taskRepository.deleteTaskWithSubtasks(nextInstance.id)
            }

            // 2. Restore archived task. Recurrence fields come from the next instance
            //    (which preserved them); fall back to the archived task's stripped
            //    state if next instance is gone (best-effort).
            val restoredTask =
                TaskReminderDateUtil.advanceReminderIfNeeded(
                    archivedTask.copy(
                        isCompleted = false,
                        completedAt = null,
                        nextInstanceId = null,
                        periodicity = nextInstance?.periodicity ?: archivedTask.periodicity,
                        periodicityInterval = nextInstance?.periodicityInterval ?: archivedTask.periodicityInterval,
                        repeatDays = nextInstance?.repeatDays ?: archivedTask.repeatDays,
                        monthDayOption = nextInstance?.monthDayOption ?: archivedTask.monthDayOption,
                        reminderDate = nextInstance?.reminderDate ?: archivedTask.reminderDate,
                    ),
                    now,
                )
            taskRepository.updateTask(restoredTask)

            // 3. Restore subtasks that were auto-completed by this toggle.
            val restoredSubtasks = restoreAutoCompletedSubtasks(archivedTask.completedAt, archivedSubtasks)

            return RollbackCommit(restoredTask, restoredSubtasks)
        }

        private suspend fun restoreAutoCompletedSubtasks(
            archivedCompletedAt: LocalDateTime?,
            archivedSubtasks: List<Task>,
        ): List<Task> {
            if (archivedCompletedAt == null) return emptyList()
            // Auto-completed subtasks are stamped with the exact parent completion timestamp.
            val toRestore =
                archivedSubtasks.filter { sub ->
                    sub.isCompleted && sub.completedAt == archivedCompletedAt
                }
            return toRestore.map { sub ->
                val restored = sub.copy(isCompleted = false, completedAt = null)
                taskRepository.updateTask(restored)
                restored
            }
        }

        private fun applyRollbackSchedulerEffects(
            nextInstance: Task?,
            commit: RollbackCommit,
        ) {
            // Cancel the deleted next instance's reminder (if it had one).
            if (nextInstance?.reminderDate != null) {
                taskReminderScheduler.cancel(nextInstance)
            }
            // Re-schedule reminders for restored subtasks.
            commit.restoredSubtasks.forEach { sub ->
                if (sub.reminderDate != null) {
                    taskReminderScheduler.schedule(sub)
                }
            }
        }

        // ── Branch B: parent without periodicity ────────────────────────────────────────
        private suspend fun toggleNonPeriodicParent(
            task: Task,
            now: LocalDateTime,
        ): Result {
            val isNowCompleted = !task.isCompleted
            val updatedTask =
                task.copy(
                    isCompleted = isNowCompleted,
                    completedAt = if (isNowCompleted) now else null,
                    // Re-completing an archived task without rolling back makes the existing
                    // nextInstanceId link stale (the spawn is no longer "the latest action").
                    // Clear it so a future uncheck doesn't try to roll back stale state.
                    nextInstanceId = if (isNowCompleted) null else task.nextInstanceId,
                )
            val updateResult = updateTaskUseCase(updatedTask)

            val subtasks = taskRepository.getSubtasksSync(task.id)
            taskRepository.updateSubtasksCompletion(
                task.id,
                isNowCompleted,
                if (isNowCompleted) now else null,
            )
            subtasks.forEach { subtask ->
                if (isNowCompleted) {
                    if (subtask.reminderDate != null) taskReminderScheduler.cancel(subtask)
                } else {
                    if (subtask.reminderDate != null) taskReminderScheduler.schedule(subtask)
                }
            }

            return Result.ParentToggled(isNowCompleted, updateResult)
        }

        // ── Branch C: subtask ───────────────────────────────────────────────────────────
        private suspend fun toggleSubtask(
            task: Task,
            now: LocalDateTime,
        ): Result {
            val isNowCompleted = !task.isCompleted
            val updatedTask =
                task.copy(
                    isCompleted = isNowCompleted,
                    completedAt = if (isNowCompleted) now else null,
                )
            val updateResult = updateTaskUseCase(updatedTask)
            return Result.SubtaskToggled(updateResult)
        }
    }
