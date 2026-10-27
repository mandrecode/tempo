## MODIFIED Requirements

### Requirement: Task editor keyboard dismissal settles in one attempt
The system SHALL route a back action to keyboard dismissal when, and only when, a task editor title or description field owns focus and the keyboard is visible as the back action begins, and SHALL keep the keyboard hidden after that dismissal until the user focuses a field again.

#### Scenario: Dismiss keyboard from a multiline description
- **WHEN** a user completes a back action while the task description field is focused
- **THEN** editor focus is cleared once, the keyboard closes, and it does not reopen without a new user focus request

#### Scenario: Dismiss keyboard from a task title
- **WHEN** a user completes a back action while the task title field is focused
- **THEN** editor focus is cleared once, the keyboard closes, and it does not reopen without a new user focus request

#### Scenario: Keyboard state changes during a routed back gesture
- **WHEN** keyboard visibility changes during a back gesture while a task editor field keeps focus
- **THEN** routing continues to follow field focus and the gesture is not reinterpreted as sheet dismissal

### Requirement: Sheet dismissal follows keyboard dismissal
The system SHALL apply the task sheet's existing guarded back behavior whenever no task editor field owns focus or the keyboard is already hidden.

#### Scenario: Invoke back after the keyboard stays hidden
- **WHEN** the task editor is open, editor focus has been cleared so the keyboard stays hidden, and the user invokes back
- **THEN** the editor follows its existing predictive animation and save, discard-confirmation, or dismissal behavior

#### Scenario: Invoke back after the keyboard closed on its own
- **WHEN** a task editor field keeps focus but the keyboard has already closed (its own hide key, or a back the keyboard consumed)
- **THEN** a single back follows the editor's existing predictive animation and save, discard-confirmation, or dismissal behavior, without first clearing focus

#### Scenario: Other sheets are unaffected
- **WHEN** a habit, category, sort, or other non-task sheet is open with a focused field
- **THEN** back follows that sheet's existing behavior without task-editor focus routing
