## ADDED Requirements

### Requirement: Editor Done action dismisses text entry

The app SHALL hide the software keyboard and clear text-field focus when the user invokes the Done IME action from an active task title, habit title, habit-chain title, or category name field.

#### Scenario: Done from a task title

- **WHEN** the user invokes Done while the task title field is focused
- **THEN** the software keyboard is dismissed and the task title field loses focus
- **AND** the task editor remains open without submitting the form

#### Scenario: Done from a habit or habit-chain title

- **WHEN** the user invokes Done while a habit or habit-chain title field is focused
- **THEN** the software keyboard is dismissed and the title field loses focus
- **AND** the editor remains open without submitting the form

#### Scenario: Done from a category name

- **WHEN** the user invokes Done while the category name field is focused
- **THEN** the software keyboard is dismissed and the category name field loses focus
- **AND** the category editor remains open without saving the form
