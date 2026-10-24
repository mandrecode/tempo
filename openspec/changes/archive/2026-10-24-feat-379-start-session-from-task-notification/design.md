# Design

## Context

`TaskReminderNotifier` builds the single task reminder notification for both the alarm and the catch-up, with a Mark as completed broadcast action. Focus sessions are started through `FocusSessionUseCases.start(taskId, taskTitle, …)`, which ends any running session first, persists the new one, arms the end alarm and posts the ongoing notification. No foreground service is involved, so a session can be started from a broadcast receiver.

## Goals / Non-Goals

**Goals:**
- One-tap start from the notification, using the same start path as the app.

**Non-Goals:**
- Asking before replacing a running session (the in-app replace dialog has no notification equivalent).

## Decisions

- **Broadcast action, not an activity launch.** The action starts the session in a `StartFocusSessionReceiver` with `goAsync()`, the same pattern as Mark as completed and Start chain. The ongoing session notification is the feedback. Opening the app was considered, but it adds MainActivity routing for no gain: the session notification already opens Focus when tapped.
- **Domain use case `StartFocusSessionOnTaskUseCase`.** Looks the task up by id, skips missing/completed tasks, treats a running non-break session on the same task as already started, and otherwise delegates to `FocusSessionUseCases.start`. It keeps the receiver thin and unit-testable in pure Kotlin. The task title comes from the repository, not the intent, so a renamed task shows its current title.
- **Request code** is `RequestCodeGenerator.forTask(taskId)`. Broadcast PendingIntents are keyed by component as well as request code, so it does not collide with the Mark as completed broadcast to a different receiver.
- **Dismissal** goes through `NotificationSyncManager.dismissTaskNotification(taskId)` in all outcomes, since the user acted on the reminder.

## Risks / Trade-offs

- [A running session on another task is replaced without asking] → minutes earned are banked by `end()`, matching every other start path; documented as a non-goal.
- [Notifications permission revoked] → the session still starts and its alarm is armed; only the ongoing notification is skipped, as with in-app starts.
- No transaction boundary: the session is a single preference write; alarm and notification are side effects performed after it by the existing use case.
