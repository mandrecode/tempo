## ADDED Requirements

### Requirement: Animated Settings Entry Button
The system SHALL present route top-bar Settings entry points as a 40dp icon surface with animated pressed and unpressed shape and color states.

#### Scenario: Settings entry is visible
- **WHEN** a route top bar includes the Settings action
- **THEN** the action is shown as a 40dp circular Settings icon surface with a visible border and the localized Settings content description

#### Scenario: Settings entry is idle
- **WHEN** the Settings action is not pressed in either light or dark theme
- **THEN** its container uses the same softened neutral surface role as cards instead of the most contrasting surface tone

#### Scenario: Settings entry responds to press
- **WHEN** the user presses the Settings action
- **THEN** the action animates its corner radius from 20dp to 14dp, container color, content color, and border color to the pressed state

#### Scenario: Settings entry opens Settings
- **WHEN** the user activates the Settings action
- **THEN** the app navigates to the existing Settings route without changing Settings screen behavior
