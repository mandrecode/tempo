# Proposal

## Why

Originating issue: [#434](https://github.com/mandrecode/tempo/issues/434).

The floating bar's add button disappears on the Focus tab. To capture a task that comes up while looking at your day, you have to switch to Tasks, add it, and come back. The issue asks for the add-task button to stay on Focus and open the same task creation sheet.

## What Changes

- On Focus, the floating bar shows the same "Add Task" button Tasks uses: next to the navigation pill in portrait, in the rail's add slot in landscape, and as the solo action in single-tab mode when Focus is the only tab. Sort stays Tasks-only.
- Tapping it opens the task editor over Focus: the same blank form, validation, auto-save and category handling as on Tasks. Focus already hosts this editor for edits and subtasks, so the new task flows through the existing Focus editor host.
- What's New announces it (id `434`), replacing the 1.15.0 entry, which has shipped.

### Non-goals

- No pre-filled date or plan for the new task. It follows the same rules as a task created on Tasks, so it appears on Focus once it has today's date or is planned from the plan sheet.
- No change to the Routines or Tasks add actions.

## Capabilities

### New Capabilities
- `focus-add-task`: adding a task from the Focus tab.

### Modified Capabilities

None.

## Impact

- Navigation: `FloatingBarState.kt` (new `FocusFloatingBarState`), `Navigation.kt`, `NavigationDestinations.kt`, `PersistentFloatingBar.kt` (`rememberAddAction`), `PortraitFloatingBarLayout.kt`.
- Focus: `FocusContract` (`UiEvent.AddTask`, `TaskEditorTarget.NewTask`), `FocusViewModel`, `FocusEditors`, `FocusScreen`.
- Strings: What's New entry (en, es).
- Tests: `FocusViewModelTest`, `PortraitFloatingBarMotionTest`.
- No domain, data, or persistence changes.
