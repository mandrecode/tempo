## ADDED Requirements

### Requirement: Continue task description dash lists
The task editor SHALL automatically insert the current dash-list prefix when a user inserts a newline immediately after a non-empty dashed description line.

#### Scenario: Continue an unindented dash list
- **WHEN** the user presses Enter after a task description line beginning with `- ` and containing item text
- **THEN** the editor inserts `- ` at the start of the new line and places the cursor after the inserted prefix

#### Scenario: Continue an indented dash list
- **WHEN** the user presses Enter after a task description line beginning with indentation followed by `- ` and containing item text
- **THEN** the editor inserts the same indentation and dash prefix at the start of the new line

#### Scenario: Preserve ordinary multiline editing
- **WHEN** a task description edit is not a single newline insertion after a non-empty dashed line
- **THEN** the editor preserves the proposed text and selection without automatic list formatting

#### Scenario: Insert a newline immediately after a populated item's prefix
- **WHEN** the user presses Enter immediately after `- ` while non-blank item text remains later on the same line
- **THEN** the editor preserves the existing marker and continues the dash-list prefix before the remaining item text

### Requirement: Exit an empty task description dash item
The task editor SHALL remove an empty dash-list marker when the user presses Enter on that item instead of continuing another dashed line.

#### Scenario: End dash-list entry
- **WHEN** the user presses Enter on a line containing only optional indentation and `- `
- **THEN** the empty marker is removed and the cursor remains on an unformatted line after the preceding content
