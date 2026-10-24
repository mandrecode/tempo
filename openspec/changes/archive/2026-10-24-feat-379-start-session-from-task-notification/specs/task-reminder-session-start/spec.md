# Spec Delta

## Purpose

Let the user begin a focus session on a task straight from that task's reminder notification, without opening the app.

## ADDED Requirements

### Requirement: Task reminders offer to start a focus session
Every task reminder notification, whether posted when the reminder fires or by the missed-reminder catch-up, SHALL include a Start session action alongside Mark as completed.

#### Scenario: Reminder fires
- **WHEN** a task reminder notification is posted
- **THEN** it shows a Start session action

### Requirement: Starting from the notification begins a session on that task
Pressing Start session SHALL start a focus session on the notified task at the configured default session length, without opening the app, and SHALL dismiss the task reminder notification. The ongoing focus-session notification SHALL then show the running session.

#### Scenario: No session running
- **WHEN** the user presses Start session on an open task's reminder and no session is running
- **THEN** a focus session starts on that task at the default length and the reminder notification is dismissed

#### Scenario: Another task's session is running
- **WHEN** the user presses Start session while a session on a different task, or a break, is running
- **THEN** that session ends with its earned minutes banked and a new session starts on the notified task

#### Scenario: Same task already in session
- **WHEN** the user presses Start session while a session on the same task is already running or paused
- **THEN** that session continues (resuming it if paused) without restarting, and the reminder notification is dismissed

#### Scenario: Same task's session has already run out
- **WHEN** the user presses Start session while the stored session on the same task has expired but its end has not been processed yet
- **THEN** the expired session is ended first and a new session starts on the task

#### Scenario: Reminder belongs to a subtask
- **WHEN** the user starts a session from a subtask's reminder
- **THEN** the session's controls in the app act on that subtask, even though Focus shows it nested under its parent

#### Scenario: Two reminders pressed at once
- **WHEN** Start session is pressed on two reminders in quick succession
- **THEN** the starts apply one after the other, leaving a single consistent session on the last task

#### Scenario: Task gone or completed
- **WHEN** the user presses Start session for a task that has been deleted or completed
- **THEN** no session starts and the reminder notification is dismissed
