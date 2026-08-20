## MODIFIED Requirements

### Requirement: Task editor keyboard dismissal settles in one attempt
The system SHALL keep the software keyboard hidden after a user dismisses it from a focused task title or description field, and SHALL decide keyboard-first routing from the state captured when that back action begins.

#### Scenario: Dismiss keyboard from a multiline description
- **WHEN** a user invokes the system keyboard-dismiss action while a multiline task description is focused
- **THEN** the keyboard closes after that action and does not reopen without a new user focus request

#### Scenario: Dismiss keyboard from a task title
- **WHEN** a user invokes the system keyboard-dismiss action while the task title is focused
- **THEN** the keyboard closes after that action and does not reopen without a new user focus request

#### Scenario: Keyboard state changes during a routed back gesture
- **WHEN** a back gesture begins while the keyboard is visible and keyboard visibility changes before the gesture completes
- **THEN** the system continues routing that gesture to keyboard dismissal and does not reinterpret it as sheet dismissal

### Requirement: Task sheet remains stable while the keyboard closes
The system SHALL keep the task editor visible, anchored, and populated while software-keyboard dismissal settles, without applying sheet predictive-back progress to a keyboard-routed gesture.

#### Scenario: Keyboard closes from a focused editor
- **WHEN** the software keyboard transitions from visible to hidden in an open task editor
- **THEN** the task sheet remains visible in its established placement and retains the entered title and description

#### Scenario: Focus moves between task fields
- **WHEN** focus moves between the task title and description and the next keyboard target remains visible
- **THEN** the system preserves the destination field focus and does not clear focus or treat the handoff as keyboard dismissal

#### Scenario: Predictive gesture begins with the keyboard visible
- **WHEN** predictive-back progress reaches the task sheet after the gesture began with the keyboard visible
- **THEN** the system consumes that progress without moving or dismissing the sheet

#### Scenario: Keyboard-routed gesture is cancelled
- **WHEN** a predictive-back gesture that began with the keyboard visible is cancelled
- **THEN** the system retains editor focus and leaves the task sheet unchanged
