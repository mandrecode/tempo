# TESTING.md

## Testing Strategy

The project uses a dual testing approach:
1. **Unit Tests** (`app/src/test/`) - Pure logic testing with mocks
2. **Instrumented Tests** (`app/src/androidTest/`) - Compose UI testing on device/emulator

**Coverage Target:** 80%+ for unit tests

## Unit Tests (`app/src/test/`)

### What to Test
- **UseCases:** All business logic and validation
- **ViewModels:** State updates and event handling
- **Repositories:** Data transformations (when not using real DB)
- **Mappers:** Entity ↔ Domain conversions
- **Utility Classes:** Extensions, helpers, formatters

### Core Libraries
- **JUnit 5:** Test framework
- **MockK:** Mocking library (use `relaxed = true` for simplicity)
- **Truth:** Assertions (`assertThat()` syntax)
- **Coroutines Test:** `runTest`, `StandardTestDispatcher`, `TestScope`
- **Turbine:** Flow/Channel testing

### Test Structure

**Naming Convention:**
- **Class:** `[Subject]Test` (e.g., `CreateTaskUseCaseTest`, `TasksViewModelTest`)
- **Method:** `given[Condition]_when[Action]_then[Result]`

**Example:**
```kotlin
class CreateTaskUseCaseTest {
    
    @Test
    fun givenValidInput_whenInvoked_thenReturnsSuccess() = runTest {
        // Arrange
        val repository = mockk<TaskRepository>(relaxed = true)
        val useCase = CreateTaskUseCase(repository, UnconfinedTestDispatcher())
        
        // Act
        val result = useCase.invoke(
            title = "Test Task",
            description = null,
            priority = Priority.HIGH,
            categoryId = null,
            dueDate = null
        )
        
        // Assert
        assertThat(result).isInstanceOf(CreateTaskResult.Success::class.java)
    }
    
    @Test
    fun givenBlankTitle_whenInvoked_thenReturnsValidationError() = runTest {
        // Test implementation
    }
}
```

### Testing ViewModels

**Key Points:**
- Mock **UseCases**, not repositories directly
- Use `StandardTestDispatcher` for deterministic execution
- Use **Turbine** to test `StateFlow` emissions
- Verify state transitions and side effects

**Example:**
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: TasksViewModel
    private lateinit var getAllTasksUseCase: GetAllTasksUseCase
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        getAllTasksUseCase = mockk(relaxed = true)
        deleteTaskUseCase = mockk(relaxed = true)
        
        viewModel = TasksViewModel(
            getAllTasksUseCase = getAllTasksUseCase,
            deleteTaskUseCase = deleteTaskUseCase
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun givenTasks_whenInitialized_thenStateContainsTasks() = runTest {
        // Arrange
        val tasks = listOf(
            Task(id = 1, title = "Task 1", /* ... */),
            Task(id = 2, title = "Task 2", /* ... */)
        )
        coEvery { getAllTasksUseCase() } returns flowOf(tasks)
        
        // Act & Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.tasks).hasSize(2)
            assertThat(state.tasks.first().title).isEqualTo("Task 1")
        }
    }
    
    @Test
    fun givenDeleteEvent_whenTaskDeleted_thenShowsSuccessSnackbar() = runTest {
        // Arrange
        val taskId = 1L
        coEvery { deleteTaskUseCase(taskId) } returns Result.Success(Unit)
        
        // Act
        viewModel.onEvent(TasksUiEvent.OnDeleteTask(taskId))
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        viewModel.uiEffect.test {
            val effect = awaitItem()
            assertThat(effect).isInstanceOf(TasksUiEffect.ShowSnackbar::class.java)
        }
    }
}
```

### Testing Coroutines

**Use `runTest` for deterministic time:**
```kotlin
@Test
fun testWithDelay() = runTest {
    val job = launch {
        delay(1000)
        // Do something
    }
    
    advanceTimeBy(1000)
    
    assertThat(job.isCompleted).isTrue()
}
```

### Testing Flows with Turbine

**Turbine** makes Flow testing clean and readable:
```kotlin
@Test
fun testFlow() = runTest {
    val flow = flowOf(1, 2, 3)
    
    flow.test {
        assertThat(awaitItem()).isEqualTo(1)
        assertThat(awaitItem()).isEqualTo(2)
        assertThat(awaitItem()).isEqualTo(3)
        awaitComplete()
    }
}
```

### Mocking with MockK

**Relaxed Mocks:**
```kotlin
val repository = mockk<TaskRepository>(relaxed = true)
```

**Stubbing Suspend Functions:**
```kotlin
coEvery { repository.getTaskById(1L) } returns flowOf(task)
coEvery { repository.createTask(any()) } returns Result.Success(task)
```

**Verification:**
```kotlin
coVerify { repository.createTask(any()) }
coVerify(exactly = 1) { repository.deleteTask(1L) }
```

### Running Unit Tests
```bash
./gradlew test
./gradlew testDebugUnitTest  # Only debug variant
```

## Instrumented Tests (`app/src/androidTest/`)

### What to Test
- **Compose UI:** `*Content` composables
- **Cards & Bottom Sheets:** User interactions
- **Screens:** Integration with ViewModel (using test doubles)
- **SharedPreferences Repositories:** (require Android Context)

### Core Libraries
- **Compose Test:** `createComposeRule()`, semantic matchers
- **MockK Android:** `mockk-android`, `mockk-agent`
- **Truth:** Assertions

### Test Structure

**Example Compose UI Test:**
```kotlin
class TasksContentTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun givenTasks_whenRendered_thenDisplaysTaskList() {
        // Arrange
        val uiState = TasksUiState(
            tasks = persistentListOf(
                Task(id = 1, title = "Task 1", /* ... */),
                Task(id = 2, title = "Task 2", /* ... */)
            )
        )
        
