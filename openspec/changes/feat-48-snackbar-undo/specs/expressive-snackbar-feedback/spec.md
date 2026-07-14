## ADDED Requirements

### Requirement: Shared Didi-inspired snackbar presentation

The app SHALL render task and routine feedback through one shared snackbar presentation with a pill-like rounded container, compact spacing, theme-derived colors, readable typography, and consistent placement above floating navigation.

#### Scenario: Message-only feedback

- **WHEN** a task or routine flow emits informational or error feedback without an action
- **THEN** the app displays the message in the shared Didi-inspired snackbar without an action control

#### Scenario: Light and dark appearance

- **WHEN** the shared snackbar is displayed in light or dark theme
- **THEN** its container, content, and action colors come from the active Material color scheme and remain legible

#### Scenario: Floating navigation clearance

- **WHEN** a snackbar is displayed while Tempo's floating navigation is visible
- **THEN** the snackbar remains fully visible above the navigation surface

#### Scenario: Didi source parity

- **WHEN** the shared snackbar is rendered
- **THEN** its container, content row, message, action surface, outline, spacing, and pressed-corner animation use the same Compose structure and values as Didi's reference snackbar, with elevation calibrated to Tempo's existing surfaces

### Requirement: Accessible localized snackbar action

An actionable snackbar SHALL show a localized action label with a minimum 48dp interactive target and SHALL report whether the action was performed or the snackbar was dismissed.

#### Scenario: User selects Undo

- **WHEN** a deletion snackbar is visible and the user selects its localized Undo action
- **THEN** the snackbar reports the action together with the deletion token that produced it

#### Scenario: Snackbar expires or is dismissed

- **WHEN** an actionable snackbar times out or is dismissed without its action being selected
- **THEN** the snackbar reports dismissal together with the deletion token that produced it

### Requirement: Previewable shared snackbar states

The shared snackbar component SHALL have debug-source previews for message-only and Undo-action states in both light and dark themes.

#### Scenario: Developer inspects snackbar previews

- **WHEN** a developer opens the shared snackbar previews in Android Studio
- **THEN** representative message-only and actionable snackbar states are available without running the app
