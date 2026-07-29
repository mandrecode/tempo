package com.mandrecode.tempo.core.domain.usecase

/**
 * Recomputes and stores today's activity counts.
 *
 * Declared here rather than in the Focus feature so that completing a task or a habit — which can
 * happen from any tab — can trigger a recount without Tasks or Routines depending on Focus. The
 * implementation lives in `features/focus` and is bound in `RepositoryModule`.
 */
interface DailyActivityRecorder {
    suspend fun recordToday()
}
