# Spec Delta

## Purpose

Defines how the Tasks list presents completed tasks: the collapsible "Completed" separator, the action that clears completed tasks, and the headers that group completed tasks.

## ADDED Requirements

### Requirement: Clear action lives in the completed separator
The "Completed" separator row SHALL show a "Clear" text action at its trailing end whenever the selected category has completed top-level tasks, regardless of whether the completed section is collapsed or expanded.

#### Scenario: Collapsed section
- **WHEN** the selected category has completed tasks and the completed section is collapsed
- **THEN** the separator row shows the "Completed" toggle and a "Clear" action at its trailing end

#### Scenario: Expanded section
- **WHEN** the user expands the completed section
- **THEN** the "Clear" action stays in the separator row at the same position

#### Scenario: Manual sort
- **WHEN** tasks are sorted manually, so the separator shows no divider
- **THEN** the "Clear" action is still shown at the trailing end of the separator row

### Requirement: Clearing keeps its confirmation and undo
Activating the "Clear" action SHALL open the existing delete-completed confirmation dialog; confirming deletes the selected category's completed tasks with the existing undo affordance, and cancelling changes nothing.

#### Scenario: Confirm clear
- **WHEN** the user taps "Clear" and confirms the dialog
- **THEN** the selected category's completed tasks are deleted and an undo is offered

#### Scenario: Cancel clear
- **WHEN** the user taps "Clear" and cancels the dialog
- **THEN** no task is deleted

#### Scenario: Toggling the section is separate
- **WHEN** the user taps the "Completed" toggle
- **THEN** the section collapses or expands and no confirmation dialog opens

### Requirement: Floating bar has no clear action
The floating bar on the Tasks tab SHALL NOT offer a clear-completed action in any layout (portrait, single-tab portrait, or landscape rail), and its size and arrangement SHALL NOT depend on whether completed tasks exist.

#### Scenario: Completed tasks exist
- **WHEN** the Tasks tab is shown and the selected category has completed tasks
- **THEN** the floating bar shows the sort action and no clear-completed action

#### Scenario: Completed tasks appear
- **WHEN** a task is completed and the category previously had no completed tasks
- **THEN** the floating bar layout does not change

### Requirement: Every completed group has its own header
When the completed section is expanded under a sort that groups completed tasks, each group, including the first, SHALL be introduced by its own group header row below the separator; the separator row SHALL NOT show a group label.

#### Scenario: Expanded by date
- **WHEN** completed tasks are sorted by date and the section is expanded
- **THEN** the separator row shows only the toggle, divider, and "Clear", and the first group (e.g. "Today") has its own header row above its tasks

#### Scenario: Collapsed by date
- **WHEN** completed tasks are sorted by date and the section is collapsed
- **THEN** no group header is shown
