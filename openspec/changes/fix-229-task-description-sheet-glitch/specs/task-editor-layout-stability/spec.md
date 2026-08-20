## ADDED Requirements

### Requirement: Task editor remains visible when hiding the keyboard
The system SHALL keep the active task editor sheet visible and stationary when a user hides the software keyboard after entering a multiline description.

#### Scenario: Hide keyboard while creating a task with a multiline description
- **WHEN** a user enters a description that wraps onto additional lines in the new-task editor and invokes back to hide the software keyboard
- **THEN** the keyboard closes without translating, dismissing, or temporarily removing the task editor sheet

#### Scenario: Hide keyboard while editing an existing task
- **WHEN** a user edits a multiline description and invokes back to hide the software keyboard
- **THEN** the keyboard closes while the task editor remains visible and retains the user's text

### Requirement: Sheet dismissal remains available after keyboard dismissal
The system SHALL restore the sheet's existing guarded back-dismiss behavior after the software keyboard is no longer visible.

#### Scenario: Invoke back after keyboard is hidden
- **WHEN** the task editor is visible, the software keyboard is hidden, and the user invokes back
- **THEN** the editor follows its existing save, discard-confirmation, or dismissal behavior

### Requirement: Adaptive task editor placements preserve behavior
The system SHALL apply keyboard-first back handling without changing task editor placement or sizing across supported window classes.

#### Scenario: Use compact or medium modal placement
- **WHEN** the task editor is shown as a modal sheet on a compact or medium window
- **THEN** keyboard dismissal remains stable and the sheet stays in its established placement

#### Scenario: Use expanded docked placement
- **WHEN** the task editor is shown as a docked pane on an expanded window
- **THEN** keyboard dismissal remains stable without changing the docked pane layout
