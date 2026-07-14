## Why

Tasks and Routines currently let their `Scaffold` consume the system-bar insets, so their page background and scrollable content stop short of the navigation-bar region even though the activity is configured for edge-to-edge drawing. This makes the main screens visually inconsistent with the Didi-derived Settings screen and leaves edge-to-edge support only partially effective ([GitHub issue #51](https://github.com/mandrecode/tempo/issues/51)).

## What Changes

- Make the Tasks and Routines screen scaffolds use edge-to-edge content insets consistently with Settings.
- Keep top app bar, floating navigation, snackbar, empty-state action, and scrollable-list content clear of system bars while allowing page backgrounds and lists to draw behind them.
- Add focused regression coverage for the scaffold inset policy.
- Non-goals: redesigning navigation or system bars, changing theme colors, changing modal-sheet behavior, or introducing a new app-wide layout architecture.

## Capabilities

### New Capabilities

- `edge-to-edge-screen-layout`: Defines how top-level Tempo screens draw through system-bar regions while preserving safe interactive content.

### Modified Capabilities

None.

## Impact

- Affects the Compose scaffolds and content-padding flow for Tasks and Routines, plus their UI regression tests.
- Reuses existing Material 3 and Compose window-inset APIs; no new dependencies, domain/data changes, or public APIs are required.
