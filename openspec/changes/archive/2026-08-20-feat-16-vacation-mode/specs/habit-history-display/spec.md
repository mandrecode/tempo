## MODIFIED Requirements

### Requirement: History distinguishes scheduled and unscheduled days
The system SHALL render the habit tracking history view such that days that are not scheduled by the habit's `repeatDays` mask are visually distinct from days that are scheduled, and such that scheduled days falling inside a vacation period are visually distinct from both.

#### Scenario: Habit scheduled on a subset of weekdays
- **WHEN** the user opens the bottom sheet for a habit whose `repeatDays` is a non-empty subset of the week (for example Monday, Wednesday, Friday)
- **THEN** dates whose day-of-week is not in `repeatDays` are rendered with a muted style and are not shown as completed even if their date appears in `completionHistory`

#### Scenario: Habit scheduled every day
- **WHEN** the user opens the bottom sheet for a habit whose `repeatDays` is null or empty
- **THEN** every day in the visible window is rendered as scheduled, indistinguishable from the previous behavior

#### Scenario: Scheduled day inside a vacation period
- **WHEN** the visible history window includes a scheduled day that falls inside a stored vacation period and has no completion
- **THEN** that day is rendered in a paused style distinct from both the completed and the missed styles, so the gap reads as intentional next to the unchanged streak label

#### Scenario: Completed day inside a vacation period
- **WHEN** the visible history window includes a day that falls inside a vacation period and does appear in `completionHistory`
- **THEN** that day is rendered as completed, matching the streak rule that credits completions recorded while paused

### Requirement: Scheduled-day check is shared with streak math
The system SHALL use a single definition of "day in scope" — combining the habit's `repeatDays` mask and the stored vacation periods — across the history dot rendering and the streak calculation.

#### Scenario: Streak calculation continues to skip unscheduled days
- **WHEN** the streak label is computed for a habit with a `repeatDays` mask
- **THEN** the streak counts consecutive scheduled days that are present in `completionHistory` and is not broken by intervening unscheduled days

#### Scenario: Streak calculation skips paused days
- **WHEN** the streak label is computed for a habit whose recent scheduled days fall inside a vacation period
- **THEN** the same paused-day check that drives the history dot style is used, so the dots and the streak label can never disagree about which days were in scope

### Requirement: Habit chains follow the same display rule
The system SHALL apply the scheduled-vs-unscheduled-vs-paused history rendering rule to habit chains using the chain's `repeatDays` and the stored vacation periods.

#### Scenario: Chain bottom sheet history
- **WHEN** the user opens the bottom sheet for a habit chain whose `repeatDays` is a non-empty subset of the week
- **THEN** the chain history view renders unscheduled days with the same muted style used for habits and the chain streak honors `repeatDays`

#### Scenario: Chain history across a vacation period
- **WHEN** the user opens the bottom sheet for a habit chain whose visible history window covers a stored vacation period
- **THEN** the paused days are rendered in the same paused style used for habits and the chain streak is not broken by them
