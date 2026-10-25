# Tasks

## 1. Implementation

- [x] 1.1 `TempoBottomNavigation` calls `onReselect(route)` when the open tab is tapped, instead of ignoring the tap.
- [x] 1.2 `PersistentFloatingBar` forwards a Tasks reselect to `TasksFloatingBarState.onReselect`, which `TasksScreen` wires to `UiEvent.ShowDefaultCategory`.
- [x] 1.3 `TasksViewModel.showDefaultCategory()` selects the default category (first category as fallback; no-op before categories load or when already selected).

## 2. Tests and verification

- [x] 2.1 `TasksViewModelTest`: default selected, fallback to first, no-op before load.
- [x] 2.2 `TempoBottomNavigationReselectTest` (instrumented): open tab reselects, other tab navigates.
- [x] 2.3 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest` and `openspec validate --strict`.
