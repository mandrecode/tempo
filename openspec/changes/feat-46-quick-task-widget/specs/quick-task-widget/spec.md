## ADDED Requirements

### Requirement: Home-screen quick-add widget
Tempo SHALL provide a home-screen App Widget ("Quick Add Task") that the user can place on their Android home screen. The widget SHALL display only a launch affordance (icon and label) and SHALL NOT render any task list, agenda, or completion state.

#### Scenario: User adds the widget to the home screen
- **WHEN** the user adds the "Quick Add Task" widget from the system widget picker
- **THEN** the widget renders showing the Tempo icon and a "Quick add task" label, with no task data displayed

### Requirement: Tapping the widget opens a quick-add surface
Tapping the widget SHALL launch a minimal quick-add surface, on top of the current context, without navigating into the full Tempo app.

#### Scenario: User taps the widget
- **WHEN** the user taps the widget on the home screen
- **THEN** a quick-add surface opens showing a title input field and a category selector
- **AND** the rest of the device UI (home screen/current app) remains visible behind or is dismissed back to on cancel

### Requirement: Saving a task from the widget reuses task creation logic
Saving from the quick-add surface SHALL create the task through the same task-creation logic used by the rest of the app (validation, sort order, reminder scheduling), with no parallel/duplicated persistence path.

#### Scenario: User saves a valid task from the widget
- **WHEN** the user enters a non-empty title (optionally selecting a category) and taps Save
- **THEN** a new task is persisted with that title and category
- **AND** the quick-add surface closes

#### Scenario: User attempts to save an empty title
- **WHEN** the user taps Save with an empty or whitespace-only title
- **THEN** no task is created
- **AND** the quick-add surface shows a validation error and remains open

#### Scenario: User cancels the quick-add surface
- **WHEN** the user dismisses the quick-add surface without saving
- **THEN** no task is created
- **AND** the surface closes

### Requirement: Widget appearance matches the app's light/dark theme
The widget's chrome (background, icon tint, text color) SHALL visually match Tempo's current color scheme, switching automatically between light and dark variants along with the system theme, using the same color tokens as the rest of the app rather than independently defined colors.

#### Scenario: Device is in light mode
- **WHEN** the system is set to light theme
- **THEN** the widget renders using Tempo's light color scheme tokens (background, primary, on-primary)

#### Scenario: Device is in dark mode
- **WHEN** the system is set to dark theme
- **THEN** the widget renders using Tempo's dark color scheme tokens (background, primary, on-primary)

#### Scenario: Quick-add surface follows the same theme
- **WHEN** the quick-add surface is shown, in either light or dark system theme
- **THEN** it renders using the app's existing `TempoTheme` Compose theme, matching the in-app task creation UI's colors
