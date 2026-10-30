# Tasks

## 1. Navigation

- [x] 1.1 Remove the editor marker routes, `setEditorVisible`, the screens' docked-state callbacks and the custom scene strategy, so `NavDisplay` uses its default single-pane scene; verify no references remain and the app builds. (Replaces the first approach, one scene keyed by the main entry, after review.)
- [x] 1.2 Confirm `navigationTransition` still classifies tab switches and onboarding hand-off; pin both with `NavigationTransitionTest` (exact hand-off transition, not just "some animation") and verify with `./gradlew testDebugUnitTest`.

## 2. Device validation

- [x] 2.1 On the Pixel Tablet emulator (landscape, 1280 dp), record opening and closing a task in Tasks and a habit in Routines (Back key, back gesture, tapping the open task again); verify no recorded frame drops the list or chips and the pane is seen growing in and shrinking out. Keep the before and after recordings for the PR.
- [x] 2.2 On the Large Desktop emulator, resize a window across 1200 dp with an editor open, and switch tabs with an editor docked; verify the editor moves between pane and sheet as before and tab switches still cut instantly.
- [x] 2.3 On a phone emulator, open and close a task; verify the bottom sheet behaves as before.
- [x] 2.4 Run `DockedEditorPaneTest` (installDebug + installDebugAndroidTest, then `am instrument`) on the Pixel Tablet AVD (or the Pixel 10 AVD per AGENTS.md) and verify it passes.

## 3. Quality gates

- [x] 3.1 Run `./gradlew ktlintFormat ktlintCheck :app:detekt testDebugUnitTest koverVerifyDebug validateDebugScreenshotTest` and verify all pass without growing the detekt baseline.
- [x] 3.2 Run `openspec validate fix-501-docked-editor-blink --strict` and verify it passes.

## Workflow follow-up

- Archive this change with `openspec-archive-change` in the same PR before the final CI run.
- PR title `fix(#501): keep the screen in place when an editor docks`, body with `Closes #501` and the recordings.
