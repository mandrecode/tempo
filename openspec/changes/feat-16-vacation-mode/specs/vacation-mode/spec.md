## ADDED Requirements

### Requirement: User can turn vacation mode on and off
The system SHALL expose a single app-level vacation-mode switch in Settings that pauses habit tracking for every habit and habit chain at once.

#### Scenario: Turning vacation mode on
- **WHEN** the user turns the vacation-mode switch on
- **THEN** a vacation period starting on the current local date is recorded and vacation mode reads as active for the current date

#### Scenario: Turning vacation mode off
- **WHEN** the user turns the vacation-mode switch off while a vacation period is active
- **THEN** that period is closed on the day before the current local date, so the switch reads as off immediately and today is tracked normally again

#### Scenario: Turning vacation mode on and off the same day
- **WHEN** the user turns the vacation-mode switch on and off again on the same local date
- **THEN** no vacation period remains stored, leaving the habit history exactly as it was

#### Scenario: Switch reflects persisted state on relaunch
- **WHEN** the user reopens the app while a vacation period covers the current date
- **THEN** the vacation-mode switch is shown as on without any further user action

### Requirement: Vacation periods carry an optional end date
The system SHALL allow the user to give the active vacation period a planned end date, and SHALL treat a period without an end date as open-ended until the user turns vacation mode off.

#### Scenario: Setting a planned end date
- **WHEN** the user picks an end date while vacation mode is on
- **THEN** the active period ends on that date inclusive, and the chosen date is shown in the vacation-mode settings section

#### Scenario: Planned end date passes
- **WHEN** the current local date moves past the active period's end date
- **THEN** vacation mode reads as inactive from the following day onward with no background job, alarm, or app launch required, and the switch is shown as off

#### Scenario: End date cannot precede the start date
- **WHEN** the user attempts to pick an end date earlier than the period's start date
- **THEN** the date is rejected and the period is left unchanged

#### Scenario: Clearing the end date
- **WHEN** the user clears a previously chosen end date
- **THEN** the active period becomes open-ended again and stays active until the user turns vacation mode off

### Requirement: An active pause is visible on the Routines screen
The system SHALL mark the Routines title with a vacation badge while a vacation period covers the current date, so the pause is discoverable without opening Settings.

#### Scenario: Badge shown while paused
- **WHEN** the user opens the Routines screen while vacation mode is active
- **THEN** a palm badge is shown next to the "Routines" title, labelled for screen readers as vacation mode being on

#### Scenario: No badge when not paused
- **WHEN** the user opens the Routines screen while no stored period covers today
- **THEN** the title is rendered exactly as before, with no badge

#### Scenario: Habit editor explains the pause
- **WHEN** the user opens a habit or chain editor while vacation mode is active
- **THEN** a notice directly above the reminder and history rows states that reminders stay silent and that skipped days will not break the streak

#### Scenario: No notice when not paused
- **WHEN** the user opens a habit or chain editor while no stored period covers today
- **THEN** the editor shows no vacation notice

#### Scenario: Badge follows the switch
- **WHEN** the user turns vacation mode off in Settings and returns to Routines
- **THEN** the badge is gone, without the screen having to be recreated

### Requirement: Past vacation periods are retained
The system SHALL persist completed vacation periods, not only the active one, so that any later computation over historical dates resolves the same paused days.

#### Scenario: Historical days stay paused
- **WHEN** a vacation period has ended and the user later views a habit whose history window still covers those days
- **THEN** those days are still resolved as paused

#### Scenario: Multiple separate trips
- **WHEN** the user has recorded several non-overlapping vacation periods over time
- **THEN** every one of them is retained and a day is paused if it falls inside any of them

#### Scenario: Restarting vacation mode inside a stored period
- **WHEN** the user turns vacation mode on while today already falls inside a stored period
- **THEN** no duplicate overlapping period is created and the stored periods remain a set of non-overlapping ranges

### Requirement: Paused days never break a streak
The system SHALL compute habit streaks such that a planned day falling inside a vacation period neither breaks nor is required for the current streak.

#### Scenario: Streak resumes across a week away
- **WHEN** a habit was completed on every planned day up to the start of a seven-day vacation period, nothing was recorded during it, and the habit is completed again on the first planned day after it
- **THEN** the streak equals the count reached before the vacation period plus that day, with the seven skipped days neither counted nor breaking it

#### Scenario: Streak reads unchanged during the pause
- **WHEN** the streak is computed for a habit on a day that falls inside an active vacation period and nothing is recorded that day
- **THEN** the streak equals the value it had on the last day before the period

#### Scenario: Completing a habit while paused still counts
- **WHEN** a habit is completed on a planned day that falls inside a vacation period
- **THEN** that day increments the streak, so keeping a habit up while away is credited rather than ignored

#### Scenario: Missing a planned day after the pause still breaks the streak
- **WHEN** a planned day after the vacation period ends has no completion
- **THEN** the streak breaks on that day exactly as it would without any vacation period, including the existing convention that an uncompleted planned *today* reads as no streak

#### Scenario: Quit habits freeze the same way
- **WHEN** the streak is computed for a habit of type QUIT over days covered by a vacation period
- **THEN** those days are skipped by the same rule used for BUILD habits

### Requirement: Habit reminders are suppressed on paused days
The system SHALL NOT post habit or habit-chain reminder notifications for a day that falls inside a vacation period, and SHALL leave reminder rescheduling unaffected.

#### Scenario: Habit reminder fires during a vacation period
- **WHEN** a habit reminder alarm fires for a date that falls inside a vacation period
- **THEN** no notification is posted for that habit

#### Scenario: Chain reminder fires during a vacation period
- **WHEN** a habit-chain reminder alarm fires for a date that falls inside a vacation period
- **THEN** no chain notification is posted

#### Scenario: Reminders resume by themselves after the pause
- **WHEN** a habit reminder alarm fires during a vacation period and its next occurrence is later reached after the period has ended
- **THEN** the reminder was still rescheduled while paused and the later occurrence posts its notification normally, with no restore step required from the user

#### Scenario: Reminders outside the pause are unaffected
- **WHEN** a habit reminder alarm fires for a date that falls outside every stored vacation period
- **THEN** the existing delivery rules apply unchanged, including the rule that no reminder is posted for a habit already completed on that date

### Requirement: Vacation periods survive backup and restore
The system SHALL include the stored vacation periods in the settings carried by an encrypted backup file and SHALL restore them on a full-replace import.

#### Scenario: Export and full restore round-trip
- **WHEN** the user exports a backup while vacation periods are stored and later imports that file in replace mode
- **THEN** the same vacation periods are restored, so streaks computed after the restore skip the same days

#### Scenario: Merge import leaves periods alone
- **WHEN** the user imports a backup file in merge mode
- **THEN** the stored vacation periods are left unchanged, consistent with how the other settings are handled on merge

#### Scenario: Backup file predating vacation mode
- **WHEN** the user imports a backup file whose settings section carries no vacation periods
- **THEN** the import succeeds and the device's stored vacation periods are treated as an empty list rather than failing the import
