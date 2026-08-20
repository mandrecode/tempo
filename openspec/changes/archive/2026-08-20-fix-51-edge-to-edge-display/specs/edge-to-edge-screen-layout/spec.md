## ADDED Requirements

### Requirement: Top-level screens draw edge-to-edge

The system SHALL allow every top-level screen surface and scrollable viewport to draw through the system status-bar and navigation-bar regions when the hosting activity is edge-to-edge.

#### Scenario: Main list screen is displayed

- **WHEN** the user opens the Tasks or Routines screen
- **THEN** the screen background and scrollable viewport extend through the navigation-bar region without a scaffold-created inset band

#### Scenario: Settings screen is displayed

- **WHEN** the user opens Settings
- **THEN** its existing edge-to-edge layout remains visually consistent with the main screens

### Requirement: Interactive content remains within safe system areas

The system SHALL keep interactive app bars, final list items, floating controls, fallback actions, and transient messages reachable and legible outside obstructing system-bar areas.

#### Scenario: User reaches the end of a list

- **WHEN** the user scrolls Tasks or Routines to the final item under gesture or three-button navigation
- **THEN** the final item can be positioned above the navigation-bar inset and floating controls

#### Scenario: Overlay control is shown

- **WHEN** a floating navigation control, fallback add action, or snackbar is displayed
- **THEN** the control is positioned clear of the navigation bar without adding a second inset to the entire screen

### Requirement: System inset ownership is singular

The system MUST apply each system-bar inset at exactly one applicable layout boundary so that switching navigation mode, orientation, or theme does not create duplicate padding.

#### Scenario: Navigation configuration changes

- **WHEN** system-bar dimensions change because of navigation mode or orientation
- **THEN** screen content recomputes its safe clearance from current window insets without duplicated scaffold padding
