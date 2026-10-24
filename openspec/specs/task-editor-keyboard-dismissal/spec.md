# task-editor-keyboard-dismissal Specification

## Purpose
Define how the task editor prioritizes keyboard dismissal over sheet dismissal while preserving field focus, sheet stability, entered content, and adaptive placement.

## Requirements

### Requirement: Task editor keyboard dismissal settles in one attempt
The system SHALL route a back action to keyboard dismissal when, and only when, a task editor title or description field owns focus as the back action begins, and SHALL keep the keyboard hidden after that dismissal until the user focuses a field again.

#### Scenario: Dismiss keyboard from a multiline description
- **WHEN** a user completes a back action while the task description field is focused
- **THEN** editor focus is cleared once, the keyboard closes, and it does not reopen without a new user focus request

#### Scenario: Dismiss keyboard from a task title
- **WHEN** a user completes a back action while the task title field is focused
- **THEN** editor focus is cleared once, the keyboard closes, and it does not reopen without a new user focus request

#### Scenario: Keyboard state changes during a routed back gesture
- **WHEN** keyboard visibility changes during a back gesture while a task editor field keeps focus
- **THEN** routing continues to follow field focus and the gesture is not reinterpreted as sheet dismissal

### Requirement: Task sheet remains stable while the keyboard closes
The system SHALL keep the task editor visible, anchored, and populated while a focused field consumes a back action, and SHALL keep the keyboard open while focus moves between task fields.

#### Scenario: Keyboard closes from a focused editor
- **WHEN** a completed back action clears task editor focus and the keyboard closes
- **THEN** the task sheet remains visible in its established placement and retains the entered title and description

#### Scenario: Focus moves between task fields
- **WHEN** focus moves title → description → title repeatedly
- **THEN** the destination field gains focus each time, the keyboard stays open, and focus is never cleared by the back-routing logic

#### Scenario: Predictive gesture begins with the keyboard visible
- **WHEN** predictive-back progress arrives while a task editor field owns focus and the keyboard is visible
- **THEN** the system consumes that progress without moving or dismissing the sheet

#### Scenario: Keyboard-routed gesture is cancelled
- **WHEN** a predictive-back gesture that began while a task editor field owned focus is cancelled
- **THEN** field focus, keyboard state, sheet position, and entered content are unchanged

### Requirement: Sheet dismissal follows keyboard dismissal
The system SHALL apply the task sheet's existing guarded back behavior whenever no task editor field owns focus.

#### Scenario: Invoke back after the keyboard stays hidden
- **WHEN** the task editor is open, editor focus has been cleared so the keyboard stays hidden, and the user invokes back
- **THEN** the editor follows its existing predictive animation and save, discard-confirmation, or dismissal behavior

#### Scenario: Other sheets are unaffected
- **WHEN** a habit, category, sort, or other non-task sheet is open with a focused field
- **THEN** back follows that sheet's existing behavior without task-editor focus routing

### Requirement: Adaptive task editor placement is unchanged
The system SHALL apply focus-based keyboard dismissal consistently to new-task and edit-task editors without changing task editor placement or sizing across supported window classes.

#### Scenario: Use compact or medium modal placement
- **WHEN** the task editor is shown as a modal sheet on a compact or medium window
- **THEN** keyboard dismissal does not change the modal sheet's established placement

#### Scenario: Use expanded docked placement
- **WHEN** the task editor is shown as a docked pane on an expanded window
- **THEN** keyboard dismissal does not change the docked pane layout
