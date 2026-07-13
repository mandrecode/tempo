## Context

The affected Compose fields set `ImeAction.Done` and provide custom `KeyboardActions.onDone` callbacks. Those callbacks clear focus, which replaces Compose's default Done implementation; the default implementation that hides the software keyboard is therefore never invoked. Didi handles the same interaction by explicitly hiding the keyboard and clearing focus.

Tempo's task and habit titles intentionally support up to three visual lines. The fix must preserve that behavior while making keyboard dismissal explicit and consistent.

## Goals / Non-Goals

**Goals:**

- Hide the software keyboard when Done is invoked on every active primary title/name field.
- Clear focus after Done so the editor visibly exits text-entry mode.
- Keep the implementation local to Compose presentation code and cover it with focused UI tests.

**Non-Goals:**

- Changing multiline title presentation, input limits, capitalization, or description input behavior.
- Submitting task, habit, or category editor forms from the Done action.
- Introducing a reusable text-field component, new architecture layer, or dependency.

## Decisions

### Invoke Compose's default Done action before clearing focus

Each custom `onDone` callback will call `defaultKeyboardAction(ImeAction.Done)` and then clear focus. Compose's default Done action owns software-keyboard dismissal, while explicit focus clearing matches Didi's completed-input state.

Task, habit, and category fields will obtain `LocalFocusManager` from inside `TempoModalSheet` content. The sheet is implemented with a Compose `Dialog`, which has a distinct focus owner; a focus manager captured by the parent before entering the dialog cannot clear the field inside it. Existing focus-manager properties on task and habit focus configuration models will therefore be removed.

This is preferred over obtaining and passing `LocalSoftwareKeyboardController` because the keyboard action scope already exposes the platform default behavior. It also shrinks the existing focus configuration models and avoids coupling field call sites to an additional controller.

Removing the custom callbacks was rejected because default Done hides the keyboard but does not express Tempo's desired focus clearing. Changing the fields to `singleLine = true` was rejected because it would alter long-title presentation.

### Keep handling at the affected call sites

The task, habit, and category fields have distinct state flows. Two explicit statements per callback are clearer than a shared helper whose only purpose would be forwarding Compose APIs.

### Remove the orphaned quick-task entry component

`QuickTaskEntryBar` lost its only production integration when the floating navigation replaced the old task bottom bar. Its remaining screen tests mounted it manually, so they did not describe reachable app behavior. Delete the component, its dedicated tests, and those artificial screen tests instead of preserving speculative behavior. Git history remains the source if a future product decision reintroduces quick entry.

### Verify behavior through Compose UI semantics and a device smoke test

Instrumented Compose tests will perform the IME action and verify focus clearing. A manual Pixel 7 smoke test will confirm the real IME closes smoothly because keyboard visibility can vary with emulator keyboard configuration.

## Risks / Trade-offs

- [Some third-party keyboards may render a newline key for multiline fields despite `ImeAction.Done`] → Preserve the current multiline UX and verify the supported Pixel/Gboard path; do not force a single-line regression.
- [Compose tests cannot reliably observe a real software keyboard in every test environment] → Assert the app-owned focus/submission results automatically and verify actual IME visibility on the physical Pixel 7.
- [A future custom Done callback could again omit the default action] → Add focused regression tests and keep the two-step callback pattern obvious at each active call site.

## Migration Plan

No data or API migration is required. Deploy as a presentation-only behavior fix. Rollback consists of reverting the editor callback changes and their tests.

## Open Questions

None.