        // Act
        composeTestRule.setContent {
            TempoTheme {
                TasksContent(
                    uiState = uiState,
                    onEvent = {}
                )
            }
        }
        
        // Assert
        composeTestRule.onNodeWithText("Task 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Task 2").assertIsDisplayed()
    }
    
    @Test
    fun givenTask_whenClicked_thenTriggersEvent() {
        // Arrange
        var clickedTaskId: Long? = null
        val task = Task(id = 1, title = "Clickable Task", /* ... */)
        val uiState = TasksUiState(tasks = persistentListOf(task))
        
        // Act
        composeTestRule.setContent {
            TempoTheme {
                TasksContent(
                    uiState = uiState,
                    onEvent = { event ->
                        if (event is TasksUiEvent.OnTaskClicked) {
                            clickedTaskId = event.taskId
                        }
                    }
                )
            }
        }
        
        composeTestRule.onNodeWithText("Clickable Task").performClick()
        
        // Assert
        assertThat(clickedTaskId).isEqualTo(1L)
    }
}
```

### Semantic Matchers

**Finding Nodes:**
- `onNodeWithText("Text")` - By text content
- `onNodeWithContentDescription("Description")` - By accessibility label
- `onNodeWithTag("TestTag")` - By test tag (`Modifier.testTag()`)
- `onAllNodesWithText("Text")` - Multiple nodes

**Assertions:**
- `.assertIsDisplayed()`
- `.assertIsNotDisplayed()`
- `.assertIsEnabled()`
- `.assertIsNotEnabled()`
- `.assertIsSelected()`
- `.assertTextEquals("Expected")`

**Actions:**
- `.performClick()`
- `.performTextInput("Text")`
- `.performScrollTo()`
- `.performTouchInput { swipeLeft() }`

### Running Instrumented Tests
```bash
./gradlew connectedAndroidTest
./gradlew connectedDebugAndroidTest  # Only debug variant
```

## Test Organization

### Mirror Source Structure
Tests should mirror the source file structure:

```text
app/src/test/java/com.mandrecode.tempo/
├── features/
│   ├── tasks/
│   │   ├── domain/
│   │   │   └── usecase/
│   │   │       └── CreateTaskUseCaseTest.kt
│   │   ├── data/
│   │   │   ├── mapper/
│   │   │   │   └── TaskMapperTest.kt
│   │   │   └── repository/
│   │   │       └── TaskRepositoryImplTest.kt
│   │   └── presentation/
│   │       └── TasksViewModelTest.kt
└── util/
    └── DateFormatterTest.kt

app/src/androidTest/java/com.mandrecode.tempo/
├── features/
│   └── tasks/
│       └── presentation/
│           ├── TasksContentTest.kt
│           └── components/
│               └── TaskCardTest.kt
```

## Best Practices

### 1. Test Isolation
Each test should be independent and not rely on other tests.

### 2. Arrange-Act-Assert (AAA)
Structure tests clearly:
```kotlin
@Test
fun testSomething() {
    // Arrange: Setup test data and mocks
    val input = "test"
    
    // Act: Execute the code under test
    val result = systemUnderTest.process(input)
    
    // Assert: Verify the results
    assertThat(result).isEqualTo("expected")
}
```

### 3. Use Descriptive Names
```kotlin
// ✓ Good
fun givenEmptyTitle_whenCreatingTask_thenReturnsValidationError()

// ✗ Bad
fun testTaskCreation()
```

### 4. Test Edge Cases
- Null values
- Empty collections
- Boundary conditions
- Error scenarios

### 5. Don't Test Framework Code
Focus on your business logic, not Android framework or library code.

## Checklist for AI

Before completing a testing task, verify:
- [ ] Are you using `MockK` with `relaxed = true`?
- [ ] Are coroutine tests using `runTest` and `StandardTestDispatcher`?
- [ ] Are Flow tests using **Turbine**?
- [ ] Is the test structure AAA (Arrange-Act-Assert)?
- [ ] Are test names descriptive (`given_when_then`)?
- [ ] Are you testing behavior, not implementation?
- [ ] For UI tests, are you using semantic matchers (text, content description)?
- [ ] Are UI tests focused on UI behavior, not business logic?
- [ ] Do unit tests mirror the source structure?
- [ ] Are edge cases covered?
- [ ] Are assertions using **Truth** (`assertThat()`)?
