# Proposal

## Why

Issue [#461](https://github.com/mandrecode/tempo/issues/461): once the keyboard is already closed,
the task sheet needs two back actions to dismiss, while routine sheets need one. The #397 fix routes
back from explicit field focus only, but the keyboard can close while a field keeps focus (its own
hide key, or a back the keyboard consumes itself). The next back then only clears an invisible focus.

## What Changes

- Back ownership by the task editor additionally requires the keyboard to be visible; a focused
  field with a closed keyboard lets back fall through to the sheet's guarded dismissal.
- Keyboard visibility only gates the back handler's `enabled`; it never moves or clears focus, so the
  title ↔ description handoff regression of #388/#394 cannot recur.
- A back gesture the editor has started stays with the editor even if the keyboard closes mid-gesture.
- The pre-Android 13 back-key intercept uses the same ownership rule.

### Non-goals

- Changing the shared sheet, other sheet consumers, or task editor placement.

## Capabilities

### Modified Capabilities

- `task-editor-keyboard-dismissal`: keyboard-first back routing requires both field focus and a
  visible keyboard.

## Impact

- `features/tasks/presentation/components/TaskEditorBackHandler.kt` and its unit/instrumented tests.
