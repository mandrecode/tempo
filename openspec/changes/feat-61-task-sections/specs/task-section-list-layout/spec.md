# Spec Delta

## Purpose

Defines how sections appear in the Tasks list at every window width class: section headers and their states, how sections combine with sorting and the Completed block, moving tasks between sections, and the Large-and-up section shortcuts and two-column flow.

## ADDED Requirements

### Requirement: A category with sections off looks as it does today
When the selected category has sections turned off, the Tasks list SHALL render exactly as it did before sections existed, at every width class, even if the category holds hidden sections. No section header, shortcut group, placeholder or add-section action SHALL appear.

#### Scenario: Sections off
- **WHEN** the user selects a category with sections off, sorted by date
- **THEN** the list shows the existing date group headers and no section UI

#### Scenario: Sections on, none yet
- **WHEN** the user selects a category with sections on and no section yet
- **THEN** its tasks are listed unsectioned, followed by an "Add section" action

### Requirement: Empty sections stay quiet
An empty section's placeholder SHALL be a soft tint without an outline, and SHALL be highlighted only while a dragged task is over it.

#### Scenario: Idle and while dragging
- **WHEN** the "Garden" section is empty and the user drags a task over its placeholder
- **THEN** the placeholder is a borderless tint until the task is over it, when it is highlighted

### Requirement: Unsectioned tasks come first, then sections in order
In a category with sections, active unsectioned tasks SHALL be listed first, without a header. Each section SHALL follow in its order, introduced by its header.

#### Scenario: Order
- **WHEN** "S&V" has one unsectioned task and sections "Cat" and "Shopping"
- **THEN** the unsectioned task is shown first, then the "Cat" header and its tasks, then the "Shopping" header and its tasks

### Requirement: A section header shows its name, count and actions
Each section header SHALL show a collapse toggle, the section's name, the number of its active tasks, an action that adds a task to the section, and a menu (Rename, Reorder sections, Add section below, Delete section). Every control SHALL be at least 48 dp and have an accessible label.

#### Scenario: Add from header
- **WHEN** the user taps the add-task action on the "Cat" header
- **THEN** the task editor opens with "S&V" and "Cat" preselected

#### Scenario: Accessible header
- **WHEN** a screen reader focuses the "Home chores" header
- **THEN** it announces the name, the task count and whether the section is expanded or collapsed

### Requirement: Collapsed sections summarise what they hide
Collapsing a section SHALL hide its tasks and keep its header. While collapsed, the header SHALL show a badge when any hidden task is overdue.

#### Scenario: Overdue hidden
- **WHEN** "Home chores" is collapsed and one of its four tasks is overdue
- **THEN** its header shows "4" and a "1 overdue" badge

### Requirement: Empty sections show a placeholder
An expanded section with no active tasks SHALL show a placeholder inviting the user to add or drag a task into it.

#### Scenario: Empty
- **WHEN** the "Garden" section has no active tasks
- **THEN** its header is followed by the placeholder

### Requirement: Sections are the outer grouping under every sort
Under every sort option, tasks SHALL be grouped by section first. The date and priority group headers SHALL NOT be shown in a category with sections. Tasks inside a section SHALL follow the selected sort.

#### Scenario: Sort by date
- **WHEN** the user sorts "S&V", which has sections, by date
- **THEN** each section lists its tasks by date, and no date group header is shown

### Requirement: Completed tasks stay in one block
Completed tasks SHALL remain in the single existing Completed block below all sections, regardless of their section.

#### Scenario: Completing moves to the block
- **WHEN** the user completes a task in "Cat"
- **THEN** it leaves "Cat" and appears in the Completed block, and "Cat"'s count drops by one

### Requirement: Tasks move between sections by drag in Manual sort
In Manual sort, the user SHALL be able to drag a task into a different section or out to unsectioned. Dropping on a collapsed section's header SHALL append the task to that section. The target SHALL be visibly highlighted during the drag.

#### Scenario: Drag into another section
- **WHEN** in Manual sort the user drags "Cat food" from "Shopping" and drops it between the tasks of "Cat"
- **THEN** "Cat food" is in "Cat" at the drop position

#### Scenario: Drop on collapsed header
- **WHEN** the user drops a task on the collapsed "Home chores" header
- **THEN** the task is appended to "Home chores", which stays collapsed and shows the higher count

### Requirement: Sections can be reordered without dragging the list
The section menu's Reorder sections action SHALL open a dedicated surface listing the category's sections, where the user can reorder them by drag. On Large and up, the same reorder surface SHALL also be reachable from the section shortcut group.

#### Scenario: Reorder from menu
- **WHEN** the user opens Reorder sections on a phone and drags "Shopping" to the top
- **THEN** after closing, "Shopping" is the first section in the list

### Requirement: Renaming happens in place
Choosing Rename SHALL turn the header's name into an editable field that saves on confirm and discards on dismiss.

#### Scenario: Rename
- **WHEN** the user renames "Shopping" to "Shopping list" and confirms
- **THEN** the header reads "Shopping list"

### Requirement: The current section header stays pinned on short windows
When the window is shorter than 480 dp, the header of the section at the top of the viewport SHALL stay pinned to the top of the list. The next section's header SHALL push it out as it arrives.

#### Scenario: Phone landscape
- **WHEN** the user scrolls through "Home chores" on a phone in landscape
- **THEN** the "Home chores" header stays visible at the top until the "Shopping" header reaches it

### Requirement: Large and up shows a section shortcut group
At Large width and up, a category with sections SHALL show a connected single-choice button group under the category row. It SHALL hold one segment per section plus "No section" when unsectioned tasks exist, each with its count, followed by separate Add section and Reorder sections buttons.

#### Scenario: Jump to section
- **WHEN** on a tablet in landscape the user taps the "Shopping" segment
- **THEN** the list scrolls so that the "Shopping" header is at the top, and the "Shopping" segment is checked

#### Scenario: Not shown below Large
- **WHEN** the window is Expanded width or narrower
- **THEN** no section shortcut group is shown

### Requirement: The checked shortcut follows scrolling and the category colour
The checked segment SHALL be the section whose header is at or nearest above the top of the viewport. It SHALL be filled with the selected category's colour. When the selected category changes, the group SHALL animate in for the new category.

#### Scenario: Scroll spy
- **WHEN** the user scrolls the list until "Home chores" reaches the top
- **THEN** the "Home chores" segment becomes the checked one

### Requirement: Sections flow into two columns when the list is wide
When the list pane is at least 900 dp wide and the selected category has sections, sections SHALL be laid out in two columns. Each section SHALL stay whole within one column, in order, and the Completed block SHALL span both columns below them. A docked editor that narrows the pane below 900 dp SHALL return the list to one column.

#### Scenario: Tablet landscape
- **WHEN** a 1280 dp window shows "S&V" with no editor docked
- **THEN** sections are split across two columns, each section whole in one column

#### Scenario: Editor docks
- **WHEN** the user opens a task and the editor docks beside the list
- **THEN** the list returns to a single column
