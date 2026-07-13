## ADDED Requirements

### Requirement: The app theme defines the reverse surface hierarchy
The app SHALL normalize the selected Tempo, dynamic, or fallback Material color scheme at the theme boundary so standard page and container roles express the reverse tonal hierarchy throughout the app without feature-specific color aliases.

#### Scenario: Any supported palette is selected
- **WHEN** Tempo selects a brand, dynamic, or fallback color scheme
- **THEN** the theme derives its page and container roles from that scheme before providing it to every composable

#### Scenario: A new component uses standard roles
- **WHEN** a screen or component uses the theme's standard `background`, `surface`, or surface-container roles
- **THEN** it automatically participates in the app-wide surface hierarchy without a Tempo-specific exception

### Requirement: Primary screens use the canonical page role
The app SHALL render primary screen backgrounds and visually continuous top-level chrome with the normalized Material `background` role.

#### Scenario: Primary screen in light theme
- **WHEN** a primary screen is rendered with a light color scheme
- **THEN** its background and continuous top-level chrome use the normalized `background` role

#### Scenario: Primary screen in dark theme
- **WHEN** a primary screen is rendered with a dark color scheme
- **THEN** its background and continuous top-level chrome use the normalized `background` role

### Requirement: Neutral cards reverse their tonal direction by theme
The app SHALL use the normalized Material `surfaceContainerLow` role for neutral task, habit, and settings cards so they are lighter than the primary screen background in light mode and darker than the primary screen background in dark mode.

#### Scenario: Neutral card in light theme
- **WHEN** a neutral content card is rendered on a primary screen with a light color scheme
- **THEN** the card is lighter than the surrounding muted background

#### Scenario: Neutral card in dark theme
- **WHEN** a neutral content card is rendered on a primary screen with a dark color scheme
- **THEN** the card is darker than the surrounding muted background

### Requirement: Existing state and accent treatments remain intact
The app MUST preserve card state overlays, animations, and explicit category/accent color overrides while applying the reverse neutral surface hierarchy.

#### Scenario: Completed task card
- **WHEN** a task card changes between active and completed states
- **THEN** its existing color animation and completed transparency treatment remain based on the neutral card role

#### Scenario: Category-colored habit card
- **WHEN** a habit card has an explicit category color
- **THEN** the explicit category color remains its container color instead of the neutral fallback
