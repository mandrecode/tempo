# habit-history-display Specification

## Purpose
Define how a habit's tracking history is rendered with respect to its periodicity (`repeatDays`), so days the habit is not scheduled on are visually distinct and never shown as completed.

## Requirements

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
- **THEN** the streak counts scheduled days that are present in `completionHistory` and is not broken by intervening unscheduled days

### Requirement: Habit chains follow the same display rule
The system SHALL apply the scheduled-vs-unscheduled history rendering rule to habit chains using the chain's `repeatDays`.

#### Scenario: Chain bottom sheet history
- **WHEN** the user opens the bottom sheet for a habit chain whose `repeatDays` is a non-empty subset of the week
- **THEN** the chain history view renders unscheduled days with the same muted style used for habits and the chain streak honors `repeatDays`

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
