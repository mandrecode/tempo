## ADDED Requirements

### Requirement: Daily catch-up for missed task reminders
The system SHALL re-notify, once per day at the configured catch-up time, every task whose reminder time has already passed and that is still incomplete.

#### Scenario: Reminder was missed while the device was off
- **WHEN** a task reminder time passes without a notification being delivered and the task is still incomplete at the catch-up time
- **THEN** the system posts that task's reminder notification at the catch-up time

#### Scenario: User dismissed the original reminder without acting
- **WHEN** a task reminder was delivered, dismissed, and the task is still incomplete at the catch-up time
- **THEN** the system posts that task's reminder notification again at the catch-up time

#### Scenario: No tasks are overdue
- **WHEN** the catch-up runs and no incomplete task has a reminder time in the past
- **THEN** the system posts no notification

### Requirement: Catch-up selection rules
The system SHALL include a task in the catch-up if and only if the task is incomplete, has a reminder date, and that reminder date is strictly earlier than the moment the catch-up runs.

#### Scenario: Task has no reminder
- **WHEN** an incomplete task has no reminder date
- **THEN** the catch-up does not notify for that task

#### Scenario: Task is completed
- **WHEN** a task with an overdue reminder date is completed
- **THEN** the catch-up does not notify for that task

#### Scenario: Reminder is still in the future
- **WHEN** an incomplete task's reminder date is later than the moment the catch-up runs
- **THEN** the catch-up does not notify for that task and the task's own reminder alarm still fires at its scheduled time

#### Scenario: Reminder fired earlier the same day
- **WHEN** an incomplete task's reminder date is earlier the same calendar day as the catch-up time
- **THEN** the catch-up notifies for that task

### Requirement: Catch-up never mutates the task
The system SHALL leave task data unchanged when running the catch-up.

#### Scenario: Overdue task is caught up
- **WHEN** the catch-up notifies for an overdue task
- **THEN** the task's reminder date, completion state, periodicity, and recurrence links are unchanged

#### Scenario: Overdue periodic task is caught up
- **WHEN** the catch-up notifies for an overdue periodic task
- **THEN** the catch-up does not create, complete, or reschedule any occurrence, and existing rollover behavior remains the only path that spawns the next occurrence

### Requirement: Catch-up repeats until the task is resolved
The system SHALL notify for the same overdue task at every catch-up time until the task is completed or its reminder is cleared.

#### Scenario: Task stays incomplete for several days
- **WHEN** an overdue incomplete task is not acted on for three consecutive days
- **THEN** the system posts its notification at the catch-up time on each of those days

#### Scenario: Task is completed after a catch-up
- **WHEN** the user completes an overdue task after a catch-up notification
- **THEN** no further catch-up notification is posted for that task

#### Scenario: Reminder is cleared after a catch-up
- **WHEN** the user removes the reminder from an overdue task
- **THEN** no further catch-up notification is posted for that task

### Requirement: Catch-up notification is the task reminder notification
The system SHALL post catch-up notifications using the same content, channel, actions, and per-task notification identity as the task's original reminder notification.

#### Scenario: Catch-up notification content
- **WHEN** the catch-up notifies for a task
- **THEN** the notification shows the task title and description, opens the task when tapped, and offers the "Mark as completed" action

#### Scenario: Original notification is still visible
- **WHEN** the catch-up notifies for a task whose original reminder notification is still in the shade
- **THEN** the existing notification is replaced rather than duplicated

#### Scenario: Notifications are not permitted
- **WHEN** the catch-up runs while the app cannot post notifications
- **THEN** the system posts nothing and does not crash

### Requirement: Catch-up can be enabled and disabled
The system SHALL let the user turn the missed-reminder catch-up on or off, and SHALL default it to on.

#### Scenario: First launch after install
- **WHEN** the user has never changed the catch-up setting
- **THEN** the catch-up is enabled and set to 09:00

#### Scenario: User disables the catch-up
- **WHEN** the user turns the catch-up off
- **THEN** no further catch-up notification is posted and any pending catch-up trigger is cancelled

#### Scenario: User re-enables the catch-up
- **WHEN** the user turns the catch-up back on
- **THEN** the system arms the next catch-up for the configured time

### Requirement: Catch-up time is configurable
The system SHALL let the user choose the time of day at which the catch-up runs, and SHALL persist that choice across app restarts.

#### Scenario: User changes the catch-up time
- **WHEN** the user picks a new catch-up time
- **THEN** the system cancels the previously armed catch-up and arms the next one at the new time

#### Scenario: New time is still ahead today
- **WHEN** the user picks a catch-up time later than the current time of day
- **THEN** the next catch-up occurs at that time today

#### Scenario: New time has already passed today
- **WHEN** the user picks a catch-up time earlier than the current time of day
- **THEN** the next catch-up occurs at that time the following day

#### Scenario: Setting survives restart
- **WHEN** the app is restarted after the user changed the catch-up time
- **THEN** the Settings screen shows the chosen time and the catch-up runs at that time

### Requirement: Catch-up survives reboot and system time changes
The system SHALL re-arm the catch-up after events that clear scheduled alarms.

#### Scenario: Device reboots
- **WHEN** the device finishes booting and the catch-up is enabled
- **THEN** the system arms the next catch-up at the configured time

#### Scenario: Time or timezone changes
- **WHEN** the system time or timezone changes and the catch-up is enabled
- **THEN** the system re-arms the catch-up for the configured local time

#### Scenario: Periodic reminder refresh runs
- **WHEN** the periodic reminder refresh runs and the catch-up is enabled
- **THEN** the system ensures a catch-up is armed for the configured time

#### Scenario: Catch-up fires
- **WHEN** a catch-up runs
- **THEN** the system arms the next catch-up for the configured time on the following day

### Requirement: Catch-up degrades without exact alarm permission
The system SHALL still run the catch-up when exact alarms cannot be scheduled, accepting that delivery may be delayed.

#### Scenario: Exact alarms are unavailable
- **WHEN** the catch-up is armed while the app cannot schedule exact alarms
- **THEN** the system arms an inexact alarm for the configured time instead of failing
