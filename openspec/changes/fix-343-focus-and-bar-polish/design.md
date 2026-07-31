## Context

Focus shipped in 1.11.0 as a third tab that shows the day's tasks, habits and chains together, hosting the owning tabs' own editors as bottom sheets rather than sending the user away. Three defects were filed against it. Two are Focus's own (#343, #345); the third (#344) is in the shared portrait floating bar and became visible because Focus is the first tab that carries *none* of the bar's optional controls, so switching to it collapses more of the bar than any previous transition did.

Current state of each:

- **#343.** `FocusContent` renders `HabitChainCard` with `onEdit = { }`. `FocusContract` has no chain-editing event and `FocusEditors.kt` has no chain editor. Tasks and habits both do: `FocusTaskEditor` drives a second `TasksViewModel`, `FocusHabitEditor` a second `RoutinesViewModel`.
- **#344.** `PersistentPortraitFloatingBar` centres a `Row` with `Arrangement.spacedBy(FloatingToolbarItemSpacing)` whose first and last children are `AnimatedVisibility`. `TaskActionButtons` repeats the pattern one level down with `Arrangement.spacedBy(TASK_ACTIONS_BUTTON_SPACING)`.
- **#345.** `UndatedTasksFooter` is a `Text` with `Modifier.fillMaxWidth().clickable{}.padding(vertical = 20.dp)` — an unbounded full-width ripple, no icon, no shape. `ImmersiveSessionView` already has the right pattern for the same hand-off, as a private `ImmersiveTextButton`.

Constraints: UI-layer only. `*Content.kt` stays free of ViewModel references, previews stay in `src/debug/`, no hardcoded strings, detekt baseline may not grow.

## Goals / Non-Goals

**Goals:**

- Make a chain card's body open its editor from Focus, reusing the Routines editor rather than writing a second one.
- Remove the discrete width change from the floating bar's tab transition, at both ends of the animation.
- Give the undated-tasks hand-off the same shape, icon and bounded ripple as the session screen's "Open in Tasks", from one shared composable.

**Non-Goals:**

- Retuning the floating bar's spring. The spring is fine; what reads as "flashy" is a layout jump layered on top of it.
- Touching the rail/landscape bar or single-tab mode. `VerticalTaskActionButtons` has the same latent pattern, but the rail's vertical group is start-aligned rather than centred, so a collapsing gap shifts nothing the user is watching.
- Any change to the agenda's contents, order, or counts.

## Decisions

### 1. Focus edits chains through the editor it already hosts for habits

`HabitEditor` in `RoutinesScreen.kt` is not a habit-only editor: it renders `HabitBottomSheet` from `uiState.habitForm`, which carries `editingHabit` *or* `editingHabitChain`, and it already wires `onConfirmHabitChain`, `onAutoSaveHabitChain` and `onDeleteHabitChain`. Focus therefore needs no new editor composable — only a second target for the one `FocusHabitEditor` already drives.

`FocusHabitEditor` becomes `FocusRoutineEditor`, taking a sealed `RoutineEditorTarget` (`SingleHabit` or `Chain`) and dispatching `ShowHabitBottomSheet(habit)` or `ShowHabitChainBottomSheet(chain)` into the same `RoutinesViewModel`. `FocusContract.UiState.editingHabit: Habit?` becomes `routineEditor: RoutineEditorTarget?`, mirroring how `taskEditor: TaskEditorTarget?` already models two reasons to open the task editor.

*Alternative considered:* a separate `FocusChainEditor` composable holding its own `RoutinesViewModel`. Rejected — two `RoutinesViewModel` instances under one screen would each observe the same flows, and a chain edit made in one would not be reflected in the other's form state.

*Alternative considered:* keeping `editingHabit` and adding a parallel `editingChain`, letting both be non-null. Rejected — the sheet can only show one thing, so two nullable fields encode a state the UI cannot render.

Because the sheet offers delete for a chain, Focus must host `DeleteHabitChainConfirmDialog` beside the `DeleteHabitConfirmDialog` it already hosts, or the delete button in the sheet would do nothing visible.

### 2. The floating bar's spacing moves inside the controls that can leave

`Arrangement.spacedBy(n)` inserts `n` between *children of the layout*, not between visible pixels. An `AnimatedVisibility` child that has shrunk to zero width is still a child, so the gap survives the shrink; when the exit transition finishes and the node is disposed, the child and its gap disappear together in one frame. For a centred group that is a `n/2` translation with no animation on it — measured at ~10 px on the reported video, against a 21 px (8.dp) spacing, and again in the other direction on the frame an arriving control is first composed.

The fix is to make the gap part of the thing that animates. The outer `Row` drops to `Arrangement.Center` (no `spacedBy`), and each optional control carries its own spacing as padding *inside* its `AnimatedVisibility` — `end` padding for controls left of the pill, `start` padding for controls right of it. `shrinkHorizontally`/`expandHorizontally` animate the child's full measured width, padding included, from and to zero, so the group's width is continuous across the whole transition including its final frame. `TaskActionButtons` gets the same treatment for its own inter-button gap, which has the same defect between sort and clear-completed.

*Alternative considered:* `Modifier.animateContentSize()` on the group. Rejected — it animates *after* the fact, so it would chase the spring rather than remove the jump, and it would also animate legitimate instantaneous changes.

*Alternative considered:* keeping `spacedBy` and holding the child alive with a zero-width placeholder. Rejected — it trades a jump for permanently wrong spacing, and needs the placeholder to know which side it is on.

*Alternative considered:* measuring the controls and driving the group with `animateDpAsState` offsets, which is what the single-tab layout does. Rejected — the current layout-driven centring is simpler and correct; only the spacing ownership is wrong.

### 3. "Open in Tasks" becomes one shared composable

`ImmersiveTextButton` is promoted out of `ImmersiveSessionView.kt` into `core/ui/components/` as `TempoLinkButton` (rounded `Surface`, bounded ripple, leading icon, primary-tinted label, `TextHandleMove` haptic). The session screen and the Focus footer both call it, so the two hand-offs cannot drift.

The footer keeps its own plural label (`focus_undated_tasks`) rather than borrowing `focus_session_open_in_tasks`: the count is the information, and "Open in Tasks" beside a count would say the destination twice. The icon carries the destination. No new strings.

*Alternative considered:* leaving `ImmersiveTextButton` private and writing a second one in Focus. Rejected — a copied button is exactly how the two affordances drift.

## Risks / Trade-offs

- **The floating bar's resting spacing changes by accident** → the padding values are the same constants (`FloatingToolbarItemSpacing`, `TASK_ACTIONS_BUTTON_SPACING`) moved, not retuned; a Compose UI test asserts the fully-shown gaps are unchanged.
- **`FocusHabitEditor`'s rename ripples into tests and previews** → it is `internal` with two call sites; the compiler finds them all.
- **A chain edited from Focus does not refresh the Focus agenda** → the agenda reads `habitChainRepository.getAllHabitChains()` as a flow, and the editor writes through the same repository, so the row updates the way an edited habit already does.
- **The `#344` fix cannot be proven by a unit test** → position over time is a device-level property. It is covered by a Compose UI test that steps the transition frame by frame under a controlled clock and asserts the group ends where its motion stopped — verified to fail by 4.19dp against the unfixed layout — plus a manual check against the reported transition.
- **`routineEditor` is a wider state field than `editingHabit`** → it is the same shape as the existing `taskEditor` field, so the contract stays internally consistent rather than gaining a new idiom.
