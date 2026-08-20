## Context

`WhatsNewEntry` (`features/whatsnew/presentation/model/WhatsNewEntry.kt`) currently carries a `versionCode: Int` / `versionName: String` pair that the author of `WhatsNewRegistry.latest` must guess at authoring time, predicting the semantic version the containing PR will ship in. `MainViewModel` gates showing the "what's new" bottom sheet by comparing `entry.versionCode > lastSeenVersionCode` (persisted in `WhatsNewPreferencesRepositoryImpl` via `SharedPreferences`), and `WhatsNewBottomSheet` displays `entry.versionName` in the sheet's legend text. This guess is fragile: it depends on knowing the current commit's conventional-commit type, the current `version.txt`, and whether any other PR will merge first and consume the "next" version — all of which routinely go stale (registry currently reads `1.3.0`; `version.txt` is `1.4.0`; a previous PR `fix(#210)` had to patch the same mistake after v1.1.0 shipped).

The actual release version is already known with certainty by the time a build runs: `AppVersionProvider` (`util/AppVersionProvider.kt`) wraps `BuildConfig.VERSION_NAME`/`VERSION_CODE`, which Gradle (`app/build.gradle.kts`) derives deterministically from `version.txt` (`major*1_000_000 + minor*1_000 + patch`). `version.txt` itself is bumped automatically by release-please based on merged conventional commits — no human ever hand-edits it. `AppVersionProvider` is already Hilt-injected and consumed by `SettingsViewModel` for the same purpose (showing the running app's version to the user).

## Goals / Non-Goals

**Goals:**
- Eliminate the need for anyone to guess or hand-maintain a future release version anywhere in the "what's new" feature.
- Keep the displayed version in the bottom sheet always correct, by sourcing it from the real running build instead of an authored guess.
- Preserve the existing user-facing behavior: show the latest feature once per onboarded user, never again after dismissal, not during onboarding/replay.
- Keep `WhatsNewRegistry` a single-entry, replace-not-append registry (no change to that authoring model).

**Non-Goals:**
- Do not attempt to retroactively track a history of previously-shown entries; only "the current one" is ever relevant, per existing design.
- Do not change `distribution/whatsnew`/`scripts/generate-whatsnew.sh` (Play Store release-notes generation) — that is a separate, already-automated pipeline unrelated to the in-app registry.
- Do not build a generalized versioning/migration scheme for preference keys; a one-time silent reset of "have I seen this" state for existing users is acceptable (see Risks).

## Decisions

### Decision: Replace `versionCode`/`versionName` on `WhatsNewEntry` with a single author-assigned `id: String`
The only reason `versionCode` existed was to answer "has the user already seen the currently-registered entry?" — that's an identity question, not an ordering question, since only one entry is ever registered at a time. A `String id` (e.g. `"encryption-at-rest"`, matching the feature slug/issue used in the branch name) is trivial for an author to pick correctly on the spot — no prediction of the future is involved, unlike a version number. Equality replaces `>` comparison in the gating logic.

Alternative considered: keep using `titleRes`/`descriptionRes` (the `@StringRes` ints) as the identity. Rejected — per the existing "New Feature Checklist," the string *resource names* (`whats_new_title`, `whats_new_description`) are reused every time (only their translated values change), so the resource ID is stable across features and cannot distinguish "new entry" from "old entry already seen."

Alternative considered: keep an `Int` ordinal that authors increment by hand. Rejected — reintroduces a manual "did I bump this" step that can be forgotten or merge-conflict with a concurrent PR the same way version guessing did; a descriptive string ID is no harder to write correctly and is self-documenting in git history/diffs.

### Decision: Source the displayed version from `AppVersionProvider`, threaded through `MainViewModel` → `MainUiState.Success` → `MainActivity` → `WhatsNewBottomSheet`
This mirrors the existing pattern already used for `SettingsViewModel` (constructor-inject `AppVersionProvider`, read `getVersionInfo().versionName`). `MainViewModel` already assembles `MainUiState.Success` from combined preference flows and already owns the "should show whats-new" decision, so it is the natural place to attach the real version string alongside `whatsNewEntry`. `WhatsNewBottomSheet` becomes a "dumb" composable that takes `versionName: String` as a parameter instead of reading it off the entry, keeping it easily testable/previewable without DI.

Alternative considered: read `BuildConfig.VERSION_NAME` directly inside `WhatsNewBottomSheet` (skip DI entirely, since it's a compile-time constant). Rejected — every other version-name consumer in the codebase goes through the injected `AppVersionProvider` abstraction rather than referencing `BuildConfig` directly from UI code; staying consistent keeps the composable testable and avoids a second, inconsistent way of reading the same fact.

### Decision: Rename preference API from `lastSeenVersionCode: StateFlow<Int>` to `lastSeenEntryId: StateFlow<String?>`
`WhatsNewPreferencesRepositoryImpl` stores the id under a new `SharedPreferences` key (`last_seen_entry_id`, string) instead of the old int key. The `>=` guard in `setLastSeenVersionCode` (`if (lastSeen.value >= versionCode) return`) existed to make persistence monotonic/idempotent under concurrent calls; with string ids there is no ordering, so the guard becomes a simple `if (lastSeen.value == id) return` (idempotent no-op on repeat dismissal, still safe under concurrent calls since `SharedPreferences.edit(commit = true)` is synchronous).

### Decision: One-time reset of "seen" state for existing users; no migration of the old int key
Existing installs have a `last_seen_version_code` int in `SharedPreferences` that becomes orphaned dead data under the new string key. The practical effect: every existing user sees the "what's new" sheet one more time after updating to this change (for whatever entry is registered as `latest` at that release), then it behaves normally (never again until the entry's `id` changes). This is a cosmetic, one-time, low-severity UX blip — not worth building key-migration logic for.

## Risks / Trade-offs

- [Risk] Existing users see the current `WhatsNewRegistry.latest` entry shown again once, even if they already saw it under the old version-code scheme → Mitigation: accepted one-time UX cost (see Decision above); no action needed, does not warrant migration complexity.
- [Risk] An author could reuse the same `id` string when replacing the registry entry for a new feature, causing the new entry to be silently treated as "already seen" for users who dismissed the old one → Mitigation: document in `WhatsNewRegistry`'s doc comment that `id` must change whenever the entry's content changes; this is a lint-free authoring convention, same trust level as the existing "replace, don't append" convention already in place.
- [Risk] Threading `AppVersionProvider` through `MainViewModel` adds a constructor dependency and changes `MainUiState.Success`'s shape → Mitigation: small, mechanical addition consistent with existing patterns (`SettingsViewModel` already does this); test fakes only need one more mocked call.

## Migration Plan

No data migration. Deploy as a normal release: ship the new `WhatsNewEntry`/preference API, update `WhatsNewRegistry.latest` to carry a `id` value, update all call sites and tests. Rollback is a normal revert (no persisted-schema irreversibility introduced).
