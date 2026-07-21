package com.mandrecode.tempo.features.whatsnew.presentation

import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.whatsnew.presentation.model.WhatsNewEntry

/**
 * Holds only the latest shipped feature — [latest] is the only entry ever shown to users, so
 * older entries carry no runtime value. Replace this value (not append to it) when shipping a
 * new feature (see openspec/changes/feat-210-whats-new-onboarding/design.md).
 */
object WhatsNewRegistry {
    val latest: WhatsNewEntry =
        WhatsNewEntry(
            // v1.1.0 already shipped (release-please cut it from #26 alone, before this
            // registry existed), so this feature (#210) ships in the next release, v1.2.0.
            versionCode = 1_002_000,
            versionName = "1.2.0",
            titleRes = R.string.whats_new_210_title,
            descriptionRes = R.string.whats_new_210_description,
        )
}
