package com.mandrecode.tempo.core.domain.util

import com.mandrecode.tempo.core.domain.model.TempoTab

/**
 * The two invariants governing tab preferences, in one place.
 *
 * Both rules were previously reimplemented in `SettingsViewModel`, `OnboardingViewModel` and
 * `BackupSettingsDataSource`, which is why a third tab would otherwise have had to be taught to
 * each of them separately. Callers apply these functions instead of restating the rules.
 */
object TabPreferencesPolicy {
    /** At least one tab is always enabled; an empty selection falls back to [TempoTab.DEFAULT]. */
    fun resolveEnabledTabs(enabledTabs: Set<TempoTab>): Set<TempoTab> = enabledTabs.ifEmpty { setOf(TempoTab.DEFAULT) }

    /** A tab may only be disabled while another enabled tab remains. */
    fun canDisable(
        enabledTabs: Set<TempoTab>,
        tab: TempoTab,
    ): Boolean = tab !in enabledTabs || enabledTabs.size > 1

    /**
     * The default tab is always an enabled one. When the stored default has been disabled, the
     * first enabled tab in display order takes over.
     */
    fun resolveDefaultTab(
        defaultTab: TempoTab,
        enabledTabs: Set<TempoTab>,
    ): TempoTab {
        val enabled = resolveEnabledTabs(enabledTabs)
        return if (defaultTab in enabled) {
            defaultTab
        } else {
            enabled.minByOrNull { it.ordinal } ?: TempoTab.DEFAULT
        }
    }

    /**
     * Applies an enable/disable request, returning the resulting set unchanged when the request
     * would leave no tab enabled.
     */
    fun withTabEnabled(
        enabledTabs: Set<TempoTab>,
        tab: TempoTab,
        enabled: Boolean,
    ): Set<TempoTab> =
        when {
            enabled -> enabledTabs + tab
            !canDisable(enabledTabs, tab) -> enabledTabs
            else -> enabledTabs - tab
        }
}
