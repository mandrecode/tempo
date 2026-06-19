# Habit Chain Live Activities

## Overview
This feature implements Live Activities (ongoing notifications) for habit chains. When a user starts completing habits within a chain, a persistent notification appears showing real-time progress. The notification updates as each habit is completed and automatically dismisses when all habits in the chain are done.

## Implementation Details

### Components Created

#### 1. HabitChainLiveActivityManager
**Location:** `app/src/main/java/com.mandrecode.tempo/liveactivity/HabitChainLiveActivityManager.kt`

A singleton manager class responsible for creating and managing ongoing notifications for habit chains.

**Key Features:**
- Creates notification channel specifically for habit chain progress
- Tracks active live activities to avoid duplicates
- Updates notifications with progress bar and completion count
- Automatically transitions notification from "ongoing" to "auto-cancel" when chain completes via the notification action button
- Uses unique notification IDs (offset by 100000) to avoid conflicts with task reminders

**Public Methods:**
- `updateLiveActivity(chain: HabitChain, completedCount: Int, totalCount: Int, currentHabitId: Long?, currentHabitTitle: String?, fromNotification: Boolean)` - Creates or updates a live activity. When all habits are complete, behavior depends on `fromNotification` (see below).
- `dismissLiveActivity(chainId: Long)` - Dismisses an active live activity
- `hasActiveLiveActivity(chainId: Long): Boolean` - Checks if a chain has an active notification

### Integration Points

#### 2. HabitRepository Enhancement
**Location:** `app/src/main/java/com.mandrecode.tempo/data/repository/HabitRepository.kt`

**Changes:**
- Added `HabitChainDao` dependency to query chains containing habits
- Added `HabitChainLiveActivityManager` dependency
- Enhanced `toggleHabitCompletion()` to trigger live activity updates
- Created `updateLiveActivitiesForHabit()` private method that:
  - Finds all chains containing the toggled habit
  - Calculates completion progress for each chain
  - Creates/updates live activity when at least one habit is completed
  - Dismisses live activity when no habits are completed

### User-Facing Behavior

#### When First Habit is Checked
When a user checks the first habit in a chain:
1. A notification appears with the chain title
2. Shows "X of Y habits completed" text
3. Displays a progress bar showing completion percentage
4. Notification is marked as "ongoing" (persistent in notification shade)
5. Tapping the notification opens the app

#### During Progress
As more habits are checked:
1. Notification updates immediately with new count
2. Progress bar advances
3. Notification remains ongoing

#### When All Habits are Completed
The behavior when the last habit is checked depends on how it was completed:

**From within the app:**
1. All linked notifications (live activity and chain reminder) are immediately dismissed
2. No success notification is created

**From the notification action button ("Complete" action):**
1. The live activity transitions to a "Chain completed! 🎉" success notification (auto-cancel, non-ongoing)
2. The chain reminder notification is not cancelled by the live activity manager

#### When Habits are Unchecked
If all habits in a chain are unchecked:
1. The live activity notification is automatically dismissed

### String Resources
**Location:** `app/src/main/res/values/strings.xml`

Added strings:
- `habit_chain_progress_title` - "Chain Name in progress"
- `habit_chain_completed_title` - "Chain Name completed!"
- `habit_chain_progress_text` - "X of Y habits completed"
- `habit_chain_all_done` - "All habits completed! 🎉"

## Technical Considerations

### Notification Channel
- **ID:** `habit_chain_live_activity_channel`
- **Importance:** DEFAULT
- **Purpose:** Unobtrusive progress tracking

### Thread Safety
- The `activeChains` set is managed in-memory and accessed only from coroutine contexts
- All database operations are suspend functions ensuring proper threading

### Performance
- Minimal overhead: Only calculates progress for chains that contain the toggled habit
- Uses Flow.first() for one-time chain lookup
- No background services or periodic updates needed

## Dependencies
- Android NotificationManager and NotificationCompat
- Hilt for dependency injection
- Kotlin coroutines for async operations
- kotlinx-datetime for date handling

## Future Enhancements
Possible improvements:
1. Add notification actions (e.g., "View Chain" button)
2. Persist active chains across app restarts
3. Customize notification priority per chain
4. Add notification sound/vibration options
5. Show individual habit names in expanded notification
