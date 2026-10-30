# Tasks

## 1. Scene strategy

- [x] 1.1 Change `EditorSupportingPaneSceneStrategy` to return one scene type keyed by the main entry's `contentKey` for a top-level route alone and for a top-level route with its supported editor on top, with correct `entries` and `previousEntries`; unsupported stacks still return `null`. Verify with new unit tests in `app/src/test/.../core/ui/navigation/` asserting the scene class and key are equal before and after the editor route is pushed, differ across tabs, and are `null` for onboarding.
- [x] 1.2 Confirm `navigationTransition` still classifies tab switches and onboarding hand-off with the new scene (main route read from `entries.first()`); add or adjust unit tests for both cases and verify they pass with `./gradlew testDebugUnitTest`.

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
