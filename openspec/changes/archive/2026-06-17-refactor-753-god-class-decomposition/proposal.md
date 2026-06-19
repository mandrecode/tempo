## Why

GitHub issue [#753](https://github.com/mandrecode/tempo/issues/753) (final phase of [#461](https://github.com/mandrecode/tempo/issues/461)) targets architecture debt concentrated in presentation-layer god files and mutable contract collections. The current structure slows onboarding, increases regression risk in feature edits, and violates the project UI contract rule that `UiState` collections are immutable.

## What Changes

- Enforce immutable MVI contracts:
  - `RoutinesContract.UiState` and `TasksContract.UiState` now use `kotlinx.collections.immutable` collection types (`ImmutableList` / `ImmutableMap`) with persistent defaults.
  - `RoutinesContract.UiEvent.CreateOrUpdateHabitChain.habitIds` and pending chain payloads use immutable lists.
- Update ViewModel state production to build persistent collections when deriving UI state.
- Replace presentation-layer `!!` with explicit safe handling in Routines/Tasks screens and task grouping.
- Narrow broad exception handling in routines presentation flows to explicit exception types (`CancellationException`, `IOException`, `IllegalArgumentException`, `IllegalStateException`) without reintroducing `catch (Exception)`.
- Decompose presentation god files into focused components/helpers:
  - Habit/task bottom-sheet logic split into content/form/footer/chain/periodicity files.
  - Habit/task cards split into dedicated chain/quit/metadata files.
  - Tasks/Routines ViewModels split into focused action/data-loading helpers while preserving public ViewModel APIs.
- Extract shared presentation logic for grouping/loading and reusable action slices to reduce duplication and improve testability.
- Record D3 decision for Settings architecture in `AGENTS.md`: Settings remains a deliberate thin exception until complexity warrants a full domain/use-case layer.

## D3 Decision: Settings domain layer

Choose **Option B** for now: document Settings as an intentional thin exception instead of force-adding a domain layer. This keeps architecture honest to feature complexity and avoids ceremonial abstractions. The rule includes an explicit promotion trigger when Settings workflow complexity increases.

## Impact

- Contracts:
  - `features/routines/presentation/RoutinesContract.kt`
  - `features/tasks/presentation/TasksContract.kt`
- Screens:
  - `features/routines/presentation/RoutinesScreen.kt`
  - `features/tasks/presentation/TasksScreen.kt`
- ViewModels + extracted helpers:
  - `features/routines/presentation/RoutinesViewModel.kt`
  - `features/routines/presentation/RoutinesViewModelActions.kt`
  - `features/tasks/presentation/TasksViewModel.kt`
  - `features/tasks/presentation/TasksViewModelDataLoading.kt`
  - `features/tasks/presentation/TasksViewModelTaskActions.kt`
  - `features/tasks/presentation/TasksViewModelCategoryAndPermissionActions.kt`
- Bottom sheets and card decomposition files under:
  - `features/routines/presentation/components/**`
  - `features/tasks/presentation/components/**`
- Architecture guidance:
  - `AGENTS.md`
