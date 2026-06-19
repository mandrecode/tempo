# DATA.md

## Responsibilities

Implement the interfaces defined in the domain layer. Manage persistence (Room Database) and external data sources.

**Key Principle:** The data layer is the **only** layer that knows about Room, SharedPreferences, or network APIs.

## Room Database

### Entities (`core/data/entity/`)

**Purpose:** Define database tables with Room annotations.

**Rules:**
- Annotated with `@Entity`
- Naming convention: `*Entity` (e.g., `TaskEntity`, `HabitEntity`)
- Use `@TypeConverter` for complex types (enums, dates, lists)
- **Import domain enums directly** - entities can reference domain models
- Located in `core/data/entity/` (shared across features)

**Example:**
```kotlin
// core/data/entity/TaskEntity.kt
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("category_id")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val priority: Priority,  // Domain enum used directly
    @ColumnInfo(name = "category_id")
    val categoryId: Long? = null,
    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null,  // Stored as epoch millis
    @ColumnInfo(name = "reminder_date")
    val reminderDate: Long? = null,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
```

### TypeConverters (`core/data/entity/Converters.kt`)

**Purpose:** Convert complex types for Room storage.

**Common Conversions:**
- Enums ↔ Strings/Ints
- `Instant` ↔ Long (epoch millis)
- Lists ↔ JSON strings

**Example:**
```kotlin
class Converters {
    @TypeConverter
    fun fromPriority(value: Priority): String = value.name
    
    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)
    
    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilliseconds()
    
    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }
}
```

### DAOs (`core/data/local/dao/`)

**Purpose:** Define database operations.

**Rules:**
- Interfaces annotated with `@Dao`
- Use `Flow` for reactive queries
- Use `suspend` for single-shot operations
- Query methods return entities, not domain models

**Example:**
```kotlin
// core/data/local/dao/TaskDao.kt
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Long): Flow<TaskEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long
    
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
}
```

### Database (`core/data/local/TempoDatabase.kt`)

**Example:**
```kotlin
@Database(
    entities = [
        TaskEntity::class,
        CategoryEntity::class,
        HabitEntity::class,
        HabitChainEntity::class
    ],
    version = 18,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TempoDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun habitDao(): HabitDao
    abstract fun habitChainDao(): HabitChainDao
}
```

## Repository Implementation

### Location
- Implementations: `features/*/data/repository/`
- Interfaces: `features/*/domain/repository/`

### Rules
1. **Implement domain interfaces**
2. **Always map Entity ↔ Domain Model** at the boundary
3. **Inject dispatcher** via `@IoDispatcher`
4. **Use `withContext(dispatcher)`** for blocking operations
5. **Handle exceptions** and return appropriate `Result` types
6. **Never return entities** to the domain layer

**Example:**
```kotlin
// features/tasks/data/repository/TaskRepositoryImpl.kt
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : TaskRepository {
    
    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)
    }
    
    override fun getTaskById(id: Long): Flow<Task?> {
        return taskDao.getTaskById(id)
            .map { it?.toDomain() }
            .flowOn(dispatcher)
    }
    
    override suspend fun createTask(task: Task): Result<Task> = withContext(dispatcher) {
        try {
            val entity = task.toEntity()
            val id = taskDao.insertTask(entity)
            Result.Success(task.copy(id = id))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updateTask(task: Task): Result<Unit> = withContext(dispatcher) {
        try {
            taskDao.updateTask(task.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun deleteTask(id: Long): Result<Unit> = withContext(dispatcher) {
        try {
            taskDao.deleteTaskById(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

## Mappers (`features/*/data/mapper/`)

**Purpose:** Convert between Room entities and domain models.

**Rules:**
- Extension functions: `toDomain()` and `toEntity()`
- Located in `features/*/data/mapper/`
- One mapper file per entity-model pair
- Handle `Instant` ↔ Long conversions
- Map domain enums directly (no conversion needed if TypeConverter handles it)

**Example:**
```kotlin
// features/tasks/data/mapper/TaskMapper.kt
fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    priority = priority,  // Domain enum used directly
    categoryId = categoryId,
    dueDate = dueDate?.let { Instant.fromEpochMilliseconds(it) },
    reminderDate = reminderDate?.let { Instant.fromEpochMilliseconds(it) },
    isCompleted = isCompleted,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    updatedAt = Instant.fromEpochMilliseconds(updatedAt)
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    priority = priority,
    categoryId = categoryId,
    dueDate = dueDate?.toEpochMilliseconds(),
    reminderDate = reminderDate?.toEpochMilliseconds(),
    isCompleted = isCompleted,
    createdAt = createdAt.toEpochMilliseconds(),
    updatedAt = updatedAt.toEpochMilliseconds()
)
```

## Dependency Injection

### Binding Repositories (`core/di/RepositoryModule.kt`)

**Every repository implementation must be bound to its interface using `@Binds`.**

**Example:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository
    
    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository
    
    // ... other bindings
}
```

### Database Module (`core/di/DatabaseModule.kt`)

Provides Room database and DAOs:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideTempoDatabase(
        @ApplicationContext context: Context
    ): TempoDatabase {
        return Room.databaseBuilder(
            context,
            TempoDatabase::class.java,
            "tempo_database"
        )
            .addMigrations(/* migrations */)
            .build()
    }
    
    @Provides
    fun provideTaskDao(database: TempoDatabase): TaskDao {
        return database.taskDao()
    }
    
    // ... other DAO providers
}
```

### Dispatcher Module (`core/di/DispatcherModule.kt`)

Provides coroutine dispatchers:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    
    @Provides
    @IoDispatcher
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    @Provides
    @DefaultDispatcher
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
```

## SharedPreferences Repositories

For simple key-value storage (settings, preferences).

### Location
- Interface: `core/data/preferences/`
- Implementation: `core/data/preferences/`

### Rules
- Use DataStore instead of SharedPreferences when possible
- Create interface + implementation
- Bind in `RepositoryModule`
- Use `@ApplicationContext` for Context injection

**Example:**
```kotlin
// core/data/preferences/SettingsRepository.kt (interface)
interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}

// core/data/preferences/SettingsRepositoryImpl.kt
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    private val preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    
    override val themeMode: Flow<ThemeMode> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_THEME_MODE) {
                trySend(getThemeMode())
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        send(getThemeMode())
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    
    override suspend fun setThemeMode(mode: ThemeMode) {
        preferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }
    
    private fun getThemeMode(): ThemeMode {
        val value = preferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return ThemeMode.valueOf(value ?: ThemeMode.SYSTEM.name)
    }
    
    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
```

## Checklist for AI

Before completing a data layer task, verify:
- [ ] Did you create the mapper (`toDomain()` / `toEntity()` extensions)?
- [ ] Is the repository interface defined in the domain layer?
- [ ] Did you add the `@Binds` entry in `core/di/RepositoryModule.kt`?
- [ ] Are entities using `@TypeConverter` for complex types?
- [ ] Does the repository inject `@IoDispatcher`?
- [ ] Are Flow operations using `.flowOn(dispatcher)`?
- [ ] Are suspend operations using `withContext(dispatcher)`?
- [ ] Do repositories return domain models (not entities)?
- [ ] Are exceptions handled and wrapped in `Result` types?
- [ ] Are database operations atomic and thread-safe?
