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

### Requirement: The task editor offers a section choice for sectioned categories
When the task's category has at least one section, the task editor SHALL show a Section option set under the category options. It SHALL be a connected single-choice button group of "No section" followed by the category's sections in order, horizontally scrollable when long, and scrolled to the selected option. The editor SHALL NOT show it for a category without sections, or for a subtask.

#### Scenario: Pick a section
- **WHEN** the user edits a task in "S&V" and taps "Cat" in the Section options
- **THEN** after saving, the task is in the "Cat" section

#### Scenario: Category without sections
- **WHEN** the user edits a task in "Work", which has no sections
- **THEN** no Section options are shown

#### Scenario: Changing category resets the section
- **WHEN** in the editor the user switches a task in "Cat" of "S&V" to "Work" and back to "S&V"
- **THEN** the Section options show "No section" selected
