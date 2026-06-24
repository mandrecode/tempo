## 1. Dismiss keyboards from title fields

- [x] 1.1 Add a Done callback to task and routine editor focus config that hides the keyboard and clears focus.
- [x] 1.2 Hide the keyboard from task title `onDone` and newline fallback handling.
- [x] 1.3 Hide the keyboard from routine title `onDone` and newline fallback handling.
- [x] 1.4 Match Didi by making task and routine editor title fields single-line inputs.
- [x] 1.5 Add focused UI regressions for task and routine title IME Done handling.
- [x] 1.6 Match Didi's title input implementation by using `BasicTextField` for task and routine editor title fields.

## 2. Verification

- [x] 2.1 Run `./gradlew ktlintFormat`.
- [x] 2.2 Run `./gradlew :app:assembleDebug`.
- [x] 2.3 Run `openspec validate fix-37-title-enter-closes-keyboard --strict`.
- [x] 2.4 Run `./gradlew ktlintCheck`.
- [x] 2.5 Run `./gradlew :app:detekt`.
- [x] 2.6 Run `./gradlew testDebugUnitTest`.
- [x] 2.7 Run targeted `./gradlew :app:connectedDebugAndroidTest` title-field IME regressions on Pixel 7.
