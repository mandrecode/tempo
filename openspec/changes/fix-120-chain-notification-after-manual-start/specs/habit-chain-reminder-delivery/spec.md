## ADDED Requirements

### Requirement: Scheduled chain reminders reflect occurrence progress
The system SHALL show a scheduled habit-chain reminder only when none of the chain's available member habits is completed for that reminder's occurrence date.

#### Scenario: Chain occurrence has not started
- **WHEN** a habit-chain reminder fires and no member habit is completed for the occurrence date
- **THEN** the system posts the habit-chain reminder notification

#### Scenario: Chain occurrence was started manually
- **WHEN** a habit-chain reminder fires and at least one member habit is completed for the occurrence date
- **THEN** the system does not post the habit-chain reminder notification

#### Scenario: Completion belongs to another date
- **WHEN** a habit-chain reminder fires and member completion history exists only for a different date
- **THEN** the system posts the habit-chain reminder notification for the scheduled occurrence

#### Scenario: Delayed alarm after stored reminder advances
- **WHEN** a habit-chain alarm is delivered after the chain's stored reminder date has advanced
- **THEN** the system evaluates member progress against the occurrence date carried by the alarm

#### Scenario: Empty chain reminder fires
- **WHEN** a habit-chain reminder fires for a chain with no member habits
- **THEN** the system handles the empty chain without querying members or crashing

### Requirement: Suppression preserves recurring reminders
The system SHALL process recurrence rollover after a scheduled chain alarm even when the current occurrence's notification is suppressed.

#### Scenario: Started recurring chain reaches reminder time
- **WHEN** a recurring habit chain has already started for the occurrence date and its scheduled alarm fires
- **THEN** the current notification is suppressed
- **AND** the chain reminder advances and schedules its next valid occurrence
