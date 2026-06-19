# Habit History Tracking Feature

## Overview
This feature adds completion history tracking to Habits and HabitChains, with a visual GitHub-style contribution graph that displays the last 21 days of completion status.

## Implementation Details

### Data Model Changes

#### Habit Model
The `Habit` model already included a `completionHistory` field that stores comma-separated dates in ISO format (e.g., "2024-01-15,2024-01-16,2024-01-17").

#### HabitChain Model
Added two new fields:
- `createdDate: LocalDateTime` - Tracks when the habit chain was created (defaults to current time)
- `completionHistory: String` - Stores comma-separated completion dates (defaults to empty string)

### Database Changes
- Database version updated from 14 to 15
- Added DAO methods for updating completion history
- Uses fallback to destructive migration (existing data will be reset)

### History Tracking Logic

#### CompletionHistoryUtil
A utility class that manages completion history:

```kotlin
// Add today's date when marking as completed
val updatedHistory = CompletionHistoryUtil.updateCompletionHistory(currentHistory, true)

// Remove today's date when marking as not completed
val updatedHistory = CompletionHistoryUtil.updateCompletionHistory(currentHistory, false)

// Get current streak
val streak = CompletionHistoryUtil.getCurrentStreak(completionHistory)
```

#### Automatic Updates
When `toggleHabitCompletion()` is called in the repository:
1. Updates the `isCompleted` flag
2. Automatically updates the `completionHistory` by adding/removing today's date

### UI Visualization

#### HabitHistoryView Component
A new Compose component that displays completion history as dots:

**Features:**
- Shows last 21 days by default (configurable via `maxDays` parameter)
- Dots are 10dp circles with 3dp spacing
- Color coding:
  - Primary color: Completed day
  - Surface variant: Day not completed
  - Transparent: Days before habit creation

**Usage:**
```kotlin
HabitHistoryView(
    completionHistory = habit.completionHistory,
    createdDate = habit.createdDate.date,
    modifier = Modifier.fillMaxWidth(),
    maxDays = 21 // optional, defaults to 21
)
```

#### Integration
The history view is displayed in:
- `HabitCard` - Shows individual habit completion history
- `HabitChainCard` - Shows habit chain completion history

## Testing

### Unit Tests
- `CompletionHistoryUtilTest`: 10 test cases covering:
  - Adding dates to history
  - Removing dates from history
  - Maintaining sorted order
  - Handling duplicates
  - Calculating streaks
  - Edge cases (empty history, gaps in streak)

- `HabitChainTest`: Updated to include new fields

### Manual Testing Scenarios

1. **Create New Habit**
   - Create a new habit
   - Verify the history view shows empty dots
   - Mark as completed for several days
   - Verify dots turn to primary color on completed days

2. **Toggle Completion**
   - Mark habit as completed today
   - Verify today's dot turns primary color
   - Mark as not completed
   - Verify today's dot returns to surface variant color

3. **History View Range**
   - Create a habit
   - Let it run for more than 21 days (or simulate with data)
   - Verify only last 21 days are shown

4. **Habit Chain**
   - Create a habit chain
   - Verify history view appears
   - Track completion over multiple days

## Future Enhancements

The completion history data enables future analytics features:
- Streak tracking widgets
- Weekly/monthly completion statistics
- Achievement badges for consistent habits
- Export history data
- Customizable history view (show more/fewer days)
- Heat map style visualization (varying intensities based on completion patterns)

## Migration Notes

For existing users:
- Existing habits will have empty completion history
- History tracking starts from the moment this feature is deployed
- Database migration uses fallback to destructive migration
- Users should backup data before updating

## API

### CompletionHistoryUtil

```kotlin
object CompletionHistoryUtil {
    // Update history with today's date
    fun updateCompletionHistory(currentHistory: String, isCompleted: Boolean): String
    
    // Update history with specific date
    fun updateCompletionHistoryForDate(
        currentHistory: String,
        date: LocalDate,
        isCompleted: Boolean
    ): String
    
    // Calculate current streak
    fun getCurrentStreak(completionHistory: String): Int
}
```

### Repository Methods

```kotlin
// HabitRepository
suspend fun toggleHabitCompletion(id: Long, isCompleted: Boolean)

// HabitChainRepository  
suspend fun toggleHabitChainCompletion(id: Long, isCompleted: Boolean)
```

## Performance Considerations

- Completion history is stored as a string to keep database simple
- Parsing happens in composables with `remember()` for efficiency
- For large histories (years of data), consider pagination or limiting view range
- Current implementation efficiently handles up to ~365 days of history
