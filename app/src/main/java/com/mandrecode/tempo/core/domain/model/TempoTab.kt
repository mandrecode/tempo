package com.mandrecode.tempo.core.domain.model

/**
 * The app's top-level navigation destinations.
 *
 * Declaration order is display order in both the bottom bar and the rail, so adding an entry here
 * is what makes a tab appear, become toggleable in Settings and Onboarding, become selectable as
 * the default tab, and round-trip through backup. [preferenceValue] is the persisted identifier —
 * it is also used as the saved "last route" name, so existing values must not be renamed.
 */
enum class TempoTab(
    val preferenceValue: String,
) {
    FOCUS("focus"),
    ROUTINES("routines"),
    TASKS("tasks"),
    ;

    companion object {
        /**
         * The tab an installation falls back to when no explicit preference is stored.
         *
         * Deliberately [ROUTINES] rather than [FOCUS]: this is the value existing installations
         * have always resolved to, and adding Focus must not silently move anyone's start tab.
         * New installations are seeded with [FOCUS] during onboarding instead.
         */
        val DEFAULT: TempoTab = ROUTINES

        fun fromPreferenceValue(value: String?): TempoTab? = entries.firstOrNull { it.preferenceValue == value }
    }
}
