# Design

## Approach

Focus follows the pattern Routines and Tasks already use for their bar actions.

- `FocusScreen` publishes a `FocusFloatingBarState(onAddTask)` through a `SideEffect`. `Navigation` holds it next to the Routines and Tasks states and passes it to `PersistentFloatingBar`.
- `rememberAddAction` returns an "Add Task" `AddAction` for `FocusRoute`. Every layout (portrait, rail, single-tab) already renders whatever `AddAction` the active tab returns, so no layout code changes. It is never compact, because Focus has no scroll-driven compaction.
- `onAddTask` sends `FocusContract.UiEvent.AddTask`, which sets `taskEditor = TaskEditorTarget.NewTask`. `FocusTaskEditor` maps that to `TasksContract.UiEvent.ShowTaskDialog()` on its lazily created `TasksViewModel`. That is the same call Tasks makes for its own add button, so the form is identical.

## Editor messages on Focus

Focus's `TasksViewModel` sends the same snackbar effects as on Tasks: "task added", reminder-scheduling failures, and the permission prompt's message. Nothing used to collect them on Focus, so a save that failed after the form closed went unreported. `showTasksEffect` (in `TasksEffects.kt`, extracted from `TasksScreen`) now renders them on both screens. `FocusScreen` starts collecting once an editor has first opened (so the view model is still created lazily) and keeps collecting after the editor closes.

## Bar geometry

Focus now has one flanking button, like Routines. Switching between Focus and Routines therefore leaves the pill still, and switching to or from Tasks re-centres the group exactly as Routines ↔ Tasks already does. `PortraitFloatingBarMotionTest` used Focus as its "nothing beside the pill" destination. It now uses the rail Settings destination, which still has no actions, and a new test checks that Focus shows add and not sort.

## Alternatives considered

- **Navigating to Tasks and opening its editor.** This was rejected because the issue asks to stay on Focus, and Focus already hosts the editor.
- **Pre-planning the new task for today.** This was rejected for now because it would diverge from "the same task creation sheet" and quietly add a reminder date. It is left as a possible follow-up.
