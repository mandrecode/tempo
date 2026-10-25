# Tasks

## 1. ViewModels

- [x] 1.1 Add `remindersAwaitingPermission` and `requestReminderPermission()` to Tasks and Routines; route every permission-error save path through it, and set the flag when the revoked dialog is shown.
- [x] 1.2 Make `onPermissionsGranted()` announce only when the flag is set and clear it; call it from the resume check in place of the `wasGrantedBefore` comparison; clear the flag when all reminders are cleared.

## 2. Tests and verification

- [x] 2.1 `RoutinesViewModelTest`: silent when never asked, once after the revoked dialog, silent after clearing reminders. `TasksViewModelTest`: silent when never asked, once after a save's permission request.
- [x] 2.2 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest lintDebug` and `openspec validate --strict`.
