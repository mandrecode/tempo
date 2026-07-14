## 1. Description list formatting

- [x] 1.1 Implement a selection-safe transformation for continuing and exiting task description dash lists
- [x] 1.2 Apply the transformation to task description edits without changing other description editors
- [x] 1.3 Add unit coverage for continuation, indentation, list exit, cursor placement, and unrelated edits

## 2. Task editor dismissal gestures

- [x] 2.1 Add default-enabled drag-to-dismiss configuration to the shared modal sheet
- [x] 2.2 Disable drag-to-dismiss for the task editor while preserving explicit dismissal paths
- [x] 2.3 Add Compose coverage proving a downward task-sheet handle drag does not dismiss the editor

## 3. Verification

- [x] 3.1 Run OpenSpec validation and focused task-description tests
- [x] 3.2 Run `ktlintFormat`, `ktlintCheck`, `:app:detekt`, and `testDebugUnitTest`
