package com.mandrecode.tempo.features.whatsnew.presentation

import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.whatsnew.presentation.model.WhatsNewEntry

/**
 * Newest-first, append-only registry of shipped features. Append one entry per feature at the
 * top when shipping it — [entries].first() is the only entry ever shown to users (see
 * openspec/changes/feat-210-whats-new-onboarding/design.md).
 */
object WhatsNewRegistry {
    val entries: List<WhatsNewEntry> =
        listOf(
            // v1.1.0 already shipped (release-please cut it from #26 alone, before this
            // registry existed), so this feature (#210) ships in the next release, v1.2.0.
            WhatsNewEntry(
                versionCode = 1_002_000,
                versionName = "1.2.0",
                titleRes = R.string.whats_new_210_title,
                descriptionRes = R.string.whats_new_210_description,
            ),
            WhatsNewEntry(
                versionCode = 1_001_000,
                versionName = "1.1.0",
                titleRes = R.string.whats_new_26_title,
                descriptionRes = R.string.whats_new_26_description,
            ),
        )
}
