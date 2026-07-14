## 1. Description list formatting

- [x] 1.1 Implement a selection-safe transformation for continuing and exiting task description dash lists
- [x] 1.2 Apply the transformation to task description edits without changing other description editors
- [x] 1.3 Add unit coverage for continuation, indentation, list exit, cursor placement, and unrelated edits

## 2. Verification

- [x] 2.1 Run OpenSpec validation and focused task-description tests
- [x] 2.2 Run `ktlintFormat`, `ktlintCheck`, `:app:detekt`, and `testDebugUnitTest`

## 3. Description editor performance

- [x] 3.1 Capture and analyze a controlled long-description typing baseline on the connected Pixel 7
- [x] 3.2 Remove URL annotation work from active task-description editing while preserving read-only link rendering
- [x] 3.3 Isolate task and habit description recomposition from unrelated form sections
- [x] 3.4 Replace keyed per-edit autosave effects with distinct debounced snapshot streams
- [x] 3.5 Make task and habit error clearing idempotent
- [x] 3.6 Add focused regression coverage for editor and autosave behavior

## 4. Performance verification

- [x] 4.1 Capture and analyze the equivalent post-change Pixel 7 trace and compare frame metrics
- [x] 4.2 Run OpenSpec validation, formatting, static analysis, unit tests, and relevant instrumented tests

## 5. Copilot review follow-up

- [x] 5.1 Require the entire current line to be empty before exiting dash-list mode
- [x] 5.2 Add regression coverage for Enter immediately after a populated item's dash prefix
