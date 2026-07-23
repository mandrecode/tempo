## ADDED Requirements

### Requirement: Reschedule reminders on app update
The system SHALL trigger a full reminder reschedule for tasks, habits, and habit chains when the app package is replaced (updated).

#### Scenario: App is updated
- **WHEN** the system delivers `ACTION_MY_PACKAGE_REPLACED` to the app
- **THEN** the system enqueues a reschedule run that re-arms every task, habit, and habit-chain reminder currently persisted with a non-null reminder date

### Requirement: Reschedule reminders immediately on app reopen
The system SHALL re-arm persisted reminders immediately when the app is opened, in addition to the existing periodic background refresh.

#### Scenario: App is reopened after being force-stopped
- **WHEN** the user launches the app after it was force-stopped (which clears its scheduled alarms and prevents boot/background triggers from reaching it)
- **THEN** the system enqueues an immediate one-off reschedule run without waiting for the periodic refresh interval

#### Scenario: App is reopened multiple times in quick succession
- **WHEN** the app is foregrounded again while an immediate reschedule run it previously enqueued is still pending or in progress
- **THEN** the system does not enqueue a redundant duplicate run

### Requirement: Restore habit-chain live activity after recovery
The system SHALL resync each habit chain's live-activity notification that was active before the app stopped, as part of reminder recovery.

#### Scenario: Live activity was active before reboot
- **WHEN** a reschedule run occurs (boot, app update, or immediate reopen recovery) and a habit chain is recorded as having an active live-activity notification
- **THEN** the system rebuilds that chain's live-activity notification from current completion state, including on Android versions where the system notification itself did not survive the event

#### Scenario: Chain state changed while the app was closed
- **WHEN** a chain recorded as having an active live-activity notification has since become fully completed, or still has no habits completed with its reminder still in the future
- **THEN** the system dismisses the stale live-activity notification instead of recreating it

### Requirement: Persist habit-chain live-activity identity across process death
The system SHALL persist which habit chains currently have an active live-activity notification so that identity survives force-close, app update, and reboot.

#### Scenario: Live activity starts
- **WHEN** a habit chain's live-activity notification is started or updated while still in progress
- **THEN** the chain's ID is recorded in persistent storage as having an active live activity

#### Scenario: Live activity ends
- **WHEN** a habit chain's live-activity notification is dismissed or completes
- **THEN** the chain's ID is removed from persistent storage
