## ADDED Requirements

### Requirement: User dismissal permanently ends a live activity
The system SHALL treat a user-initiated dismissal of a habit-chain live activity notification as a permanent end of that live activity session, so that dismissal is never undone by a later recovery run.

#### Scenario: User swipes the live activity away
- **GIVEN** a habit chain has an active live-activity notification recorded as active
- **WHEN** the user dismisses that notification from the notification shade
- **THEN** the system clears the chain's recorded active-live-activity state

#### Scenario: Dismissed live activity is not rebuilt on app open
- **GIVEN** the user has dismissed a habit chain's live-activity notification
- **WHEN** the user opens the app and a reschedule run executes
- **THEN** no live-activity notification is rebuilt for that chain

#### Scenario: Dismissal while the app process is not running
- **GIVEN** the app process is not running
- **WHEN** the user dismisses a habit chain's live-activity notification
- **THEN** the system still clears that chain's recorded active-live-activity state

#### Scenario: App-initiated cancellation is not a user dismissal
- **WHEN** the system itself cancels a live-activity notification because the chain completed, became stale, or notifications were revoked
- **THEN** the chain's recorded active-live-activity state is cleared exactly once, without treating the cancellation as a duplicate user dismissal

### Requirement: Active live activity records are scoped to a date
The system SHALL record the scheduled date each active habit-chain live activity belongs to, alongside the chain identity.

#### Scenario: Live activity starts or updates
- **WHEN** a habit chain's live-activity notification is posted or updated while still in progress
- **THEN** the system records the chain's ID together with the scheduled date that progress belongs to

#### Scenario: Live activity moves to a different date
- **GIVEN** a chain has a recorded active live activity for one date
- **WHEN** that chain's live activity is posted again for a different scheduled date
- **THEN** the recorded date is replaced with the newer date rather than kept alongside it

#### Scenario: Records persisted before dates were recorded
- **WHEN** the system reads a persisted active-live-activity record that carries no date
- **THEN** that record is treated as stale and is not counted as an active live activity

### Requirement: Recovery only rebuilds live activities that are still current
The system SHALL rebuild a habit-chain live-activity notification during a reschedule run only when its recorded date is today, and SHALL clear records for any other date.

#### Scenario: Recovering a live activity from today
- **GIVEN** a chain has a recorded active live activity dated today
- **WHEN** a reschedule run executes after a reboot, app update, or app reopen
- **THEN** the system rebuilds that chain's live-activity notification from current completion state

#### Scenario: Recovering a live activity from a previous day
- **GIVEN** a chain has a recorded active live activity dated before today
- **WHEN** a reschedule run executes
- **THEN** the system clears both the record and any lingering notification for that chain
- **AND** no live-activity notification is rebuilt for that chain

#### Scenario: Repeated app opens on the same day
- **GIVEN** a chain has a recorded active live activity dated today that the user has not dismissed
- **WHEN** the user opens the app several times during that day
- **THEN** the chain's live-activity notification reflects current completion state each time, and no notification the user removed is restored
