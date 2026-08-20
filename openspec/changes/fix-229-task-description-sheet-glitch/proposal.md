## Why

When a user creates a task and the description grows onto additional lines, IME visibility changes can briefly move the task sheet completely off-screen while its scrim remains visible. The equivalent edit flow stays stable, so task creation needs the same uninterrupted layout behavior documented in [GitHub issue #229](https://github.com/mandrecode/tempo/issues/229).

## What Changes

- Keep the task creation sheet visible and bottom-anchored while a multiline description grows and the keyboard is shown or hidden.
- Keep the sheet anchored when focus moves between the title and description while the keyboard remains open.
- Preserve the existing focus, scrolling, dismissal, and task-editing behavior.
- Add regression coverage for multiline descriptions in the new-task path.
- Non-goals: redesigning the task form, changing validation or persistence, or changing sheet behavior for unrelated features.

## Capabilities

### New Capabilities

- `task-editor-layout-stability`: Defines stable task editor presentation while text content and IME insets change.

### Modified Capabilities

None.

## Impact

The change affects the Compose task editor and/or shared modal-sheet layout code plus focused UI regression coverage. It adds no API, data-model, persistence, localization, or dependency changes. Compact and medium modal layouts must remain stable; the expanded docked-pane layout must retain its existing behavior.
