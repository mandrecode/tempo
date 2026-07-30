package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import jakarta.inject.Inject

/**
 * Picks the tasks the Up next row offers, in the order the day already puts them in.
 *
 * Deliberately does no sorting of its own. The row sits directly above the list it shortlists, and
 * a row that disagreed with the list about what comes first made one of them look wrong — so the
 * caller hands over the agenda already ordered and this only chooses what belongs in the row.
 *
 * Tasks only. The row's whole purpose is the one thing you start a session on, and a habit is not
 * something you sit down and run a timer against — it is ticked off in passing. Completed work is
 * never a candidate.
 */
class GetUpNextItemUseCase
    @Inject
    constructor() {
        operator fun invoke(candidates: List<FocusAgendaItem>): List<FocusAgendaItem.TaskEntry> =
            candidates
                .asSequence()
                .filterIsInstance<FocusAgendaItem.TaskEntry>()
                .filterNot { it.isCompleted }
                .take(MAX_UP_NEXT)
                .toList()

        private companion object {
            /**
             * Enough to swipe past what you are not ready for, few enough that the row stays a
             * shortlist rather than a second copy of the day.
             */
            const val MAX_UP_NEXT = 5
        }
    }
