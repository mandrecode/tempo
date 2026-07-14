## Context

The task editor stores descriptions as plain strings and edits them through a `TextFieldValue`, which already preserves selection and adds visual URL annotations. The shared custom modal sheet recognizes drag-to-dismiss gestures only on its handle, while task content is vertically scrollable. Issue #39 combines lightweight description organization with protection against unintended task-editor dismissal.

## Goals / Non-Goals

**Goals:**

- Make `- ` description lists continue naturally when Enter is pressed.
- Let Enter on an empty dashed item exit list entry without leaving a marker behind.
- Preserve cursor position and leave unrelated edits unchanged.
- Disable handle drag-to-dismiss for task editing without changing other modal sheets or explicit dismissal paths.
- Cover the pure text transformation with unit tests and sheet behavior with a Compose test.

**Non-Goals:**

- Rich-text or Markdown persistence and rendering.
- Interactive checkboxes inside descriptions; tasks already provide persisted subtasks for checkable work.
- Automatic numbered lists, nested list semantics, or reformatting existing descriptions.
- Disabling back, scrim, cancel, save, or accessibility dismissal actions.

## Decisions

### Transform only a single newline insertion

Add a small task-presentation utility that receives the previous and proposed `TextFieldValue`. It applies dash continuation only when both selections are collapsed and the proposal is exactly one inserted newline at the cursor. This avoids reformatting paste, deletion, selection replacement, IME composition, or restored content.

The utility examines the line immediately before the inserted newline. A non-empty line beginning with optional indentation and `- ` receives the same prefix on the new line. An empty dashed line removes its marker and the inserted newline so the cursor exits list mode on that line. Selection is recalculated explicitly; ordinary edits return the proposed value unchanged.

Alternative considered: use `KeyboardActions` to intercept Enter. Multiline text fields do not expose a reliable Enter action across software and hardware keyboards, so transforming `onValueChange` is more portable and testable.

### Keep formatting local to task descriptions

The behavior is invoked by `TaskBottomSheetContent` when its description changes. It is not added to the shared link-annotation utility or habit editor because issue #39 is task-scoped and changing all description editors would broaden product behavior.

### Configure task-sheet drag dismissal at the shared primitive boundary

Add a default-enabled `dragToDismissEnabled` parameter to the shared modal-sheet wrappers and use it to omit the drag pointer handler. The task editor passes `false`; existing call sites retain current behavior through the default. The handle remains visible and keeps its accessibility dismiss action, and all explicit dismissal mechanisms continue through the existing guarded dismissal state.

Alternative considered: consume vertical gestures in task content. That would interfere with scrolling and would couple form content to sheet gesture internals.

## Risks / Trade-offs

- [Some users may expect checklist boxes rather than dashes] → Keep this change to the issue's explicitly allowed auto-dash alternative; persisted subtasks remain the app's interactive checklist mechanism.
- [IME updates can contain composition or multi-character changes] → Only transform the narrowly detectable single-newline insertion and leave every other update untouched.
- [A shared API change could affect other sheets] → Default drag dismissal to enabled and add a task-editor-specific regression test.
- [Disabling handle dragging removes one convenience dismissal path] → Retain cancel, save, scrim, back, and accessibility dismissal.

## Migration Plan

No data migration is required. Ship the presentation change directly; rollback removes the task-sheet flag and text transformation without affecting stored descriptions.

## Open Questions

None.
