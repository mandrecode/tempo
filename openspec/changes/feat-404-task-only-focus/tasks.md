## 1. Implementation

- [x] 1.1 Remove routine agenda variants/dependencies and make daily recording task-only.
- [x] 1.2 Remove Focus routine interactions/editors and routine-triggered Focus recording.
- [x] 1.3 Update localized empty-state/What's New copy, documentation, and Spaces compatibility notes.
- [x] 1.4 Update tests and fixtures for task-only behavior, history preservation, and task day freshness.

## 2. Verification and delivery

- [x] 2.1 Validate OpenSpec and run assembleDebug, testDebugUnitTest, ktlintFormat, ktlintCheck, detekt, lintDebug, coverage, and screenshot validation; review intended reference changes if any.
- [x] 2.2 Prune resolved detekt entries and lower CI baseline ceiling if applicable (no resolved suppressions; baseline remains 155).
- [ ] 2.3 Run the FocusContent and PlanTasksSheet instrumented checks on the user-created Pixel 10 AVD.
- [ ] 2.4 Sync/archive completed change and run final verification of delivered state.
- [x] 2.5 Commit and open a draft PR against main closing #404 (device verification remains pending).
