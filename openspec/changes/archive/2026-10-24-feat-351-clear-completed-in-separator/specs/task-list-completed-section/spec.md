# Spec Delta

## Purpose

Defines how the Tasks list presents completed tasks: the collapsible "Completed" separator and the action that clears completed tasks.

## ADDED Requirements

### Requirement: Clear action follows the expanded completed tasks
When the completed section is expanded, the Tasks list SHALL show a "Clear completed" action at the trailing end, below the last completed task. When the section is collapsed, the list SHALL NOT show a clear action.

#### Scenario: Collapsed section
- **WHEN** the selected category has completed tasks and the completed section is collapsed
- **THEN** the separator row shows only the "Completed" toggle (and divider), and no clear action is shown

#### Scenario: Expanded section
- **WHEN** the user expands the completed section
- **THEN** "Clear completed" appears at the trailing end below the last completed task

#### Scenario: Grouped completed tasks
- **WHEN** completed tasks are sorted by date and the section is expanded
- **THEN** the separator still shows the first group's label inline, and "Clear completed" comes after the last group's tasks

### Requirement: Clearing keeps its confirmation and undo
Activating "Clear completed" SHALL open the existing delete-completed confirmation dialog; confirming deletes the selected category's completed tasks with the existing undo affordance, and cancelling changes nothing.

#### Scenario: Confirm clear
- **WHEN** the user taps "Clear completed" and confirms the dialog
- **THEN** the selected category's completed tasks are deleted and an undo is offered

#### Scenario: Cancel clear
- **WHEN** the user taps "Clear completed" and cancels the dialog
- **THEN** no task is deleted

### Requirement: Floating bar has no clear action
The floating bar on the Tasks tab SHALL NOT offer a clear-completed action in any layout (portrait, single-tab portrait, or landscape rail), and its size and arrangement SHALL NOT depend on whether completed tasks exist.

#### Scenario: Completed tasks exist
- **WHEN** the Tasks tab is shown and the selected category has completed tasks
- **THEN** the floating bar shows the sort action and no clear-completed action

#### Scenario: Completed tasks appear
- **WHEN** a task is completed and the category previously had no completed tasks
- **THEN** the floating bar layout does not change

### Requirement: Running-session chip stays compact on Tasks
On the portrait bottom bar, the running-session chip SHALL show only its icon while the Tasks tab is shown, because the bar also carries the sort and add actions there.

#### Scenario: Session running on a narrow phone
- **WHEN** a focus session is running, all tabs are enabled, and the Tasks tab is shown on a 360dp-wide portrait window
- **THEN** the session chip is compact and the add button keeps its full size
