# Habit Chain Deletion Options

## Overview
When deleting a habit chain, users are now presented with two options for handling the child habits:
1. **Keep habits as individual habits** - Only the chain is deleted, habits remain and inherit chain properties
2. **Delete chain and all habits** - Both the chain and all child habits are deleted

## User Experience

### Dialog Flow
When a user attempts to delete a habit chain, instead of a simple confirmation dialog, they see:
- Dialog title: "Delete Habit Chain"
- Message: "What would you like to do with the habits in this chain?"
- Two action buttons:
  - **"Keep habits as individual"** (primary color) - Recommended default option
  - **"Delete chain and all habits"** (error/destructive color) - Permanent deletion
- Cancel button to abort the operation

### Behavior Details

#### Option 1: Keep Habits as Individual
When this option is selected:
- The habit chain is deleted
- All child habits remain in the system
- If the chain had a periodic reminder configured:
  - Each habit inherits the chain's reminder time
  - Individual reminders are scheduled for each habit
- Habits retain their color (already inherited from chain during creation)
- Habits become independent and can be:
  - Edited individually
  - Added to new chains
  - Deleted individually

#### Option 2: Delete Chain and All Habits
When this option is selected:
- The habit chain is deleted
- All child habits are permanently deleted
- All reminders (both chain and individual habits) are cancelled
- This action cannot be undone

## Technical Implementation

### Components Modified

1. **DeleteHabitChainConfirmDialog.kt**
   - Changed from simple confirm/cancel dialog to dual-action dialog
   - Two buttons embedded in the text section of AlertDialog
   - Each button calls `onConfirm` with appropriate boolean flag
   - Uses Material3 design with animated button corners

2. **RoutinesViewModel.kt**
   - `deleteHabitChain(deleteHabits: Boolean = false)` method updated
   - When `deleteHabits = false`:
     - Parses habit IDs from chain
     - If chain has periodic reminder, transfers it to each habit
     - Schedules individual reminders for each habit
     - Deletes only the chain
   - When `deleteHabits = true`:
     - Retrieves all child habits
     - Deletes each habit and cancels its reminders
     - Deletes the chain

3. **strings.xml**
   - Added new string resources:
     - `delete_habit_chain_title`: "Delete Habit Chain"
     - `delete_habit_chain_message`: "What would you like to do with the habits in this chain?"
     - `delete_chain_keep_habits`: "Keep habits as individual"
     - `delete_chain_and_habits`: "Delete chain and all habits"

### Testing

Created comprehensive unit tests in `DeleteHabitChainWithOptionsTest.kt`:
- Test deletion with habits kept (no reminder transfer)
- Test deletion with habits kept and reminder transfer
- Test deletion with all habits removed
- Test edge case: empty habit chain

All tests verify:
- Correct repository method calls
- Proper reminder scheduling/cancellation
- UI state updates
- Success messages

## Design Decisions

### Why Two Buttons Instead of Checkbox?
Using two distinct buttons makes the choice more explicit and reduces the chance of accidental deletion. The user must actively select which action to take.

### Why Transfer Reminders?
When keeping habits as individuals, it makes sense to preserve the reminder functionality. Since the chain acted as a unified reminder, distributing it to individual habits maintains the user's intent to be reminded about these habits.

### Color Inheritance
Colors are already inherited when habits are added to a chain (see `proceedWithHabitChainCreation` in RoutinesViewModel), so no additional action is needed during deletion.

## Future Enhancements

Potential improvements could include:
- Option to selectively keep/delete specific habits
- Preview of habits that will be affected
- Undo functionality for deletion
- Bulk operations on multiple chains
