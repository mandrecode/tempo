# Tasks

## 1. Domain

- [x] 1.1 Add `StartFocusSessionOnTaskUseCase` in `features/focus/domain/usecase/` covering start, same-task carry-on, and missing/completed tasks; verify with `StartFocusSessionOnTaskUseCaseTest` via `./gradlew testDebugUnitTest`

## 2. Notification

- [x] 2.1 Add `StartFocusSessionReceiver` (registered in the manifest) that runs the use case and dismisses the task reminder notification; verify with `./gradlew assembleDebug`
- [x] 2.2 Add the Start session action to `TaskReminderNotifier` with en/es strings; verify with `./gradlew lintDebug`
- [x] 2.3 Replace the What's New entry with one for #379 (en/es strings); verify with `./gradlew testDebugUnitTest`

## 3. Verification

- [x] 3.1 Run `openspec validate feat-379-start-session-from-task-notification`, `./gradlew ktlintFormat ktlintCheck :app:detekt testDebugUnitTest koverVerifyDebug`
- [x] 3.2 Smoke test on device/emulator: a fired task reminder shows Start session, pressing it starts the session notification and dismisses the reminder

## Workflow follow-up

- Archive the change with `openspec-archive-change` in the same PR before the final CI run.
