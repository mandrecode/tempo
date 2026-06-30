## ADDED Requirements

### Requirement: Animated Settings Entry Button
The system SHALL present route top-bar Settings entry points as a shaped icon surface with animated pressed and unpressed states.

#### Scenario: Settings entry is visible
- **WHEN** a route top bar includes the Settings action
- **THEN** the action is shown as a rounded Settings icon surface with a visible border and the localized Settings content description

#### Scenario: Settings entry responds to press
- **WHEN** the user presses the Settings action
- **THEN** the action animates its corner radius, container color, content color, and border color to the pressed state

#### Scenario: Settings entry opens Settings
- **WHEN** the user activates the Settings action
- **THEN** the app navigates to the existing Settings route without changing Settings screen behavior
