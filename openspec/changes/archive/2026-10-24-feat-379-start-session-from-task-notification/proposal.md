# Proposal

## Why

A task reminder is the moment the user is told to get on with the work, but beginning a focus session on it means opening the app, finding the task in Focus and pressing start. [Issue #379](https://github.com/mandrecode/tempo/issues/379) asks for a Start session button directly on the task notification.

## What Changes

- Task reminder notifications (from the alarm and from the missed-reminder catch-up) gain a **Start session** action next to **Mark as completed**.
- Pressing it starts a focus session on that task at the configured default length, without opening the app; the reminder notification is dismissed and the ongoing focus-session notification takes its place.
- If a session is already running on the same task, the action carries on with it instead of restarting it. If the task no longer exists or is already completed, nothing starts.
- A What's New entry announces the action.

Non-goals:
- No confirmation step when a session on another task is running; the notification action replaces it the same way starting a new session always does, banking the earned minutes.
- No session action on habit or chain notifications.
- No change to session length configuration or the session notification's own controls.

## Capabilities

### New Capabilities
- `task-reminder-session-start`: starting a focus session on a task from its reminder notification.

### Modified Capabilities

## Impact

Task reminder notifier, a new broadcast receiver registered in the manifest, a focus-domain use case for starting a session on a task by id, localized strings, What's New. No new dependencies, no database change.
