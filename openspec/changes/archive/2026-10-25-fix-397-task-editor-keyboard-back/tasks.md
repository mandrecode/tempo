# Tasks

## 1. Implementation

- [x] 1.1 Add `TaskEditorFocusState` and a task-editor predictive-back handler in the task sheet
  components package; verify with unit tests for focus derivation across handoffs
- [x] 1.2 Report title and description `onFocusChanged` into the focus state and compose the handler
  inside the task sheet content; verify `./gradlew assembleDebug`
- [x] 1.3 Add instrumented coverage for title ↔ description focus transfer and back clearing focus
  without dismissing the sheet; verify it compiles with `./gradlew compileDebugAndroidTestKotlin`

## 2. Verification

- [x] 2.1 Run `openspec validate fix-397-task-editor-keyboard-back`
- [x] 2.2 Run `./gradlew ktlintFormat ktlintCheck :app:detekt testDebugUnitTest`
- [x] 2.3 Run `./gradlew validateDebugScreenshotTest` and confirm no visual differences (the 4
  `FocusContentMidDay` failures are pre-existing and reproduce on unchanged `main`)
- [x] 2.4 Manually validate the full interaction matrix from #397 on the physical Pixel 7 with Gboard
  and gesture navigation, debug app installed alongside PROD

## Workflow follow-up

- Archive `fix-393-restore-task-sheet-keyboard-baseline` with `--skip-specs` (superseded).
- Archive this change in the PR once device validation passes.
