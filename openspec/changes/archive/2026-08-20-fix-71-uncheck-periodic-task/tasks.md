## 1. Domain Behavior

- [x] 1.1 Replace periodic-completion rollback with transactional detachment that restores the archived occurrence at its original reminder date without recurrence or a next-occurrence link.
- [x] 1.2 Preserve the linked open next occurrence and restore only subtasks auto-completed with the archived parent.

## 2. Presentation Behavior

- [x] 2.1 Route the corrected uncheck through normal parent-toggle handling so the task expands without a next-occurrence cancellation snackbar.

## 3. Regression Coverage

- [x] 3.1 Update domain unit tests to verify the archived occurrence is restored overdue and non-periodic while the next occurrence and scheduler remain untouched.
- [x] 3.2 Add ViewModel coverage confirming corrected uncheck handling emits no cancellation snackbar.

## 4. Verification

- [x] 4.1 Run focused task use-case and ViewModel unit tests.
- [x] 4.2 Run `./gradlew ktlintFormat`, `./gradlew testDebugUnitTest`, `./gradlew ktlintCheck`, and `./gradlew :app:detekt`.
- [x] 4.3 Run `openspec validate fix-71-uncheck-periodic-task` and confirm all tasks and artifacts are complete.
