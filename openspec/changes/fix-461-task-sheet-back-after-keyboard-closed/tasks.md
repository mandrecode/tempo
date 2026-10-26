# Tasks

## 1. Implementation

- [x] 1.1 Track keyboard visibility in `TaskEditorFocusState` and require it for `ownsBack`; latch a
  started editor gesture; apply the same rule to the legacy back-key intercept
- [x] 1.2 Unit tests for ownership with the keyboard closed and reopened
- [x] 1.3 Instrumented tests: first back reaches the sheet with the keyboard closed, mid-gesture
  keyboard close keeps the gesture, legacy intercept passes the key through

## 2. Verification

- [x] 2.1 `openspec validate fix-461-task-sheet-back-after-keyboard-closed`
- [x] 2.2 `./gradlew ktlintCheck :app:detekt testDebugUnitTest`
- [x] 2.3 `TaskEditorBackHandlerTest` on the Pixel 10 AVD
- [ ] 2.4 Manually validate on the physical Pixel 7 with Gboard: hide keyboard, one back dismisses;
  title ↔ description handoffs keep the keyboard open
