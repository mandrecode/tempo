package com.mandrecode.tempo.features.whatsnew.presentation

import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.whatsnew.presentation.model.WhatsNewEntry

/**
 * Holds only the latest shipped feature — [latest] is the only entry ever shown to users, so
 * older entries carry no runtime value. Replace this value (not append to it) — including
 * `whats_new_title`/`whats_new_description` in strings.xml — when shipping a new feature (see
 * openspec/changes/feat-210-whats-new-onboarding/design.md).
 */
object WhatsNewRegistry {
    val latest: WhatsNewEntry =
        WhatsNewEntry(
            // v1.2.0 already shipped this registry itself (#210); this feature (#46) is a
            // `feat` commit with no BREAKING CHANGE footer, so it ships in the next minor
            // release, v1.3.0 — recheck against version.txt/the release cadence before merge.
            // Replaces the entry #28 (encrypt local database at rest) added for the same
            // v1.3.0 slot — only one entry is ever kept, see the class doc below.
            versionCode = 1_003_000,
            versionName = "1.3.0",
            titleRes = R.string.whats_new_title,
            descriptionRes = R.string.whats_new_description,
        )
}
