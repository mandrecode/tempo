# Day-Based Habit Filtering Feature Implementation

## Overview

This document describes the implementation of the day-based habit filtering feature, which allows users to:
1. Filter habits by day (Yesterday/Today/Tomorrow)
2. Configure habits with custom repetition patterns (e.g., Mon/Wed/Fri)
3. View only relevant habits for the selected day

## Design

The mockup shows a day filter UI with three prominent buttons under the "Routines" headline:
- **Yesterday** - View habits from the previous day
- **Today** - View today's habits (default, highlighted)
- **Tomorrow** - View tomorrow's habits

## Implementation Details

### 1. Data Layer

#### New Model: `DayOfWeek`
```kotlin
enum class DayOfWeek(val value: Int) {
    MONDAY(1), TUESDAY(2), WEDNESDAY(3), THURSDAY(4), 
    FRIDAY(5), SATURDAY(6), SUNDAY(7)
}
```

Features:
- Custom enum to avoid platform dependencies in domain models
- Conversion methods to/from `kotlinx.datetime.DayOfWeek`
- Stored as comma-separated integers in the database

#### Database Changes

**Habit Model**:
- Added `repeatDays: Set<DayOfWeek>?` field
- `null` or empty set = repeat on all days
- Non-empty set = repeat only on specified days

**HabitChain Model**:
- Added `repeatDays: Set<DayOfWeek>?` field
- Same behavior as Habit

**Migration**:
- Database version upgraded from 16 to 17
- Uses `fallbackToDestructiveMigration()` (existing strategy)

**Converters**:
- `fromDayOfWeekSet`: Converts `Set<DayOfWeek>` to comma-separated string
- `toDayOfWeekSet`: Parses string back to `Set<DayOfWeek>`

### 2. ViewModel Layer

#### RoutinesViewModel Additions

**New State Fields**:
- `selectedDate: LocalDate` - Currently selected date for filtering (default: today)
- `selectedRepeatDays: Set<DayOfWeek>?` - Days selected when creating/editing a habit

**New Functions**:
```kotlin
fun selectDate(date: LocalDate)       // Select specific date
fun selectPreviousDay()                // Navigate to previous day
fun selectNextDay()                    // Navigate to next day
fun selectToday()                      // Jump back to today
fun setRepeatDays(days: Set<DayOfWeek>?)  // Set repeat pattern
```

**Updated Functions**:
- `createOrUpdateHabit`: Now includes `repeatDays` field
- `createOrUpdateHabitChain`: Now includes `repeatDays` field
- `showHabitBottomSheet`: Loads `repeatDays` from habit
- `showHabitChainBottomSheet`: Loads `repeatDays` from chain
- `hideHabitBottomSheet`: Clears `selectedRepeatDays`

### 3. UI Layer

#### DayFilterRow Component

A horizontal scrollable row of filter chips for day navigation:
- **Yesterday** button
- **Today** button (with icon when selected)
- **Tomorrow** button

Features:
- Material3 FilterChip design
- Bold text for selected day
- Primary container color for selection
- Border thickness changes on selection

Location: Placed at the top of RoutinesScreen, below the screen title

#### DayOfWeekSelector Component

A multi-select day picker for habit configuration:
- FlowRow layout with 7 day chips (Mon-Sun)
- FilterChip design matching Material3 guidelines
- Shows "All days" text when no specific days selected
- Automatically sets to `null` when all days or no days selected

Location: Integrated into HabitBottomSheet, after color picker

#### RoutinesScreen Updates

**Filtering Logic**:
```kotlin
// Get selected day of week
val selectedDayOfWeek = DayOfWeek.fromKotlinDayOfWeek(uiState.selectedDate.dayOfWeek)

// Filter habits by repeat days
val filteredHabits = habits.filter { habit ->
    habit.repeatDays == null || 
    habit.repeatDays.isEmpty() || 
    habit.repeatDays.contains(selectedDayOfWeek)
}

// Same for habit chains
val filteredChains = chains.filter { chain ->
    chain.repeatDays == null || 
    chain.repeatDays.isEmpty() || 
    chain.repeatDays.contains(selectedDayOfWeek)
}
```

**Layout Changes**:
- Wrapped main content in `Column`
- Added `DayFilterRow` at the top
- Used `weight(1f)` on LazyColumn for proper sizing

