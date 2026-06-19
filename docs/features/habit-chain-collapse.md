# Habit Chain Expand/Collapse Feature

## Overview
This feature adds the ability to expand and collapse habit chains in the Routines screen. When collapsed, the progress bar and completion pill remain visible, while the individual habits are hidden. The chain automatically collapses with a subtle animation when all habits are completed.

## Requirements Met

### 1. Arrow Icon in Top Right Corner
- **Implementation**: Added `IconButton` with `Icons.Filled.KeyboardArrowDown` in the header row.
- **Animation**: Smooth 180° rotation (300ms) indicating collapse state.
  - 0° (pointing down) = Expanded.
  - 180° (pointing up) = Collapsed.

### 2. Visibility Management
When collapsed, the following remain visible:
- ✅ Chain title
- ✅ Linear progress bar (status bar)
- ✅ "X/Y done" pill
- ✅ Arrow icon for expanding

Only the individual habit items and checkboxes are hidden.

### 3. Auto-Collapse on Completion
- **Trigger**: When all habits in the chain are completed.
- **Delay**: 800ms to allow the user to see the 100% state.
- **Animation**: Smooth shrink and fade animation.

## Technical Implementation

### State Management
Stored in `RoutinesContract.UiState` using an `ImmutableSet<Long>` of collapsed chain IDs.
```kotlin
collapsedChainIds: ImmutableSet<Long> = persistentSetOf()
```

### UI Components
- **AnimatedVisibility**: Wraps the habit list for smooth transitions.
- **LaunchedEffect**: Handles the auto-collapse logic when progress reaches 100%.

## Testing
Comprehensive unit tests in `HabitChainCollapseTest.kt` cover:
- Default expanded state.
- Manual toggle logic.
- Independent state for multiple chains.
- State immutability for Compose stability.

## User Experience
This feature reduces visual clutter by allowing users to focus only on active routines while still providing at-a-glance progress for collapsed ones.
