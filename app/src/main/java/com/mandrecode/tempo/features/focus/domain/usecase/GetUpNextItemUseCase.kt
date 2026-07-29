package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import jakarta.inject.Inject

/**
 * Picks the single item the Up next card spotlights: highest priority first, earliest due time
 * second.
 *
 * Priority outranks time on purpose — a high-priority task due this afternoon matters more than an
 * unprioritised one due this morning. Items with no time sort after timed ones, and completed items
 * are never candidates.
 */
class GetUpNextItemUseCase
    @Inject
    constructor() {
        operator fun invoke(candidates: List<FocusAgendaItem>): FocusAgendaItem? =
            candidates
                .asSequence()
                .filterNot { it.isCompleted }
                .sortedWith(
                    compareBy<FocusAgendaItem> { it.priority?.sortOrder ?: UNPRIORITISED }
                        .thenBy { it.dueTime == null }
                        .thenBy { it.dueTime }
                        .thenBy { it.id },
                ).firstOrNull()

        private companion object {
            /** Sorts after every real priority, whatever their sort orders are. */
            const val UNPRIORITISED = Int.MAX_VALUE
        }
    }
