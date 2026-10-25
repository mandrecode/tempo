# Proposal

## Why

Originating issue: [#452](https://github.com/mandrecode/tempo/issues/452).

Tapping the tab that is already open does nothing. On Tasks, the natural expectation is that it takes you back to where the tab starts — the default (favourite) category — after browsing other categories.

## What Changes

- Tapping the open Tasks tab in the navigation bar or rail selects the default category. If no category is marked default, the first category is used, the same fallback the list already applies when the selection disappears. The category chip row scrolls to it, as it does for any selection.
- The navigation bar reports a tap on the open tab as a reselect. Only Tasks acts on it; Routines and Focus keep doing nothing.

### Non-goals

- No scroll-to-top or other reset of the Tasks list.
- No change to Routines or Focus reselect behaviour.
- No change to how the default category is chosen or stored.

## Capabilities

### New Capabilities
- `tab-reselect`: what tapping the already-open tab does.

### Modified Capabilities

None.

## Impact

- UI: `TempoBottomNavigation` (`onReselect`), `PersistentFloatingBar`, `TasksFloatingBarState.onReselect`, `TasksScreen`.
- Presentation: `TasksContract.UiEvent.ShowDefaultCategory`, `TasksViewModel.showDefaultCategory()`.
- Tests: `TasksViewModelTest`, new instrumented `TempoBottomNavigationReselectTest`.
- No domain, data, or persistence changes.
