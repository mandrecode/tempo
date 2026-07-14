## Context

The task editor stores descriptions as plain strings and edits them through a `TextFieldValue`, which already preserves selection and adds visual URL annotations. Issue #39 requests lightweight description organization similar to Google Keep.

## Goals / Non-Goals

**Goals:**

- Make `- ` description lists continue naturally when Enter is pressed.
- Let Enter on an empty dashed item exit list entry without leaving a marker behind.
- Preserve cursor position and leave unrelated edits unchanged.
- Cover the pure text transformation with unit tests.
- Keep description input responsive for long task and habit descriptions.
- Preserve autosave behavior while avoiding a new coroutine for every keystroke.
- Validate performance with controlled before/after traces from the connected Pixel 7.

**Non-Goals:**

- Rich-text or Markdown persistence and rendering.
- Interactive checkboxes inside descriptions; tasks already provide persisted subtasks for checkable work.
- Automatic numbered lists, nested list semantics, or reformatting existing descriptions.
- Changes to modal-sheet dragging or dismissal behavior.

## Decisions

### Transform only a single newline insertion

Add a small task-presentation utility that receives the previous and proposed `TextFieldValue`. It applies dash continuation only when both selections are collapsed and the proposal is exactly one inserted newline at the cursor. This avoids reformatting paste, deletion, selection replacement, IME composition, or restored content.

The utility examines the line immediately before the inserted newline. A non-empty line beginning with optional indentation and `- ` receives the same prefix on the new line. An empty dashed line removes its marker and the inserted newline so the cursor exits list mode on that line. Selection is recalculated explicitly; ordinary edits return the proposed value unchanged.

Alternative considered: use `KeyboardActions` to intercept Enter. Multiline text fields do not expose a reliable Enter action across software and hardware keyboards, so transforming `onValueChange` is more portable and testable.

### Keep formatting local to task descriptions

The behavior is invoked by `TaskBottomSheetContent` when its description changes. It is not added to the shared link-annotation utility or habit editor because issue #39 is task-scoped and changing all description editors would broaden product behavior.

### Render editable task text without URL annotations

Pass the task description's existing `TextFieldValue` directly to the Material text field. URL detection and annotated-string construction remain available to read-only description surfaces, where click handling is useful, but are removed from the per-keystroke editing path. Auto-dash formatting continues to operate on the original `TextFieldValue`, including its selection and composition state.

### Keep rapidly changing description state at a narrow composition boundary

Give task and habit description fields their own composable boundary and pass unrelated form sections only the values and callbacks they consume. A description edit may recompose its field and the lightweight editor coordinator, but unchanged category, priority, reminder, periodicity, subtask, and footer sections remain skippable.

Alternative considered: annotate the broad form parameter bundles as stable. Those bundles contain ordinary lists and frequently replaced values, so promising stability would hide rather than fix invalidation.

### Collect one debounced autosave stream per editor

Launch autosave once for the edited entity and collect Compose snapshots through `snapshotFlow`, followed by `distinctUntilChanged` and a 350 ms debounce. Updated callbacks and externally supplied form state are read through current Compose state so the collector does not restart for each character. The final snapshot still participates in sheet-dismissal flushing.

### Make clearing errors idempotent

Task and habit form-state holders return their existing state instance when errors are already absent. This prevents avoidable allocations and downstream invalidation during ordinary typing.

## Risks / Trade-offs

- [Some users may expect checklist boxes rather than dashes] → Keep this change to the issue's explicitly allowed auto-dash alternative; persisted subtasks remain the app's interactive checklist mechanism.
- [IME updates can contain composition or multi-character changes] → Only transform the narrowly detectable single-newline insertion and leave every other update untouched.
- [Changing autosave orchestration could drop the final edit] → Preserve pending-snapshot tracking and explicit dismissal flushing; cover debounce and final-snapshot behavior with focused tests.
- [Debug tracing exaggerates costs] → Compare the same debug build type, device, input length, and input script before and after; use the trace as relative evidence rather than a release-build guarantee.

## Migration Plan

No data migration is required. Ship the presentation change directly; rollback removes the text transformation without affecting stored descriptions.

## Open Questions

None.
