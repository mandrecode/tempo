## 1. Domain rollover update

- [x] 1.1 Update overdue periodic rollover to persist the original task as incomplete but non-periodic once a next instance is committed.
- [x] 1.2 Keep existing stale-link handling and idempotent next-instance reuse behavior intact.

## 2. Tests

- [x] 2.1 Update `RollOverduePeriodicTaskUseCaseTest` to assert recurrence metadata is stripped from the original on create/reuse paths.
- [x] 2.2 Add/adjust regression assertions so failed/non-applicable rollovers do not strip recurrence metadata.

## 3. Verification

- [x] 3.1 Run `openspec validate fix-760-autoschedule-drop-periodicity --strict`.
- [x] 3.2 Run `./gradlew ktlintFormat`.
- [x] 3.3 Run `./gradlew :app:detekt`.
- [x] 3.4 Run `./gradlew testDebugUnitTest`.
