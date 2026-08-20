## 1. Predictive-back routing

- [x] 1.1 Prevent shared sheet predictive-back handling while the software keyboard is visible
- [x] 1.2 Preserve guarded sheet dismissal after the keyboard closes across modal and docked placements

## 2. Regression coverage

- [x] 2.1 Add unit coverage for keyboard-first sheet back routing
- [x] 2.2 Verify the multiline new-task sequence on the Pixel 10 AVD against the supplied failure recording
- [x] 2.3 Verify repeated title/description focus switching does not move the sheet

## 3. Quality gates

- [x] 3.1 Run focused tests, `testDebugUnitTest`, ktlint, detekt, and the debug build
- [x] 3.2 Verify the task-sheet screenshot suite and review the full screenshot report
- [x] 3.3 Obtain a clean full screenshot validation; CI for merged PR #388 passed the full screenshot suite
- [x] 3.4 Validate the OpenSpec change and review the final scoped diff
