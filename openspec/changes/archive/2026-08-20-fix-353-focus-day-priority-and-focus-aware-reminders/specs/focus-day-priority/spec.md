# focus-day-priority Specification

## ADDED Requirements

### Requirement: Up next offers today's work before overdue work
The Up next shortlist SHALL draw its candidates from the Today section first, and SHALL fall back to
the Overdue section only once the Today section has no further uncompleted task to offer. Within each
of those groups the shortlist SHALL keep the order that group is already listed in, so the row never
disagrees with the list beneath it.

#### Scenario: Today's task outranks an older one
- **WHEN** one task is overdue from yesterday and one task is due today, and neither is completed
- **THEN** the first card in Up next is the task due today

#### Scenario: Overdue work fills the remaining places
- **WHEN** two tasks are due today and four are overdue, and the shortlist holds five cards
- **THEN** the first two cards are today's tasks and the next three are overdue tasks, in the order
  the Overdue section lists them

#### Scenario: Nothing is due today
- **WHEN** no uncompleted task is due today and an overdue task exists
- **THEN** the overdue task is offered in Up next

#### Scenario: Everything due today is done
- **WHEN** every task due today is completed and an overdue task is still open
- **THEN** the overdue task is offered in Up next, because completed work is never a candidate

### Requirement: An Up next card carried over from a previous day says so instead of showing a time
Because the shortlist mixes today's work with what is left over from before it, a card for a task
whose due date is earlier than today SHALL show an "Overdue" label in place of its due time, using
the same word the Tasks tab groups such work under. A card due today SHALL keep showing its time.

#### Scenario: Overdue card
- **WHEN** an Up next card is for a task that was due yesterday at 08:00
- **THEN** its metadata line reads "OVERDUE" where the time would be, and does not show 08:00

#### Scenario: Today's card is unchanged
- **WHEN** an Up next card is for a task due today at 09:00
- **THEN** its metadata line shows 09:00 in the user's 12h/24h preference

#### Scenario: The rest of the line is unaffected
- **WHEN** an overdue Up next card's task has a priority and a category
- **THEN** the priority and category still appear ahead of the "OVERDUE" label

### Requirement: The agenda reads Up next, then Today, then Overdue
The Focus agenda SHALL render its sections in the order Up next, Today, Overdue. Both the Today and
the Overdue section SHALL keep their own header carrying their item count, and the Overdue section
SHALL keep every item it holds today — it is demoted below Today, not hidden or trimmed.

#### Scenario: All sections populated
- **WHEN** there is a non-empty Up next row, seven items due today, and one overdue task
- **THEN** the sections render in the order Up next, "Today · 7", "Overdue · 1"

#### Scenario: Nothing overdue
- **WHEN** no items are overdue
- **THEN** the Overdue section and its header are omitted entirely and Today is the only section

#### Scenario: Nothing due today
- **WHEN** nothing is due today and two tasks are overdue
- **THEN** the Today section and its header are omitted and "Overdue · 2" follows Up next directly

### Requirement: Ordering within a section is unchanged
Within a section, items SHALL remain ordered with uncompleted work first, then timed items in clock
order, then untimed items, with completed items last.

#### Scenario: Mixed section contents
- **WHEN** the Today section holds a task due at 12:30, an untimed habit, and a completed task
- **THEN** they are listed in the order task at 12:30, habit, completed task

### Requirement: Subtasks are listed in their stored order
Wherever the Focus screen lists a task's subtasks, they SHALL be ordered by their stored sort order
and then by id — the same order the Tasks tab lists them in — rather than in the order the underlying
task query happens to return.

#### Scenario: Focus and Tasks agree
- **WHEN** a task's subtasks were created in the order "Draft", "Review", "Send"
- **THEN** the Focus agenda card lists them "Draft", "Review", "Send", matching the Tasks tab

#### Scenario: Equal sort orders fall back to id
- **WHEN** two subtasks share the same sort order
- **THEN** the one with the lower id is listed first
