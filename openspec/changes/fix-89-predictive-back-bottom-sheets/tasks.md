## 1. Implementation

- [x] 1.1 Replace completed-only back handling in `TempoModalBottomSheet` with predictive-back progress handling.
- [x] 1.2 Restore the sheet to the open position when predictive back is cancelled.
- [x] 1.3 Route predictive-back completion through the existing dismiss request path so unsaved changes still show discard confirmation.

## 2. Verification

- [x] 2.1 Run `openspec validate fix-89-predictive-back-bottom-sheets`.
- [x] 2.2 Run focused Kotlin compile or tests for the app UI change.
- [x] 2.3 Run `./gradlew ktlintFormat` and confirm formatting/static checks relevant to the touched files.
