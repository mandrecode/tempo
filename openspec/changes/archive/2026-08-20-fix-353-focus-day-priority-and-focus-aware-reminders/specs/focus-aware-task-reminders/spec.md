# focus-aware-task-reminders Specification

## ADDED Requirements

### Requirement: A task already focused on today is not reminded about
A task reminder notification SHALL NOT be posted for a task that has had focus time today — a session
counted against it, minutes recorded against it, or a session currently running on it. The rule
SHALL apply to both notification paths: the scheduled reminder alarm and the daily missed-reminder
catch-up sweep.

#### Scenario: Reminder lands after a finished session
- **WHEN** a 25-minute session finishes on a task at 09:01 and that task's reminder alarm fires at
  09:01
- **THEN** no reminder notification is posted for that task

#### Scenario: Reminder lands during a running session
- **WHEN** a session is running on a task and that task's reminder alarm fires
- **THEN** no reminder notification is posted for that task

#### Scenario: Reminder lands during a break taken from that task
- **WHEN** a break is running after a session on a task and that task's reminder alarm fires
- **THEN** no reminder notification is posted for that task

#### Scenario: Catch-up sweep skips work already done today
- **WHEN** the daily missed-reminder sweep runs and one of the overdue tasks it would notify about has
  had a session today
- **THEN** that task is skipped and the remaining overdue tasks are still notified

#### Scenario: A task with no focus time is still reminded about
- **WHEN** a task's reminder fires and no session has run on it today
- **THEN** the reminder notification is posted as before

### Requirement: The suppression window is the current day only
Focus time SHALL only suppress a reminder on the day it was earned. Sessions run on a previous day
SHALL NOT suppress today's reminder, whether the app process has been alive across midnight or is
started cold.

#### Scenario: Yesterday's session does not silence today
- **WHEN** a task had a session yesterday and its reminder fires today
- **THEN** the reminder notification is posted

#### Scenario: A long-lived process crosses midnight
- **WHEN** the app process has been running since before midnight, holds yesterday's focus record in
  memory, and a task reminder fires after midnight
- **THEN** the stored record is judged against today's date, found not to belong to today, and the
  reminder notification is posted

### Requirement: Suppressing the notification does not suppress the work that rides with it
Withholding a reminder notification SHALL NOT change any other effect of the reminder firing. In
particular an overdue periodic task SHALL still be rolled over to its next occurrence, and the
missed-reminder sweep SHALL still re-arm itself for the next day.

#### Scenario: Periodic rollover still happens
- **WHEN** a periodic task with a session today has its reminder fire
- **THEN** no notification is posted and the task is still rolled over to its next occurrence

#### Scenario: The sweep re-arms regardless
- **WHEN** the daily sweep suppresses every task it considered
- **THEN** the next day's sweep is still scheduled
