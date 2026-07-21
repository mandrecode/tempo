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
            WhatsNewEntry(
                versionCode = 1_001_000,
                versionName = "1.1.0",
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
