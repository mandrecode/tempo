# Inverted Habits Cannot Be In Chains

## Overview
This feature prevents inverted habits from being added to habit chains. Inverted habits track the absence of an action (e.g., "no smoking", "no caffeine"), which doesn't conceptually fit into sequential habit chains meant for completing a series of positive actions.

## User Experience

### Before
Users could see and select inverted habits when creating or editing habit chains, which could lead to confusion and invalid chain configurations.

### After
When creating or editing a habit chain, only non-inverted habits are displayed in the habit selector. Inverted habits are automatically filtered out and never appear as options.

## Implementation Details

### Components Modified

#### 1. HabitChainUtil.kt
Added a new utility function `filterHabitsForChain()`:
```kotlin
/**
 * Filters habits to only include those that can be added to a habit chain.
 * Inverted habits cannot be added to chains.
 *
 * @param habits The list of habits to filter.
 * @return A list of habits that can be added to a chain (non-inverted habits).
 */
fun filterHabitsForChain(habits: List<Habit>): List<Habit> {
    return habits.filter { !it.isInverted }
}
```

#### 2. HabitBottomSheet.kt
Updated the `HabitMultiSelector` call to use the filtering function:
```kotlin
HabitMultiSelector(
    habits = filterHabitsForChain(habits),  // Previously: habits
    selectedHabitIds = selectedHabitIds,
    onHabitsSelected = { selectedHabitIds = it }
)
```

### Test Coverage
Added 4 comprehensive unit tests in `HabitChainUtilTest.kt`:
1. **filterHabitsForChain_filtersOutInvertedHabits**: Verifies mixed list filtering
2. **filterHabitsForChain_returnsAllNonInvertedHabits**: Verifies all non-inverted pass through
3. **filterHabitsForChain_returnsEmptyWhenAllHabitsAreInverted**: Tests edge case
4. **filterHabitsForChain_returnsEmptyForEmptyList**: Tests empty list handling

## Technical Rationale

### Why This Restriction?
1. **Conceptual Mismatch**: Inverted habits track NOT doing something, while chains are for completing sequential actions
2. **User Confusion**: Including inverted habits in chains could confuse users about completion semantics
3. **Consistency**: Inverted habits already have restrictions (e.g., cannot have reminders per line 448 in HabitBottomSheet.kt)

### Design Decisions
- **Filter at UI Layer**: Filtering happens transparently in the UI, preventing invalid selections from the start
- **Reusable Function**: Created a utility function for potential future use in other contexts
- **Minimal Changes**: Only touched necessary files to reduce risk

## Related Features
- **Inverted Habits**: See habit model with `isInverted` flag
- **Habit Chains**: See `docs/features/habit-chains.md` for chain functionality
- **Habit Reminders**: Inverted habits also cannot have time-based reminders

## Future Considerations
- If habits already in chains are converted to inverted, they should be automatically removed from chains
- Consider showing a message when all habits are inverted and no habits are available for chains
