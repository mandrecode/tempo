# Habit Chains/Routines Feature

## Overview
Habit Chains (also called Routines) allow users to group multiple habits into a cohesive routine. This feature helps users track and complete sets of related habits as a unified workflow, such as "Morning Routine" or "Workout Routine".

## Key Features

### 1. Grouping Habits
- Users can create a habit chain with multiple habits
- Each chain has a title, description, optional color, and optional reminder
- Habits are linked by their IDs stored as a comma-separated string

### 2. Visual Progress Tracking
The habit chain card displays real-time progress in multiple ways:

#### Circular Progress Indicator
- Shows completion ratio (e.g., "2/5")
- Located at the top-left of the card
- Updates dynamically as habits are checked off
- Color-coded based on the chain's assigned color

#### Linear Progress Bar
- Appears when the chain is expanded
- Shows percentage-based progress across the full width
- Smooth animation when habits are toggled

#### Percentage Display
- Shows completion percentage (e.g., "40% complete")
- Calculated based on today's completed habits only

### 3. Individual Habit Check-off
- Expand the chain to see all member habits
- Each habit has a checkbox for marking completion
- Checking a habit updates its completion history for today
- Checked habits show with strikethrough text decoration
- Checkbox colors match individual habit colors if set

### 4. Expand/Collapse Functionality
- Click the title/description area to expand
- Expanded view shows:
  - Linear progress bar
  - List of all habits in the chain
  - Interactive checkboxes for each habit
  - Full description (not truncated)
- Collapse by clicking title/description again

## Data Model

### HabitChain Entity
```kotlin
@Entity(tableName = "habit_chains")
data class HabitChain(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val color: Color? = null,
    val icon: String? = null,
    val habitIds: String = "",  // Comma-separated habit IDs
    val periodicReminder: LocalDateTime? = null,
    val createdDate: LocalDateTime,
    val completionHistory: String = ""  // For future analytics
)
```

### Fields Explanation

- **id**: Auto-generated unique identifier
- **title**: Name of the routine (e.g., "Morning Routine")
- **description**: Optional description explaining the purpose
- **color**: Optional color for visual identification
- **icon**: Optional icon name (for future use)
- **habitIds**: Comma-separated list of habit IDs belonging to this chain
- **periodicReminder**: Optional date/time for reminders
- **createdDate**: When the chain was created
- **completionHistory**: Track when the entire chain was completed (for future widgets)

## Architecture

### Database Layer
- **DAO**: `HabitChainDao` - CRUD operations for habit chains
- **Repository**: `HabitChainRepository` - Coordinates data operations
- **Converters**: Type converters for `Color` and `LocalDateTime`

### Domain Layer
- Pure Kotlin model (HabitChain)
- No Android dependencies
- Clean separation of concerns

### UI Layer
- **Screen**: `RoutinesScreen` - Navigation entry point
- **ViewModel**: `RoutinesViewModel` - Business logic orchestration
- **Card Component**: `HabitChainCard` - Displays individual chains
- **Bottom Sheet**: `HabitBottomSheet` - Create/edit chains
- **Contract**: `RoutinesContract` - UiState, UiEvent, and UiEffect definitions

## Progress Calculation Logic

Progress is calculated dynamically based on today's completions:

```kotlin
val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

val chainHabits = habitChain.habitIds
    .split(",")
    .mapNotNull { it.toLongOrNull() }
    .mapNotNull { id -> habits.find { it.id == id } }

val totalCount = chainHabits.size
val completedCount = chainHabits.count { habit ->
    habit.completionHistory.split(",").any { it.trim() == today }
}
val progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
```

### Progress States
- **0%**: No habits completed today
- **33%**: 1 out of 3 habits completed
- **50%**: 2 out of 4 habits completed
- **100%**: All habits in chain completed today

## User Experience

### Creating a Habit Chain
1. Click the "+" FAB on the Routines screen
2. Switch to "Habit Chain" tab
3. Enter title and description
4. Select habits to include from existing habits
5. Optionally set a color and reminder
6. Click "Add habit" to save

### Using a Habit Chain
1. View the chain card showing progress
2. Click on the title area to expand
3. Check off individual habits as you complete them
4. Progress updates in real-time
5. Click edit icon to modify the chain
6. Click delete icon to remove the chain

