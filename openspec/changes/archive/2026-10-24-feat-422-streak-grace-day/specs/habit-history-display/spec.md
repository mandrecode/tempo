## MODIFIED Requirements

### Requirement: Scheduled-day check is shared with streak math
The system SHALL use a single definition of "scheduled day" across the history dot rendering and the streak calculation.

#### Scenario: Streak calculation continues to skip unscheduled days
- **WHEN** the streak label is computed for a habit with a `repeatDays` mask
- **THEN** the streak counts scheduled days that are present in `completionHistory` and is not broken by intervening unscheduled days

## ADDED Requirements

### Requirement: A lone missed day does not break the streak
The system SHALL keep a habit's streak alive across a single missed required day and break it only after two consecutive missed required days. Missed days SHALL NOT add to the streak count; unscheduled and paused days SHALL neither break nor reset the miss run.

#### Scenario: One missed day inside the streak
- **WHEN** a daily habit was completed today, missed yesterday, and completed the three days before
- **THEN** the streak label shows a 4 day streak

#### Scenario: Two consecutive missed days
- **WHEN** a daily habit was completed today but missed yesterday and the day before
- **THEN** the streak counts only today

#### Scenario: Today not yet completed
- **WHEN** a daily habit was completed yesterday and the days before but today is not checked yet
- **THEN** the streak still shows the days completed through yesterday

### Requirement: Streak label encourages after a missed day
The system SHALL show encouraging streak copy, for both build and quit habits, when the streak is positive and the most recent required day before today has no completion.

#### Scenario: Recovering from a missed day
- **WHEN** a habit's streak is alive and yesterday was a missed required day
- **THEN** the streak label uses the encouraging "keep going" variant with the current count

#### Scenario: Ordinary pending day
- **WHEN** the most recent required day before today was completed and today is not checked yet
- **THEN** the streak label uses the regular streak copy
