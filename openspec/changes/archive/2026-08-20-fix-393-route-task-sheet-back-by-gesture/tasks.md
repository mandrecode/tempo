## 1. Baseline and routing policy

- [x] 1.1 Restore the shared modal and docked sheets to standard IME padding and one continuously active predictive-back handler.
- [x] 1.2 Add a pure gesture-route policy that separates keyboard progress/completion/cancellation effects from sheet effects.

## 2. Compose integration

- [x] 2.1 Capture the latest keyboard visibility once when a back callback begins and retain that route through completion or cancellation.
- [x] 2.2 Apply keyboard-route completion focus cleanup without mutating focus during normal title/description handoffs.

## 3. Regression coverage

- [x] 3.1 Add unit coverage for visible/hidden keyboard routes, progress forwarding, completion, and cancellation effects.
- [x] 3.2 Run focused tests, `./gradlew testDebugUnitTest`, `./gradlew ktlintFormat`, `./gradlew ktlintCheck`, `./gradlew :app:detekt`, and `./gradlew assembleDebug`.
- [x] 3.3 Validate repeated title/description handoffs and keyboard-first then sheet-first back behavior on the connected Pixel 7 debug application.

## 4. OpenSpec and delivery

- [x] 4.1 Run `openspec validate fix-393-route-task-sheet-back-by-gesture` and synchronize the completed task checklist.
- [x] 4.2 Prepare the completed change, synchronized specification, and verification evidence for archive and draft PR #394.
