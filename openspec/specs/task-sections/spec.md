# task-sections Specification

## Purpose
Defines what a task section is: a named divider inside one category that structures a long list. Covers how tasks belong to a section, the section lifecycle, and how sections persist and travel through backups.

## Requirements

### Requirement: Sections are turned on per category
Each category SHALL have a Sections option in its edit sheet, off by default for new and existing categories. Turning it off SHALL show every task of the category as one list without changing which section each task belongs to; the sections and their tasks SHALL be back exactly as they were when it is turned back on.

#### Scenario: Turn on
- **WHEN** the user turns Sections on for "S&V" and saves
- **THEN** the "S&V" list offers "Add section"

#### Scenario: Turn off and back on
- **WHEN** the user turns Sections off for "S&V", which has sections "Cat" and "Shopping" holding tasks, then turns it back on
- **THEN** while off all its tasks show as one unsectioned list, and once on again "Cat" and "Shopping" are back holding the same tasks

### Requirement: A section belongs to exactly one category
A section SHALL have a name and a position, and SHALL belong to exactly one category. It SHALL NOT have a due date, priority, reminder, recurrence, completion state, icon, colour, default flag or sharing identity. Sections SHALL NOT contain other sections.

A section's name SHALL be unique within its category, ignoring case; creating or renaming a section to a name the category already has SHALL be refused with a message. Merging a backup into a category that has sections turned off SHALL still add its sections, which stay hidden until the category turns sections on.

#### Scenario: Section is not completable
- **WHEN** the user views a section in the Tasks list
- **THEN** it offers no completion control, and no date, priority or reminder can be set on it

#### Scenario: Sections are per category
- **WHEN** the user creates a section "Cat" in the "S&V" category and then selects the "Work" category
- **THEN** "Cat" is not shown in "Work"

### Requirement: A task belongs to at most one section of its own category
Each top-level task SHALL be either unsectioned or in exactly one section of the category it belongs to. A subtask SHALL be shown with its parent and SHALL NOT be placed in a section independently.

#### Scenario: Unsectioned by default
- **WHEN** the user creates a task from the add-task action or the widget
- **THEN** the task is unsectioned

#### Scenario: Created from a section
- **WHEN** the user taps the add-task action on the "Shopping" section header and saves a task
- **THEN** the task is in "Shopping"

### Requirement: Moving a task to another category clears its section
When a task's category changes, its section SHALL be cleared, because a section cannot follow a task out of its category.

#### Scenario: Category change
- **WHEN** a task in section "Cat" of "S&V" is moved to the "Work" category
- **THEN** the task is unsectioned in "Work"

### Requirement: Sections can be created, renamed and reordered
The user SHALL be able to create a section in the selected category, rename it, and change the order of sections within the category. Section names SHALL be non-blank after trimming. They need not be unique.

#### Scenario: Create
- **WHEN** the user adds a section named "Garden" to "S&V"
- **THEN** "Garden" appears as the last section of "S&V", empty

#### Scenario: Blank name rejected
- **WHEN** the user tries to save a section name made only of spaces
- **THEN** the section is not created or renamed

#### Scenario: Reorder
- **WHEN** the user moves "Shopping" above "Home chores"
- **THEN** "Shopping" is shown before "Home chores" in the list and the order survives an app restart

### Requirement: Empty sections persist
A section SHALL remain until the user deletes it, even when it has no tasks.

#### Scenario: Last task leaves
- **WHEN** the only task in "Garden" is moved out or deleted
- **THEN** "Garden" is still shown, empty

### Requirement: Deleting a section unsections its tasks with undo
Deleting a section SHALL move all of its tasks, completed ones included, to unsectioned in the same category, and SHALL offer the existing undo affordance. Undo SHALL restore the section, its position and every task's membership.

#### Scenario: Delete and undo
- **WHEN** the user deletes "Shopping", which has two tasks, and then taps Undo
- **THEN** after the delete both tasks are unsectioned, and after Undo "Shopping" is back in its place with both tasks in it

### Requirement: Deleting a category deletes its sections
Deleting a category SHALL delete its sections together with its tasks. Undoing the category deletion SHALL restore its sections and every task's section.

#### Scenario: Category undo
- **WHEN** the user deletes "S&V" and taps Undo
- **THEN** "S&V" returns with its sections and every task in the section it had

### Requirement: Section membership survives recurrence and completion
Completing or uncompleting a task SHALL NOT change its section, and the next occurrence spawned from a recurring task SHALL be in the same section as the task it came from.

#### Scenario: Recurring task
- **WHEN** the user completes a weekly task in section "Home chores"
- **THEN** its next occurrence is in "Home chores"

#### Scenario: Uncomplete
- **WHEN** the user uncompletes a completed task that was in "Cat"
- **THEN** it returns to the "Cat" section

### Requirement: Collapse state is remembered per section on the device
Each section SHALL remember whether it is collapsed. The state SHALL survive navigation and process death, SHALL be stored only on the device, and SHALL NOT be included in backups.

#### Scenario: Survives navigation
- **WHEN** the user collapses "Home chores", opens Routines, and returns to Tasks
- **THEN** "Home chores" is still collapsed

### Requirement: Backups carry sections
A backup SHALL include every section and each task's section. Restoring or merging a backup SHALL recreate them and reassign them to the restored categories and tasks. A backup written before sections existed SHALL import unchanged, with every task unsectioned.

#### Scenario: Round trip
- **WHEN** the user exports a backup and restores it on a fresh install
- **THEN** every category has the same sections in the same order, and every task is in the same section

#### Scenario: Older backup
- **WHEN** the user imports a backup file written by a version without sections
- **THEN** the import succeeds and every task is unsectioned

#### Scenario: Merge import
- **WHEN** the user merges a backup into an install that already has data
- **THEN** imported sections attach to the imported categories, and imported tasks to the imported sections, without touching existing sections
