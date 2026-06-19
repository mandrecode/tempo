## ADDED Requirements

### Requirement: Full-capacity history row remains stable on completion toggle
The system SHALL keep the habit history dot row visually stable when a completion toggle updates the streak label in constrained/full-capacity layouts.

#### Scenario: Toggling completion in a full-capacity row
- **WHEN** the history row is rendered at dot capacity and the user marks the habit complete (or incomplete) from the bottom sheet
- **THEN** the dot layout does not jump or shift due to transient streak-label size changes
- **AND** truncation state (including ellipsis presence) does not flip solely because of that transition

### Requirement: Bottom-sheet row alignment remains unchanged
The system SHALL preserve the existing right-edge alignment behavior of the streak pill in bottom-sheet row context.

#### Scenario: History row inside habit bottom sheet composition
- **WHEN** `HabitHistoryView` is rendered in the icon + weighted-column row used by `HabitBottomSheet`
- **THEN** the streak pill remains flush with the row end as established by #681

### Requirement: Preview surfaces cover the regression case
The system SHALL provide debug previews that explicitly show the full-capacity before/after toggle states used to validate this bug fix.

#### Scenario: Developer validates #687 visually
- **WHEN** a developer opens habit history previews in Android Studio
- **THEN** they can inspect paired full-row states (before and after completion toggle) without manually constructing runtime data
