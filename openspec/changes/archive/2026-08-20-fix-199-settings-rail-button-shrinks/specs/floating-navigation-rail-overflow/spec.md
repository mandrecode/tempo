## ADDED Requirements

### Requirement: Settings button remains fully rendered when the floating navigation rail overflows
On windows wide enough to use the vertical floating navigation rail (`isFloatingNavigationRailLayout()` is true), the rail SHALL keep the Settings button rendered at its full, correct size and reachable, even when the combined height of the items above it (title, add action, navigation tabs, Sort button, Clear-completed button) exceeds the rail's available vertical height.

#### Scenario: Tasks route with completed tasks on a height-constrained rail
- **WHEN** the app is on the Tasks route in the vertical floating navigation rail layout, there are completed tasks (so both the Sort and Clear-completed buttons are visible), and the sum of the rail's item heights exceeds the available window height
- **THEN** the Settings button at the bottom of the rail SHALL be drawn at its normal, unclipped size and SHALL remain clickable, reachable via scrolling if necessary

#### Scenario: Rail content fits within the available height
- **WHEN** the vertical floating navigation rail's content height is less than or equal to the available window height
- **THEN** the rail SHALL render exactly as before this change: no scroll indicator is shown, and the Settings button stays pinned to the bottom of the rail via the existing spacer behavior
