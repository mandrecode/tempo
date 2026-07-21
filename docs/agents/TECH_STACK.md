# TECH_STACK.md

## Project Context
- **App:** Tempo & Habits Tracker
- **Language:** Kotlin (JVM 21)
- **Min SDK:** 24 | **Target SDK:** 36

## Core Stack
- **UI:** Jetpack Compose (Material3)
- **Architecture:** Clean Architecture + MVI (Model-View-Intent) with Unidirectional Data Flow (UDF)
- **DI:** Hilt (using `jakarta.inject`)
- **Async:** Coroutines & Flow
- **Navigation:** **Navigation 3** (Type-safe Navigation API with `@Serializable` routes)

## Mandatory Libraries
- **Date/Time:** `kotlin.time` (Primary for durations), `kotlinx-datetime` (Secondary for dates/timestamps). *No `java.util.Date` or `java.time`*.
- **Serialization:** `kotlinx.serialization`
- **Database:** Room (with KSP for annotation processing), encrypted at rest via SQLCipher (`net.zetetic:sqlcipher-android`) — see [`docs/DB_ENCRYPTION.md`](../DB_ENCRYPTION.md)
- **Collections:** `kotlinx.collections.immutable` (For stable Compose state)
- **Adaptive layout:** `androidx.compose.material3.adaptive:adaptive` (BOM-managed) — window size classes via `currentWindowAdaptiveInfo()`; never use `LocalConfiguration` screen fields for layout decisions

## Directory Structure Strategy

### Gradle Modules

```text
Tempo/
├── app/        # Production Android application module
└── benchmark/  # Non-runtime macrobenchmark/test module targeting :app
```

Production domain, data, UI, and infrastructure code stays in `:app`. Additional modules should be
tooling/test/support modules unless a dedicated architecture change explicitly expands the runtime
module boundary.

### App Package Layout

```text
app/src/main/java/com.mandrecode.tempo/
├── core/                  # Shared components across features
│   ├── data/
│   │   ├── entity/        # Room entities (*Entity) and TypeConverters
│   │   ├── local/         # Room (TempoDatabase, DAOs)
│   │   │   └── dao/       # TaskDao, CategoryDao, HabitDao, HabitChainDao
│   │   └── preferences/   # SharedPreferences repos (interface + *Impl)
│   ├── di/                # Hilt modules
│   │   ├── DatabaseModule.kt
│   │   ├── DispatcherModule.kt    # @IoDispatcher, @DefaultDispatcher
│   │   ├── RepositoryModule.kt    # @Binds for domain/data repos
│   │   ├── PreferencesRepositoryModule.kt    # @Binds for SharedPreferences-backed repos
│   │   └── InfrastructureModule.kt
│   ├── domain/
│   │   └── model/         # Shared enums (Priority, Periodicity, DayOfWeek, ThemeMode, AppLanguage)
│   └── ui/
│       ├── theme/         # Color, Theme, Type, Spacing, ColorPalette, HabitIcon
│       ├── components/    # Generic reusable widgets
│       ├── navigation/    # Nav 3 configuration & Type-safe routes
│       └── util/          # UI utilities, Priority extensions
├── features/              # Feature-based organization
│   ├── tasks/
│   │   ├── data/
│   │   │   ├── mapper/    # TaskMapper, CategoryMapper
│   │   │   └── repository/ # TaskRepositoryImpl, CategoryRepositoryImpl
│   │   ├── domain/
│   │   │   ├── model/     # Task, Category
│   │   │   ├── repository/ # TaskRepository, CategoryRepository (interfaces)
│   │   │   └── usecase/   # CreateTask, UpdateTask, DeleteTask, ToggleTaskCompletion, etc.
│   │   └── presentation/
│   │       ├── TasksContract.kt, TasksViewModel.kt, TasksScreen.kt, TasksContent.kt
│   │       ├── model/     # UI-only models (SortOption)
│   │       └── components/ # TaskBottomSheet, cards/, dialogs/, sections/
│   ├── routines/
│   │   ├── data/
│   │   │   ├── mapper/    # HabitMapper, HabitChainMapper
│   │   │   └── repository/ # HabitRepositoryImpl, HabitChainRepositoryImpl
│   │   ├── domain/
│   │   │   ├── model/     # Habit, HabitChain
│   │   │   ├── repository/ # HabitRepository, HabitChainRepository (interfaces)
│   │   │   └── usecase/   # CreateHabit, UpdateHabit, DeleteHabit, ToggleHabitCompletion, etc.
│   │   └── presentation/
│   │       ├── RoutinesContract.kt, RoutinesViewModel.kt, RoutinesScreen.kt, RoutinesContent.kt
│   │       └── components/ # HabitBottomSheet, cards/, dialogs/, sections/
│   └── settings/
│       └── presentation/  # SettingsContract, SettingsViewModel, SettingsScreen, SettingsContent
├── infrastructure/        # Cross-cutting concerns
│   ├── permissions/       # PermissionChecker interface + PermissionCheckerImpl
│   ├── reminders/
│   │   ├── receivers/     # BroadcastReceivers
│   │   └── scheduler/     # Scheduler interfaces + implementations
│   └── liveactivity/      # HabitChainLiveActivityManager
├── util/                  # App-wide pure Kotlin helpers
├── MainActivity.kt
└── TempoApp.kt
```

## Operational Commands
- **Build:** `./gradlew assembleDebug`
- **Test:** `./gradlew testDebugUnitTest` (Target: 80%+ coverage)
- **Lint:**
  - `./gradlew ktlintCheck` (Code formatting)
  - `./gradlew :app:detekt` (Static analysis — code smells, complexity, naming)
- **Sync:** Perform Gradle Sync after `libs.versions.toml` changes

## Git & Commit Guidelines
> Full details in [`AGENTS.md` → Git Conventions](../../AGENTS.md#git-conventions)

- **Conventional Commits:** `<type>(#[ID]): <description>` or `<type>: <description>` (no ID)
- **Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`
- **Example:** `feat(#88): implement habit history graph`
