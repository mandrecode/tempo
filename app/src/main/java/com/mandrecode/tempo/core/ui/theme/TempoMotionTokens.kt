package com.mandrecode.tempo.core.ui.theme

/**
 * Central source of truth for animation durations across the app.
 *
 * Use these semantic tokens instead of redeclaring per-file `ANIMATION_DURATION` constants so that
 * navigation transitions, modal sheets and other motion stay consistent and are tuned in one place.
 *
 * Values follow the Material 3 motion duration scale (short / medium / standard / long).
 */
object TempoMotionTokens {
    /** Small, incidental motion such as press-state corner morphs. */
    const val DURATION_SHORT_MILLIS = 150

    /** Quick content swaps, expand/collapse of compact elements. */
    const val DURATION_MEDIUM_MILLIS = 200

    /** Modal sheet entrance/exit motion, tuned to feel close to Material3 ModalBottomSheet. */
    const val DURATION_SHEET_MILLIS = DURATION_MEDIUM_MILLIS

    /** Default app transition: navigation fades, modal sheet open/close, settings slides. */
    const val DURATION_STANDARD_MILLIS = 300

    /** Larger, more expressive motion such as full-width selectors. */
    const val DURATION_LONG_MILLIS = 400
}
