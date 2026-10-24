# Tasks

## 1. Focus

- [x] 1.1 Add `UiEvent.AddTask` and `TaskEditorTarget.NewTask`; handle it in `FocusViewModel.onEditorEvent` and map it to `ShowTaskDialog()` in `FocusTaskEditor`.
- [x] 1.2 Publish `FocusFloatingBarState(onAddTask)` from `FocusScreen`.
- [x] 1.3 Extract `showTasksEffect` from `TasksScreen` and collect the Focus-hosted `TasksViewModel`'s effects on Focus's snackbar once an editor has opened. Checked by hand ("Task added successfully" on Focus); `TasksScreenTest` still passes.

## 2. Floating bar

- [x] 2.1 Add `FocusFloatingBarState`; thread it through `Navigation`, `FocusDestination`, `PersistentFloatingBar`, `PersistentPortraitFloatingBar` and `rememberAddAction` (Focus → "Add Task").
- [x] 2.2 Point `PortraitFloatingBarMotionTest`'s "no actions" destination at rail Settings, and add `focus_offersTheAddTaskButton_andNoSort`.

## 3. What's New and verification

- [x] 3.1 Replace the What's New entry with id `434` (en, es).
- [x] 3.2 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest lintDebug` and `openspec validate --strict`.
- [x] 3.3 Run `PortraitFloatingBarMotionTest`, `FocusContentTest` and `TasksScreenTest` on the Pixel 10 AVD, and check by hand that the Focus add button opens the sheet over Focus.
