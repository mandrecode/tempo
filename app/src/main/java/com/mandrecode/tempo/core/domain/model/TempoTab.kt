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
         * The tab the app opens on unless the user has chosen otherwise.
         *
         * Focus is the app's home surface, so it is the default for new and existing
         * installations alike. Existing users are moved to it once by the preference migration
         * and told about the change in the what's-new sheet, which points at the Settings entry
         * that reverses it.
         */
        val DEFAULT: TempoTab = FOCUS

        fun fromPreferenceValue(value: String?): TempoTab? = entries.firstOrNull { it.preferenceValue == value }
    }
}
