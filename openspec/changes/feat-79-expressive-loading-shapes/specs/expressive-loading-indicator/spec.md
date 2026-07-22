## ADDED Requirements

### Requirement: First-load state uses Material 3 Expressive loading shapes
The shared `TempoLoadingIndicator` composable SHALL render Material 3 Expressive's indeterminate morphing-shape `LoadingIndicator` (not a `CircularProgressIndicator` spinner) while displaying its loading message.

#### Scenario: Tasks list first load
- **WHEN** the Tasks screen is in its first-load state (`TasksContract.UiState.isLoading` true, no tasks yet)
- **THEN** `TasksContent` shows `TempoLoadingIndicator`, which renders the Expressive `LoadingIndicator` morphing shape above the "Loading tasks…" message

#### Scenario: Habits (Routines) list first load
- **WHEN** the Routines screen is in its first-load state (`RoutinesContract.UiState.isLoading` true, no habits yet)
- **THEN** `RoutinesContent` shows `TempoLoadingIndicator`, which renders the Expressive `LoadingIndicator` morphing shape above the "Loading habits…" message

#### Scenario: Component API stays stable
- **WHEN** a caller invokes `TempoLoadingIndicator(message, modifier)`
- **THEN** the composable accepts the same `message: String` and `modifier: Modifier` parameters as before, with no signature change required of callers
