## ADDED Requirements

### Requirement: Home-screen quick-add widget
Tempo SHALL provide a home-screen App Widget ("Quick Add Task") that the user can place on their Android home screen. The widget SHALL display only an icon, with no label text on the tile itself, and SHALL NOT render any task list, agenda, or completion state.

#### Scenario: User adds the widget to the home screen
- **WHEN** the user adds the "Quick Add Task" widget from the system widget picker
- **THEN** the widget renders showing only the Tempo task icon, with no text and no task data displayed
- **AND** the widget is identifiable by its own name (not the app's name) in the widget picker

### Requirement: Tapping the widget opens the app's existing task-creation sheet
Tapping the widget SHALL launch the app, navigate to the Tasks tab, and open the same task-creation sheet used by the in-app "add task" control — not a separate widget-only UI.

#### Scenario: User taps the widget
- **WHEN** the user taps the widget on the home screen
- **THEN** the app opens on the Tasks tab
- **AND** the task-creation sheet is shown, pre-populated the same way it is when opened via the in-app "+" button (blank title, default category)

#### Scenario: Saving a task from the widget-triggered sheet
- **WHEN** the user enters a title and saves the task-creation sheet after tapping the widget
- **THEN** the task is created through the app's existing task-creation logic, identically to saving the sheet when opened in-app

### Requirement: Widget appearance matches the app's light/dark theme
The widget's chrome (background, icon tint) SHALL visually match Tempo's current color scheme, switching automatically between light and dark variants along with the system theme, using the same color tokens as the rest of the app rather than independently defined colors.

#### Scenario: Device is in light mode
- **WHEN** the system is set to light theme
- **THEN** the widget renders using Tempo's light color scheme tokens (background, primary icon tint)

#### Scenario: Device is in dark mode
- **WHEN** the system is set to dark theme
- **THEN** the widget renders using Tempo's dark color scheme tokens (background, primary icon tint)
