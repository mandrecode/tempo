## ADDED Requirements

### Requirement: Neutral cards use the adjacent card-side surface role
The app SHALL render neutral task, habit, habit-chain, quit-habit, and Settings section cards with the normalized `surfaceContainer` role so their contrast from the primary page is one surface step softer than `surfaceContainerLow`.

#### Scenario: Neutral card in light theme
- **WHEN** a neutral card is rendered with a light color scheme
- **THEN** the card uses `surfaceContainer` and remains lighter than the surrounding `background`

#### Scenario: Neutral card in dark theme
- **WHEN** a neutral card is rendered with a dark color scheme
- **THEN** the card uses `surfaceContainer` and remains darker than the surrounding `background`

#### Scenario: Settings section card
- **WHEN** a Settings section is rendered
- **THEN** its card uses the same neutral `surfaceContainer` role as task and routine cards

### Requirement: Existing card state and accent treatments remain intact
The app MUST preserve existing card animations, completion transparency, and explicit category or accent overrides while softening the neutral card surface.

#### Scenario: Completed task card
- **WHEN** a task card changes between active and completed states
- **THEN** its container continues to animate and its completed state applies the existing transparency treatment to `surfaceContainer`

#### Scenario: Explicitly colored routine card
- **WHEN** a habit, habit-chain, or quit-habit card has an explicit color
- **THEN** that explicit color remains the card container color instead of the neutral surface role
