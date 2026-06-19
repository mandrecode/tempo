# DOMAIN.md

## Core Principle: Pure Kotlin

**ABSOLUTE RULE:** The domain layer must NOT contain `android.*` imports (except `android.os.Parcelable` if absolutely necessary, but this should be avoided).

This layer represents the **business logic** and is completely independent of frameworks, UI, or data sources.

## Structure

### 1. Models (`features/*/domain/model/` + `core/domain/model/`)

**Purpose:** Define the business entities.

**Rules:**
- Pure Kotlin data classes
- No Room, Compose, or Android dependencies
- Rich models with behavior, not just data containers
- Feature-specific models in `features/*/domain/model/`
- Shared enums in `core/domain/model/` (Priority, Periodicity, DayOfWeek, ThemeMode, AppLanguage)

**Examples:**
```kotlin
// features/tasks/domain/model/Task.kt
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val priority: Priority,
    val categoryId: Long? = null,
    val dueDate: Instant? = null,
    val reminderDate: Instant? = null,
    val isCompleted: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)

// core/domain/model/Priority.kt
enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}
```

**Important:** Android-specific properties (like `Color`, `@StringRes`) belong in `core/ui/util/` as extension properties, NOT in domain models.

### 2. Repository Interfaces (`features/*/domain/repository/`)

**Purpose:** Define contracts for data access without exposing implementation details.

**Rules:**
- Interfaces only (implementations live in `data/repository/`)
- Functions should be `suspend` for single operations
- Return `Flow` for reactive/streaming data
- Return domain models, never entities
- Operations should be atomic and focused

**Example:**
```kotlin
// features/tasks/domain/repository/TaskRepository.kt
interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getTaskById(id: Long): Flow<Task?>
    suspend fun createTask(task: Task): Result<Task>
    suspend fun updateTask(task: Task): Result<Unit>
    suspend fun deleteTask(id: Long): Result<Unit>
    suspend fun toggleTaskCompletion(id: Long): Result<Unit>
}
```

### 3. UseCases (`features/*/domain/usecase/`)

**Purpose:** Encapsulate single-responsibility business logic operations.

**When to Create a UseCase:**
- Logic involves >1 Repository
- Logic is >15 lines
- Logic is reused in multiple places
- Operation has complex validation or business rules

**Rules:**
- One class per use case
- Single public function: `suspend operator fun invoke(...)`
- Use `@Inject constructor` for dependency injection
- Return sealed `Result` types for operations with multiple outcomes
- No Hilt module needed (Hilt auto-injects)

**Result Types:**
```kotlin
// Typical Result sealed class
sealed class CreateTaskResult {
    data class Success(val task: Task) : CreateTaskResult()
    data class ValidationError(val message: String) : CreateTaskResult()
    data class DatabaseError(val exception: Throwable) : CreateTaskResult()
}
```

**Example UseCase:**
```kotlin
// features/tasks/domain/usecase/CreateTaskUseCase.kt
class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(
        title: String,
        description: String?,
        priority: Priority,
        categoryId: Long?,
        dueDate: Instant?
    ): CreateTaskResult = withContext(dispatcher) {
        // Validation
        if (title.isBlank()) {
            return@withContext CreateTaskResult.ValidationError("Title cannot be empty")
        }
        
        // Verify category exists if provided
        if (categoryId != null) {
            val categoryExists = categoryRepository.getCategoryById(categoryId).first() != null
            if (!categoryExists) {
                return@withContext CreateTaskResult.ValidationError("Category not found")
            }
        }
        
        // Create task
        val task = Task(
            title = title.trim(),
            description = description?.trim(),
            priority = priority,
            categoryId = categoryId,
            dueDate = dueDate,
            isCompleted = false,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        
        when (val result = taskRepository.createTask(task)) {
            is Result.Success -> CreateTaskResult.Success(result.data)
            is Result.Error -> CreateTaskResult.DatabaseError(result.exception)
        }
    }
}
```

**Simpler UseCases:**
For simple operations, you can return a basic `Result<T>`:
```kotlin
class ToggleTaskCompletionUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(taskId: Long): Result<Unit> = withContext(dispatcher) {
        taskRepository.toggleTaskCompletion(taskId)
    }
}
```

## Hilt & Dependency Injection

### UseCases
- Use `@Inject constructor` 
- Hilt automatically provides instances
- No need to create a module for UseCases

### Dispatcher Injection
Inject coroutine dispatchers via qualifiers from `core/di/DispatcherModule`:
- `@IoDispatcher` for IO operations
- `@DefaultDispatcher` for CPU-intensive work

**Example:**
```kotlin
class FetchTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    // ...
}
```

## Scheduler Interfaces

For infrastructure concerns like reminders, define **interfaces** in the domain layer:

**Location:** `features/*/domain/scheduler/` or `core/domain/model/` for results

**Example:**
```kotlin
// features/tasks/domain/scheduler/TaskReminderScheduler.kt
interface TaskReminderScheduler {
    suspend fun scheduleReminder(task: Task): ScheduleResult
    suspend fun cancelReminder(taskId: Long): ScheduleResult
    suspend fun updateReminder(task: Task): ScheduleResult
}

// core/domain/model/ScheduleResult.kt
sealed class ScheduleResult {
    object Success : ScheduleResult()
    data class Error(val message: String) : ScheduleResult()
}
```

**Implementation** lives in `infrastructure/reminders/scheduler/`.

## Date/Time Handling

**Mandatory:** Use `kotlinx-datetime` for dates and timestamps, `kotlin.time` for durations.

**Never use:** `java.util.Date`, `java.util.Calendar`, or `java.time.*`

**Examples:**
```kotlin
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

val now: Instant = Clock.System.now()
val tomorrow: Instant = now.plus(1.days)
val duration: Duration = tomorrow - now
```

## Checklist for AI

Before completing a domain layer task, verify:
- [ ] Are there any `android.*` imports? (If yes, remove or move to appropriate layer)
- [ ] Are we using `kotlinx-datetime` for dates and `kotlin.time` for durations?
- [ ] Do repository interfaces return domain models (not entities)?
- [ ] Are UseCases focused on single responsibilities?
- [ ] Do UseCases use `@Inject constructor`?
- [ ] Are coroutine dispatchers injected (not hardcoded)?
- [ ] Are scheduler interfaces defined in domain but implemented in infrastructure?
- [ ] Do complex operations return sealed `Result` types?
- [ ] Is validation logic present in UseCases where needed?
- [ ] Are edge cases handled (empty lists, null values, validation failures)?
