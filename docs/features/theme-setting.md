# Theme Setting Feature

## Overview
This document describes the implementation of the theme setting feature that allows users to switch between Light, Dark, and System theme modes following Material 3 Expressive guidelines.

## Feature Description
The theme setting allows users to customize the app appearance by selecting one of three theme modes:

1. **Light Mode**: Forces the app to use light theme regardless of system settings
2. **Dark Mode**: Forces the app to use dark theme regardless of system settings
3. **System Mode**: Automatically follows the device's appearance settings (default)

## Architecture

### Data Layer
- **ThemeMode Enum** (`data/model/ThemeMode.kt`): Defines the three theme mode options (LIGHT, DARK, SYSTEM)
- **ThemePreferencesRepository** (`data/repository/ThemePreferencesRepository.kt`): 
  - Manages theme preference persistence using SharedPreferences
  - Exposes theme mode as a `Flow<ThemeMode>` for reactive updates
  - Methods:
    - `getThemeMode(): Flow<ThemeMode>` - Observes current theme mode
    - `setThemeMode(mode: ThemeMode)` - Saves selected theme mode

### UI Layer
- **SettingsContract** (`ui/settings/SettingsContract.kt`): Defines the MVI contract
  - `UiState`: Contains selected theme mode and available options
  - `UiEvent.ThemeModeSelected`: Event for theme mode selection
  - `UiEffect`: (Empty for now)
  
- **SettingsViewModel** (`ui/settings/SettingsViewModel.kt`):
  - Observes theme mode from repository
  - Updates UI state when theme changes
  - Handles theme selection events
  
- **SettingsScreen** (`ui/settings/SettingsScreen.kt`):
  - Displays theme selector using Material 3 Expressive `ButtonGroup`
  - Shows icons for each theme mode (sun, moon, contrast)
  - Displays description text for System mode
  - Follows Screen/Content separation pattern

### Integration
- **MainActivity** (`MainActivity.kt`):
  - Observes theme mode from repository
  - Applies theme to `TempoTheme` composable
  - Maps theme mode to boolean for `darkTheme` parameter

## UI Design

### Material 3 Compliance
- **ButtonGroup**: Uses Material 3 Expressive's `ButtonGroup` component with `toggleableItem()`
- **Icons**: Material Icons for visual clarity
  - Light Mode: `Icons.Filled.LightMode`
  - Dark Mode: `Icons.Filled.DarkMode`
  - System Mode: `Icons.Outlined.Contrast`
- **Expressive Animations**: Dynamic width animations on selection
- **Typography**: Material 3 typography scale
- **Spacing**: Consistent 16dp, 12dp, and 8dp spacing

### Accessibility
- Content descriptions for all icons
- Proper touch targets (Material 3 defaults to 48dp minimum)
- High contrast colors from Material 3 color scheme

## User Flow
1. User navigates to Settings screen
2. User sees current theme selection in button group
3. User taps desired theme mode button (with expressive animation)
4. Theme changes immediately throughout the app
5. Selection is persisted for future app launches

## Testing
- **Unit Tests** (`test/ui/settings/SettingsViewModelTest.kt`):
  - Initial state verification
  - Theme mode selection
  - Repository integration
  - State updates

## String Resources
```xml
<string name="theme">Theme</string>
<string name="theme_light">Light</string>
<string name="theme_dark">Dark</string>
<string name="theme_system">System</string>
<string name="theme_description">Syncs with your device appearance settings</string>
```

## Technical Details

### Dependency Injection
The `ThemePreferencesRepository` is provided as a singleton via Hilt:
- `@Singleton` scope ensures single instance
- `@ApplicationContext` for SharedPreferences access
- Injected into `MainActivity` and `SettingsViewModel`

### State Management
- Uses `MutableStateFlow` for internal state in repository
- Exposes read-only `StateFlow` to consumers
- ViewModel collects Flow and updates UI state
- MainActivity observes theme as `State` using `collectAsStateWithLifecycle`

### Persistence
- Stored in SharedPreferences named "theme_prefs"
- Key: "theme_mode"
- Value: Enum name as String (LIGHT/DARK/SYSTEM)
- Default value: SYSTEM

## Future Enhancements
- Dynamic color toggle (Material You)
- Theme scheduling (auto-switch based on time)
- Custom theme colors
- Accent color selection
