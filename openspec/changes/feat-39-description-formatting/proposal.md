## Why

Task descriptions are awkward to organize as short lists because every dash must be entered manually, and scrolling the task editor can accidentally become a dismiss gesture. GitHub issue [#39](https://github.com/mandrecode/tempo/issues/39) requests lightweight Google Keep-style formatting and safer task editing gestures.

## What Changes

- Continue a description dash list automatically when the user inserts a newline after a dashed item.
- End automatic dash continuation when the current dashed line contains no content.
- Preserve text selection and ordinary multiline editing when no dash-list rule applies.
- Prevent drag gestures on the task editor sheet from dismissing the editor while retaining explicit cancel, scrim, and back dismissal flows.
- Add focused tests for list transformation and task-sheet behavior.
- Non-goals: rich-text storage, interactive description checkboxes, Markdown rendering, and changes to habit or generic sheet dismissal behavior.

## Capabilities

### New Capabilities

- `task-description-formatting`: Lightweight list-entry behavior and safe scrolling requirements for task descriptions.

### Modified Capabilities

None.

## Impact

- Task bottom-sheet presentation code and shared modal-sheet configuration in `:app`.
- Unit tests for description text transformation and Compose tests for the task editor.
- No persistence schema, domain model, dependency, or public API changes.
