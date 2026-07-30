## ADDED Requirements

### Requirement: Users can create, rename and reorder spaces
The system SHALL allow a user to create a space with a name and icon, rename it, and change its position in the switcher.

#### Scenario: Creating a second space reveals the switcher
- **WHEN** a user creates a second space
- **THEN** the space exists with no categories or habits, and the space switcher becomes visible

#### Scenario: Renaming a space updates every surface
- **WHEN** a user renames a space
- **THEN** the new name appears in the switcher and anywhere the space is labelled

#### Scenario: Reordering changes switcher order and shortcut positions
- **WHEN** a user reorders spaces
- **THEN** the switcher reflects the new order and keyboard position shortcuts follow it

### Requirement: The default space is unnamed until a second space exists
The system SHALL leave the seeded default space without a user-facing name while it is the only space, and SHALL require a name once a second space is created.

#### Scenario: Sole space needs no name
- **WHEN** only the seeded default space exists
- **THEN** no space name is displayed anywhere and the user is not asked to provide one

#### Scenario: Creating a second space prompts for the first one's name
- **WHEN** a user creates a second space and the default space has no name
- **THEN** the user is asked to name the default space so both are identifiable

### Requirement: Deleting a space lets the user choose what happens to its content
The system SHALL ask the user, when deleting a space that contains content, whether to move that content to another space or delete it with the space, and SHALL state the consequence of each option before applying it.

#### Scenario: Deletion offers both outcomes
- **WHEN** a user chooses to delete a space that contains content
- **THEN** the confirmation states how many items are affected and offers both moving them to another space and deleting them with the space

#### Scenario: Moving content preserves it
- **WHEN** a user deletes a space and chooses to move its content to another space
- **THEN** the space is removed and its categories, habits and chains belong to the chosen space

#### Scenario: Deleting content is never pre-selected
- **WHEN** the deletion confirmation is shown
- **THEN** the option that deletes content is not selected by default

#### Scenario: Empty space deletes without a choice
- **WHEN** a user deletes a space that contains no categories, habits or chains
- **THEN** the space is removed without asking what to do with content

#### Scenario: The last remaining space cannot be deleted
- **WHEN** only one space exists
- **THEN** deleting it is not offered

#### Scenario: Deleting the active space activates another
- **WHEN** the active space is deleted
- **THEN** another space becomes active

### Requirement: Enabled tabs and default tab are configured per space
The system SHALL store the set of enabled navigation tabs and the default tab per space, so different spaces can offer different tabs.

#### Scenario: Disabling a tab affects only its space
- **WHEN** a user disables the Routines tab in one space
- **THEN** that tab is absent in that space and unchanged in every other space

#### Scenario: Default tab is per space
- **WHEN** a user switches to a space
- **THEN** the space's own default tab determines which tab is shown on entry

#### Scenario: Existing global configuration migrates
- **WHEN** an installation with a global tab configuration is migrated
- **THEN** the seeded default space adopts that configuration unchanged
