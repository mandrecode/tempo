## ADDED Requirements

### Requirement: Task lists provide bottom scroll clearance
Task list screens SHALL include invisible trailing scroll clearance after the last real task item when tasks are present.

#### Scenario: Last task can scroll above navigation
- **WHEN** a task list contains one or more task cards and the user scrolls to the end of the list
- **THEN** the final task card remains a real task item and can be positioned with visible clearance below it

#### Scenario: Empty task list has no ghost item
- **WHEN** a task list has no task cards
- **THEN** the screen shows the existing empty state without an extra visible or interactive placeholder item

### Requirement: Habit lists provide bottom scroll clearance
Habit list screens SHALL include invisible trailing scroll clearance after the last real habit item when habits are present.

#### Scenario: Last habit can scroll above navigation
- **WHEN** a habit list contains one or more habit cards and the user scrolls to the end of the list
- **THEN** the final habit card remains a real habit item and can be positioned with visible clearance below it

#### Scenario: Empty habit list has no ghost item
- **WHEN** a habit list has no habit cards
- **THEN** the screen shows the existing empty state without an extra visible or interactive placeholder item
