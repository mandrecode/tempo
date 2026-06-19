# Day-Based Habit Filtering - Visual Guide

## UI Layout

```
┌─────────────────────────────────────────┐
│                                         │
│          📱 Routines Screen             │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │         Routines                │   │ ← Headline
│  └─────────────────────────────────┘   │
│                                         │
│  ┌───────┬─────────┬─────────────┐     │
│  │Yester │ 📅TODAY │  Tomorrow   │     │ ← Day Filter Row
│  │  day  │(Selected)│             │     │   (NEW COMPONENT)
│  └───────┴─────────┴─────────────┘     │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │ 🟣 Morning Routine              │   │ ← Habit scheduled
│  │    • Meditation (✓)             │   │   for TODAY
│  │    • Exercise                   │   │
│  └─────────────────────────────────┘   │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │ 🟢 Study Programming            │   │ ← Habit with
│  │    Repeats: Mon, Wed, Fri       │   │   M/W/F pattern
│  └─────────────────────────────────┘   │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │ 🔵 Daily Journaling             │   │ ← Habit without
│  │    Repeats: Every day           │   │   specific days
│  └─────────────────────────────────┘   │
│                                         │
│                              ┌──────┐   │
│                              │  +   │   │ ← Add Habit FAB
│                              └──────┘   │
└─────────────────────────────────────────┘
```

## Habit Creation Flow

```
┌─────────────────────────────────────────┐
│     Create / Edit Habit                 │
│                                         │
│  Title: ___________________________     │
│                                         │
│  Description: _____________________     │
│                                         │
│  [Color Picker Section]                 │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │        Repeat on                │   │ ← NEW SECTION
│  │                                 │   │
│  │  ┌───┐ ┌───┐ ┌───┐ ┌───┐       │   │
│  │  │Mon│ │Tue│ │Wed│ │Thu│       │   │
│  │  └───┘ └───┘ └───┘ └───┘       │   │
│  │   ✓            ✓                │   │ ← Mon & Wed
│  │  ┌───┐ ┌───┐ ┌───┐             │   │   selected
│  │  │Fri│ │Sat│ │Sun│             │   │
│  │  └───┘ └───┘ └───┘             │   │
│  │   ✓                             │   │ ← Fri selected
│  │                                 │   │
│  │  Repeats on Mon, Wed, Fri       │   │
│  └─────────────────────────────────┘   │
│                                         │
│  [Reminder Section]                     │
│                                         │
│       [Cancel]         [Save]           │
└─────────────────────────────────────────┘
```

## Data Flow Diagram

```
┌──────────────────────────────────────────────────────────┐
│                     User Actions                         │
└──────────────────────────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────┐
│                   RoutinesViewModel                      │
│                                                          │
│  State:                                                  │
│  • selectedDate: LocalDate = today                       │
│  • selectedRepeatDays: Set<DayOfWeek>? = null            │
│                                                          │
│  Functions:                                              │
│  • selectDate(date)                                      │
│  • selectPreviousDay() / selectNextDay()                 │
│  • setRepeatDays(days)                                   │
│  • createOrUpdateHabit(title, desc) → includes repeatDays│
└──────────────────────────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────┐
│                   Data Layer (Room)                      │
│                                                          │
│  Habit:                                                  │
│  • id: Long                                              │
│  • title: String                                         │
│  • description: String                                   │
│  • repeatDays: Set<DayOfWeek>? ← NEW                     │
│                                                          │
│  Stored as: "1,3,5" (Mon=1, Wed=3, Fri=5)               │
│  null = all days                                         │
└──────────────────────────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────┐
│                  Filtering Logic                         │
│                                                          │
│  For each habit:                                         │
│    IF repeatDays == null OR isEmpty()                    │
│      → Show on ALL days                                  │
│    ELSE IF repeatDays.contains(selectedDayOfWeek)        │
│      → Show on this day                                  │
│    ELSE                                                  │
│      → Hide this habit                                   │
└──────────────────────────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────┐
│                    UI Render                             │
│                                                          │
│  • DayFilterRow shows selected date                      │
│  • LazyColumn shows filtered habits                      │
│  • DayOfWeekSelector shows current pattern              │
└──────────────────────────────────────────────────────────┘
```

## Example Scenarios

### Scenario 1: Gym Habit (Mon/Wed/Fri Only)

```
User creates "Go to Gym" habit
Selects: Mon, Wed, Fri

Storage: repeatDays = [MONDAY, WEDNESDAY, FRIDAY]
         Database: "1,3,5"

Viewing behavior:
┌────────────┬──────────────────┐
│    Day     │  Habit Visible?  │
├────────────┼──────────────────┤
│  Monday    │       ✅         │
│  Tuesday   │       ❌         │
│  Wednesday │       ✅         │
│  Thursday  │       ❌         │
│  Friday    │       ✅         │
│  Saturday  │       ❌         │
│  Sunday    │       ❌         │
└────────────┴──────────────────┘
```

### Scenario 2: Daily Journaling (Every Day)

```
User creates "Daily Journaling" habit
Does NOT select specific days (leaves default)

Storage: repeatDays = null
         Database: null

Viewing behavior:
┌────────────┬──────────────────┐
│    Day     │  Habit Visible?  │
├────────────┼──────────────────┤
│  Monday    │       ✅         │
│  Tuesday   │       ✅         │
│  Wednesday │       ✅         │
│  Thursday  │       ✅         │
│  Friday    │       ✅         │
│  Saturday  │       ✅         │
│  Sunday    │       ✅         │
└────────────┴──────────────────┘
```

