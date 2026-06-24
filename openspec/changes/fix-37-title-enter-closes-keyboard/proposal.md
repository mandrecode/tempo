## Why

GitHub issue [#37](https://github.com/mandrecode/tempo/issues/37) reports that pressing Enter while focused in a title field does nothing, when the expected behavior is to close the keyboard.

The affected task and routine editor title fields are multi-line text fields with `ImeAction.Done`. Unlike Didi's check-title field, which is single-line, Android keyboards may treat Tempo's title Enter key as a line-break action instead of dispatching Done. The fields already clear focus from `onDone`, and they also guard against inserted newline characters from entering the title. However, the multiline input mode means the Done path is not reliably reached.

## What Changes

- Explicitly hide the software keyboard when task editor and routine editor title fields handle Done.
- Explicitly hide the software keyboard when those title fields receive a newline fallback through `onValueChange`.
- Make task editor and routine editor title fields single-line title inputs, matching Didi's check-title behavior so IMEs dispatch Done consistently.
- Keep the current title validation, focus clearing, overflow-to-description handling, and save behavior unchanged.

Non-goals:

- Do not submit or save the form when Enter is pressed in the title.
- Do not change description field behavior.
- Do not change domain, data, persistence, scheduling, or navigation behavior.

## Capabilities

### New Capabilities

- `title-field-keyboard-dismissal`: Defines keyboard dismissal behavior for editor title fields when Enter/Done is pressed.

### Modified Capabilities

- None.

## Impact

- `app/src/main/java/com/mandrecode/tempo/features/tasks/presentation/components/TaskBottomSheetContent.kt`
- `app/src/main/java/com/mandrecode/tempo/features/tasks/presentation/components/TaskBottomSheetFormSections.kt`
- `app/src/main/java/com/mandrecode/tempo/features/tasks/presentation/components/TaskBottomSheetModels.kt`
- `app/src/main/java/com/mandrecode/tempo/features/routines/presentation/components/HabitBottomSheetContent.kt`
- `app/src/main/java/com/mandrecode/tempo/features/routines/presentation/components/HabitBottomSheetFormSections.kt`
- `app/src/main/java/com/mandrecode/tempo/features/routines/presentation/components/HabitBottomSheetModels.kt`