### Visual Feedback
- Circular progress shows at-a-glance completion
- Linear progress bar shows detailed percentage
- Checkboxes provide clear interaction points
- Strikethrough text indicates completed items
- Color-coding helps identify chains quickly

## Future Enhancements (TBD)

### Icon Picker
- Add predefined icon selection for chains
- Visual icons to quickly identify routines
- Material Icons or custom icon set

### Completion History Analytics
- Track when entire chains are completed
- Show streaks for consistent routine completion
- Widgets displaying chain progress
- Weekly/monthly statistics

### Live Activities (Android)
- Real-time progress on lock screen
- Quick actions to mark habits complete
- Dynamic Island-style updates (when available)

### Smart Scheduling
- Suggest optimal times based on history
- Adaptive reminders based on completion patterns
- Reorder habits within chains based on performance

## Testing

### Unit Tests
- **HabitChainTest**: Model validation and field testing
- **HabitChainProgressTest**: Progress calculation logic
  - Zero progress with no habits
  - Full progress with all habits completed
  - Partial progress scenarios
  - Date-based completion tracking

### Integration Tests
- Repository operations (insert, update, delete)
- DAO query validation
- Flow observation and updates

## Technical Decisions

### Why Comma-Separated Habit IDs?
- Simple implementation for v1
- Easy to parse and update
- Room supports string storage natively
- Can migrate to relation tables later if needed

### Why Calculate Progress at Runtime?
- Always shows current state
- No need to update chain on habit toggle
- Reduces database writes
- Single source of truth (habit completion history)

### Why Expand/Collapse?
- Reduces visual clutter with many chains
- Shows overview by default
- Details on demand
- Better UX for large routine lists

## Code Quality

### Follows Clean Architecture
- Clear separation between layers
- Repository pattern for data access
- ViewModels handle business logic
- Composables are pure and stateless

### Follows MVI Pattern
- Single source of truth (StateFlow)
- Unidirectional data flow
- Immutable state updates
- Clear event handling

### Kotlin Best Practices
- Null safety with nullable types
- Extension functions for utilities
- Data classes for models
- Coroutines for async operations
- Flow for reactive data

## Related Files

### Model & Data
- `app/src/main/java/com.mandrecode.tempo/data/model/HabitChain.kt`
- `app/src/main/java/com.mandrecode.tempo/data/local/dao/HabitChainDao.kt`
- `app/src/main/java/com.mandrecode.tempo/data/repository/HabitChainRepository.kt`

### UI
- `app/src/main/java/com.mandrecode.tempo/ui/routines/HabitCards.kt`
- `app/src/main/java/com.mandrecode.tempo/ui/routines/RoutinesScreen.kt`
- `app/src/main/java/com.mandrecode.tempo/ui/routines/RoutinesViewModel.kt`
- `app/src/main/java/com.mandrecode.tempo/ui/routines/RoutinesContract.kt`
- `app/src/main/java/com.mandrecode.tempo/ui/routines/RoutinesContent.kt`
- `app/src/main/java/com.mandrecode.tempo/ui/routines/components/HabitBottomSheet.kt`

### Tests
- `app/src/test/java/com.mandrecode.tempo/data/model/HabitChainTest.kt`
- `app/src/test/java/com.mandrecode.tempo/ui/routines/HabitChainProgressTest.kt`

## Accessibility

### Screen Reader Support
- Content descriptions for all icons
- Semantic labeling for progress indicators
- Checkbox labels for habit names

### Touch Targets
- Minimum 48dp touch targets for all interactive elements
- Adequate spacing between clickable areas
- Clear visual feedback on press

### Color Contrast
- High contrast text on backgrounds
- Progress indicators use theme colors
- Works in both light and dark modes

## Performance Considerations

- Progress calculated once per composition
- Remember keys prevent unnecessary recalculations
- Lazy evaluation of chain habits
- Efficient list filtering with mapNotNull
- Flow-based data observation (Room)

## Conclusion

The Habit Chains feature provides a powerful way to organize and track related habits. With visual progress tracking, interactive check-offs, and clean architecture, it enhances the app's capability to support comprehensive habit management and routine building.
