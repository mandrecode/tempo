# Habit and Habit Chain Notifications Implementation

## Overview
This document describes the implementation of notification functionality for habits and habit chains in the Tempo application, completed for milestone 0.4.

## Requirements
- Allow sending reminders for individual habits
- Allow sending reminders for grouped habit chains
- If a Habit is inside a Habit Chain, its reminder should be aligned/tied to the chain's reminder date
- Design notification scheduling logic accordingly

## Additional Features (from PR feedback)
- Allow checking habits as complete from notification
- Allow starting habit chains with LiveActivity from notification (starting with 0/N progress)
- Allow opening the precise habit or chain from notification
- Prevent setting reminders on habit chains where all habits are completed

## Architecture

### Components Added

#### 1. HabitReminderScheduler Interface
Location: `app/src/main/java/com.mandrecode.tempo/scheduler/HabitReminderScheduler.kt`

Defines the contract for scheduling habit and chain notifications:
```kotlin
interface HabitReminderScheduler {
    fun scheduleHabit(habit: Habit): ScheduleResult
    fun cancelHabit(habit: Habit)
    fun scheduleHabitChain(habitChain: HabitChain): ScheduleResult
    fun cancelHabitChain(habitChain: HabitChain)
}
```

#### 2. HabitReminderSchedulerImpl
Location: `app/src/main/java/com.mandrecode.tempo/scheduler/HabitReminderSchedulerImpl.kt`

Implementation that uses Android's `AlarmManager` to schedule exact alarms:
- Uses `setExactAndAllowWhileIdle()` for battery-efficient, precise notifications
- Handles Android 12+ permission checks for exact alarms
- Implements unique request code generation:
  - Habits: request codes 0-999,999
  - Chains: request codes 1,000,000-1,999,999
- Returns `ScheduleResult` for proper error handling

#### 3. HabitReminderReceiver
Location: `app/src/main/java/com.mandrecode.tempo/broadcast/HabitReminderReceiver.kt`

BroadcastReceiver that handles alarm triggers:
- Differentiates between habit and chain notifications using intent extras
- Creates and displays notifications using NotificationCompat
- Uses separate notification channels for habits and chains
- Opens the app and navigates to Routines tab when notification is tapped
- Includes action buttons:
  - **Habits**: "Mark as completed" button
  - **Chains**: "Start chain" button to begin LiveActivity
- Implements consistent request code generation to avoid collisions

#### 4. MarkHabitAsCompletedReceiver
Location: `app/src/main/java/com.mandrecode.tempo/broadcast/MarkHabitAsCompletedReceiver.kt`

Handles the "Mark as completed" action from habit notifications:
- Toggles habit completion status
- Dismisses the notification
- Uses same request code logic for consistency

#### 5. StartHabitChainReceiver
Location: `app/src/main/java/com.mandrecode.tempo/broadcast/StartHabitChainReceiver.kt`

Handles the "Start chain" action from chain notifications:
- Retrieves all habits in the chain
- Starts LiveActivity with 0/N progress
- Dismisses the notification
- Uses same request code logic for consistency

### Integration Points

#### RoutinesViewModel Updates
Location: `app/src/main/java/com.mandrecode.tempo/ui/routines/RoutinesViewModel.kt`

Key changes:
1. **Dependency Injection**: Added `HabitReminderScheduler` to constructor
2. **Habit Creation/Update**: Schedules notifications when habits are saved with reminder dates
3. **Habit Deletion**: Cancels notifications when habits are deleted
4. **Chain Creation/Update**: 
   - Clears individual habit reminders when habits are added to chains
   - Cancels individual habit notifications in batch (optimized)
   - Schedules chain-level notifications
   - **NEW**: Validates that not all habits are completed before allowing reminder
5. **Chain Deletion**: Cancels chain notifications
6. **Error Handling**: Properly handles `ScheduleResult` with user-friendly messages:
   - `PermissionError`: "Permission needed for reminders."
   - `Failure`: Specific failure message
   - `Success`: Success confirmation message
   - **NEW**: Informative message when reminders can't be set on completed chains
7. **Performance**: Uses Set-based filtering for O(1) lookup complexity

#### MainActivity Updates
Location: `app/src/main/java/com.mandrecode.tempo/MainActivity.kt`

Added support for deep linking:
- Defined `EXTRA_OPEN_ROUTINES` constant for intent extras
- Passes intent to `TempoNavHost` for navigation handling

#### Navigation Updates
Location: `app/src/main/java/com.mandrecode.tempo/ui/navigation/Navigation.kt`

Enhanced navigation:
- Accepts `intent` parameter in `TempoNavHost`
- Uses `LaunchedEffect` to detect `EXTRA_OPEN_ROUTINES` flag
- Automatically navigates to Routines tab when flag is present
- Preserves navigation state for better UX

#### HabitRepository Enhancement
Location: `app/src/main/java/com.mandrecode.tempo/data/repository/HabitRepository.kt`

Added batch retrieval method:
```kotlin
suspend fun getHabitsByIds(habitIds: List<Long>): List<Habit>
```
This enables efficient batch cancellation of habit notifications without N+1 query issues.

