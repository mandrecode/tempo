## ADDED Requirements

### Requirement: Space switcher placement adapts to window size
The system SHALL present the space switcher in the top app bar actions at compact widths, and as a chip anchored above the navigation rail at medium and expanded widths.

#### Scenario: Compact width shows the switcher in the top bar
- **WHEN** more than one space exists and the window is compact width
- **THEN** the space switcher appears in the top app bar actions, adjacent to the settings action

#### Scenario: Rail layouts show the switcher above the rail
- **WHEN** more than one space exists and a navigation rail is displayed
- **THEN** the space switcher appears as a chip anchored above the rail's primary action

#### Scenario: Expanded width shows the space name
- **WHEN** more than one space exists and the labeled rail is displayed
- **THEN** the switcher shows the active space's name alongside its icon

### Requirement: Selecting a space changes the active space
The system SHALL make the chosen space active when the user selects it from the switcher, and SHALL update every space-scoped surface accordingly.

#### Scenario: Choosing a space updates listings
- **WHEN** the user selects a different space from the switcher
- **THEN** the Tasks and Routines listings show only that space's content

#### Scenario: Switcher lists all spaces
- **WHEN** the user opens the space switcher
- **THEN** every space is listed, with the active one indicated

### Requirement: Hardware keyboards can switch spaces
The system SHALL allow switching to a space by its position using a modifier plus number key when a hardware keyboard is present.

#### Scenario: Keyboard shortcut activates a space
- **WHEN** more than one space exists and the user presses the modifier with a number matching a space's position
- **THEN** that space becomes active

### Requirement: Switching spaces closes an open editor pane
The system SHALL dismiss an open editor pane or sheet when the active space changes, honouring the existing unsaved-changes guard.

#### Scenario: Docked editor closes on space change
- **WHEN** an editor pane is open and the user switches to another space
- **THEN** the editor is dismissed through the same path as a back gesture, including any unsaved-changes prompt

#### Scenario: Unsaved changes are not silently discarded
- **WHEN** an editor with unsaved changes is open and the user switches space
- **THEN** the user is prompted before the editor is dismissed
