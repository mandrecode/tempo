## 1. Receiver Behavior

- [x] 1.1 Audit habit and task reminder receivers for delivery-time repository reads and stale completed-state suppression.
- [x] 1.2 Patch any missing suppression so completed or missing delayed task alarms cannot display notifications or trigger rollover.
- [x] 1.3 Patch any missing scheduled-date handling so delayed habit alarms check the fired occurrence date against completion history.

## 2. Test Coverage

- [x] 2.1 Add or extend habit receiver tests for delayed completed occurrence suppression after the reminder advances.
- [x] 2.2 Add task receiver or domain tests proving completed/missing delayed task alarms do not notify.
- [x] 2.3 Add rollover coverage proving completed overdue periodic tasks do not create or schedule a next occurrence from delayed delivery.

## 3. Validation

- [x] 3.1 Run `openspec validate fix-72-delayed-completed-notifications`.
- [x] 3.2 Run focused unit tests for reminder receivers and periodic rollover.
- [x] 3.3 Run `./gradlew ktlintFormat` and relevant final verification commands.