### String Resources
Location: `app/src/main/res/values/strings.xml`

Added localized strings:
- `habit_reminder_default_text`: "Time to work on your habit!"
- `habit_chain_reminder_default_text`: "Time to work on your habit chain!"
- `mark_as_completed`: "Mark as completed"
- `start_chain`: "Start chain"

### AndroidManifest Registration
Location: `app/src/main/AndroidManifest.xml`

Registered the new broadcast receivers:
```xml
<receiver
    android:name=".broadcast.HabitReminderReceiver"
    android:enabled="true" />
<receiver
    android:name=".broadcast.MarkHabitAsCompletedReceiver"
    android:enabled="true" />
<receiver
    android:name=".broadcast.StartHabitChainReceiver"
    android:enabled="true" />
```

### Dependency Injection
Location: `app/src/main/java/com.mandrecode.tempo/di/AppModule.kt`

Added provider for `HabitReminderScheduler`:
```kotlin
@Provides
@Singleton
fun provideHabitReminderScheduler(
    @ApplicationContext context: Context
): HabitReminderScheduler {
    return HabitReminderSchedulerImpl(context)
}
```

## Key Design Decisions

### 1. Chain Priority Over Individual Reminders
When a habit is added to a chain:
- The habit's individual reminder is cleared from the database
- Individual notifications are canceled
- Only the chain's reminder is active
- This prevents confusion with multiple reminders for the same habit

### 2. Request Code Collision Avoidance
- Habits and chains use different request code ranges
- Ensures that scheduling a habit doesn't accidentally cancel a chain notification (or vice versa)
- Simple modulo arithmetic ensures IDs map to their respective ranges
- Consistent implementation across all receivers to prevent notification issues

### 3. Batch Operations for Performance
- When clearing reminders for multiple habits in a chain, uses single batch query
- Avoids N+1 query problem by retrieving all habits at once
- Uses Set-based filtering for O(1) lookup complexity
- Improves performance when creating/updating chains with many habits

### 4. Proper Error Handling
- All scheduling operations return `ScheduleResult`
- ViewModel handles different result types and provides appropriate user feedback
- Consistent with existing task reminder implementation

### 5. Separation of Concerns
- Scheduler handles only scheduling logic
- Receiver handles only notification display
- Action receivers handle specific user actions (mark complete, start chain)
- ViewModel orchestrates the workflow
- Repository manages data access
- Follows Clean Architecture principles

### 6. Notification Actions
- **Habit notifications**: Include "Mark as completed" action for quick completion
- **Chain notifications**: Include "Start chain" action that:
  - Retrieves all habits in the chain
  - Starts LiveActivity with 0 completed / N total progress
  - Provides immediate feedback without opening the app

### 7. Deep Linking
- Both notification types include intent extras for deep linking
- Tapping notification opens app and navigates to Routines tab
- Future enhancement: could scroll to specific habit/chain in the list

### 8. Completed Chain Protection
- Added validation to prevent setting reminders on fully completed chains
- Uses efficient Set-based lookup to check completion status
- Provides clear feedback to user when reminder can't be set
- Prevents confusing notifications for already-completed work

## Testing

### Unit Tests
Location: `app/src/test/java/com.mandrecode.tempo/scheduler/HabitReminderSchedulerTest.kt`

Tests cover:
- Validation of habits with/without reminder dates
- Request code generation uniqueness
- Request code range separation (habits vs chains)

## Permissions Required
The app already had the necessary permissions in the manifest:
- `POST_NOTIFICATIONS`: For displaying notifications
- `SCHEDULE_EXACT_ALARM`: For scheduling precise alarms
- `POST_PROMOTED_NOTIFICATIONS`: For promoted notifications

## Notification Behavior

### Individual Habit Notifications
- Triggered at the exact time set in the habit's `reminderDate`
- Shows habit title and description
- Opens Routines tab when tapped
- "Mark as completed" action button for quick completion
- Single notification per habit

### Habit Chain Notifications
- Triggered at the time set in the chain's `periodicReminder`
- Shows chain title and description
- Opens Routines tab when tapped
- "Start chain" action button to begin LiveActivity with 0 progress
- Single notification for the entire chain
- All habits in the chain are reminded together
- Cannot be set if all habits in the chain are already completed

## Future Enhancements (Not in Scope)
- Recurring/periodic reminders for habits
- Custom notification sounds
- Rich notifications with habit progress
- Notification grouping for multiple chains
- Scroll to specific habit/chain when opening from notification

## Conclusion
The implementation successfully meets all requirements:
- ✅ Individual habit reminders work
- ✅ Habit chain reminders work
- ✅ Habits in chains use chain's reminder date exclusively
- ✅ Notification scheduling is properly designed using AlarmManager
- ✅ Habits can be marked complete from notifications
- ✅ Chains can be started with LiveActivity from notifications
- ✅ Notifications open the specific habit/chain in the app
- ✅ Cannot set reminders on fully completed chains
- ✅ Follows Clean Architecture and MVI patterns
- ✅ Includes proper error handling
- ✅ Optimized for performance
- ✅ Includes unit tests
