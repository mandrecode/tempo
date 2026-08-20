## ADDED Requirements

### Requirement: Compact collapsed task description
The task card SHALL display a non-empty task description on at most one line while the description is collapsed and SHALL indicate visual overflow with an ellipsis.

#### Scenario: Long description is initially collapsed
- **WHEN** a task card renders a description that exceeds the available single-line width
- **THEN** the card displays one ellipsized description line and provides an expand action

### Requirement: Expandable task description
The task card SHALL allow an overflowing collapsed description to expand to display its full text and SHALL allow the expanded description to collapse again.

#### Scenario: User expands an overflowing description
- **WHEN** the user activates the expand action on a task card with an overflowing description
- **THEN** the task card displays the full multi-line description and changes the action to collapse

#### Scenario: User collapses an expanded description
- **WHEN** the user activates the collapse action on an expanded task description
- **THEN** the task card returns the description to at most one ellipsized line

### Requirement: Stable top-aligned task controls
The task card SHALL keep its completion control, primary content region, and trailing action region top-aligned when the task description changes between collapsed and expanded states.

#### Scenario: Description expansion preserves control alignment
- **WHEN** the user expands an overflowing task description
- **THEN** the completion control and trailing actions remain aligned with the top of the primary task content instead of shifting toward the card's vertical center
