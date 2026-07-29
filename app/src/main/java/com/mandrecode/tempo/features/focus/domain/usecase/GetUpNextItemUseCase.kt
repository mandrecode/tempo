package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import jakarta.inject.Inject

/**
 * Picks the single task the Up next card spotlights: highest priority first, earliest due time
 * second.
 *
 * Tasks only. The card's whole purpose is to be the one thing you start a session on, and a habit
 * is not something you sit down and run a timer against — it is ticked off in passing. A habit in
 * that slot offers a start button that cannot start, so habits stay in the day's list where they
 * belong.
 *
 * Priority outranks time on purpose — a high-priority task due this afternoon matters more than an
 * unprioritised one due this morning. Tasks with no time sort after timed ones, and completed tasks
 * are never candidates.
 */
class GetUpNextItemUseCase
    @Inject
    constructor() {
        operator fun invoke(candidates: List<FocusAgendaItem>): FocusAgendaItem.TaskEntry? =
            candidates
                .asSequence()
                .filterIsInstance<FocusAgendaItem.TaskEntry>()
                .filterNot { it.isCompleted }
                .sortedWith(
                    compareBy<FocusAgendaItem.TaskEntry> { it.priority?.sortOrder ?: UNPRIORITISED }
                        .thenBy { it.dueTime == null }
                        .thenBy { it.dueTime }
                        .thenBy { it.id },
                ).firstOrNull()

        private companion object {
            /** Sorts after every real priority, whatever their sort orders are. */
            const val UNPRIORITISED = Int.MAX_VALUE
        }
    }
