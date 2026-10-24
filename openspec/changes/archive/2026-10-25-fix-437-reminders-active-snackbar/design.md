# Design

## Approach

Each of the two ViewModels keeps a private `remindersAwaitingPermission` flag. It is not UI state, because nothing renders it.

- **Set** by `requestReminderPermission()`, which every save path that ends in `ScheduleResult.PermissionError` now calls in place of setting `showPermissionRequestDialog` directly. Also set when the resume check finds missing permissions and existing reminders (the revoked dialog).
- **Consumed** by `onPermissionsGranted()`. It shows the message only when the flag is set, and clears the flag first. Both the screen's `OnPermissionsGranted` event and the resume check call it, so the first one to see the grant shows the message and the second does nothing.
- **Cleared** by "clear all reminders".

The previous `wasGrantedBefore` comparison is removed. It was the source of the bug, because it reacted to any transition, whoever caused it.

## Alternatives considered

- **A single app-wide announcer.** This would de-duplicate across tabs, but it still could not tell which screen asked. It is also more machinery than a per-screen flag.
- **Dropping the resume announcement entirely.** This loses the confirmation after the revoked dialog sends the user to system settings, which is the case the "again" copy was written for.
