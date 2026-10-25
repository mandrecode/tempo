# Design

## Approach

Each of the two ViewModels keeps a private `remindersAwaitingPermission` flag. It is not UI state, because nothing renders it.

- **Set** by `requestReminderPermission()`, which every save path that ends in `ScheduleResult.PermissionError` now calls in place of setting `showPermissionRequestDialog` directly. Also set when the resume check finds missing permissions and existing reminders (the revoked dialog).
- **Consumed** by `onPermissionsGranted()`. It shows the message only when the flag is set, and clears the flag first. Both the screen's `OnPermissionsGranted` event and the resume check call it, so the first one to see the grant shows the message and the second does nothing.
- **Cleared** by "clear all reminders".

The revoked-permission branch re-checks permissions after its reminder lookup returns. The lookup is a suspended database query, so the grant can arrive and be announced while it is still running. Setting the flag after that would re-arm an announcement that has already been made, and the next resume would repeat it. The `a slow reminder lookup finishing after the grant does not announce it twice` tests cover this for both screens (Copilot raised it in review).

The lookup also runs as a tracked job (`permissionLookupJob`). It is cancelled by a newer permission check and by "clear all reminders", so a lookup that started before every reminder was cleared cannot come back with a stale list and reopen the revoked dialog or re-arm the announcement. The `a reminder lookup outlasting clear all…` tests cover this for both screens (Copilot).

The previous `wasGrantedBefore` comparison is removed. It was the source of the bug, because it reacted to any transition, whoever caused it.

## Alternatives considered

- **A single app-wide announcer.** This would de-duplicate across tabs, but it still could not tell which screen asked. It is also more machinery than a per-screen flag.
- **Dropping the resume announcement entirely.** This loses the confirmation after the revoked dialog sends the user to system settings, which is the case the "again" copy was written for.
