## 1. Retention Domain And Persistence

- [x] 1.1 Add the retention preferences contract and SharedPreferences implementation with disabled/30-day defaults, reactive values, and 1..365 clamping
- [x] 1.2 Add the cleanup scheduler contract plus configuration and expired-task deletion use cases
- [x] 1.3 Extend the task DAO and repository to delete eligible completed top-level task trees atomically at a cutoff
- [x] 1.4 Bind new persistence and scheduler implementations through Hilt

## 2. Background Cleanup

- [x] 2.1 Implement unique immediate and daily WorkManager scheduling with cancellation support
- [x] 2.2 Implement a Hilt cleanup worker that reads the latest policy, computes the local cutoff, and safely executes deletion
- [x] 2.3 Add unit tests for configuration orchestration, cutoff deletion, scheduling, worker behavior, and preferences persistence

## 3. Settings Experience

- [x] 3.1 Extend the settings MVI contract and ViewModel to observe and update the retention policy
- [x] 3.2 Add the automatic-removal switch and bounded day controls to Settings content with accessibility semantics
- [x] 3.3 Add matching English and Spanish strings plus debug-source previews
- [x] 3.4 Add ViewModel and Compose UI tests for retention settings behavior

## 4. Verification

- [x] 4.1 Run `openspec validate feat-14-autoremove-completed-tasks`
- [x] 4.2 Run `./gradlew ktlintFormat` and `./gradlew testDebugUnitTest`
- [x] 4.3 Run `./gradlew ktlintCheck`, `./gradlew :app:detekt`, and `./gradlew lintDebug`
- [x] 4.4 Build and smoke-test the settings and cleanup flow on the connected Pixel 7 when available, otherwise the Pixel 10 AVD

## 5. Retention Picker Refinement

- [x] 5.1 Restore the opt-in switch and make the minus/value/plus control step through useful presets
- [x] 5.2 Model the supported preset intervals through the settings MVI contract and configuration flow
- [x] 5.3 Update English/Spanish resources, previews, ViewModel tests, and Compose tests
- [x] 5.4 Run OpenSpec validation, formatting, focused tests, static analysis, lint, and a Pixel 10 AVD smoke test
- [x] 5.5 Refine stepper alignment and animation, and move Notifications immediately before Language
