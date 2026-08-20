## ADDED Requirements

### Requirement: Task editor keyboard dismissal settles in one attempt
The system SHALL keep the software keyboard hidden after a user dismisses it from a focused task title or description field.

#### Scenario: Dismiss keyboard from a multiline description
- **WHEN** a user invokes the system keyboard-dismiss action while a multiline task description is focused
- **THEN** the keyboard closes after that action and does not reopen without a new user focus request

#### Scenario: Dismiss keyboard from a task title
- **WHEN** a user invokes the system keyboard-dismiss action while the task title is focused
- **THEN** the keyboard closes after that action and does not reopen without a new user focus request

### Requirement: Task sheet remains stable while the keyboard closes
The system SHALL keep the task editor visible, anchored, and populated while software-keyboard dismissal settles.

#### Scenario: Keyboard closes from a focused editor
- **WHEN** the software keyboard transitions from visible to hidden in an open task editor
- **THEN** the task sheet remains visible in its established placement and retains the entered title and description

#### Scenario: Focus moves between task fields
- **WHEN** focus moves between the task title and description and the next keyboard target remains visible
- **THEN** the system preserves the destination field focus and does not treat the handoff as keyboard dismissal

### Requirement: Sheet dismissal follows keyboard dismissal
The system SHALL restore the task sheet's existing guarded back behavior after keyboard dismissal has settled.

#### Scenario: Invoke back after the keyboard stays hidden
- **WHEN** the task editor is open, the software keyboard has been dismissed, and the user invokes back again
- **THEN** the editor follows its existing save, discard-confirmation, or dismissal behavior

### Requirement: Adaptive task editor placement is unchanged
The system SHALL apply settled keyboard dismissal without changing task editor placement or sizing across supported window classes.

#### Scenario: Use compact or medium modal placement
- **WHEN** the task editor is shown as a modal sheet on a compact or medium window
- **THEN** keyboard dismissal settles without changing the modal sheet's established placement

#### Scenario: Use expanded docked placement
- **WHEN** the task editor is shown as a docked pane on an expanded window
- **THEN** keyboard dismissal settles without changing the docked pane layout
