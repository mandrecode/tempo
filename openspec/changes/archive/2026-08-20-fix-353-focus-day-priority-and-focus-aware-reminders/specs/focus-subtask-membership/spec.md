# focus-subtask-membership Specification

## ADDED Requirements

### Requirement: A dated subtask stands on its own when its parent is not on the day
A subtask SHALL appear as its own row in the Focus agenda — and therefore as a candidate for the Up
next shortlist and for a focus session — when it satisfies the agenda's membership rule (due today,
or overdue and still open) **and** its parent task does not itself satisfy that rule. When the parent
is on the day, the subtask SHALL NOT appear as its own row; it stays a step inside the parent's card
as before.

#### Scenario: Undated parent, dated subtask
- **WHEN** a task has no date and one of its subtasks is due today
- **THEN** the subtask appears as its own row in the Today section and can be started as a session

#### Scenario: Parent due today keeps its steps nested
- **WHEN** a task is due today and one of its subtasks is also due today
- **THEN** only the parent appears as a row, and the subtask is listed inside the parent's card

#### Scenario: Future-dated parent, overdue subtask
- **WHEN** a task is due next week — so it is not in the agenda — and one of its subtasks was due
  yesterday and is still open
- **THEN** the subtask appears as its own row in the Overdue section

#### Scenario: Undated subtasks never appear
- **WHEN** a task has no date and its subtasks have no date either
- **THEN** neither the task nor its subtasks appear as rows in the agenda

#### Scenario: A promoted subtask carries its own steps
- **WHEN** a promoted subtask has subtasks of its own
- **THEN** its row lists them the way any task card lists its subtasks

### Requirement: A promoted subtask is an ordinary agenda row
A subtask promoted to its own row SHALL render, sort, and behave exactly as any other task row of the
agenda — same card, same category and priority treatment, same completion and edit actions, same
placement by due time within its section. It SHALL NOT gain parent breadcrumbs, indentation, or a
badge marking it as a subtask.

#### Scenario: Sorted with everything else
- **WHEN** a promoted subtask is due at 09:00 and a top-level task is due at 12:30
- **THEN** the promoted subtask is listed above the top-level task in the Today section

#### Scenario: Completing from the agenda
- **WHEN** the user ticks a promoted subtask's checkbox in the Focus agenda
- **THEN** it is completed with the same behavior as ticking it in the Tasks tab

### Requirement: The day's counts follow the same membership rule
The day's scheduled and completed counts — which drive the Focus hero, its progress, and the history
heatmap — SHALL count exactly the rows the agenda shows: a subtask counts when, and only when, it is
promoted to its own row, and a subtask nested inside a counted parent is never counted separately.

#### Scenario: Promoted subtask counts once
- **WHEN** an undated task has two subtasks due today
- **THEN** the day's scheduled count includes those two subtasks and does not include the parent

#### Scenario: Nested subtasks do not inflate the day
- **WHEN** a task due today has five subtasks also due today
- **THEN** the day's scheduled count includes that task once and none of its subtasks

### Requirement: The undated footer still counts only top-level tasks
The "N tasks without a date" footer SHALL keep counting undated top-level tasks only, so promoting a
dated subtask never changes it and an undated subtask is never counted there.

#### Scenario: Undated subtask is not in the footer
- **WHEN** a task due today has three undated subtasks and there are two undated top-level tasks
- **THEN** the footer reports two tasks without a date
