## ADDED Requirements

### Requirement: Categories, habits and chains belong to exactly one space
The system SHALL assign every category, habit and habit chain to exactly one space. A category MUST NOT appear in more than one space.

#### Scenario: Category belongs to a single space
- **WHEN** a category is created while a space is active
- **THEN** the category is assigned to that space and is not visible from any other space

#### Scenario: Habit belongs to a single space
- **WHEN** a habit is created while a space is active
- **THEN** the habit is assigned to that space and is not visible from any other space

### Requirement: A chain and its member habits share a space
The system SHALL prevent a habit chain from containing habits belonging to a different space than the chain.

#### Scenario: Chain membership is restricted to same-space habits
- **WHEN** a user selects habits to add to a chain
- **THEN** only habits belonging to the chain's space are offered

#### Scenario: Moving a habit out of a chain's space is prevented
- **WHEN** an operation would place a habit in a different space from a chain it belongs to
- **THEN** the system rejects the operation and the chain and its habits remain in one space

### Requirement: Tasks and Routines show only the active space
The system SHALL filter task, category, habit and chain listings to the active space.

#### Scenario: Tasks list excludes other spaces
- **WHEN** a space is active and the user opens the Tasks tab
- **THEN** only categories belonging to the active space, and the tasks within them, are listed

#### Scenario: Routines list excludes other spaces
- **WHEN** a space is active and the user opens the Routines tab
- **THEN** only habits and chains belonging to the active space are listed

### Requirement: The active space is device-local
The system SHALL store the active space selection per device and SHALL NOT synchronise it between devices.

#### Scenario: Active space is not shared across devices
- **WHEN** the active space is changed on one device
- **THEN** the active space on any other device signed into the same account is unchanged

#### Scenario: Active space survives restart
- **WHEN** the app is closed and reopened
- **THEN** the previously active space is active again

### Requirement: A single space is invisible
The system SHALL hide every space affordance while only one space exists.

#### Scenario: No switcher with one space
- **WHEN** exactly one space exists
- **THEN** no space switcher, badge or indicator is shown anywhere in the app

#### Scenario: Affordances appear with a second space
- **WHEN** a second space is created
- **THEN** the space switcher becomes visible

### Requirement: Existing data migrates into one default space
The system SHALL migrate an existing installation into a single space containing all existing categories, habits and chains, without user intervention and without changing any visible behaviour.

#### Scenario: Migration assigns all existing content
- **WHEN** an installation created before spaces is upgraded
- **THEN** every existing category, habit and chain belongs to one seeded space, and the app presents the same content as before the upgrade

#### Scenario: Migrated installation shows no space affordances
- **WHEN** an installation has been migrated and no second space has been created
- **THEN** the app is indistinguishable from its pre-upgrade state

### Requirement: The quick-task widget targets a space chosen when it is placed
The system SHALL ask which space a quick-task widget targets when the widget is placed, and SHALL keep targeting that space regardless of which space is active in the app.

#### Scenario: Placing a widget chooses its space
- **WHEN** a user places a quick-task widget and more than one space exists
- **THEN** the user is asked which space the widget targets

#### Scenario: Widget target does not follow the active space
- **WHEN** a task is added through a widget while a different space is active in the app
- **THEN** the task is created in the widget's configured space

#### Scenario: A single space needs no choice
- **WHEN** a user places a quick-task widget and only one space exists
- **THEN** the widget targets that space without asking

### Requirement: Reminders are not filtered by the active space
The system SHALL deliver task and habit reminders regardless of which space is active.

#### Scenario: Reminder for a non-active space still fires
- **WHEN** a reminder is due for an item in a space that is not currently active
- **THEN** the reminder is delivered as scheduled

#### Scenario: Opening a reminder switches space
- **WHEN** the user opens a reminder notification for an item in a non-active space
- **THEN** the app makes that item's space active and shows the item
