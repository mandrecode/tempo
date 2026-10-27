# Design

## Context

`TaskEditorBackHandler` is enabled while `TaskEditorFocusState.ownsBack`, which was derived from
field focus alone. On API 33+ the keyboard may consume a back itself, and its hide key closes it
without touching focus, leaving `ownsBack` true with nothing to dismiss.

## Decisions

- **Gate ownership, never focus.** `TaskEditorFocusState` records keyboard visibility reported from
  `WindowInsets.isImeVisible` and `ownsBack = focused && keyboardVisible`. Clearing focus when the IME
  hides (#388) was rejected: Gboard briefly reports hidden during field handoffs, which closed and
  reopened the keyboard. Toggling only `enabled` means a transient flicker can at worst route a back
  that lands in that instant to the sheet.
- **Latch the started gesture.** The handler stays enabled while its own gesture runs, so a keyboard
  closing mid-gesture does not hand the completion to the sheet.
- **Same rule for the legacy intercept.** With the keyboard already closed the back key is not
  intercepted, so it reaches the sheet in one press on API < 33 too.
- **Keyboard visibility is injectable** (`isKeyboardVisible` parameter) so tests do not depend on
  whether the test device shows a soft keyboard.

## Risks / Trade-offs

- [Hardware keyboard: no IME is ever visible] → back dismisses the sheet directly, matching other
  sheets.
