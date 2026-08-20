## ADDED Requirements

### Requirement: Chain habit selector preserves first-row alignment
The system SHALL vertically align the habit-selection property icon with the first visible selector row in the habit-chain editor, regardless of whether the chain currently contains selected habits.

#### Scenario: No habits selected
- **WHEN** the user opens or creates a habit chain with no selected habits and available habits are displayed as selector chips
- **THEN** the habits property icon is vertically centered with the first selector-chip row

#### Scenario: Selected habits displayed
- **WHEN** the habit-chain editor displays one or more selected habit rows
- **THEN** the habits property icon remains vertically aligned with the first selected-habit row
