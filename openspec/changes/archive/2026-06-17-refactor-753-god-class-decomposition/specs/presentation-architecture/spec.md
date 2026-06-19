## ADDED Requirements

### Requirement: Routines and Tasks UiState use immutable collections
The Routines and Tasks presentation contracts SHALL model collection-shaped UiState fields with `kotlinx.collections.immutable` types so ViewModels and UI consumers operate on persistent immutable data.

#### Scenario: Routines contract exposes immutable lists
- **WHEN** `RoutinesContract.UiState` is declared
- **THEN** habit, habit-chain, and timeline collections are typed as immutable lists with persistent defaults

#### Scenario: Tasks contract exposes immutable lists and maps
- **WHEN** `TasksContract.UiState` is declared
- **THEN** tasks/categories collections and grouped/count maps are typed as immutable collections with persistent defaults

### Requirement: Presentation event handling avoids force-unwrapping
Routines and Tasks presentation code SHALL avoid Kotlin `!!` in user-event handling paths and use explicit nullable handling.

#### Scenario: Screen delete flows handle null selections safely
- **WHEN** delete actions are dispatched from routines or tasks screens
- **THEN** nullable selected entities are guarded with explicit checks before dispatching destructive events

#### Scenario: Task grouping handles nullable parent IDs safely
- **WHEN** subtasks are grouped in task loading logic
- **THEN** grouping uses nullable-safe transforms and does not force-unwrap `parentTaskId`

### Requirement: Routines presentation catches specific exceptions
Routines presentation flows SHALL use targeted exception handling and preserve coroutine cancellation semantics.

#### Scenario: Cancellation is rethrown
- **WHEN** a routines flow catches `CancellationException`
- **THEN** the exception is rethrown without being converted to user-facing error state

#### Scenario: Recoverable failures are handled explicitly
- **WHEN** routines operations fail due to IO or invalid state/arguments
- **THEN** the ViewModel handles those explicit exception types and surfaces feedback without broad `catch (Exception)`

### Requirement: Presentation god files are decomposed into focused units
Large presentation files in routines/tasks bottom sheets, cards, and ViewModels SHALL be decomposed into focused helper/content/action units while preserving existing public entry points.

#### Scenario: Bottom sheet entry composables remain stable
- **WHEN** callers use `HabitBottomSheet` or `TaskBottomSheet`
- **THEN** the public entry composable signatures remain intact and delegate to extracted focused sections

#### Scenario: ViewModel public APIs remain stable
- **WHEN** UI layers interact with `RoutinesViewModel` and `TasksViewModel`
- **THEN** event API and exposed state/effect flows remain unchanged while implementation logic is split into helper files

### Requirement: Settings architecture exception is documented
Settings SHALL remain a deliberate thin-layer exception until complexity warrants domain/use-case extraction.

#### Scenario: AGENTS guidance defines the exception and trigger
- **WHEN** architecture guidance is reviewed in `AGENTS.md`
- **THEN** Settings is documented as an explicit thin exception with a clear promotion trigger for adding a dedicated domain layer
