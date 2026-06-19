# Habit Chain Selector Feature

## Overview
Enhanced habit selector component for creating and managing habit chains with full reordering capability. This feature allows users to select habits, arrange them in a specific order, and manage their habit chains more effectively.

## User Story
As a user creating or editing a habit chain, I want to:
- See a clear list of available habits I can add to the chain
- Select habits from the available list
- See my selected habits in a clear, ordered list
- Reorder the selected habits to match my preferred sequence
- Remove habits from the selection if needed
- Distinguish between selected and available habits visually

## Features

### 1. Two-Section Layout
The habit selector is organized into two distinct sections:

#### Selected Habits Section
- **Visual Design**: Card-based container with surface variant background
- **Order Display**: Each habit shows its position number (1., 2., 3., etc.)
- **Color Indicators**: Small filled squares showing the habit's assigned color
- **Interactive Controls**: Up/down arrows and remove button for each habit
- **Section Header**: "Selected habits (in order)" label

#### Available Habits Section
- **Visual Design**: Horizontal scrollable row of filter chips
- **Interaction**: Tap any chip to add the habit to selected list
- **Section Header**: "Available habits" label
- **Empty State**: Message when no habits are available

### 2. Reordering Capability
Users can rearrange selected habits using intuitive controls:

#### Up/Down Arrows
- **Functionality**: Swap habit position with adjacent item
- **Visual Feedback**: Arrows are disabled (grayed out) at boundaries
- **First Item**: Up arrow is disabled (can't move higher)
- **Last Item**: Down arrow is disabled (can't move lower)
- **Icon Size**: 18dp for compact display
- **Touch Target**: 32dp buttons for accessibility

#### Remove Button
- **Functionality**: Remove habit from selected list
- **Visual Design**: Red X icon for clear action indication
- **Position**: Always enabled, on the right side of each habit
- **Effect**: Moves habit back to available section

### 3. Visual Feedback
Clear visual indicators for all states:

#### Selection State
- Selected habits appear in the ordered list with numbers
- Available habits appear as chips in the horizontal scrollable list
- Color indicators help identify habits quickly

#### Interaction States
- Arrow buttons change opacity when disabled
- All buttons maintain 48dp minimum touch targets for accessibility
- Smooth transitions when habits are added/removed/reordered

#### Empty States
- "No habits available. Create habits first." - When no habits exist
- "Please select at least one habit for the habit chain" - When selection is empty

## Technical Implementation

### Component Architecture
```kotlin
HabitMultiSelector(
    habits: List<Habit>,
    selectedHabitIds: List<Long>,
    onHabitsSelected: (List<Long>) -> Unit
)
```

#### Props
- `habits`: All available habits (filtered to exclude inverted habits)
- `selectedHabitIds`: Ordered list of currently selected habit IDs
- `onHabitsSelected`: Callback when selection changes

#### Internal Components
- `SelectedHabitItem`: Individual habit row with controls
- `FilterChip`: Available habit selection chips

### Utility Functions
Located in `util/HabitChainUtil.kt`:

```kotlin
fun moveHabitUp(habitIds: List<Long>, index: Int): List<Long>
fun moveHabitDown(habitIds: List<Long>, index: Int): List<Long>
```

#### Logic
- Simple swap algorithm: exchange positions with adjacent item
- Boundary checks prevent IndexOutOfBoundsException
- Returns original list if move is invalid (at boundary or invalid index)
- Immutable approach: returns new list instead of modifying input

### State Management
- Selection state maintained in parent component
- Order preserved in `selectedHabitIds` list
- Changes propagated through `onHabitsSelected` callback
- MVI pattern: unidirectional data flow

### Performance Optimizations
- Color resolution wrapped in `remember()` to prevent recalculation
- Efficient filtering for available habits
- Minimal recompositions through proper key usage

## User Experience

### Creating a Habit Chain
1. Open habit chain bottom sheet
2. Enter title and description
3. View available habits in horizontal scrollable list
4. Tap habits to add them to the chain
5. Selected habits appear in ordered list above
6. Reorder habits using up/down arrows if needed
7. Remove unwanted habits with X button
8. Save the habit chain

### Editing a Habit Chain
1. Existing habits appear in current order
2. Add new habits from available section
3. Reorder existing habits as needed
4. Remove habits that should no longer be in chain
5. Update the habit chain

### Visual Flow
```
Available Habits (horizontal chips)
  ↓ (tap to add)
Selected Habits (vertical list)
  - Habit 1 [↑] [↓] [X]
  - Habit 2 [↑] [↓] [X]
  - Habit 3 [↑] [↓] [X]
```

## Accessibility

### Touch Targets
- All interactive elements: 48dp minimum touch target
- Arrow buttons: 32dp button with 18dp icon
- Remove buttons: 32dp button with 18dp icon
- Filter chips: Standard Material3 dimensions

### Content Descriptions
- Up arrow: "Move up"
- Down arrow: "Move down"
- Remove button: "Remove habit"
- All icons have proper semantic labels for screen readers

### Visual Clarity
- High contrast text on backgrounds
- Clear disabled states (reduced opacity)
- Color indicators supplement text (not sole identifier)

## Edge Cases

### Empty List
- Displays "No habits available" message
- No interactive elements shown
- Prompts user to create habits first

### Single Habit
- Both arrows disabled (can't move)
- Remove button still functional

### All Habits Selected
- Available section becomes empty
- Only selected section visible
- Full reordering capability available

### Inverted Habits
- Filtered out from available habits
- Cannot be added to chains (per business rules)
- Prevents invalid configurations

## Localized Strings

### English (default)
- `select_habits`: "Select habits"
- `selected_habits`: "Selected habits (in order)"
- `available_habits`: "Available habits"
- `move_up`: "Move up"
- `move_down`: "Move down"
- `remove_habit`: "Remove habit"

### Usage
All strings use `stringResource(R.string.*)` for proper localization support.

## Testing

### Unit Tests (`HabitSelectorReorderingTest`)
- ✅ Move first item down
- ✅ Move second item up
- ✅ Move middle item down
- ✅ Move middle item up
- ✅ Move last item up
- ✅ Boundary: First item up (no-op)
- ✅ Boundary: Last item down (no-op)
- ✅ Single item list (both directions)
- ✅ Empty list (both directions)
- ✅ Multiple sequential moves

### Test Coverage
- All reordering scenarios covered
- Boundary conditions verified
- Edge cases handled
- Tests use same utility functions as production code

## Future Enhancements

### Potential Improvements
1. **Drag and Drop**: Touch and drag to reorder (more intuitive on mobile)
2. **Search/Filter**: Search available habits by name
3. **Bulk Selection**: Select multiple habits at once
4. **Habit Preview**: Show habit details on long-press
5. **Smart Suggestions**: Recommend habit order based on typical routines
6. **Undo/Redo**: History of reordering actions

### Not Planned
- Nested habit chains (complexity vs value)
- Habit groups within chains (over-engineering)
- Conditional habits (adds complexity)

## Related Files

### Implementation
- `app/src/main/java/com.mandrecode.tempo/ui/routines/components/HabitBottomSheet.kt`
- `app/src/main/java/com.mandrecode.tempo/util/HabitChainUtil.kt`
- `app/src/main/res/values/strings.xml`

### Tests
- `app/src/test/java/com.mandrecode.tempo/ui/routines/HabitSelectorReorderingTest.kt`

### Related Features
- [Habit Chains](./habit-chains.md)
- [Habit Chain Collapse](./habit-chain-collapse.md)
- [Inverted Habits Chains Restriction](./inverted-habits-chains-restriction.md)

## Design Principles

### Clean Architecture
- Separation of concerns maintained
- Utility functions in proper layer
- No business logic in UI components

### MVI Pattern
- Unidirectional data flow
- Immutable state updates
- Pure UI components

### Material Design 3
- Standard component usage (FilterChip, IconButton)
- Proper spacing and elevation
- Consistent with app theme

### Kotlin Best Practices
- Immutable data structures
- Extension functions for utilities
- Null safety throughout
- Clear, readable code

## Conclusion
The enhanced habit selector provides a user-friendly interface for creating and managing habit chains with full control over habit order. The implementation follows all architectural guidelines, provides comprehensive test coverage, and maintains excellent code quality.
