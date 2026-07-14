## ADDED Requirements

### Requirement: Responsive description input
The task and habit editors SHALL limit per-keystroke work to the actively changing description path so that unrelated form sections do not require recomposition during description input.

#### Scenario: Edit a long task description
- **WHEN** the user changes a task description containing a large amount of text
- **THEN** the editor updates the plain text and selection without rebuilding URL annotations for the editable value
- **AND** read-only task descriptions continue to recognize supported links

#### Scenario: Edit a habit description
- **WHEN** the user changes a habit description
- **THEN** unrelated habit configuration sections remain eligible for Compose skipping

### Requirement: Debounced description autosave
The task and habit editors SHALL autosave distinct form snapshots after the configured quiet period without restarting the collector for every individual edit.

#### Scenario: Type several characters continuously
- **WHEN** multiple description changes occur within the 350 ms debounce interval
- **THEN** only the latest distinct valid form snapshot is dispatched after input becomes idle

#### Scenario: Dismiss before the debounce completes
- **WHEN** the sheet is dismissed with a valid pending snapshot
- **THEN** the latest snapshot is flushed using the existing dismissal behavior

### Requirement: Idempotent form error clearing
Task and habit form state SHALL remain unchanged when a description edit requests error clearing and no form errors are present.

#### Scenario: Type with no validation errors
- **WHEN** the editor requests error clearing while the current error state is empty
- **THEN** the form-state holder retains the current state without publishing a replacement
