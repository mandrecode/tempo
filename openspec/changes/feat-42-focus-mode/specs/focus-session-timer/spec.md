# focus-session-timer Specification

## ADDED Requirements

### Requirement: A focus session starts from the Up next card
The user SHALL be able to start a focus session on the Up next item with a single tap, without
navigating away from the Focus screen and without choosing a duration at start time.

#### Scenario: Starting a session
- **WHEN** the user taps the start action on the Up next card
- **THEN** a session begins for that item using the configured default length, and the card becomes
  the running session card in place

#### Scenario: Agenda stays usable during a session
- **WHEN** a session is running
- **THEN** the Overdue and Today sections remain visible and their items can still be completed

### Requirement: Exactly one focus session exists at a time
The app SHALL model at most one active session. Starting a session while another is running SHALL
replace the running session rather than creating a second one.

#### Scenario: Starting a session on another item
- **WHEN** a session is running on one task and the user starts a session on another
- **THEN** the first session ends, its elapsed minutes are recorded, and the new session begins

#### Scenario: State holds a single session
- **WHEN** session state is inspected
- **THEN** it holds a single nullable session rather than a collection

### Requirement: A running session is visible from anywhere in the app
While a session is running the app SHALL show its remaining time on the Focus screen, on the
persistent floating bar when another tab is active, and in an ongoing notification.

#### Scenario: Viewing another tab
- **WHEN** the user switches to Routines while a session runs
- **THEN** the floating bar shows a timer chip with the remaining time that returns to Focus when
  tapped

#### Scenario: Leaving the app
- **WHEN** the app is backgrounded while a session runs
- **THEN** an ongoing notification shows the task title and counts the remaining time down

#### Scenario: Session ends
- **WHEN** the session ends or is cancelled
- **THEN** the timer chip and the ongoing notification are removed

### Requirement: The session timer uses alarms and a chronometer notification
The countdown SHALL be driven by an exact alarm for the session end and an ongoing notification
using the platform's count-down chronometer, and the app SHALL NOT start a foreground service for
focus sessions.

#### Scenario: Notification counts down without updates
- **WHEN** the ongoing session notification is posted
- **THEN** the remaining time is rendered by the platform chronometer rather than by periodic
  notification updates from the app

#### Scenario: No foreground service is declared
- **WHEN** the app manifest is inspected
- **THEN** no foreground service or foreground service permission is declared for focus sessions

### Requirement: Sessions survive force-close and reboot
An active session SHALL be persisted so that its remaining time, alarm and notification are restored
after the app is force-closed, updated, or the device restarts, and a session whose end time has
already passed SHALL be completed rather than resumed.

#### Scenario: Reboot during a session
- **WHEN** the device restarts while a session has time remaining
- **THEN** the session, its alarm, and its notification are restored with the correct remaining time

#### Scenario: End time passed while the app was not running
- **WHEN** the app starts and an active session's end time is in the past
- **THEN** the session is completed, its minutes are recorded, and no stale countdown is shown

#### Scenario: Dismissed notification is not re-posted
- **WHEN** the user dismisses the ongoing notification
- **THEN** it is not re-posted for that session

### Requirement: Session completion is reported without grading
When a session's time is up the app SHALL present a modal sheet stating the elapsed time and the
item worked on, offering to take a break, start another session, or stop. The sheet SHALL NOT
present streak, score, or celebratory content.

#### Scenario: Time is up
- **WHEN** a session reaches its configured length
- **THEN** a sheet reports the minutes completed and the item, and offers a break, another session,
  and stopping as equally available choices

#### Scenario: Completing the task during a session
- **WHEN** the user marks the session's task complete before the time is up
- **THEN** the session ends immediately, the elapsed minutes are recorded, and the completion sheet
  is shown

### Requirement: Focus minutes are recorded in the daily activity record
Elapsed session minutes SHALL be added to the current day's activity record so that the summary
reports focus time accumulated across sessions rather than only the current one.

#### Scenario: Minutes accumulate across sessions
- **WHEN** two sessions of twenty-five minutes complete on the same day
- **THEN** the day's recorded focus time is fifty minutes

#### Scenario: Cancelled session banks elapsed time
- **WHEN** the user ends a session after ten minutes
- **THEN** ten minutes are recorded for that day

### Requirement: Both lengths are configurable
Settings SHALL offer a default focus session length, defaulting to 25 minutes, and a break length,
defaulting to 5 minutes. Each SHALL be set on a clock face in hours and minutes, and the chosen
values SHALL apply to sessions and breaks started afterwards.

A session SHALL also be startable at a length chosen for that start alone, which SHALL NOT change
the configured default.

#### Scenario: Changing the default length
- **WHEN** the user sets the default session length to 45 minutes and starts a session
- **THEN** the session runs for 45 minutes

#### Scenario: Changing the break length
- **WHEN** the user sets the break length to 15 minutes and takes a break
- **THEN** the break runs for 15 minutes

#### Scenario: A length chosen for one session only
- **WHEN** the user starts a session at 40 minutes from the session surface
- **THEN** that session runs for 40 minutes and the configured default is unchanged

#### Scenario: Change does not affect a running session
- **WHEN** the default length is changed while a session is running
- **THEN** the running session keeps its original length
