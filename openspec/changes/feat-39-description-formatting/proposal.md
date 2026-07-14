## Why

Task descriptions are awkward to organize as short lists because every dash must be entered manually. GitHub issue [#39](https://github.com/mandrecode/tempo/issues/39) requests lightweight Google Keep-style formatting.

## What Changes

- Continue a description dash list automatically when the user inserts a newline after a dashed item.
- End automatic dash continuation when the current dashed line contains no content.
- Preserve text selection and ordinary multiline editing when no dash-list rule applies.
- Add focused tests for the list transformation.
- Non-goals: rich-text storage, interactive description checkboxes, Markdown rendering, and changes to sheet dismissal behavior.

## Capabilities

### New Capabilities

- `task-description-formatting`: Lightweight list-entry behavior for task descriptions.

### Modified Capabilities

None.

## Impact

- Task bottom-sheet presentation code in `:app`.
- Unit tests for description text transformation.
- No persistence schema, domain model, dependency, or public API changes.
