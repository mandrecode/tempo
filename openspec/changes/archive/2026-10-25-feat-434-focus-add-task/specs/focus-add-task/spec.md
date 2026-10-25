# Spec Delta

## Purpose

Defines how a task is added from the Focus tab.

## ADDED Requirements

### Requirement: Focus offers the add-task action
While the Focus tab is active, the floating navigation SHALL show the same "Add Task" action the Tasks tab shows, in every layout: portrait bar, navigation rail, and single-tab mode. It SHALL NOT show the Tasks sort action.

#### Scenario: Portrait bar on Focus
- **WHEN** the user is on the Focus tab in a portrait layout
- **THEN** the floating bar shows the "Add Task" button beside the navigation pill and no sort button

#### Scenario: Focus as the only tab
- **WHEN** Focus is the only enabled tab
- **THEN** the floating bar shows the "Add Task" button as its solo action

### Requirement: Adding from Focus opens the task creation sheet over Focus
Activating "Add Task" on Focus SHALL open the same blank task creation sheet Tasks opens, over the Focus tab and without navigating away. Closing the sheet SHALL return to Focus as it was.

#### Scenario: Create a task from Focus
- **WHEN** the user taps "Add Task" on Focus, enters a title and closes the sheet
- **THEN** the task is created exactly as if it had been added from Tasks, and the user is still on Focus

#### Scenario: Add twice
- **WHEN** the user closes the sheet and taps "Add Task" again
- **THEN** a new blank sheet opens again
