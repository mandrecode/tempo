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

### Requirement: Widget appearance matches the app's theme preference, in both light and dark
The widget's chrome (background, icon tint) SHALL follow the same theme choice the app itself uses — Tempo's static brand colors when the user has "use Tempo colors" enabled, or Android 12+ dynamic (wallpaper-based) colors otherwise on devices that support it — switching automatically between light and dark variants along with the system theme, using the same color tokens/logic as the rest of the app rather than independently defined colors.

#### Scenario: User has "use Tempo colors" enabled, light mode
- **WHEN** the user has "use Tempo colors" enabled in Settings and the system is set to light theme
- **THEN** the widget renders using Tempo's light color scheme tokens (background, primary icon tint)

#### Scenario: User has "use Tempo colors" enabled, dark mode
- **WHEN** the user has "use Tempo colors" enabled in Settings and the system is set to dark theme
- **THEN** the widget renders using Tempo's dark color scheme tokens (background, primary icon tint)

#### Scenario: User has "use Tempo colors" disabled on a device that supports dynamic color
- **WHEN** the user has "use Tempo colors" disabled in Settings and the device supports Android 12+ dynamic color
- **THEN** the widget renders using the device's dynamic (wallpaper-based) color scheme, in light or dark to match the system theme

#### Scenario: User has "use Tempo colors" disabled on a device that does not support dynamic color
- **WHEN** the user has "use Tempo colors" disabled in Settings and the device does not support Android 12+ dynamic color
- **THEN** the widget falls back to rendering with Tempo's static color scheme tokens rather than an unbranded default

#### Scenario: User toggles "use Tempo colors" while the widget is already placed
- **WHEN** the user changes the "use Tempo colors" setting while a widget instance is on the home screen
- **THEN** the placed widget updates to reflect the new color scheme without requiring the user to remove and re-add it
