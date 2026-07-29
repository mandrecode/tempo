package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import jakarta.inject.Inject

/**
 * Orders the tasks the Up next row offers: highest priority first, earliest due time second.
 *
 * A queue rather than a single pick, because the best next thing is not always the one you are
 * ready to do — the row lets you swipe past it to the one you are, without leaving the screen.
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
        operator fun invoke(candidates: List<FocusAgendaItem>): List<FocusAgendaItem.TaskEntry> =
            candidates
                .asSequence()
                .filterIsInstance<FocusAgendaItem.TaskEntry>()
                .filterNot { it.isCompleted }
                .sortedWith(
                    compareBy<FocusAgendaItem.TaskEntry> { it.priority?.sortOrder ?: UNPRIORITISED }
                        .thenBy { it.dueTime == null }
                        .thenBy { it.dueTime }
                        .thenBy { it.id },
                ).take(MAX_UP_NEXT)
                .toList()

        private companion object {
            /** Sorts after every real priority, whatever their sort orders are. */
            const val UNPRIORITISED = Int.MAX_VALUE

            /**
             * Enough to swipe past what you are not ready for, few enough that the row stays a
             * shortlist and the day's own list still has something left in it.
             */
            const val MAX_UP_NEXT = 5
        }
    }
