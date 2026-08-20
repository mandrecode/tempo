## 1. Shared IME transition policy

- [x] 1.1 Add deterministic policy helpers for real IME dismissal, focus handoff, and predictive-back enablement
- [x] 1.2 Settle a real IME hide by clearing sheet focus before re-registering predictive back

## 2. Regression coverage

- [x] 2.1 Extend shared sheet IME unit tests for visible-to-hidden dismissal and non-zero-target focus handoff
- [x] 2.2 Verify existing task-sheet adaptive screenshot cases remain unchanged

## 3. Device verification

- [x] 3.1 Reproduce the supplied multiline-description sequence and verify Gboard stays hidden after one dismissal on the Pixel 7 or prescribed AVD fallback
- [x] 3.2 Verify title-to-description focus handoff remains uninterrupted and the next back action retains guarded sheet dismissal

## 4. Quality gates

- [x] 4.1 Run focused tests and `./gradlew testDebugUnitTest`
- [x] 4.2 Run `./gradlew ktlintFormat`, `./gradlew ktlintCheck`, `./gradlew :app:detekt`, and `./gradlew assembleDebug`
- [x] 4.3 Run `./gradlew validateDebugScreenshotTest`, review the report, and validate the OpenSpec change
