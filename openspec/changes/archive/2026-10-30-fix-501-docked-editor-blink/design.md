# Design

## Context

When an editor docks, the screen (`TasksScreen`, `RoutinesScreen`) shows the pane itself through `DockedEditorPane`, and also reports it to the navigator: `setEditorVisible` pushes `TasksEditorRoute` / `RoutinesEditorRoute`, whose entries draw nothing. The route only mirrors the pane's visibility; Back is handled by the docked sheet's own `PredictiveBackHandler`, nested inside the screen.

`NavDisplay` (Navigation 3) runs an `AnimatedContent` over scenes keyed by `AnimatedSceneKey(scene::class, scene.key)`. With one entry, the default `SinglePaneScene` is used (key = the entry's `contentKey`). With the editor on top, `EditorSupportingPaneSceneStrategy` returns `EditorSupportingPaneScene` (key = main `contentKey` to editor `contentKey`). The class and key both change, so `NavDisplay` animates between two scenes with `defaultNavigationTransition` (fade in + fade out). A `NavEntry` is drawn in only one scene at a time (`LocalEntriesToExcludeFromCurrentScene`), so the outgoing scene is empty while the incoming one, holding the same screen, fades in from transparent. That fade is the blink, and it also hides the pane's `expandHorizontally`. During the transition the entries are also capped at `STARTED`.

## Goals / Non-Goals

**Goals:**
- Docking and undocking produce no `NavDisplay` transition at all, so the screen's composition, scroll state and lifecycle are untouched and only `DockedEditorPane` animates.
- Keep tab switches, onboarding hand-off and any future screen changes on their existing transitions.

**Non-Goals:**
- Any change to `DockedEditorPane` motion, or to how editor state is owned (it stays in each screen's view model).

## Decisions

### Remove the editor marker routes

The editor routes carried no state: their entries drew nothing, the form lives in the view model, the docked sheet handles Back and Escape itself, and the draft survives resizes through the screen's movable content. They only mirrored the pane's visibility into the back stack, and that mirroring is what turned docking into a scene change. They are removed, with `setEditorVisible`, the screens' `onDockedEditorVisibilityChange` callbacks and the custom scene strategy. `NavDisplay` falls back to its default single-pane scene for every stack, so opening or closing an editor never reaches navigation. The metadata that marks top-level tab entries, still needed to recognise a tab switch, is renamed `TOP_LEVEL_ROUTE_METADATA`.

This supersedes the #151 design's plan to promote the editor into the back stack; that plan was never fully realised (the routes ended up as empty markers) and no spec requires back-stack participation.

**Alternative considered, and first implemented: one scene class keyed by the main entry.** The strategy returned the same scene with or without the editor, so `NavDisplay` saw no change. It worked on device, but review found it relied on `NavDisplay`'s internal scene identity (`AnimatedSceneKey(scene::class, scene.key)`), changed which entries `NavDisplay` considered "previous" (enabling its own back handling while an editor was open), and left every future editor route (#502) to fit the special case. Removing the routes fixes the cause instead.

**Alternative considered: a no-op transition when only the editor changes.** A guard in `navigationTransition`; still a scene swap underneath, with the lifecycle capped at `STARTED` until it settles. Rejected.

## Risks / Trade-offs

- [Something relied on the editor route being on the back stack] → none found: `currentRoute` skipped it explicitly, no test, spec or notification path references it, and the floating bar reads `currentRoute`/`topLevelRoute`.
- [Back while an editor is docked] → handled by the docked sheet's own `PredictiveBackHandler`, as before; with no editor route, `NavDisplay` no longer has a previous entry to compete for Back. Verified on the tablet with the Back key, the back gesture and re-tapping the open task.
- [A saved back stack from an older build containing an editor route] → saved navigation state doesn't survive an app update, so no migration is needed.
- [The emulator's screen recorder drops frames, so a blink can be missed] → each case recorded more than once and compared against the before-fix recordings, which caught it every time.
