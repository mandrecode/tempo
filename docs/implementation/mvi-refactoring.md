# MVI Refactoring & Clean Architecture Implementation

## Overview
Standardized all features to follow MVI (Model-View-Intent) pattern with Contract-based state management, introduced a full Clean Architecture domain layer with pure domain models, mappers, repository interfaces, and reorganized component structure.

## Phases

### Phase 1: MVI Contract Pattern (All Features)
Each feature now follows the same structure:
- **`[Feature]Contract.kt`** — Defines `UiState`, `UiEvent` (sealed interface), and `UiEffect`
- **`[Feature]Screen.kt`** — Thin wrapper: `hiltViewModel()`, `collectAsStateWithLifecycle()`, effect handling
- **`[Feature]Content.kt`** — Pure UI composable: takes `UiState` + `(UiEvent) -> Unit`, no ViewModel reference
- **`[Feature]ViewModel.kt`** — Single `onEvent()` dispatcher, all internal methods private

### Phase 2: Domain Layer (Repository Interfaces)
- Created `domain/repository/` with interfaces for all 7 repositories
- Concrete implementations in `data/repository/` implement these interfaces
- `di/RepositoryModule.kt` uses Hilt `@Binds` to wire interface → implementation
- All consumers (ViewModels, BroadcastReceivers, etc.) depend on interfaces, not concrete classes

### Phase 3: Full Domain Separation
- **Pure Domain Models** (`domain/model/`): Task, Habit, HabitChain, Category, DayOfWeek, Periodicity, Priority, ThemeMode, AppLanguage — no Android or Room dependencies
- **Room Entity Renames** (`data/model/`): Task→TaskEntity, Habit→HabitEntity, HabitChain→HabitChainEntity, Category→CategoryEntity
- **Mappers** (`data/mapper/`): TaskMapper, HabitMapper, HabitChainMapper, CategoryMapper with `toDomain()`/`toEntity()` extension functions
- **UI Extensions** (`ui/util/PriorityExtensions.kt`): Android-specific properties (`Priority.titleResId`, `Priority.color`) moved out of domain

### Phase 4: Component Reorganization
Standardized routines components to match tasks structure:
```
components/
├── HabitBottomSheet.kt      (or TaskBottomSheet.kt)
├── cards/                   # Card composables
├── dialogs/                 # Confirmation dialogs
└── sections/                # Section composables (filters, selectors, etc.)
```

### Phase 5: Entity Layer Cleanup
- **Renamed** `data/model/` → `data/entity/` to clearly signal "Room persistence layer"
- **Deleted 5 duplicate enums** from data layer (DayOfWeek, Periodicity, Priority, AppLanguage, ThemeMode) — entities now import domain enums directly
- **Simplified mappers** — removed `valueOf(it.name)` bridging since entities and domain models share the same enum types
- **Moved `SortOption`** from `data/model/` to `ui/tasks/model/` (UI-only concern)
- Result: `data/entity/` contains only Room entities + Converters. `domain/model/` is the single source of truth for all models and enums.

## Files Created
- `domain/model/` — 9 pure domain models and enums
- `domain/repository/` — 7 repository interfaces
- `data/mapper/` — 4 mapper files (TaskMapper, HabitMapper, HabitChainMapper, CategoryMapper)
- `di/RepositoryModule.kt` — Hilt `@Binds` module
- `ui/util/PriorityExtensions.kt` — Priority UI extensions
- `ui/tasks/TasksContract.kt`, `ui/tasks/TasksContent.kt`
- `ui/routines/RoutinesContract.kt`, `ui/routines/RoutinesContent.kt`
- `ui/settings/SettingsContent.kt` (extracted from SettingsScreen.kt)

## Files Reorganized
- `ui/routines/HabitCards.kt` → `ui/routines/components/cards/HabitCards.kt`
- 4 confirm dialogs → `ui/routines/components/dialogs/`
- DayFilterRow, DayOfWeekSelector, HabitHistoryView, IconPicker → `ui/routines/components/sections/`

## Tests Added
- `data/model/ConvertersTest.kt` — Room TypeConverter tests
- `data/model/DayOfWeekTest.kt` — Enum mapping tests
- `data/repository/CategoryRepositoryTest.kt` — Repository delegation tests
- `data/repository/HabitChainRepositoryTest.kt` — Repository + completion history tests
- `data/mapper/TaskMapperTest.kt` — Task entity/domain round-trip tests
- `data/mapper/HabitMapperTest.kt` — Habit entity/domain round-trip tests
- `data/mapper/HabitChainMapperTest.kt` — HabitChain entity/domain round-trip tests
- `data/mapper/CategoryMapperTest.kt` — Category entity/domain round-trip tests
- `domain/model/AppLanguageTest.kt` — AppLanguage.fromString enum tests
- `ui/util/PriorityExtensionsTest.kt` — Priority color and titleResId mapping tests
- `ui/util/SortOptionUtilsTest.kt` — SortOption icon mapping tests
- `ui/util/ColorSelectionUtilTest.kt` — Random color selection logic tests
- `ui/routines/RoutinesContentKtTest.kt` — formatTime helper tests
- Total unit tests: 246 (up from 162)

## Android Instrumented Tests Added
- `data/repository/ThemePreferencesRepositoryTest.kt` — SharedPreferences theme persistence
- `data/repository/NavigationPreferencesRepositoryTest.kt` — Tab toggles, default tab, route persistence
- `data/repository/AppPreferencesRepositoryTest.kt` — App language preference persistence
- `ui/routines/RoutinesContentTest.kt` — Compose UI: loading, empty state, habit display, FAB
- `ui/settings/SettingsContentTest.kt` — Compose UI: version display, theme modes, tab toggles
- `util/DateTimeFormatterTest.kt` — Date formatting with natural dates (today/tomorrow/yesterday)
- Total androidTests: 7 test files (up from 1)
