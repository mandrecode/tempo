package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.features.focus.domain.repository.FocusSessionRepository
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import jakarta.inject.Inject

/**
 * Starts a focus session on a task known only by its id — the entry point for a task reminder's
 * Start session action, which runs in the background with nothing but the id in its intent.
 *
 * The title is read from the task rather than carried in the intent, so a task renamed after its
 * reminder fired still runs under the name the user now knows it by.
 */
class StartFocusSessionOnTaskUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val sessionRepository: FocusSessionRepository,
        private val focusSessionUseCases: FocusSessionUseCases,
    ) {
        /** @return whether a session is now running on [taskId]. */
        suspend operator fun invoke(taskId: Long): Boolean {
            // A reminder can outlive its task's open state: it may have been ticked off in the app
            // since, and then there is no work left to focus on.
            val task = taskRepository.getTaskById(taskId)?.takeIf { !it.isCompleted } ?: return false

            // Pressing start on the work already under way is carrying on, not starting over —
            // restarting would throw away the time already on the clock.
            val running = sessionRepository.activeSession.value
            val alreadyRunning = running != null && !running.isBreak && running.taskId == taskId
            if (!alreadyRunning) {
                focusSessionUseCases.start(taskId = task.id, taskTitle = task.title)
                // The same as starting from the card: once running, the task is no longer a preview.
                sessionRepository.setPreviewTaskId(null)
            }
            return true
        }
    }
