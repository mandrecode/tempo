## 1. Implementation

- [x] 1.1 Update habit reminder scheduling so future reminder alarms are not skipped solely because `isCompleted` is true.
- [x] 1.2 Update habit notification display gating to use completion history for the reminder occurrence date.
- [x] 1.3 Preserve task reminder and habit-chain reminder behavior.

## 2. Verification

- [x] 2.1 Add or update unit tests for future completed-habit scheduling and occurrence-date notification suppression.
- [x] 2.2 Run `openspec validate fix-95-habit-notifications`.
- [x] 2.3 Run focused reminder unit tests.
- [x] 2.4 Run `./gradlew ktlintFormat` before committing.
