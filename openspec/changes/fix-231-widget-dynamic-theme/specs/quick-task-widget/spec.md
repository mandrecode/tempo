## MODIFIED Requirements

### Requirement: Widget appearance always follows OS dynamic color when available
The widget's chrome (background, icon tint) SHALL follow Android 12+ dynamic (wallpaper-based) colors whenever the device supports them, independent of the app's own "use Tempo colors" in-app theme preference — switching automatically between light and dark variants along with the system theme. On devices/OS versions that do not support dynamic color, the widget SHALL fall back to Tempo's static color scheme tokens rather than an unbranded default.

#### Scenario: Device supports dynamic color, app preference is "use Tempo colors" enabled
- **WHEN** the device supports Android 12+ dynamic color and the user has "use Tempo colors" enabled in Settings
- **THEN** the widget still renders using the device's dynamic (wallpaper-based) color scheme, ignoring the in-app preference

#### Scenario: Device supports dynamic color, app preference is "use Tempo colors" disabled
- **WHEN** the device supports Android 12+ dynamic color and the user has "use Tempo colors" disabled in Settings
- **THEN** the widget renders using the device's dynamic (wallpaper-based) color scheme, in light or dark to match the system theme

#### Scenario: Device does not support dynamic color, regardless of app preference
- **WHEN** the device does not support Android 12+ dynamic color
- **THEN** the widget falls back to rendering with Tempo's static color scheme tokens, whether "use Tempo colors" is enabled or disabled in Settings

#### Scenario: User toggles "use Tempo colors" while the widget is already placed
- **WHEN** the user changes the "use Tempo colors" setting while a widget instance is on the home screen
- **THEN** the placed widget's appearance does not change as a result of that setting, since the widget no longer reads it
