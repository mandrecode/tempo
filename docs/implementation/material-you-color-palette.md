# Material You Color Palette System

## Overview
The Tempo & Habits app features a comprehensive color palette system that integrates Material You dynamic colors with a rainbow of pastel colors. Colors are displayed as segmented circles showing 3 accent variations, similar to Android system theme settings. The system ensures visual consistency and allows users to personalize their habits and habit chains.

## Features

### 1. Material You Integration
The app automatically extracts colors from the system's Material You theme (Android 12+):
- **Primary colors**: Primary, Secondary, Tertiary
- **Container colors**: Primary Container, Secondary Container, Tertiary Container

These colors adapt to the user's wallpaper and system theme, providing a personalized experience.

### 2. Pastel Color Palette
A carefully curated set of 12 pastel colors with light/dark variants:
- Pastel Red, Orange, Yellow
- Pastel Green, Cyan, Blue
- Pastel Purple, Pink, Lavender
- Pastel Mint, Peach, Rose

Each color has two variants for better contrast:
- **Light variant**: For light theme backgrounds
- **Dark variant**: For dark theme backgrounds

### 3. Segmented Circle Display
Each color is displayed as a segmented circle showing 3 accent variations:
- **Top-left quadrant**: Subtle accent (40% opacity)
- **Top-right quadrant**: Medium accent (70% opacity)
- **Bottom semicircle**: Bold/full accent (100% opacity)

This visual representation matches the Android system theme settings UI and shows users exactly how the color will appear at different intensities before selection.

### 4. Dynamic Theme Adaptation
Colors automatically switch between light/dark variants based on the system theme:
- Ensures proper legibility in both modes
- Maintains accessibility standards
- Seamless transition when theme changes

### 5. Automatic Color Selection
When creating a new habit or habit chain:
- The system automatically selects a random color
- Material You colors are preferred first
- Existing colors are avoided to ensure variety
- No manual selection required (but can be changed)

## Architecture

### Components

#### ColorPalette.kt
- `ColorOption`: Data class holding light/dark color variants with labels
- `getAccents(isDarkTheme)`: Returns 3 accent variations (subtle, medium, bold)
- `getColor(isDarkTheme)`: Returns the primary color for current theme
- `getMaterialYouColors()`: Composable function to extract theme colors
- `getPastelColors()`: Function providing pastel color palette

#### ColorSelectionUtil.kt
- `selectRandomColor()`: Utility function for random color selection
- Avoids existing colors when possible
- Works directly with ColorOptions in the UI layer

#### UI Integration
- `ColorPicker`: Enhanced composable in HabitBottomSheet
- Displays Material You colors first, then pastel colors
- Uses SegmentedColorCircle for visual representation
- Supports disabled state with explanatory messages

#### SegmentedColorCircle
- Custom composable using Canvas API
- Draws 3-segment circle showing accent variations
- 56dp size for accessibility and visibility
- Selection indicator with primary color border

### Data Flow

1. **User opens habit creation sheet**
   - `RoutinesViewModel.showHabitBottomSheet()` sets `shouldAutoSelectColor = true`
   
2. **Auto-selection triggered**
   - `HabitBottomSheet` LaunchedEffect detects the flag
   - Gets existing colors from current habits/chains
   - Calls `selectRandomColor()` utility with Material You colors (preferred)
   - Falls back to including pastel colors if needed
   - Calls `onSetColor()` to update ViewModel

3. **User manually selects color**
   - Taps on a segmented color circle
   - Color stored in UiState using `colorOption.getColor(isDarkTheme)`
   - Selection state updates immediately

4. **Habit/Chain saved**
   - Selected color stored in Room database as ARGB integer
   - Color displayed in habit cards with animations
   - Color automatically adapts when theme changes

## Usage Examples

### Creating a Habit with Auto-Selected Color
```kotlin
// ViewModel
fun showHabitBottomSheet(habit: Habit? = null) {
    _uiState.update {
        it.copy(
            showHabitBottomSheet = true,
            shouldAutoSelectColor = habit == null // Auto-select for new habits
        )
    }
}
```

### Getting Material You Colors in Composable
```kotlin
@Composable
fun MyComposable() {
    val materialYouColors = getMaterialYouColors(MaterialTheme.colorScheme)
    // Use colors for UI elements
}
```

### Selecting a Random Color
```kotlin
fun selectColor() {
    val isDarkTheme = isSystemInDarkTheme()
    val materialYouColors = getMaterialYouColors(MaterialTheme.colorScheme)
    val existingColors = listOf(Color.Red, Color.Blue) // Colors already in use
    
    val randomColor = selectRandomColor(
        availableOptions = materialYouColors,
        isDarkTheme = isDarkTheme,
        existingColors = existingColors
    )
}
```

### Displaying Segmented Circle
```kotlin
@Composable
fun ColorDisplay(colorOption: ColorOption) {
    val isDarkTheme = isSystemInDarkTheme()
    val accents = colorOption.getAccents(isDarkTheme)
    // accents[0] = subtle (40% opacity)
    // accents[1] = medium (70% opacity)
    // accents[2] = bold (100% opacity)
    
    SegmentedColorCircle(
        colorOption = colorOption,
        isDarkTheme = isDarkTheme,
        isSelected = false,
        enabled = true,
        onClick = { /* handle selection */ }
    )
}
```

## Benefits

1. **Personalization**: Material You integration adapts to user's system theme
2. **Variety**: 18 total color options (6 Material You + 12 Pastel)
3. **Consistency**: Automatic color selection ensures visual harmony
4. **Accessibility**: Light/dark variants ensure proper contrast
5. **User Control**: Users can override auto-selection and choose any color
6. **Visual Feedback**: Segmented circles show exactly how colors will appear
7. **System Consistency**: Matches Android system theme picker UI

## Future Enhancements

Potential improvements to consider:
- Custom color picker with HSV/RGB sliders
- Color schemes/themes (e.g., "Ocean", "Sunset", "Forest")
- Color favorites/recent colors
- Accessibility checks for color contrast
- Color palettes based on time of day
- Animation when changing between light/dark mode
