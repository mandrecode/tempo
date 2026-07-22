## Why

Task and habit editing (`TaskBottomSheet*` / `HabitBottomSheet*`) implement the same interaction shape — snapshot-based dirty tracking, debounced autosave, discard-on-dismiss confirmation, footer Cancel/Confirm/Delete actions, title/description validation, and property-row field layout — as two independently maintained code paths. Concrete duplication found: identical constants (`MAX_TITLE_LENGTH`, `AUTO_SAVE_DEBOUNCE_MS`, `PROPERTY_ROW_GAP`, `DELETE_BUTTON_CORNER_RADIUS`) redeclared per feature, near-identical footer composables, four separate copies of the same `TextField` color styling, two independently invented snapshot-diffing/autosave-flush implementations, duplicated `ValidationErrorType` enums and two-layer (ViewModel + use case) title/description validation, and a duplicated `toUserFacingMessage()` extension. This drift means a fix or improvement to one editor (e.g. a validation edge case, a dismiss-confirmation bug) routinely does not reach the other. Closes #24.

## What Changes

- Extract a shared bottom-sheet editor scaffold (snapshot capture, dirty-diff, debounced autosave + flush-on-dismiss, discard-confirmation wiring) used by both `TaskBottomSheetContent` and `HabitBottomSheetContent`, replacing their independently implemented versions of the same algorithm.
- Extract shared footer composable(s) (Cancel/Confirm/Delete row with animated enable/disable styling) used by both editors, replacing `TaskBottomSheetFooterSection.kt` and `HabitBottomSheetFooterSection.kt`.
- Extract a shared `PropertyRow`/labeled-icon-row composable and shared title/description `TextField` styling, replacing the per-feature repeated blocks in `TaskBottomSheetFormSections.kt` / `HabitBottomSheetFormSections.kt`.
- Consolidate the editor-session-id → `rememberSaveable` key derivation pattern into a shared helper.
- Consolidate duplicated editor constants (`MAX_TITLE_LENGTH`, `AUTO_SAVE_DEBOUNCE_MS`, `PROPERTY_ROW_GAP`, `DELETE_BUTTON_CORNER_RADIUS`) and the duplicated `toUserFacingMessage()` extension into a single shared location.
- Consolidate title/description validation (`ValidationErrorType`, the `ValidationUtils.validateTitle`/`validateDescription` dispatch) into a single shared domain-layer helper reused by `CreateTaskUseCase`, `UpdateTaskUseCase`, `CreateHabitUseCase`, `UpdateHabitUseCase`, and `CreateOrUpdateHabitChainUseCase`, removing the per-feature duplicated enums/switches while preserving each use case's existing return/result shape.
- Add or extend shared Compose UI-test utilities (test-tag conventions / robot helpers) so `TaskBottomSheetTest.kt` and `HabitBottomSheetTest.kt` can share dirty-tracking/autosave/discard test coverage instead of each re-testing the same shared behavior independently.
- Preserve all feature-specific behavior as-is: task periodicity/scheduling, habit Build/Quit type switching, habit-chain member selection and its confirmation dialogs, icon/color pickers, and habit history — none of this is touched or redesigned.

**Non-goals:** no redesign of task or habit/routine flows, no visual redesign, no new user-facing features, no change to persisted data models or public `TaskBottomSheet`/`HabitBottomSheet` entry-point signatures.

## Capabilities

### New Capabilities
(none — this introduces internal shared UI/domain building blocks, not a new product capability)

### Modified Capabilities
- `presentation-architecture`: add a requirement that shared bottom-sheet editor behavior (snapshot/dirty-tracking, debounced autosave, discard-confirmation, footer actions, property-row layout, title/description validation) is implemented once and reused by the Tasks and Routines editors, rather than duplicated per feature, while keeping `TaskBottomSheet`/`HabitBottomSheet` public entry points stable — extending the existing "presentation god files are decomposed into focused units" guardrail from the #753 refactor.

## Impact

- **UI layer**: `features/tasks/presentation/components/{TaskBottomSheetContent,TaskBottomSheetFooterSection,TaskBottomSheetFormSections,TaskBottomSheetModels}.kt`, `features/routines/presentation/components/{HabitBottomSheetContent,HabitBottomSheetFooterSection,HabitBottomSheetFormSections,HabitBottomSheetModels}.kt`, plus a new shared package (e.g. `core/ui/editor/`) for the extracted scaffold/footer/property-row components.
- **Domain layer**: `features/tasks/domain/usecase/{CreateTaskUseCase,UpdateTaskUseCase}.kt`, `features/routines/domain/usecase/{CreateHabitUseCase,UpdateHabitUseCase,CreateOrUpdateHabitChainUseCase}.kt`, and `core/domain/util/ValidationUtils.kt` (or a new shared validation use case/helper alongside it).
- **Presentation actions**: `features/tasks/presentation/TasksViewModelTaskActions.kt`, `features/routines/presentation/RoutinesViewModelActions.kt` (shared `toUserFacingMessage()` extension, validation dispatch).
- **Tests**: `TaskBottomSheetTest.kt`, `HabitBottomSheetTest.kt`, `CreateTaskUseCaseTest.kt`, `UpdateTaskUseCaseTest.kt`, `CreateHabitUseCaseTest.kt`, `UpdateHabitUseCaseTest.kt`, `CreateOrUpdateHabitChainUseCaseTest.kt`, plus any new shared test utilities.
- No database schema, navigation, or public API changes. No user-visible behavior change is intended; this is a structural refactor to reduce future drift.
