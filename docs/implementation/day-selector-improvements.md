# Day Selector UI Improvements

## Changes Made

### Issue #1: FAB (Add Habit Button) Was Hidden

**Problem**: The bottom sheet was in partially expanded state, covering the FAB.

**Solution**: Added `skipPartiallyExpanded = true` to the modal bottom sheet state.

```kotlin
// Before
val sheetState = rememberModalBottomSheetState()

// After
val sheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true
)
```

**Result**: Bottom sheet now opens fully expanded, leaving the FAB visible and accessible.

---

### Issue #2: Day Selector Took Too Much Vertical Space

**Problem**: Used `FlowRow` which wrapped day chips across multiple lines, making the bottom sheet too tall.

**Solution**: Changed to `LazyRow` for horizontal scrolling (matching Tasks screen pattern).

```kotlin
// Before: FlowRow (wraps to multiple lines)
FlowRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    days.forEach { (day, labelRes) ->
        FilterChip(...)
    }
}

// After: LazyRow (single scrollable row)
LazyRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(horizontal = 4.dp)
) {
    items(days) { (day, labelRes) ->
        FilterChip(...)
    }
}
```

**Additional Improvements**:
- Moved "All days" indicator inline with the "Repeat on" label
- Reduced vertical spacing
- Consistent with Tasks screen UX

---

## Visual Comparison

### Before (FlowRow - Wrapping)
```
┌─────────────────────────────────────┐
│  Repeat on                          │
│                                     │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐           │
│  │Mon│ │Tue│ │Wed│ │Thu│           │
│  └───┘ └───┘ └───┘ └───┘           │
│  ┌───┐ ┌───┐ ┌───┐                 │
│  │Fri│ │Sat│ │Sun│                 │  <- Wraps to 2nd line
│  └───┘ └───┘ └───┘                 │
│                                     │
│  All days                           │  <- Separate line
│                                     │
└─────────────────────────────────────┘
   ↑ Takes 4-5 lines of vertical space
```

### After (LazyRow - Scrollable)
```
┌─────────────────────────────────────┐
│  Repeat on    All days              │  <- Inline
│                                     │
│  ┌───┬───┬───┬───┬───┬───┬──→      │
│  │Mon│Tue│Wed│Thu│Fri│Sat│Sun      │  <- Scrollable
│  └───┴───┴───┴───┴───┴───┴──→      │
│                                     │
└─────────────────────────────────────┘
   ↑ Takes only 2-3 lines (more compact!)
```

---

## Pattern Consistency

### Tasks Screen (Reference)
```kotlin
// TaskDialog.kt - Priority Selector
LazyRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(horizontal = 4.dp)
) {
    items(Priority.priorities) { priority ->
        FilterChip(...)
    }
}
```

### Routines Screen (Now Matching)
```kotlin
// DayOfWeekSelector.kt
LazyRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(horizontal = 4.dp)
) {
    items(days) { (day, labelRes) ->
        FilterChip(...)
    }
}
```

**Benefits of Consistency**:
1. Users already familiar with Tasks screen
2. Same scrolling behavior
3. Same visual style
4. Easier maintenance

---

## Technical Benefits

### Space Efficiency
- **Before**: ~80-100dp height (2 rows of chips + labels)
- **After**: ~40-50dp height (1 row + inline label)
- **Saved**: 40-50dp vertical space

### Better UX
- **Horizontal scroll**: Natural on mobile devices
- **All days visible**: Can see all options at once by scrolling
- **No wrapping**: Predictable layout on all screen sizes
- **Inline indicator**: Immediate feedback about "all days" state

### Performance
- **LazyRow**: Only renders visible items (though all 7 fit on most screens)
- **FlowRow**: Renders all items always
- **Scrolling**: Smooth native Android scrolling

---

## Commit Details

**Commit**: `70dae0f`
**Files Changed**: 2
- `DayOfWeekSelector.kt`: Changed from FlowRow to LazyRow
- `HabitBottomSheet.kt`: Added `skipPartiallyExpanded = true`

**Lines Changed**:
- Added: 45 lines
- Removed: 33 lines
- Net: +12 lines (mostly imports and restructuring)
