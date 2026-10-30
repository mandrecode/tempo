# Proposal

## Why

On a Large window, opening or closing the docked task or habit editor makes the whole screen blink ([#501](https://github.com/sherrerapiqueras/tempo/issues/501), part of [#468](https://github.com/sherrerapiqueras/tempo/issues/468)). For at least one frame everything except the rail disappears, then the screen comes back with the editor already in place, so the pane's own slide-in is never seen. Reproduced on the Pixel Tablet emulator in landscape (1280 dp) by recording the screen: the frame after the tap shows only the rail on the background.

Docking a pane is the most frequent layout change on large windows, and Focus is about to dock its editors too ([#502](https://github.com/sherrerapiqueras/tempo/issues/502)), which would add a third blinking path.

## What Changes

- Opening or closing a docked editor no longer counts as a navigation between screens: the screen under it stays drawn and in place, and only the pane slides in or out with the gutter, as already specified for docking.
- Navigation that really changes screen (switching tabs, leaving onboarding) keeps its current transitions.

### Non-goals

- No change to which editors dock, the breakpoints, the pane's size, seams or animation.
- No change to how Back closes the docked editor.
- No change to bottom sheets below Large.
- Focus docking is [#502](https://github.com/sherrerapiqueras/tempo/issues/502), not this change.

## Capabilities

### New Capabilities

_None._

### Modified Capabilities

- `adaptive-window-classes`: adds a requirement that docking or undocking an editor keeps the screen beside it continuously visible.

## Impact

- UI navigation: `core/ui/navigation/EditorSupportingPaneScene.kt` (the scene strategy NavDisplay uses for top-level screens and their docked editors); possibly `core/ui/navigation/OnboardingNavigationTransitions.kt` if a transition guard is still needed.
- Tests: unit tests for the scene strategy; the existing instrumented `DockedEditorPaneTest` keeps covering the pane.
- Manual validation on the Pixel Tablet (1280 dp, Tasks and Routines) and a Large Desktop window, with screen recordings before and after; a phone checks the bottom-sheet path is unaffected.
