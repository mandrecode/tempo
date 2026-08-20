## Why

Task descriptions are awkward to organize as short lists because every dash must be entered manually. GitHub issue [#39](https://github.com/mandrecode/tempo/issues/39) requests lightweight Google Keep-style formatting.

Profiling the implemented editor on a Pixel 7 also found that sustained description input can miss frames because each keystroke reprocesses task link annotations and broadly recomposes the task or habit form. The description experience should remain responsive at the supported 5,000-character limit.

## What Changes

- Continue a description dash list automatically when the user inserts a newline after a dashed item.
- End automatic dash continuation when the current dashed line contains no content.
- Preserve text selection and ordinary multiline editing when no dash-list rule applies.
- Add focused tests for the list transformation.
- Keep task-description typing responsive by updating URL styling only for the changed paragraph; retain enhanced link rendering in read-only surfaces.
- Isolate task and habit description changes from unrelated form sections.
- Replace restart-per-keystroke autosave effects with one distinct, debounced snapshot stream per editor.
- Avoid redundant form-error state updates and verify the result with comparable Pixel 7 traces.
- Non-goals: rich-text storage, interactive description checkboxes, Markdown rendering, and changes to sheet dismissal behavior.

## Capabilities

### New Capabilities

- `task-description-formatting`: Lightweight list-entry behavior for task descriptions.
- `description-editor-performance`: Responsive task and habit description editing with debounced persistence.

### Modified Capabilities

None.

## Impact

- Task bottom-sheet presentation code in `:app`.
- Habit bottom-sheet presentation code and task/habit form state handling in `:app`.
- Unit tests for description text transformation.
- No persistence schema, domain model, dependency, or public API changes.
