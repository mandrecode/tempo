## Why

GitHub issue [#182](https://github.com/mandrecode/tempo/issues/182) asks for a way to reach Tempo's git repository from inside the app. Tempo is developed in the open, but nothing in the app points at the source: the About section only offers onboarding, Play Store review, and the feedback form, so users who want to read the code, file an issue, or contribute have no in-app path to it.

## What Changes

- Add a "Source code" entry to the Settings → About section that opens the Tempo GitHub repository in the user's browser.
- Show the same missing-browser toast already used by the feedback entry when no app can handle the link.
- Publish the repository URL as a build config value so it lives beside the existing feedback-form URL instead of being hardcoded in UI code.
- Announce the entry through the "What's New" sheet by replacing `WhatsNewRegistry.latest`.
- Non-goal: no in-app browser or WebView, no contributor/licence/attribution screen, no changes to the existing About entries.

## Capabilities

### New Capabilities
- `settings-source-code-link`: Covers the Settings About entry that opens Tempo's public git repository and how it behaves when no browser is available.

### Modified Capabilities
- None.

## Impact

- Affected code: `features/settings/presentation/SettingsContent.kt` (About section), `features/settings/presentation/SettingsExternalActions.kt` (external intent), `features/whatsnew/presentation/WhatsNewRegistry.kt`, `app/build.gradle.kts` (build config field), `res/values/strings.xml` and `res/values-es/strings.xml`.
- APIs: no external API changes; no domain, data, or persistence changes (Settings stays within the thin-Settings exception in AGENTS.md).
- Dependencies: none added.
- Verification: instrumented Compose coverage for the new About entry, plus `./gradlew ktlintCheck`, `:app:detekt`, `testDebugUnitTest`, and `lintDebug` for the localized strings.
