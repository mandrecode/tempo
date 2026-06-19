## ADDED Requirements

### Requirement: History distinguishes scheduled and unscheduled days
The system SHALL render the habit tracking history view such that days that are not scheduled by the habit's `repeatDays` mask are visually distinct from days that are scheduled.

#### Scenario: Habit scheduled on a subset of weekdays
- **WHEN** the user opens the bottom sheet for a habit whose `repeatDays` is a non-empty subset of the week (for example Monday, Wednesday, Friday)
- **THEN** dates whose day-of-week is not in `repeatDays` are rendered with a muted style and are not shown as completed even if their date appears in `completionHistory`

#### Scenario: Habit scheduled every day
- **WHEN** the user opens the bottom sheet for a habit whose `repeatDays` is null or empty
- **THEN** every day in the visible window is rendered as scheduled, indistinguishable from the previous behavior

### Requirement: History window remains anchored to calendar days
The system SHALL keep the visible history window anchored to the most recent calendar days rather than to scheduled occurrences.

#### Scenario: Window length matches calendar days
- **WHEN** the history view is rendered for any habit
- **THEN** the visible dots cover at most the last 21 calendar days ending on today and at least one day, bounded below by the habit's effective creation date

### Requirement: Scheduled-day check is shared with streak math
The system SHALL use a single definition of "scheduled day" across the history dot rendering and the streak calculation.

#### Scenario: Streak calculation continues to skip unscheduled days
- **WHEN** the streak label is computed for a habit with a `repeatDays` mask
- **THEN** the streak counts consecutive scheduled days that are present in `completionHistory` and is not broken by intervening unscheduled days

### Requirement: Habit chains follow the same display rule
The system SHALL apply the scheduled-vs-unscheduled history rendering rule to habit chains using the chain's `repeatDays`.

#### Scenario: Chain bottom sheet history
- **WHEN** the user opens the bottom sheet for a habit chain whose `repeatDays` is a non-empty subset of the week
- **THEN** the chain history view renders unscheduled days with the same muted style used for habits and the chain streak honors `repeatDays`
