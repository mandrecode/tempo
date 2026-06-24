## ADDED Requirements

### Requirement: Editor title fields dismiss the keyboard on Enter
The system SHALL dismiss the software keyboard when the user presses Enter or the IME Done action while focused in an editor title field.

#### Scenario: Pressing Done in the task title field
- **WHEN** the task editor is open and the task title field has focus
- **AND** the user presses the IME Done action or Enter key
- **THEN** the software keyboard is dismissed
- **AND** the title remains unchanged except for existing title-length handling

#### Scenario: Pressing Done in the routine title field
- **WHEN** the habit or habit-chain editor is open and the title field has focus
- **AND** the user presses the IME Done action or Enter key
- **THEN** the software keyboard is dismissed
- **AND** the title remains unchanged except for existing title-length handling

### Requirement: Editor title fields use single-line input mode
The system SHALL present task, habit, and habit-chain editor title fields as single-line title inputs so Android IMEs dispatch Done instead of treating Enter as a multiline line break.

#### Scenario: Editing a long task title
- **WHEN** the task editor title reaches a long value within the title limit
- **THEN** the field remains a single-line title input

#### Scenario: Editing a long routine title
- **WHEN** the habit or habit-chain editor title reaches a long value within the title limit
- **THEN** the field remains a single-line title input
