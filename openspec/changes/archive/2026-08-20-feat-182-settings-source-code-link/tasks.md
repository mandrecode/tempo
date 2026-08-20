## 1. Configuration and Strings

- [x] 1.1 Add a `SOURCE_CODE_URL` `buildConfigField` for `https://github.com/mandrecode/tempo` next to `FEEDBACK_FORM_URL` in `app/build.gradle.kts`.
- [x] 1.2 Add `source_code` and `source_code_description` strings to `res/values/strings.xml` and their Spanish counterparts in `res/values-es/strings.xml`.

## 2. Implementation

- [x] 2.1 Extract a shared `openExternalLink` helper in `SettingsExternalActions.kt` and route `openFeedback` through it without changing its behavior.
- [x] 2.2 Add `openSourceCode(context)` built on that helper, using `BuildConfig.SOURCE_CODE_URL` and the existing `no_browser_app` toast fallback.
- [x] 2.3 Add the source code `SettingsItem` to the About section in `SettingsContent.kt`, after the feedback entry, using `ic_code` and `ic_chevron_right`.
- [x] 2.4 Replace `WhatsNewRegistry.latest` with a `settings-source-code-link` entry and rewrite `whats_new_title`/`whats_new_description` in both locales.

## 3. Verification

- [x] 3.1 Add an instrumented `SettingsContentTest` case asserting the source code entry is displayed in the About section.
- [x] 3.2 Run `openspec validate feat-182-settings-source-code-link`.
- [x] 3.3 Run `./gradlew ktlintFormat`, `./gradlew ktlintCheck`, and `./gradlew :app:detekt`.
- [x] 3.4 Run `./gradlew testDebugUnitTest` and `./gradlew lintDebug` (localization gates on the new strings).
