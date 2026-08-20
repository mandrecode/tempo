## Why

GitHub issue [#199](https://github.com/mandrecode/tempo/issues/199) reports that on wider windows (tablets/desktop, where the app uses a vertical floating navigation rail instead of a bottom bar), viewing the Tasks tab with the Sort and Clear-completed buttons both visible can leave no room in the rail's column for the Settings button, which then appears to shrink.

The root cause is in `PersistentLandscapeFloatingBar` (`app/src/main/java/com/mandrecode/tempo/core/ui/navigation/PersistentFloatingBar.kt`): the rail is a `Column(Modifier.fillMaxHeight()...)` holding the app title (expanded rail only), the Add action button, the Routines/Tasks nav pill, the Sort and Clear-completed buttons (`VerticalTaskActionButtons`), a `Spacer(Modifier.weight(1f))`, and finally the Settings button. When the fixed-size items above the spacer add up to more than the available rail height — most likely on an expanded rail (labels shown, taller rows) in a shorter window — the weighted spacer collapses to `0.dp` and the trailing Settings button is pushed past the bottom of the column's bounds. Because `SettingsRailButton` keeps its own fixed size, the button doesn't actually resize; it gets clipped by the containing bounds, which reads visually as shrinking.

## What Changes

- Make the floating navigation rail's content vertically scrollable so that when the rail's items (title, add action, nav pill, sort/clear-completed buttons, settings) don't fit the available height, the rail scrolls instead of clipping the pinned Settings button.
- Preserve current visual behavior when content fits within the available height: no visible scrollbar or layout shift, Settings stays pinned at the bottom via the existing weighted spacer.
- No change to which buttons are shown or their sizes/styling — only to how overflow is handled.

Non-goals:

- Do not change the bottom-bar (portrait, non-rail) navigation layout; it is unaffected by this bug.
- Do not change which task actions appear (Sort, Clear-completed) or their conditions (`hasCompletedTasks`, `isTasksRoute`).
- Do not redesign the rail's visual hierarchy (title → add → nav → task actions → settings) or introduce a different way to prioritize items when space is tight.

## Capabilities

### New Capabilities

- `floating-navigation-rail-overflow`: Defines the expected behavior of the vertical floating navigation rail (used on medium+ width windows) when its content height exceeds the available vertical space, ensuring the Settings button remains fully visible and correctly sized.

### Modified Capabilities

- None.

## Impact

- `app/src/main/java/com/mandrecode/tempo/core/ui/navigation/PersistentFloatingBar.kt` (`PersistentLandscapeFloatingBar`)
