## Context

The task editor stores descriptions as plain strings and edits them through a `TextFieldValue`, which already preserves selection and adds visual URL annotations. Issue #39 requests lightweight description organization similar to Google Keep.

## Goals / Non-Goals

**Goals:**

- Make `- ` description lists continue naturally when Enter is pressed.
- Let Enter on an empty dashed item exit list entry without leaving a marker behind.
- Preserve cursor position and leave unrelated edits unchanged.
- Cover the pure text transformation with unit tests.

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

## Risks / Trade-offs

- [Some users may expect checklist boxes rather than dashes] → Keep this change to the issue's explicitly allowed auto-dash alternative; persisted subtasks remain the app's interactive checklist mechanism.
- [IME updates can contain composition or multi-character changes] → Only transform the narrowly detectable single-newline insertion and leave every other update untouched.

## Migration Plan

No data migration is required. Ship the presentation change directly; rollback removes the text transformation without affecting stored descriptions.

## Open Questions

None.
