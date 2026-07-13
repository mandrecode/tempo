## 1. Keyboard Behavior

- [x] 1.1 Invoke Compose's default Done action and clear focus in task, habit, habit-chain, and category editor fields
- [x] 1.2 Remove the orphaned quick-task entry component, its tests, and its detekt suppression

## 2. Regression Coverage

- [x] 2.1 Add Compose UI coverage for Done-action focus clearing in task, habit, and category editors

## 3. Verification

- [x] 3.1 Validate the OpenSpec change and run `./gradlew ktlintFormat`, relevant Compose instrumented tests, and `./gradlew testDebugUnitTest`
- [x] 3.2 Run `./gradlew ktlintCheck`, `./gradlew :app:detekt`, and a Pixel keyboard-dismissal smoke test when a device is available
