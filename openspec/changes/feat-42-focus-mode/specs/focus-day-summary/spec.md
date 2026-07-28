# focus-day-summary Specification

## ADDED Requirements

### Requirement: Focus opens with a day summary hero
The Focus screen SHALL present a summary card above the agenda. Its header line SHALL carry the
weekday and the current focus streak as secondary context; its headline SHALL describe today's
progress; and it SHALL also show the last seven days of activity and a completion indicator.

#### Scenario: Streak is header context, not the headline
- **WHEN** the hero is rendered with an active streak
- **THEN** the streak appears in the smaller header line beside the weekday, and the headline
  describes today's progress instead

#### Scenario: Summary reflects today's state
- **WHEN** the Focus screen is shown with five of nine scheduled items completed
- **THEN** the hero reports today's progress as five of nine

#### Scenario: Summary updates when an item is completed
- **WHEN** the user completes an item in the agenda below
- **THEN** the hero's headline, progress and today's history entry update without leaving the screen

#### Scenario: First run with no history
- **WHEN** no daily activity has been recorded yet
- **THEN** the hero renders with no streak in the header and an empty history row, and does not show
  an error or empty-state placeholder

### Requirement: The hero headline reflects how much of today is done
The headline SHALL be selected from four completion bands, evaluated as completed divided by
scheduled items for today. The copy SHALL state progress plainly, without praise, scoring, or
pressure to continue.

#### Scenario: Under a third complete
- **WHEN** the completion ratio is below one third
- **THEN** the first band's copy is shown

#### Scenario: Between a third and two thirds
- **WHEN** the completion ratio is at least one third and below two thirds
- **THEN** the second band's copy is shown

#### Scenario: Two thirds or more but not complete
- **WHEN** the completion ratio is at least two thirds and at least one item is uncompleted
- **THEN** the third band's copy is shown

#### Scenario: Everything complete
- **WHEN** every scheduled item for today is completed
- **THEN** the fourth band's copy is shown

#### Scenario: Nothing scheduled today
- **WHEN** today has no scheduled items and the ratio is therefore undefined
- **THEN** a distinct copy for an unscheduled day is shown rather than the fully-complete copy

### Requirement: Day history uses exactly three states
Each day in the history SHALL be rendered in one of three states — nothing completed, some
completed, or all completed — with no intermediate gradient and no distinct failure styling.

#### Scenario: Partially completed day
- **WHEN** a day had four scheduled items and two were completed
- **THEN** that day renders in the "some completed" state

#### Scenario: Fully completed day
- **WHEN** every scheduled item for a day was completed
- **THEN** that day renders in the "all completed" state

#### Scenario: Day with nothing scheduled
- **WHEN** a day had no scheduled items
- **THEN** that day renders in the same neutral state as a day with nothing completed, with no
  distinct outline, colour, or warning treatment

### Requirement: The streak is forgiving and skips unscheduled days
The focus streak SHALL count any day on which at least one scheduled item was completed, and SHALL
skip days with nothing scheduled rather than treating them as breaks, mirroring the existing habit
streak behavior for days outside a habit's repeat days.

#### Scenario: Partial day continues the streak
- **WHEN** the previous day had three scheduled items and one was completed
- **THEN** the streak includes that day

#### Scenario: Unscheduled day does not break the streak
- **WHEN** a day between two completed days had nothing scheduled
- **THEN** the streak spans all three days

#### Scenario: Scheduled day with nothing completed breaks the streak
- **WHEN** a day had scheduled items and none were completed
- **THEN** the streak restarts after that day

### Requirement: Daily activity is persisted independently of task retention
The app SHALL record per-day scheduled counts, completed counts, and focus minutes in durable
storage so that summary history is unaffected by the deletion of completed tasks.

#### Scenario: History survives a retention purge
- **WHEN** completed-task retention deletes completed tasks older than its configured window
- **THEN** the affected days keep their recorded counts and continue to render their previous state

#### Scenario: Completion updates the day record
- **WHEN** an item is completed or un-completed
- **THEN** that day's completed count is updated to match

#### Scenario: State is derived, not stored
- **WHEN** the three-state thresholds are evaluated
- **THEN** the state is computed from the stored counts rather than read from a stored state field

### Requirement: Today's progress uses an expressive wavy progress indicator
Today's completion SHALL be shown with a determinate `CircularWavyProgressIndicator` whose amplitude
follows the Material 3 default, so the indicator is flat when the day has not started, wavy while
the day is in progress, and flat again when every scheduled item is complete.

#### Scenario: Day not started
- **WHEN** no scheduled item has been completed today
- **THEN** the indicator shows a track with no completed arc and no wave

#### Scenario: Day complete
- **WHEN** every scheduled item for today is completed
- **THEN** the indicator shows a closed ring with no wave

### Requirement: Daily activity is included in backup
Backup export and import SHALL carry daily activity records, and imports of exports written before
this change SHALL succeed with no recorded history.

#### Scenario: Round trip preserves history
- **WHEN** a backup containing daily activity is exported and imported
- **THEN** the restored history renders the same day states as before the export

#### Scenario: Older export without daily activity
- **WHEN** a backup produced before this change is imported
- **THEN** the import succeeds and the summary starts from an empty history
