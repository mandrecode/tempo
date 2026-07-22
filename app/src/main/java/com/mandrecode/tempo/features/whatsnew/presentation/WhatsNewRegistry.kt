package com.mandrecode.tempo.features.whatsnew.presentation

import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.whatsnew.presentation.model.WhatsNewEntry

/**
 * Holds only the latest shipped feature — [latest] is the only entry ever shown to users, so
 * older entries carry no runtime value. Replace this value (not append to it) — including
 * `whats_new_title`/`whats_new_description` in strings.xml and choosing a new [WhatsNewEntry.id]
 * — when shipping a new feature (see openspec/changes/feat-210-whats-new-onboarding/design.md).
 * The `id` only needs to be a stable, unique-per-feature slug (e.g. the feature's issue number);
 * it is never a release version, so there is nothing to guess or recheck against version.txt —
 * the version shown to users is always read from the running build at display time.
 */
object WhatsNewRegistry {
    val latest: WhatsNewEntry =
        WhatsNewEntry(
            id = "encryption-at-rest",
            titleRes = R.string.whats_new_title,
            descriptionRes = R.string.whats_new_description,
        )
}
