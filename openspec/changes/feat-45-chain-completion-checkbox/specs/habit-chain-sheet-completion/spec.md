## ADDED Requirements

### Requirement: Habit chain title checkbox reflects chain identity and completion
When the habit bottom sheet is editing an existing habit chain, the sheet SHALL render a completion checkbox next to the title field, using the same visual component as the single-habit title checkbox. The checkbox SHALL display the chain's icon (or the in-progress selected icon from the form, if set) and the chain's resolved color (or the in-progress selected color from the form, if set). The checkbox's checked state SHALL be true only when every habit belonging to the chain has a completion entry for the selected date, and false otherwise (including when the chain has no member habits).

#### Scenario: Chain checkbox shown while editing an existing chain
- **WHEN** the user opens the bottom sheet to edit an existing habit chain
- **THEN** a completion checkbox is rendered next to the chain title, showing the chain's icon and color

#### Scenario: Checkbox reflects all-completed state
- **WHEN** every habit in the chain has a completion entry for the selected date
- **THEN** the chain title checkbox renders in its completed (checked) state

#### Scenario: Checkbox reflects partially or fully incomplete state
- **WHEN** at least one habit in the chain does not have a completion entry for the selected date
- **THEN** the chain title checkbox renders in its uncompleted (unchecked) state

#### Scenario: No checkbox while creating a new chain or editing a habit
- **WHEN** the bottom sheet is creating a brand-new chain (no `editingHabitChain`) or is on the single-habit tab
- **THEN** the chain title checkbox is not rendered; the existing title-only or single-habit-checkbox layout is used instead

### Requirement: Tapping the chain title checkbox bulk-toggles member habit completion
Tapping the chain title checkbox, when enabled, SHALL toggle completion for the selected date on every habit that belongs to the chain: if the chain is not fully completed, every member habit SHALL be marked completed; if the chain is fully completed, every member habit SHALL be marked incomplete. Each member habit's completion SHALL be updated through the existing single-habit completion toggle path, and only habits whose current state differs from the target state SHALL be toggled.

#### Scenario: Tapping an incomplete chain checkbox completes all member habits
- **WHEN** the user taps the chain title checkbox while the chain is not fully completed for the selected date
- **THEN** every member habit that was not already completed for that date becomes completed for that date

#### Scenario: Tapping a fully completed chain checkbox uncompletes all member habits
- **WHEN** the user taps the chain title checkbox while every member habit is completed for the selected date
- **THEN** every member habit becomes incomplete for that date

#### Scenario: Already-matching habits are not redundantly toggled
- **WHEN** the chain title checkbox is tapped and some member habits already match the target completion state
- **THEN** those habits are not toggled again; only habits differing from the target state have their completion changed

### Requirement: Chain title checkbox respects the existing toggle window and empty-chain guard
The chain title checkbox SHALL be disabled (non-interactive) when the selected date is neither today nor yesterday, matching the toggle window enforced for individual habit and chain-member checkboxes elsewhere in the app. The checkbox SHALL also be disabled when the chain currently has zero member habits.

#### Scenario: Checkbox disabled outside the toggle window
- **WHEN** the selected date in the bottom sheet is older than yesterday or in the future
- **THEN** the chain title checkbox is rendered disabled and does not respond to taps

#### Scenario: Checkbox disabled for an empty chain
- **WHEN** the chain being edited currently has no member habits
- **THEN** the chain title checkbox is rendered disabled and does not respond to taps