### Scenario 3: Weekend Chores (Sat/Sun Only)

```
User creates "House Cleaning" habit
Selects: Sat, Sun

Storage: repeatDays = [SATURDAY, SUNDAY]
         Database: "6,7"

Viewing behavior:
┌────────────┬──────────────────┐
│    Day     │  Habit Visible?  │
├────────────┼──────────────────┤
│  Monday    │       ❌         │
│  Tuesday   │       ❌         │
│  Wednesday │       ❌         │
│  Thursday  │       ❌         │
│  Friday    │       ❌         │
│  Saturday  │       ✅         │
│  Sunday    │       ✅         │
└────────────┴──────────────────┘
```

## Component Hierarchy

```
RoutinesScreen
├── DayFilterRow ← NEW
│   ├── FilterChip (Yesterday)
│   ├── FilterChip (Today) ← Selected
│   └── FilterChip (Tomorrow)
│
├── LazyColumn (Habits List)
│   ├── HabitCard (filtered)
│   ├── HabitChainCard (filtered)
│   └── ...
│
└── FloatingActionButton
    └── onClick → showHabitBottomSheet()
        └── HabitBottomSheet
            ├── Title/Description Fields
            ├── ColorPicker
            ├── DayOfWeekSelector ← NEW
            │   ├── FilterChip (Mon)
            │   ├── FilterChip (Tue)
            │   ├── FilterChip (Wed)
            │   ├── FilterChip (Thu)
            │   ├── FilterChip (Fri)
            │   ├── FilterChip (Sat)
            │   └── FilterChip (Sun)
            ├── ReminderPicker
            └── Save/Cancel Buttons
```

## State Management Flow (MVI)

```
┌─────────────────────────────────────────────────────────┐
│                      UI Event                           │
│  (User taps "Tomorrow" button)                          │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│                   ViewModel                             │
│  selectNextDay()                                        │
│    → _uiState.update {                                  │
│         it.copy(selectedDate = tomorrow)                │
│       }                                                 │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│                   StateFlow                             │
│  uiState.value = RoutinesContract.UiState(               │
│    selectedDate = tomorrow,                             │
│    habits = [...],                                      │
│    ...                                                  │
│  )                                                      │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│                   UI Recompose                          │
│  remember(uiState.selectedDate, habits) {               │
│    habits.filter { habit →                              │
│      habit.repeatDays?.contains(dayOfWeek) != false     │
│    }                                                    │
│  }                                                      │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│                 Screen Updates                          │
│  • DayFilterRow highlights "Tomorrow"                   │
│  • LazyColumn shows filtered habits                     │
└─────────────────────────────────────────────────────────┘
```

## Database Schema Changes

```sql
-- Before (v16)
CREATE TABLE habits (
    id INTEGER PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    colorKey TEXT,
    reminderDate TEXT,
    isCompleted INTEGER,
    isInverted INTEGER,
    createdDate TEXT,
    completionHistory TEXT
);

-- After (v17)
CREATE TABLE habits (
    id INTEGER PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    colorKey TEXT,
    reminderDate TEXT,
    isCompleted INTEGER,
    isInverted INTEGER,
    createdDate TEXT,
    completionHistory TEXT,
    repeatDays TEXT  ← NEW (stores "1,3,5" or NULL)
);
```

## Key Design Decisions

### 1. Null vs Empty vs Explicit All Days

**Chosen**: `null` = all days

```kotlin
// Option A: null = all days (CHOSEN ✅)
repeatDays: Set<DayOfWeek>? = null

// Option B: Empty = all days (NOT chosen)
repeatDays: Set<DayOfWeek> = emptySet()

// Option C: Explicit all days (NOT chosen)
repeatDays: Set<DayOfWeek> = DayOfWeek.entries.toSet()
```

**Rationale**:
- ✅ More storage efficient (no data vs 7 integers)
- ✅ Clearer intent: absence = no restriction
- ✅ Backward compatible: existing habits work correctly
- ✅ Easier to check: `if (days == null || days.isEmpty())`

### 2. Custom DayOfWeek vs kotlinx-datetime

**Chosen**: Custom enum with conversion

```kotlin
// Our enum (CHOSEN ✅)
enum class DayOfWeek(val value: Int) {
    MONDAY(1), TUESDAY(2), ...
}

// Alternative: Use kotlinx.datetime.DayOfWeek directly
// NOT chosen because it's platform-dependent
```

**Rationale**:
- ✅ Clean Architecture: no platform deps in domain
- ✅ Simple serialization: just store integers
- ✅ Easy conversion when needed for UI

### 3. Filtering Location: UI vs Repository

**Chosen**: Filter in UI layer

```kotlin
// In RoutinesScreen (CHOSEN ✅)
val filteredHabits = remember(habits, selectedDate) {
    habits.filter { ... }
}

// Alternative: Filter in Repository
// NOT chosen
```

**Rationale**:
- ✅ Filtering is a VIEW concern
- ✅ Doesn't affect data storage
- ✅ Easier to add more filters
- ✅ Follows MVI: transform in ViewModel/UI
