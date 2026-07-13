## ADDED Requirements

### Requirement: Undo is offered for successful data deletions

The app SHALL offer a localized Undo snackbar action after successfully deleting an individual task, a category, completed tasks, an individual habit, or a habit chain. The action SHALL be associated with the exact deletion snapshot that produced the snackbar.

#### Scenario: Individual task is deleted

- **WHEN** the user confirms deletion of a task
- **THEN** the task and its deleted descendants are removed and an Undo snackbar is shown

#### Scenario: Category is deleted

- **WHEN** the user confirms deletion of a non-default category
- **THEN** the category and its tasks are removed and an Undo snackbar is shown

#### Scenario: Completed tasks are deleted

- **WHEN** the user confirms deletion of completed tasks in the selected category
- **THEN** all affected completed tasks are removed and an Undo snackbar is shown

#### Scenario: Habit is deleted

- **WHEN** the user confirms deletion of a habit
- **THEN** the habit is removed from persistence and its chains and an Undo snackbar is shown

#### Scenario: Habit chain is deleted without deleting habits

- **WHEN** the user confirms deletion of a habit chain while preserving its habits
- **THEN** the chain is removed, the current preserve-habit reminder behavior is applied, and an Undo snackbar is shown

#### Scenario: Habit chain and habits are deleted

- **WHEN** the user confirms deletion of a habit chain and chooses to delete its habits
- **THEN** the chain and selected habits are removed and an Undo snackbar is shown

### Requirement: Undo restores the complete deleted snapshot

Selecting Undo SHALL atomically restore all persisted records captured for that deletion with their original identifiers, values, hierarchy, ordering, completion data, and relationships.

#### Scenario: Task deletion is undone

- **WHEN** the user selects Undo for an individual task deletion
- **THEN** the task and every descendant deleted with it are restored with their original hierarchy and ordering

#### Scenario: Category deletion is undone

- **WHEN** the user selects Undo for a category deletion
- **THEN** the category and all of its deleted tasks are restored and the previously selected category is selected when it is valid

#### Scenario: Completed-task deletion is undone

- **WHEN** the user selects Undo for a completed-task deletion
- **THEN** every task deleted by that bulk action is restored with its completion state and parent relationship

#### Scenario: Habit deletion is undone

- **WHEN** the user selects Undo for a habit deletion
- **THEN** the habit, completion history, reminder data, and ordered membership in every affected chain are restored

#### Scenario: Preserve-habits chain deletion is undone

- **WHEN** the user selects Undo for a chain deletion that preserved its habits
- **THEN** the chain and ordered membership are restored and the habits' reminder state is restored to its pre-deletion value

#### Scenario: Delete-habits chain deletion is undone

- **WHEN** the user selects Undo for a chain deletion that also deleted habits
- **THEN** the chain, deleted habits, their completion histories, and all memberships affected by the deletion are restored

### Requirement: Database mutations are atomic and scheduler work is separated

Snapshot capture and deletion SHALL occur in one Room transaction, restoration SHALL occur in a separate Room transaction, and Android reminder scheduling or cancellation MUST occur only after the corresponding database transaction commits.

#### Scenario: Deletion transaction fails

- **WHEN** snapshot capture or any database delete operation fails
- **THEN** the transaction is rolled back, no success Undo snackbar is shown, and no deletion scheduler side effect is started

#### Scenario: Restore transaction fails

- **WHEN** any database record in a deletion snapshot cannot be restored
- **THEN** the restore transaction is rolled back and the app reports that Undo failed without leaving a partially restored snapshot

#### Scenario: Reminder restoration fails after data restore

- **WHEN** persisted data is restored but one or more reminder schedules fail
- **THEN** the restored data remains authoritative and the app reports the reminder failure without deleting the restored records again

### Requirement: Reminder reconciliation is stable and idempotent

After Undo commits restored data, the app SHALL reconcile reminders from that snapshot using the existing domain scheduler interfaces and stable restored identifiers. Repeating reconciliation MUST NOT create duplicate active reminders.

#### Scenario: Future reminder is restored

- **WHEN** an undone snapshot contains an eligible future task, habit, or chain reminder
- **THEN** the app schedules it using the restored record's original identifier

#### Scenario: Ineligible reminder is restored

- **WHEN** an undone snapshot has no reminder or its reminder is past, completed, or otherwise ineligible under normal scheduling rules
- **THEN** the app does not create an active reminder for that record

### Requirement: Undo lifetime follows its snackbar

A deletion snapshot SHALL remain eligible for Undo until its associated snackbar action is selected or the snackbar is dismissed, and SHALL then be discarded. Undo history SHALL NOT survive process death.

#### Scenario: Multiple deletion snackbars are queued

- **WHEN** more than one deletion occurs before earlier snackbar feedback finishes
- **THEN** each snackbar action targets only its own tokenized deletion snapshot

#### Scenario: Undo snackbar is dismissed

- **WHEN** the Undo snackbar expires or is dismissed without action
- **THEN** its deletion snapshot is discarded and cannot be restored through a later snackbar action

#### Scenario: Process ends during undo window

- **WHEN** the app process ends while an Undo snackbar is active
- **THEN** the transient deletion snapshot and its undo opportunity are not restored on the next process start

### Requirement: Non-deletion feedback remains non-reversible

The app SHALL NOT show Undo for errors, informational feedback, reminder-clearing actions, completion changes, or edits that are outside the defined data-deletion flows.

#### Scenario: Reminders are cleared

- **WHEN** the user confirms clearing reminders
- **THEN** the app shows the existing completion feedback without an Undo action

#### Scenario: Deletion fails

- **WHEN** a destructive operation fails before producing a committed deletion snapshot
- **THEN** the app shows error feedback without an Undo action
