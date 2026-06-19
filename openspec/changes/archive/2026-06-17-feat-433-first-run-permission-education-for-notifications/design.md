## Context

Reminder permission handling is centralized in `HandleReminderPermissions`. Task and routine reminder entry points call it before showing their reminder picker, and screen-level flows call it after scheduling fails with a permission error. On Android 13+, the component currently launches the system notification permission request immediately when notification permission is missing. If the user denies permission, Tempo shows a generic rationale or settings path. Settings and revoked-permission dialogs already provide later re-enable paths for reminders that exist after permissions are revoked.

This change affects UI/infrastructure permission orchestration only. Reminder data, recurrence calculations, scheduler side effects, notification channels, and Room transactions remain unchanged.

## Goals / Non-Goals

**Goals:**

- Educate users before the Android notification permission prompt when they try to create a reminder while notification permission is missing.
- Keep the education contextual to task/routine reminders rather than app-launch onboarding.
- Let users continue or defer without creating a reminder.
- Preserve accepted, denied, permanently denied, and exact-alarm follow-up flows.
- Keep all user-facing copy localized.

**Non-Goals:**

- Add persistent onboarding state or a new onboarding screen.
- Change scheduling semantics or resync behavior.
- Change notification channel setup.
- Introduce dependencies or new data storage.

## Decisions

1. Show education at reminder intent time inside `HandleReminderPermissions`.

   Rationale: Both task and routine flows already use this component before opening reminder pickers, so the education stays close to the feature behavior and avoids app-launch prompts. This also keeps the change minimal and avoids duplicating logic across bottom sheets.

   Alternative considered: app first-run onboarding. Rejected because it would request attention before users have chosen reminder behavior and would duplicate the reminder-specific flow.

2. Do not persist a one-time education flag.

   Rationale: Once notification permission is granted, the education path is skipped by the permission check. If users defer, showing the explanation again the next time they ask for a reminder is still contextual and not a stored onboarding concern. If users deny permanently, the permission result routes them to the settings path instead of silently failing.

   Alternative considered: SharedPreferences tracking that education was seen. Rejected because it adds state without improving the core reminder flow, and it could suppress helpful context after a user defers.

3. Keep exact alarm permission as a second step after notification permission.

   Rationale: Exact alarms are only useful after reminders can notify the user. Existing exact-alarm rationale/settings behavior remains the authoritative Android scheduler-side gate.

4. Preserve transaction and scheduler boundaries.

   Rationale: This change only affects whether the UI proceeds to reminder picking/scheduling. It does not alter database writes, Room transactions, scheduler calls, WorkManager, AlarmManager, or receiver behavior.

## Risks / Trade-offs

- Education may appear more than once when a user repeatedly defers -> Keep copy short and action-oriented.
- Adding another dialog before the system prompt adds one extra tap -> Only show it when notification permission is missing and the user explicitly asks for a reminder.
- Some schedule-failure recovery flows may also use the education component -> The copy remains reminder-specific and the permission checks still route already-denied states to rationale/settings behavior.
