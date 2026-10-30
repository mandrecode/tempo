# Design

## Context

When an editor docks, the screen (`TasksScreen`, `RoutinesScreen`) shows the pane itself through `DockedEditorPane`, and also reports it to the navigator: `setEditorVisible` pushes `TasksEditorRoute` / `RoutinesEditorRoute`, whose entries draw nothing. The route only mirrors the pane's visibility; Back is handled by the docked sheet's own `PredictiveBackHandler`, nested inside the screen.

`NavDisplay` (Navigation 3) runs an `AnimatedContent` over scenes keyed by `AnimatedSceneKey(scene::class, scene.key)`. With one entry, the default `SinglePaneScene` is used (key = the entry's `contentKey`). With the editor on top, `EditorSupportingPaneSceneStrategy` returns `EditorSupportingPaneScene` (key = main `contentKey` to editor `contentKey`). The class and key both change, so `NavDisplay` animates between two scenes with `defaultNavigationTransition` (fade in + fade out). A `NavEntry` is drawn in only one scene at a time (`LocalEntriesToExcludeFromCurrentScene`), so the outgoing scene is empty while the incoming one, holding the same screen, fades in from transparent. That fade is the blink, and it also hides the pane's `expandHorizontally`. During the transition the entries are also capped at `STARTED`.

## Goals / Non-Goals

**Goals:**
- Docking and undocking produce no `NavDisplay` transition at all, so the screen's composition, scroll state and lifecycle are untouched and only `DockedEditorPane` animates.
- Keep tab switches, onboarding hand-off and any future screen changes on their existing transitions.

**Non-Goals:**
- Replacing the editor route mechanism (the back stack entry stays; other code may rely on it, and #502 will reuse it).
- Any change to `DockedEditorPane` motion.

## Decisions

### One scene class for a top-level screen, with or without its editor

`EditorSupportingPaneSceneStrategy` returns a single scene type (renamed to reflect it, for example `TopLevelScene`) whenever the top of the stack is a top-level main route alone, or a supporting editor route above one. Its `key` is the main entry's `contentKey` in both cases; `entries` is `[main]` or `[main, editor]`; `previousEntries` is the stack below the main entry plus, when an editor is open, the main entry, so `NavDisplay`'s back target stays correct. Content draws the main entry at the same call site in both shapes and the editor entry after it, so the main entry's composition is reused, not moved.

Because `AnimatedSceneKey` is equal before and after docking, `AnimatedContent` does not start a transition; it recomposes the same slot with the new scene.

Stacks the strategy doesn't recognise (onboarding, anything without the main-route metadata) return `null` and keep the single-pane fallback, so their transitions are unchanged. A tab switch changes the main entry, hence the key, so `navigationTransition` still sees it as a tab switch: the main route is read from `entries.first()`, which stays the main entry.

**Alternative considered: a no-op transition when only the editor changes.** In `navigationTransition`, return `EnterTransition.None togetherWith ExitTransition.None` when both scenes share a main entry. It's two lines, but `NavDisplay` still swaps scenes: the main entry is moved into a new scene, a transition runs (lifecycle drops to `STARTED` until it settles), and correctness depends on every future transition spec remembering the guard. Rejected in favour of removing the scene change.

**Alternative considered: dropping the editor routes.** The pane would no longer touch navigation at all, but it changes what the back stack represents and would have to be redone for #502. Out of scope for a bug fix.

## Risks / Trade-offs

- [Scene key collision: two different stacks producing the same key] → the key is the main entry's `contentKey`, unique per top-level route; an editor never appears without its main route below it, and unsupported pairs fall back to `null`.
- [Predictive Back on the editor] → the docked sheet's own handler consumes the gesture before `NavDisplay`, as today; verified manually on the tablet with the back gesture and the Back key.
- [Screenshot or instrumented tests assuming a fade] → none reference the scene transition today (`DockedEditorPaneTest` exercises the pane only); covered by the unit tests below.
- [The emulator's screen recorder drops frames, so a blink can be missed] → record each case more than once and compare against the before-fix recordings, which caught it every time.
