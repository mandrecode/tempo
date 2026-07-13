## Context

The affected Compose fields set `ImeAction.Done` and provide custom `KeyboardActions.onDone` callbacks. Those callbacks clear focus or submit data, which replaces Compose's default Done implementation; the default implementation that hides the software keyboard is therefore never invoked. Didi handles the same interaction by explicitly hiding the keyboard and clearing focus.

Tempo's task and habit titles intentionally support up to three visual lines, and quick task Done intentionally submits the current title. The fix must preserve those behaviors while making keyboard dismissal explicit and consistent.

## Goals / Non-Goals

**Goals:**

- Hide the software keyboard when Done is invoked on every active primary title/name field.
- Clear focus after Done so the editor visibly exits text-entry mode.
- Preserve quick-task submission and all validation behavior.
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

Removing the custom callbacks was rejected because default Done hides the keyboard but does not express Tempo's desired focus clearing or quick-task submission. Changing the fields to `singleLine = true` was rejected because it would alter long-title presentation.

### Keep handling at the affected call sites

The task, habit, category, and quick-task fields have distinct state and submission flows. Two explicit statements per callback are clearer than a shared helper whose only purpose would be forwarding Compose APIs.

For quick task entry, Done will run the existing submission path and invoke the default Done action. Focus clearing remains part of successful submission; an empty title still dismisses the keyboard through the default action without creating a task.

### Verify behavior through Compose UI semantics and a device smoke test

Instrumented Compose tests will perform the IME action and verify focus clearing and, for quick entry, single submission. A manual Pixel 7 smoke test will confirm the real IME closes smoothly because keyboard visibility can vary with emulator keyboard configuration.

## Risks / Trade-offs

- [Some third-party keyboards may render a newline key for multiline fields despite `ImeAction.Done`] → Preserve the current multiline UX and verify the supported Pixel/Gboard path; do not force a single-line regression.
- [Compose tests cannot reliably observe a real software keyboard in every test environment] → Assert the app-owned focus/submission results automatically and verify actual IME visibility on the physical Pixel 7.
- [A future custom Done callback could again omit the default action] → Add focused regression tests and keep the two-step callback pattern obvious at each active call site.

## Migration Plan

No data or API migration is required. Deploy as a presentation-only behavior fix. Rollback consists of reverting the four callback changes and their tests.

## Open Questions

None.
