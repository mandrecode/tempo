## Context

The app has no in-app mechanism to announce new features after an update. It has an adjacent but distinct first-run **onboarding** flow (`features/onboarding`, gated by `OnboardingPreferencesRepository` on `SharedPreferences`, redirecting `rememberStartDestination` in `MainActivity.kt`) and a Play-Store-only "what's new" text pipeline (`scripts/generate-whatsnew.sh` → `distribution/whatsnew/*`) that the app never reads at runtime. Neither can be reused directly: onboarding only fires *before* first-run completion, and the Play Store pipeline has no in-app representation of "features" at all.

Version identity is already available via `AppVersionProvider` (`com.mandrecode.tempo.util`, wrapping `BuildConfig.VERSION_NAME`/`VERSION_CODE`), and `version.txt` guarantees `VERSION_CODE` is monotonically increasing across releases.

## Goals / Non-Goals

**Goals:**
- Show a `TempoModalBottomSheet` with the single latest feature entry, once per version, after onboarding is complete.
- Give feature authors a small, low-friction place to register a new entry as part of their feature PR.
- Reuse existing patterns exactly (`SharedPreferences`-backed repository, `TempoModalBottomSheet`, `AppVersionProvider`) rather than introducing new infrastructure.

**Non-Goals:**
- No "view all past features" UI or history screen.
- No change to `scripts/generate-whatsnew.sh` / Play Store listing generation.
- No semantic-version string parsing — comparisons use the existing integer `VERSION_CODE`.

## Decisions

**1. Feature scope: thin, Settings-style — no dedicated domain/use-case layer.**
The only orchestration is "read one preference, compare an int, read a static list" — a single repository, no multi-repository workflow. Per the D3 precedent already documented in `AGENTS.md` for Settings, this stays presentation + data, with a plain Kotlin model class shared between them. If a future need arises (e.g. server-driven entries, analytics on views), promote to a full domain/use-case layer then.

**2. Registry holds a single "current feature" constant, not an append-only list.**
`WhatsNewRegistry.latest: WhatsNewEntry` (presentation layer, `features/whatsnew/presentation/WhatsNewRegistry.kt`) holds exactly one `WhatsNewEntry(versionCode: Int, versionName: String, @StringRes titleRes: Int, @StringRes descriptionRes: Int)`. Feature authors replace this value (and delete the previous entry's now-orphaned strings) as part of their PR, per the `AGENTS.md` New Feature Checklist.
- *Alternative considered, and initially built*: an append-only `List<WhatsNewEntry>` sorted newest-first, keeping every past feature's entry even though only `entries.first()` is ever shown. Reverted after review — since nothing in the product reads past entries (explicitly a non-goal: no history screen), the list bought no real safety over a single constant, while guaranteeing the registry file and `strings.xml` grow by one entry/string-pair forever for a UI that only ever shows the newest. A single constant is simpler to reason about and each feature PR being required to actively replace it is no less safe than being required to prepend to a list — both fail the same way (silently skip the announcement) if a contributor forgets the step.

**3. Gate on integer `VERSION_CODE`, not `VERSION_NAME` string.**
`WhatsNewPreferencesRepository` persists `lastSeenVersionCode: Int` (mirrors `OnboardingPreferencesRepositoryImpl`'s `SharedPreferences` pattern exactly — file `whats_new_preferences`, key `last_seen_version_code`, synchronous `commit()` write, `MutableStateFlow` cache). The sheet shows when `registry.first().versionCode > lastSeenVersionCode`. After the user dismisses it, `lastSeenVersionCode` is set to `registry.first().versionCode` (not `BuildConfig.VERSION_CODE`), so a version bump with no new registry entry never re-triggers the sheet.

**4. Trigger point: post-onboarding check in `MainViewModel`/`MainActivity`, not `rememberStartDestination`.**
`rememberStartDestination` only decides the initial route before onboarding is completed and isn't recomposed afterward. Instead, `MainViewModel` exposes `whatsNewEntry: WhatsNewEntry?` (non-null when `state.isOnboardingCompleted && registry.latest.versionCode > lastSeenVersionCode`), and `MainActivity`'s composable shows `WhatsNewBottomSheet` via `if (whatsNewEntry != null && !isOnboardingSectionActive)` over the current screen — independent of navigation/back stack, similar to how other transient overlays are shown.

`isOnboardingSectionActive` is fed by a new `onOnboardingActiveChange` callback threaded through `TempoNavHost`. It's keyed off `navigator.currentRoute is OnboardingRoute`, not `navigator.section == ONBOARDING` — a Settings-triggered replay (`navigator.navigate(OnboardingRoute(isReplay = true))`) pushes onto whichever back stack is already active without changing `section`, so a section-based check would silently never suppress the sheet during a replay. Caught in code review; verified on-device afterward.

**5. Reuse `TempoModalBottomSheet`, not Material3 `ModalBottomSheet`.**
Every existing bottom sheet in the app (`SortBottomSheet`, `TaskBottomSheetContent`, `HabitBottomSheetContent`) goes through this wrapper for drag/predictive-back/adaptive-docked-pane consistency. A raw Material3 sheet would look and behave inconsistently on large screens.

## Risks / Trade-offs

- **[Risk]** A feature author forgets to replace `WhatsNewRegistry.latest` when shipping a feature → the "what's new" moment silently doesn't fire for that release (it just re-announces the previous feature, or nothing if already seen). **Mitigation**: add a line to `AGENTS.md`'s New Feature Checklist calling this out explicitly (documentation-only fix, no code can enforce this).
- **[Risk]** Showing the sheet at the same time as an onboarding *replay* (`OnboardingRoute(isReplay = true)`, triggered from Settings) would be confusing. **Mitigation**: `isOnboardingSectionActive` gate, keyed off `navigator.currentRoute is OnboardingRoute` — see decision 4.
- **[Trade-off]** Using `VERSION_CODE` instead of `VERSION_NAME` means the legend text (`New features in vX.Y.Z: ...`) is sourced from the registry entry's own `versionName` field (set by hand when the entry is authored), not derived from the running build's version — this keeps the legend meaningful even if the registry entry lags a patch release that has no new feature.

## Migration Plan

- No data migration: `lastSeenVersionCode` defaults to `0` for all existing installs, so the registry's newest entry will show once on the first launch after this change ships — this is the intended "announce what you missed" behavior, not a bug.
- No feature flag / rollback complexity: the sheet is purely additive UI; reverting the change removes the trigger and repository with no data cleanup required (the orphaned `SharedPreferences` file is harmless).

## Open Questions

- Resolved: the registry is seeded with a single entry — this "what's new" onboarding feature itself (#210, `versionCode 1_002_000`, `versionName "1.2.0"`), so the mechanism is visibly exercised as soon as it ships. An earlier iteration also carried an Import/Export (#26) entry to demonstrate the (then append-only) registry with two entries, but that entry was never shown to any user (only the head of the list ever rendered) and was removed once the registry moved to a single-entry model — it added no value, only a permanently-dead second entry and its strings.
- Resolved: `versionName`/`versionCode` are set by hand at authoring time rather than derived from `AppVersionProvider`/`BuildConfig`, because the entry is written before the release that ships it exists. `main` cut `v1.1.0` (Import/Export only) while this PR was in review, which is why this feature's entry targets `v1.2.0` rather than the `v1.1.0` originally assumed — a reminder that this field needs a final check against `version.txt`/the release cadence right before merge, not just at authoring time.
