## 1. Restore Baseline Behavior

- [x] 1.1 Restore `TempoModalSheet.kt` exactly to its pre-#229 implementation at `f311e02`
- [x] 1.2 Remove the gesture-scoped keyboard back-route helper and its unit test
- [x] 1.3 Verify the scoped app files are identical to the pre-#229 baseline

## 2. Verify Rollback

- [x] 2.1 Run `openspec validate fix-393-restore-task-sheet-keyboard-baseline`
- [x] 2.2 Run `./gradlew ktlintFormat`, unit tests, formatting checks, detekt, and debug assembly
- [x] 2.3 Run screenshot validation and review any reported visual differences
- [ ] 2.4 Install and smoke-test only the debug application on the connected Pixel 7 (device not
  currently visible to Android tooling)

## 3. Deliver Replacement

- [x] 3.1 Commit and push the rollback branch
- [x] 3.2 Open a rollback PR that references #388 and #394 without closing #393

## 4. Finalize Change

- [ ] 4.1 After the Pixel 7 smoke test passes, archive this OpenSpec change in the rollback PR and
  rerun final verification against the archived state
