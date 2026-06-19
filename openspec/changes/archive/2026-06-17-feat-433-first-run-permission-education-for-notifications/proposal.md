## Why

GitHub issue [#433](https://github.com/mandrecode/tempo/issues/433) reports that notification permission prompts can feel abrupt and easy to deny when users do not yet understand how Tempo uses reminders. Reminder permission education should appear at the moment users try to create a reminder so the system prompt is contextual, optional, and aligned with Android best practices.

## What Changes

- Add a contextual education step before requesting Android notification permission for reminder creation while notification permission is missing.
- Explain reminders in product terms: Tempo sends task and routine reminders at the times users choose.
- Allow users to continue to the system permission prompt or defer without pressure.
- Keep denial and later re-enable flows clear by routing permanently denied notification permission to app notification settings.
- Reuse existing reminder permission checks for exact alarms and existing settings/revoked-permission paths.

Non-goals:

- Do not add a general app-launch onboarding sequence.
- Do not request notification permission before the user interacts with reminder functionality.
- Do not change reminder scheduling semantics, notification channel behavior, or alarm receiver behavior.
- Do not introduce new dependencies.

## Capabilities

### New Capabilities

- `notification-permission-education`: Defines how Tempo educates users before notification permission requests and handles continue, defer, denied, and settings re-enable paths for reminders.

### Modified Capabilities

- None. No existing OpenSpec specs are present.

## Impact

- Shared reminder permission UI in `core/ui/components/HandleReminderPermissions.kt`.
- Task and routine reminder creation flows that already invoke `HandleReminderPermissions`.
- Notification settings and revoked-permission copy if needed for consistency.
- Localized string resources in English and Spanish.
- Debug previews or focused tests for the new permission education UI where practical.
