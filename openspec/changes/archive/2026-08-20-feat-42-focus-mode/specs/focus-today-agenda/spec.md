# focus-today-agenda Specification

## ADDED Requirements

### Requirement: Focus shows only today's work and what is overdue
The Focus agenda SHALL include tasks due today, tasks overdue, habits scheduled today, and habit
chains scheduled today. It SHALL exclude items scheduled for future dates and tasks with no date.

#### Scenario: Future-dated task is excluded
- **WHEN** a task is due tomorrow
- **THEN** it does not appear in the Focus agenda

#### Scenario: Habit outside its repeat days is excluded
- **WHEN** a habit's repeat days do not include today
- **THEN** it does not appear in the Focus agenda

#### Scenario: Overdue task is included
- **WHEN** a task was due two days ago and is not completed
- **THEN** it appears in the Overdue section

#### Scenario: Agenda has no category or day-of-week chrome
- **WHEN** the Focus agenda is rendered
- **THEN** no category chip row and no day-of-week selector are shown, and each row carries its type
  and category as pills instead

### Requirement: The agenda is ordered Up next, then Overdue, then Today
Sections SHALL appear in the order Up next, Overdue, Today, with Overdue and Today introduced by
section headers carrying their item counts.

#### Scenario: All sections populated
- **WHEN** there is a ranked next item, one overdue task, and seven items due today
- **THEN** the sections render in the order Up next, "Overdue · 1", "Today · 7"

#### Scenario: Nothing overdue
- **WHEN** no items are overdue
- **THEN** the Overdue section and its header are omitted entirely

### Requirement: Up next surfaces a single ranked item
Up next SHALL show at most one item, chosen by priority first and due time second, and SHALL be the
only place in the agenda offering to start a focus session.

#### Scenario: Priority outranks due time
- **WHEN** a high-priority task is due at 17:00 and a task with no priority is due at 09:00
- **THEN** the high-priority task is shown in Up next

#### Scenario: Due time breaks ties within a priority
- **WHEN** two tasks share the same priority and are due at 09:00 and 12:30
- **THEN** the task due at 09:00 is shown in Up next

#### Scenario: Completed items are never ranked
- **WHEN** the highest-ranked item is completed
- **THEN** Up next advances to the next uncompleted item

### Requirement: Up next collapses when nothing qualifies
When no uncompleted item qualifies, the Up next card and its label SHALL be removed from the layout
rather than shown as an empty or placeholder card.

#### Scenario: Everything for today is done
- **WHEN** every item in the agenda is completed
- **THEN** the Up next card is not rendered and the Overdue or Today section moves to the top

#### Scenario: Only overdue work remains
- **WHEN** nothing is due today but an overdue task exists
- **THEN** the overdue task is eligible for Up next

### Requirement: Tasks, habits and chains interleave in one list
The agenda SHALL present tasks, habits and habit chains in a single list without separating them by
type, each row identifying its own type, and completion SHALL be actionable in place.

#### Scenario: Mixed section ordering
- **WHEN** the Today section contains a habit chain, a task and a habit
- **THEN** they appear in one list ordered by due time, each with a pill identifying its type

#### Scenario: Completing from the agenda
- **WHEN** the user completes a habit from the Focus agenda
- **THEN** the habit is marked complete using the same behavior as the Routines tab, and the Focus
  summary updates

### Requirement: Undated tasks are excluded but accounted for
Tasks with no date SHALL NOT appear in the agenda, and their count SHALL be shown as a footer
entry that navigates to the Tasks tab.

#### Scenario: Undated tasks exist
- **WHEN** twelve tasks have no date
- **THEN** a footer reports twelve undated tasks and opens the Tasks tab when tapped

#### Scenario: No undated tasks
- **WHEN** every task has a date
- **THEN** the footer is not shown

### Requirement: Focus adapts to wide windows
On windows at or above the medium breakpoint the Focus screen SHALL reserve the floating rail
clearance and cap its content at the shared readable width, consistent with Routines and Tasks.

#### Scenario: Medium window
- **WHEN** the window is at the medium breakpoint
- **THEN** the Focus content is offset by the rail clearance and does not underlap the rail

#### Scenario: Expanded window
- **WHEN** the window is at the expanded breakpoint
- **THEN** the summary and agenda are laid out as two panes, each capped at the readable width
