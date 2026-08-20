## ADDED Requirements

### Requirement: Sort options apply a deterministic tie-break chain

The Tasks list SHALL order active tasks by the chain below for the selected sort option, falling
through to the next criterion only when the previous one compares equal. Manual order is the
final criterion in every chain, so the resulting order is total and stable across reloads.

| Sort option | Tie-break chain |
|:---|:---|
| `Date` | reminder datetime ascending (undated last) → priority (unprioritised last) → manual order |
| `Priority` | priority (unprioritised last) → reminder datetime ascending (undated last) → manual order |
| `Title` | title case-insensitive ascending → manual order |
| `Manual` | manual order |

#### Scenario: Equal priority is broken by the closer reminder

- **WHEN** the list is sorted by `Priority` and two active tasks are both `HIGH`, one due today and one due next month
- **THEN** the task due today is listed above the task due next month

#### Scenario: Equal reminder datetime is broken by priority

- **WHEN** the list is sorted by `Date` and two active tasks share the exact same reminder datetime, one `HIGH` and one `LOW`
- **THEN** the `HIGH` task is listed above the `LOW` task

#### Scenario: Undated and unprioritised tasks sort last within their criterion

- **WHEN** the list is sorted by `Priority` and an undated `HIGH` task and a dated `HIGH` task are both present
- **THEN** the dated task is listed above the undated task

#### Scenario: Fully tied tasks fall back to manual order

- **WHEN** the list is sorted by `Priority` and two active tasks share the same priority and the same reminder datetime
- **THEN** they are listed in ascending manual order relative to each other, in the same order after every reload

### Requirement: Fully tied active tasks form a reorderable run

Consecutive active tasks within a rendered group that compare equal on every criterion the
active sort applies before manual order SHALL form a reorderable run. A run of two or more
tasks SHALL be reorderable by long-press drag; a task that no other task ties with SHALL NOT be
draggable.

#### Scenario: Two tasks tied on priority and date become draggable

- **WHEN** the list is sorted by `Priority` and two active tasks share the same priority and the same reminder datetime
- **THEN** both tasks are long-press draggable

#### Scenario: A task distinguished by the sort is not draggable

- **WHEN** the list is sorted by `Priority` and a `HIGH` task is the only task with its priority-and-date combination
- **THEN** that task is not long-press draggable

#### Scenario: Undated unprioritised tasks are all tied

- **WHEN** the list is sorted by `Priority` and three active tasks have no priority and no reminder date
- **THEN** all three form one reorderable run and can be dragged into any order among themselves

#### Scenario: Manual sort treats every active task as tied

- **WHEN** the list is sorted by `Manual`
- **THEN** all active tasks form a single reorderable run, preserving the existing full-list drag-and-drop behaviour

### Requirement: Drags are confined to the dragged task's run

A drag SHALL only move a task to a position within its own reorderable run. The sort criteria
SHALL always take precedence over manual order, so a drag SHALL NOT move a task across a group
boundary or past a task the active sort can distinguish it from.

#### Scenario: Dragging beyond the run clamps to the run

- **WHEN** the user drags a task past the last task of its reorderable run
- **THEN** the task lands at the last position of that run and no task outside the run changes position

#### Scenario: Group membership is unchanged by a drag

- **WHEN** the user reorders tasks inside a run belonging to the `HIGH` priority group
- **THEN** every task in the run remains under the `HIGH` priority group header

### Requirement: Manual order from a drag is persisted

A completed drag SHALL persist the new manual order of the run's tasks so that it is applied
whenever the tie-break chain reaches manual order, including after switching sort options,
leaving the screen, or restarting the app.

#### Scenario: Order survives a sort switch round trip

- **WHEN** the user reorders a tied run under `Priority` sort, switches to `Date` sort, and switches back
- **THEN** the run's tasks are shown in the order the user dragged them into

#### Scenario: Order survives an app restart

- **WHEN** the user reorders a tied run and the app is restarted
- **THEN** the run's tasks are shown in the order the user dragged them into

### Requirement: Reordering preserves the manual order of tasks outside the run

Persisting a reorder SHALL redistribute only the manual-order values already held by the
reordered tasks among themselves, leaving every other task's manual-order value untouched, so
that reordering a subset never changes the relative order of tasks outside it.

#### Scenario: Reordering a tie run leaves the Manual list otherwise intact

- **WHEN** the user reorders a tied run under `Priority` sort and then switches to `Manual` sort
- **THEN** the run's tasks appear in the dragged order and every other task keeps its previous position in the list

#### Scenario: Reordering subtasks does not disturb parent tasks

- **WHEN** the user reorders the subtasks of a parent task
- **THEN** no task outside that subtask list changes position

### Requirement: Completed tasks are not reorderable

Completed tasks SHALL NOT form reorderable runs and SHALL NOT be draggable in any sort option.

#### Scenario: Completed tasks cannot be dragged

- **WHEN** the completed section is expanded and it contains two tasks completed at the same time with the same priority
- **THEN** neither task is long-press draggable
