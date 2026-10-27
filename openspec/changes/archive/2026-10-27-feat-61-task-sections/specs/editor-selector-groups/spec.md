# Spec Delta

## ADDED Requirements

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