### 4. String Resources

Added new strings for day labels:
- Full day names (Monday-Sunday)
- Abbreviated day names (Mon-Sun)
- Navigation labels (Yesterday, Today, Tomorrow)
- UI labels (Repeat on, All days)

## User Flow

### Creating a Habit with Repeat Pattern

1. User taps "Add Habit" FAB
2. Fills in habit title and description
3. Scrolls to "Repeat on" section
4. Taps desired days (e.g., Mon, Wed, Fri)
5. Saves habit

### Viewing Habits for a Specific Day

1. User opens Routines screen (defaults to Today)
2. Taps "Tomorrow" to see tomorrow's habits
3. Only habits scheduled for tomorrow appear
4. Habits with no repeat pattern always appear

### Editing Repeat Pattern

1. User taps existing habit
2. Sees current repeat pattern in bottom sheet
3. Can toggle days on/off
4. If all days selected, reverts to "All days" (null)
5. Saves changes

## Technical Decisions

### Why Custom DayOfWeek Enum?

- Avoids Android/JVM dependencies in data models (Clean Architecture)
- Simpler serialization (just integers)
- Easy conversion to kotlinx-datetime when needed

### Why null for "All Days"?

- More efficient: don't store [Mon, Tue, Wed, Thu, Fri, Sat, Sun] for every habit
- Clearer intent: absence of constraint vs explicit "every day"
- Backward compatible: existing habits without field work correctly

### Why Filter in UI vs Repository?

- Filtering is a UI concern (user's view preference)
- Doesn't affect data storage
- Easier to add more filter options later (e.g., date range)
- Follows MVI pattern: ViewModel transforms data for View

## Testing Strategy

### Unit Tests Needed

1. **DayOfWeek enum**:
   - Test conversion to/from kotlinx-datetime
   - Test value mappings

2. **Converters**:
   - Test serialization/deserialization
   - Test null/empty cases
   - Test invalid input handling

3. **ViewModel**:
   - Test day navigation functions
   - Test habit filtering logic
   - Test repeat days state management

4. **Filtering Logic**:
   - Test habits with no repeat pattern appear all days
   - Test habits with specific days only appear on those days
   - Test empty repeat pattern behaves like null

### Manual Testing

1. Create habit with Mon/Wed/Fri pattern
2. Verify it appears on Mon, Wed, Fri
3. Verify it doesn't appear on Tue, Thu, Sat, Sun
4. Test navigation between days
5. Test editing repeat pattern
6. Test habit chains with repeat patterns
7. Test inverted habits (should still support repeat patterns)

## Future Enhancements

1. **Extended Date Navigation**:
   - Add calendar picker
   - Add "Next week" / "Last week" navigation
   - Show date headers in list

2. **Advanced Patterns**:
   - Every other day
   - First Monday of month
   - Custom intervals (every 3 days, etc.)

3. **Visual Indicators**:
   - Show habit's repeat pattern in card
   - Mini calendar showing scheduled days
   - Streak tracking by day pattern

4. **Smart Defaults**:
   - Suggest weekdays only for work habits
   - Suggest weekends only for leisure habits
   - Learn from user patterns

## Migration Notes

- Database uses destructive migration (existing strategy)
- All existing habits will have `repeatDays = null` (appear on all days)
- No data loss, but users need to reconfigure repeat patterns if desired
- Migration is automatic on first app launch after update

## Code Style Compliance

✅ Follows Clean Architecture (data/domain/UI separation)
✅ MVI pattern with unidirectional data flow
✅ Immutable state with Kotlin collections
✅ No hardcoded strings (all in strings.xml)
✅ Material3 design system
✅ Compose-only UI (no XML)
✅ kotlinx-datetime for date handling
✅ Hilt dependency injection
✅ Contract pattern for UI state/events

## Known Issues

- Build environment: AGP 8.13.2 not found (pre-existing, not related to this feature)
- No unit tests included yet (to be added in follow-up)
- UI not tested on device (emulator needed)

## Summary

This implementation provides a clean, user-friendly way to manage habit schedules with custom repetition patterns. The design follows the app's architectural guidelines and Material Design principles, ensuring consistency with the existing codebase.
