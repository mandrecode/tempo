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

## 6. Idle URL styling follow-up

- [x] 6.1 Add a cached, text-preserving visual transformation for task-description URL spans
- [x] 6.2 Refresh task-description URL styling after 300 ms of idle input
- [x] 6.3 Add focused regression coverage and run the relevant quality checks

## 7. Copilot tab-isolation follow-up

- [x] 7.1 Rebind habit editor state and autosave identity to the selected tab
- [x] 7.2 Compare debounced and dismissal snapshots against the active tab's initial entity
- [x] 7.3 Add regression coverage for switching between unchanged habit and chain editors
- [x] 7.4 Reply to and resolve every Copilot review conversation

## 8. Stale URL styling regression

- [x] 8.1 Hide cached URL spans while their source text differs from the current description
- [x] 8.2 Add regression coverage for typing between multiple cached links
- [x] 8.3 Run focused tests and quality checks without committing

## 9. Incremental live URL styling experiment

- [x] 9.1 Replace delayed whole-description URL refresh with changed-paragraph range updates
- [x] 9.2 Preserve auto-dash output, selection offsets, and unrelated link styling during edits
- [x] 9.3 Add focused coverage for insertion, deletion, paste, and edits inside links
- [x] 9.4 Run quality checks and install the uncommitted experiment on the Pixel 7
