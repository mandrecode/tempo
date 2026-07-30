package com.mandrecode.tempo.features.focus.domain.model

/**
 * How far into the day the user is, as one of four bands plus the unscheduled case.
 *
 * The hero headline describes progress rather than the streak, so the copy has to change as the day
 * fills in. Bands rather than a percentage on purpose: the summary reports the day, it does not
 * score it.
 */
enum class FocusHeadlineBand {
    /** Nothing scheduled at all — distinct from [COMPLETE], which means the work is done. */
    NOTHING_SCHEDULED,
    JUST_STARTED,
    UNDER_WAY,
    NEARLY_THERE,
    COMPLETE,
    ;

    companion object {
        private const val UNDER_WAY_THRESHOLD = 1.0 / 3.0
        private const val NEARLY_THERE_THRESHOLD = 2.0 / 3.0

        /**
         * Bands are evaluated on completed ÷ scheduled. [COMPLETE] requires every scheduled item to
         * be done rather than merely crossing a ratio, so a rounding artefact can never claim a day
         * is finished while something is still open.
         */
        fun resolve(
            scheduledCount: Int,
            completedCount: Int,
        ): FocusHeadlineBand =
            when {
                scheduledCount <= 0 -> NOTHING_SCHEDULED
                completedCount >= scheduledCount -> COMPLETE
                else ->
                    when (completedCount.toDouble() / scheduledCount) {
                        in 0.0..<UNDER_WAY_THRESHOLD -> JUST_STARTED
                        in UNDER_WAY_THRESHOLD..<NEARLY_THERE_THRESHOLD -> UNDER_WAY
                        else -> NEARLY_THERE
                    }
            }
    }
}
