# TECH_STACK.md

## Project Context
- **App:** Tempo & Habits Tracker
- **Language:** Kotlin (JVM 21)
- **Min SDK:** 24 | **Target SDK / Compile SDK:** 37

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
- **Background work:** `androidx.work:work-runtime-ktx` for deferrable work, `AlarmManager` for exact reminders and focus-session ends
- **Home-screen widget:** Glance (`androidx.glance:glance-appwidget`, `glance-material3`)
- **Screenshot testing:** `com.android.compose.screenshot` Gradle plugin + `com.android.tools.screenshot:screenshot-validation-api` (`@PreviewTest`). Renders previews on the JVM via layoutlib; requires `android.experimental.enableScreenshotTest=true` in both `gradle.properties` and the module's `android` block

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
│   │   │   └── dao/       # TaskDao, CategoryDao, HabitDao, HabitChainDao,
│   │   │                  #   HabitChainMemberDao, DailyFocusActivityDao
│   │   ├── mapper/        # Mappers for cross-feature entities (DailyFocusActivity)
│   │   ├── repository/    # Repo impls for cross-feature entities
│   │   └── preferences/   # SharedPreferences repos (interface + *Impl)
│   ├── di/                # Hilt modules
│   │   ├── DatabaseModule.kt
│   │   ├── DispatcherModule.kt    # @IoDispatcher, @DefaultDispatcher
│   │   ├── RepositoryModule.kt    # @Binds for domain/data repos
│   │   ├── PreferencesRepositoryModule.kt    # @Binds for SharedPreferences-backed repos
│   │   ├── RemindersModule.kt
│   │   └── InfrastructureModule.kt
│   ├── domain/
│   │   ├── model/         # Shared enums (Priority, Periodicity, DayOfWeek, ThemeMode,
│   │   │                  #   AppLanguage) and cross-feature models (DailyFocusActivity)
│   │   ├── repository/    # Cross-feature repository interfaces
│   │   ├── usecase/       # Cross-feature use cases and recorder interfaces
│   │   └── util/
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
│   ├── focus/
│   │   ├── data/          # FocusSessionRepositoryImpl (SharedPreferences-backed)
│   │   ├── domain/
│   │   │   ├── model/     # FocusAgenda, FocusAgendaItem, FocusSession, TaskFocusToday
│   │   │   ├── repository/ # FocusSessionRepository (interface)
│   │   │   ├── scheduler/ # FocusSessionScheduler (interface)
│   │   │   └── usecase/   # GetFocusAgenda, GetUpNextItem, RecordDailyActivity, etc.
│   │   └── presentation/
│   │       ├── FocusContract.kt, FocusViewModel.kt, FocusScreen.kt, FocusContent.kt
│   │       ├── FocusSessionScreen.kt   # The session's own destination
│   │       └── components/ # UpNextCard, RunningSessionCard, SessionFinishedSheet, etc.
│   ├── backup/
│   │   ├── data/          # Versioned DTOs, mappers, BackupRepositoryImpl
│   │   └── domain/        # ImportMode, MergePlan, validation, use cases
│   ├── onboarding/
│   │   └── presentation/  # First-run setup and permission education
│   ├── whatsnew/
│   │   └── presentation/  # WhatsNewRegistry + the post-update bottom sheet
│   ├── widget/
│   │   └── presentation/  # QuickAddTaskWidget (Glance)
│   └── settings/
│       └── presentation/  # SettingsContract, SettingsViewModel, SettingsScreen, SettingsContent,
│                          #   plus per-area sections (Reminders, Focus, Backup, VacationMode, …)
├── infrastructure/        # Cross-cutting concerns
│   ├── permissions/       # PermissionChecker interface + PermissionCheckerImpl
│   ├── notifications/     # Channels, notifiers, notification sync
│   ├── reminders/
│   │   ├── receivers/     # BroadcastReceivers
│   │   ├── workers/       # WorkManager reschedule work
│   │   └── scheduler/     # Scheduler interfaces + implementations
│   ├── focus/             # Session alarms and their receivers
│   ├── backup/            # Backup file I/O and reminder scheduling
│   ├── security/          # BackupEncryptionService (passphrase-derived export encryption)
│   ├── tasks/             # Completed-task cleanup scheduling and its worker
│   └── liveactivity/      # HabitChainLiveActivityManager
├── util/                  # App-wide pure Kotlin helpers
├── MainActivity.kt
└── TempoApp.kt
```

## Operational Commands
- **Build:** `./gradlew assembleDebug`
- **Test:** `./gradlew testDebugUnitTest` (Target: 80%+ coverage)
- **Screenshots:** `./gradlew validateDebugScreenshotTest` (compare) / `updateDebugScreenshotTest` (regenerate references)
- **Lint:**
  - `./gradlew ktlintCheck` (Code formatting)
  - `./gradlew :app:detekt` (Static analysis — code smells, complexity, naming)
- **Sync:** Perform Gradle Sync after `libs.versions.toml` changes

## Git & Commit Guidelines
> Full details in [`AGENTS.md` → Git Conventions](../../AGENTS.md#git-conventions)

- **Conventional Commits:** `<type>(#[ID]): <description>` or `<type>: <description>` (no ID)
- **Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`
- **Example:** `feat(#88): implement habit history graph`
