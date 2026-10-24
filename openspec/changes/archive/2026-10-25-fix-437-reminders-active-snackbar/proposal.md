# Proposal

## Why

Originating issue: [#437](https://github.com/mandrecode/tempo/issues/437).

"All set! Reminders are active again." pops up in situations that have nothing to do with it, most visibly on Routines. Tasks and Routines each check permissions every time they resume, and each one announced the message whenever permissions went from missing to granted. That includes permission granted from inside the habit or task sheet (which already opens the time picker), from Focus, or after the user denied once while having no reminders at all. Both tabs could even announce the same grant.

## What Changes

- Tasks and Routines show "reminders are active again" only to answer their own request for permission: either the request after saving a reminder without permission, or the revoked-permission dialog shown because existing reminders stopped working.
- When permission comes back after such a request, the message is shown once. The request flow and the resume check both see the grant, but only one of them shows the message.
- Clearing every reminder from the revoked dialog drops the pending message, because nothing is left to become active again.
- In every other case, the resume check records the new permission state silently.

### Non-goals

- No change to the permission education, rationale, or revoked dialogs, or to the in-sheet permission flow.
- No change to the message copy.

## Capabilities

### New Capabilities
- `reminder-permission-recovery-message`: when the app confirms that reminders work again.

### Modified Capabilities

None.

## Impact

- Presentation: `TasksViewModel`, `TasksViewModelCategoryAndPermissionActions`, `TasksViewModelTaskActions`, `RoutinesViewModel`, `RoutinesViewModelActions`.
- Tests: `TasksViewModelTest`, `RoutinesViewModelTest`.
- No domain, data, or UI layout changes.
