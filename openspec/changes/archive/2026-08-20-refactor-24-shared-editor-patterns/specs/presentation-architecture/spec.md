## ADDED Requirements

### Requirement: Tasks and Routines editors share common bottom-sheet editor mechanics
The Tasks and Routines bottom-sheet editors SHALL implement snapshot-based dirty tracking, debounced autosave with flush-on-dismiss, discard-confirmation wiring, footer Cancel/Confirm/Delete actions, and property-row field layout via shared implementations rather than per-feature duplicated logic, while keeping `TaskBottomSheet` and `HabitBottomSheet` public entry composable signatures stable.

#### Scenario: Dirty tracking and autosave use a shared mechanism
- **WHEN** a task or habit bottom sheet's form fields change from their baseline snapshot
- **THEN** the shared snapshot-diff and debounced-autosave mechanism used by both editors detects the change, dispatches autosave, and flushes on dismiss, without each feature reimplementing its own snapshot-diff/autosave algorithm

#### Scenario: Footer actions use a shared composable
- **WHEN** the Cancel/Confirm/Delete footer is rendered in a task or habit bottom sheet
- **THEN** both editors render it via the same shared footer composable, supplying only feature-specific labels and enabled/callback state

#### Scenario: Public entry points remain stable
- **WHEN** callers use `TaskBottomSheet` or `HabitBottomSheet`
- **THEN** their public composable signatures remain unchanged while internally delegating to shared editor-scaffold components

### Requirement: Tasks and Routines editors share title/description validation
Title and description validation for task and habit/habit-chain creation and update use cases SHALL be implemented via a single shared validation helper rather than per-feature duplicated validation enums and dispatch logic, while each use case preserves its existing result/error type as observed by callers.

#### Scenario: Title validation is consistent across features
- **WHEN** a task or a habit is saved with an empty or over-length title
- **THEN** the shared validation helper produces the validation outcome, and each use case maps it into its own existing error/result type unchanged from prior behavior

#### Scenario: Description validation is consistent across features
- **WHEN** a task or a habit is saved with an over-length description
- **THEN** the shared validation helper produces the validation outcome, and each use case maps it into its own existing error/result type unchanged from prior behavior
