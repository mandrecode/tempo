# editor-selector-groups Specification

## Purpose
Defines how the task and habit editors present single-choice options.

## Requirements

### Requirement: Single-choice options are connected button groups
The editors' single-choice options (task priority, periodicity, monthly day option, category; habit vs habit chain) SHALL be presented as connected toggle buttons in the Material 3 Expressive button-group style. The selected option SHALL be visibly emphasised, every option SHALL be at least 48 dp tall, and each SHALL be announced as a radio button.

#### Scenario: Priority
- **WHEN** the user opens the task editor on a 360 dp phone
- **THEN** High, Medium and Low are shown in full as one connected group filling the row

#### Scenario: Choose and clear priority
- **WHEN** the user taps Medium, then taps Medium again
- **THEN** Medium is selected in its priority colour, and the second tap clears the priority

### Requirement: Long option sets stay reachable
Option sets that can outgrow the row (categories, periodicity) SHALL remain horizontally scrollable, with every option reachable, and SHALL scroll the selected option into view.

#### Scenario: Many categories
- **WHEN** a task belongs to a category beyond the visible width
- **THEN** the row scrolls so that the selected category is visible, and the others are reachable by scrolling
