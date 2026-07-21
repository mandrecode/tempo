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

**2. Registry is a static, append-only Kotlin list, not a single "current feature" constant.**
`WhatsNewRegistry` (presentation layer, e.g. `features/whatsnew/presentation/WhatsNewRegistry.kt`) holds `List<WhatsNewEntry>` sorted newest-first, where `WhatsNewEntry(versionCode: Int, versionName: String, @StringRes titleRes: Int, @StringRes descriptionRes: Int)`. Feature authors add one entry per feature, described in the `AGENTS.md` New Feature Checklist. Only `registry.first()` (highest `versionCode`) is ever shown to a user.
- *Alternative considered*: replace a single "latest feature" constant on every release. Rejected — a single mutable slot is one accidental omission away from silently showing nothing or the wrong entry on the next release; an append-only list is safer and self-documents feature history in code even though the UI never surfaces more than the head.

**3. Gate on integer `VERSION_CODE`, not `VERSION_NAME` string.**
`WhatsNewPreferencesRepository` persists `lastSeenVersionCode: Int` (mirrors `OnboardingPreferencesRepositoryImpl`'s `SharedPreferences` pattern exactly — file `whats_new_preferences`, key `last_seen_version_code`, synchronous `commit()` write, `MutableStateFlow` cache). The sheet shows when `registry.first().versionCode > lastSeenVersionCode`. After the user dismisses it, `lastSeenVersionCode` is set to `registry.first().versionCode` (not `BuildConfig.VERSION_CODE`), so a version bump with no new registry entry never re-triggers the sheet.

**4. Trigger point: post-onboarding check in `MainViewModel`/`MainActivity`, not `rememberStartDestination`.**
`rememberStartDestination` only decides the initial route before onboarding is completed and isn't recomposed afterward. Instead, `MainViewModel` exposes `shouldShowWhatsNew: Boolean` (derived from `state.isOnboardingCompleted && !isReplay && registry.first().versionCode > lastSeenVersionCode`), and `MainActivity`'s composable shows `WhatsNewBottomSheet` via a simple `if (shouldShowWhatsNew)` over the current screen — independent of navigation/back stack, similar to how other transient overlays are shown.

**5. Reuse `TempoModalBottomSheet`, not Material3 `ModalBottomSheet`.**
Every existing bottom sheet in the app (`SortBottomSheet`, `TaskBottomSheetContent`, `HabitBottomSheetContent`) goes through this wrapper for drag/predictive-back/adaptive-docked-pane consistency. A raw Material3 sheet would look and behave inconsistently on large screens.

## Risks / Trade-offs

- **[Risk]** A feature author forgets to add a registry entry when shipping a feature → the "what's new" moment silently doesn't fire for that release. **Mitigation**: add a line to `AGENTS.md`'s New Feature Checklist calling this out explicitly (documentation-only fix, no code can enforce this).
- **[Risk]** `strings.xml`/`values-es/strings.xml` grow by 2 strings per feature indefinitely. **Mitigation**: acceptable — same growth pattern already exists for onboarding strings; no pruning needed since old entries are inert once superseded.
- **[Risk]** Showing the sheet at the same time as an onboarding *replay* (`OnboardingRoute(isReplay = true)`, triggered from Settings) would be confusing. **Mitigation**: gate explicitly excludes replay (`!isReplay`) — the check only applies to the real post-onboarding app shell state.
- **[Trade-off]** Using `VERSION_CODE` instead of `VERSION_NAME` means the legend text (`New features in vX.Y.Z: ...`) is sourced from the registry entry's own `versionName` field (set by hand when the entry is authored), not derived from the running build's version — this keeps the legend meaningful even if the registry entry lags a patch release that has no new feature.

## Migration Plan

- No data migration: `lastSeenVersionCode` defaults to `0` for all existing installs, so the registry's newest entry will show once on the first launch after this change ships — this is the intended "announce what you missed" behavior, not a bug.
- No feature flag / rollback complexity: the sheet is purely additive UI; reverting the change removes the trigger and repository with no data cleanup required (the orphaned `SharedPreferences` file is harmless).

## Open Questions

- Resolved: the registry ships seeded with two entries so the mechanism is visibly exercised as soon as it ships — (1) this "what's new" onboarding feature itself (#210), listed first/newest so it's the entry users actually see, and (2) the Import/Export feature (#26). Initially both were assumed to land in the same upcoming release and share a `versionCode`, but `main` cut `v1.1.0` (Import/Export only) while this PR was in review — so the entries now carry their true, distinct versions: Import/Export at `1.1.0` (`1_001_000`, already shipped) and this feature at `1.2.0` (`1_002_000`, the next release). This is exactly the scenario the append-only-list design (over a single mutable "current feature" slot) was meant to handle: as release cadence shifts underneath a long-lived branch, each entry keeps its own accurate version instead of all sharing one guessed value.
