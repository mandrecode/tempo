## 1. Notification Date Propagation

- [x] 1.1 Add scheduled-date metadata to the habit-chain live notification content intent.
- [x] 1.2 Extend `PendingNotificationAction.OpenHabitChain` and pending-action saved state to carry an optional scheduled date.

## 2. Routines Handling

- [x] 2.1 Update routines notification handling to select the scheduled date before opening the habit-chain sheet.
- [x] 2.2 Preserve existing behavior when a habit-chain notification action has no scheduled date.

## 3. Verification

- [x] 3.1 Add or update focused unit tests for pending action date parsing/persistence and routines notification opening.
- [x] 3.2 Run `openspec validate fix-91-live-notification-date`.
- [x] 3.3 Run relevant Gradle checks for the touched code.
