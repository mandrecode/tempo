## Why

Users who update the app have no way to discover what just changed — new capabilities ship silently and go unnoticed. We want a lightweight, non-intrusive "what's new" moment after an update that teaches the single most recent feature, without requiring users to read a changelog. (Closes #210)

## What Changes

- Add a `WhatsNew` bottom sheet, shown at most once per app version, that displays only the latest feature using the legend format `New features in vX.Y.Z: <feature title>`.
- Add an in-code, per-feature entry registry: each shipped feature contributes a small structured entry (version, title, description, optional icon). Only the entry for the current app version is shown; older entries stay in the registry as history but are never surfaced automatically.
- Persist the last-seen "what's new" version (mirroring the existing `OnboardingPreferencesRepository` `SharedPreferences` pattern) so the sheet is gated to "last shown version < current app version" and never reappears for the same version.
- Surface the sheet from the main app shell (post-onboarding) shortly after launch when the gate condition is met — distinct from the existing first-run `OnboardingRoute` redirect, which only fires before onboarding completes.
- Add `whats_new_*` string resources (en + es) for the sheet chrome and the initial entry (e.g. the already-shipped Import/Export feature, #26).

### Non-Goals

- No historical "changelog" screen or list of all past features — only the latest is ever shown in-app.
- No change to the existing Play Store listing generation (`scripts/generate-whatsnew.sh`, `distribution/whatsnew/*`) — that pipeline is unrelated and untouched.
- No change to the first-run onboarding flow (`features/onboarding`) itself.

## Capabilities

### New Capabilities
- `whats-new-onboarding`: Post-update "what's new" bottom sheet that shows the single latest feature entry once per app version, backed by an in-code feature registry and a persisted last-shown-version gate.

### Modified Capabilities
(none — no existing capability's requirements change)

## Impact

- **New code**: `features/whatsnew/` (or similar) domain model/registry, a `WhatsNewPreferencesRepository` (+ impl) following the `SharedPreferences` pattern in `core/data/preferences/`, ViewModel/UiState, and a `TempoModalBottomSheet`-based Content composable.
- **Modified code**: `core/di/RepositoryModule.kt` (bind new repository), `MainActivity.kt` / `core/ui/MainViewModel.kt` (trigger check post-onboarding), `app/src/main/res/values/strings.xml` + `values-es/strings.xml` (new strings).
- **Process**: Every future feature PR must add one small registry entry describing the feature — this becomes a standing convention documented in `AGENTS.md`'s New Feature Checklist.
- **No dependency or schema changes.**
