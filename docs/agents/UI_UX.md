# UI_UX.md

## Architecture: MVI + UDF (Unidirectional Data Flow)

### Contract Pattern

Every screen has a `[Feature]Contract.kt` containing:

1. **`data class UiState`**: Immutable state representation
    - Must use `val` properties
    - Must use `kotlinx.collections.immutable` collections (ImmutableList, ImmutableMap)
    - Single source of truth for the UI
2. **`sealed interface UiEvent`**: User actions from the UI
    - Represents all possible user interactions
    - Examples: `OnTaskClicked`, `OnDeleteClicked`, `OnFilterChanged`
3. **`sealed interface UiEffect`**: One-time events
    - Navigation events
    - Toast/Snackbar messages
    - System interactions (vibration, etc.)

### ViewModel Rules

1. **State Management:**
    - Maintains a single `StateFlow<UiState>`
    - State updates are the only way to communicate with UI
    - Exposes `onEvent(event: UiEvent)` function for user actions
2. **Business Logic:**
    - ViewModels are **thin orchestrators** that delegate to UseCases
    - Never hold `Context` directly
    - Use `@StringRes Int` for messages (resolve in Screen via `stringResource()`)
3. **Dispatcher Injection:**
    - Inject `CoroutineDispatcher` via Hilt (`@IoDispatcher`, `@DefaultDispatcher`)
    - Never hardcode `Dispatchers.IO` or `Dispatchers.Default`
4. **Context Isolation:**
    - Use `PermissionChecker` interface for permission checks
    - Use resource IDs for strings/messages, resolve in composables

### Screen/Content Split

**Critical Separation Pattern:**

#### `[Feature]Screen.kt`

- Navigation entry point
- Collects state: `val uiState by viewModel.uiState.collectAsStateWithLifecycle()`
- Handles side effects: `LaunchedEffect` for `UiEffect` channel
- Manages navigation
- Resolves resource IDs to actual strings/colors
- Calls `[Feature]Content` with plain data

#### `[Feature]Content.kt`

- **Pure UI** - No ViewModel reference allowed
- Accepts `uiState: UiState` and `onEvent: (UiEvent) -> Unit` as parameters
- Fully previewable
- Contains the actual Compose layout
- Must have `@Preview` annotations for Light/Dark themes

**Example:**

```kotlin
// TasksScreen.kt (✓ Correct)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel = hiltViewModel(),
    onNavigateToDetail: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.NavigateToDetail -> onNavigateToDetail(effect.taskId)
            }
        }
    }

    TasksContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

// TasksContent.kt (✓ Correct)
@Composable
fun TasksContent(
    uiState: TasksUiState,
    onEvent: (TasksUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pure UI implementation
}
```

## Jetpack Compose Rules

### Resource Management

- **Hardcoded Strings:** Strictly forbidden
- Use `stringResource(R.string.resource_id)` for strings
- Use `quantityStringResource()` for plurals
- Use `MaterialTheme.colorScheme.*` for colors
- No hex color codes in composables

### Accessibility

- **Touch Targets:** Minimum 48dp for interactive elements
- **Content Descriptions:** Mandatory for all icons and images
- Use `Modifier.semantics { contentDescription = "..." }` when needed

### Previews

Every `Content` composable must have:

- `@Preview` annotation for different themes (Light/Dark)
- Use `PreviewParameterProvider` for state variations
- Device specification: `@Preview(device = "id:pixel_9")`

New or modified **internal composables** (headers, separators, dialogs, cards, etc.) must also
include `@Preview` functions to enable visual verification in Android Studio without running the
full app.

**All `@Preview` composables live under `src/debug/`**, not in the main source set. Place them in
a `[Feature]ContentPreviews.kt` file mirroring the main package structure.

**Example:**

```kotlin
@Preview(name = "Light Mode", device = "id:pixel_9")
@Preview(name = "Dark Mode", device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TasksContentPreview(
    @PreviewParameter(TasksUiStateProvider::class) uiState: TasksUiState
) {
    TempoTheme {
        TasksContent(
            uiState = uiState,
            onEvent = {}
        )
    }
}
```

### Performance

- Use `remember` for expensive calculations
- Use `derivedStateOf` for computed state
- Use `key()` in loops for stable identity
- Avoid recomposition by hoisting state appropriately

## Navigation 3 Specifics

### Type-Safe Routes

- Routes are **data classes/objects**, not strings
- Must be annotated with `@Serializable` from kotlinx.serialization
- Navigation logic lives in `core/ui/navigation/`

**Example:**

```kotlin
@Serializable
data class TaskDetailRoute(val taskId: Long)

@Serializable
object TasksRoute

// Navigation
navController.navigate(TaskDetailRoute(taskId = 123))
```

### Navigation Setup

- Define all routes in a central navigation file
- Use `NavHost` with type-safe navigation
- Pass navigation callbacks to screens, not NavController

## UI Extensions

### Android-Specific Properties

Domain models stay pure Kotlin. Android-specific properties (colors, string resources) live as *
*extension properties** in `core/ui/util/`.

**Example:**

```kotlin
// core/ui/util/PriorityExtensions.kt
val Priority.color: Color
@Composable get() = when (this) {
    Priority.HIGH -> MaterialTheme.colorScheme.error
    Priority.MEDIUM -> MaterialTheme.colorScheme.primary
    Priority.LOW -> MaterialTheme.colorScheme.secondary
}

val Priority.titleResId: Int
get() = when (this) {
    Priority.HIGH -> R.string.priority_high
    Priority.MEDIUM -> R.string.priority_medium
    Priority.LOW -> R.string.priority_low
}
```

## Checklist for AI

Before completing a UI task, verify (see also the release-gate
[visual consistency checklist](../design/release-visual-consistency-checklist.md) when preparing
a release or reviewing UI-heavy changes):

- [ ] Is `UiState` immutable with `val` properties?
- [ ] Are we using `kotlinx.collections.immutable` collections?
- [ ] Is the ViewModel reference kept out of `*Content` composables?
- [ ] Do all strings use `stringResource()`?
- [ ] Are all icons accessible with content descriptions?
- [ ] Does the UI use Material3 design tokens?
- [ ] Is business logic kept out of composables?
- [ ] Are routes `@Serializable` type-safe objects?
- [ ] Do `*Content` composables have `@Preview` annotations?
- [ ] Do new/modified internal composables (headers, dialogs, cards) have `@Preview` annotations?
- [ ] Are Android-specific extensions in `core/ui/util/`?
