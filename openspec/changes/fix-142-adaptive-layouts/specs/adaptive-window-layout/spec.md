## ADDED Requirements

### Requirement: Window width is classified with window size classes
The app SHALL derive layout decisions from the window size class of the current window, not from device-configuration screen fields.

#### Scenario: Landscape phone uses the rail layout
- **WHEN** the app window width is at least the medium width breakpoint (600dp)
- **THEN** the navigation area is presented as a vertical rail at the window's start edge

#### Scenario: Compact width uses the bottom bar
- **WHEN** the app window width is below the medium width breakpoint
- **THEN** the navigation area is presented as the floating bottom bar

### Requirement: The navigation rail never overlaps screen content
In rail layouts, top-level screen content SHALL be laid out entirely outside the rail's footprint.

#### Scenario: Day filter chips clear the rail
- **WHEN** the Routines screen is shown in a rail layout
- **THEN** the day-filter chips, list content, and top bar start after the rail's reserved width

#### Scenario: Tasks controls clear the rail
- **WHEN** the Tasks screen is shown in a rail layout
- **THEN** category chips, section headers, task cards, and the sort/clear buttons do not render under the rail

#### Scenario: Content stays stable when the bar hides
- **WHEN** a task or habit editor sheet opens in a rail layout and the floating controls hide
- **THEN** screen content keeps its horizontal position

### Requirement: Screen content respects horizontal safe-drawing insets
The navigation shell SHALL inset its content and floating controls from horizontal display cutouts and system navigation bars.

#### Scenario: Content clears the landscape navigation bar
- **WHEN** the device shows 3-button navigation on a short edge in landscape
- **THEN** cards, buttons, and floating controls are not covered by the system bar

### Requirement: Main content keeps a readable width on expanded windows
Top-level destinations SHALL cap their content at a readable maximum width on expanded window widths, centered in the available space.

#### Scenario: Tablet-width cards do not stretch full-bleed
- **WHEN** a top-level screen is shown in a window wider than the readable maximum plus navigation clearance
- **THEN** screen content is laid out at the readable maximum width, horizontally centered

### Requirement: Modal sheets adapt to the window
Modal task/habit sheets SHALL size themselves from the current window size and SHALL cap their width on wide windows.

#### Scenario: Sheet height follows the window in landscape
- **WHEN** a modal sheet opens in a landscape or resized window
- **THEN** its maximum height derives from the actual window height rather than device-configuration screen height

#### Scenario: Sheet width is capped on wide windows
- **WHEN** a modal sheet opens in a window wider than the sheet's maximum width
- **THEN** the sheet is horizontally centered at its maximum width
