package com.mandrecode.tempo.core.domain.model

/**
 * The app's top-level navigation destinations.
 *
 * Declaration order is display order in both the bottom bar and the rail, and every entry is always
 * shown. [preferenceValue] is the persisted identifier — it is the saved "last route" name the app
 * reopens on, so existing values must not be renamed.
 */
enum class TempoTab(
    val preferenceValue: String,
) {
    // Focus sits in the middle (#440): it is the home surface, and the centre of the bar is the
    // easiest reach from either thumb. Identity is preferenceValue, never position, so reordering
    // touches no stored data.
    ROUTINES("routines"),
    FOCUS("focus"),
    TASKS("tasks"),
    ;

    companion object {
        /**
         * The tab the app opens on when there is no last-used tab to reopen on (a first launch).
         * Focus is the app's home surface.
         */
        val DEFAULT: TempoTab = FOCUS

        fun fromPreferenceValue(value: String?): TempoTab? = entries.firstOrNull { it.preferenceValue == value }
    }
}
