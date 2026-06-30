## 1. Implementation

- [x] 1.1 Replace completed-only back handling in `TempoModalBottomSheet` with predictive-back progress handling.
- [x] 1.2 Restore the sheet to the open position when predictive back is cancelled.
- [x] 1.3 Route predictive-back completion through the existing dismiss request path so unsaved changes still show discard confirmation.

## 2. Verification

- [x] 2.1 Run `openspec validate fix-89-predictive-back-bottom-sheets`.
- [x] 2.2 Run focused Kotlin compile or tests for the app UI change.
- [x] 2.3 Run `./gradlew ktlintFormat` and confirm formatting/static checks relevant to the touched files.

## 3. Material3 Experiment

- [x] 3.1 Replace the custom bottom-sheet internals with a Material3 `ModalBottomSheet` wrapper.
- [x] 3.2 Preserve the existing `TempoModalBottomSheet` call-site API and unsaved-changes guard.
- [x] 3.3 Build and run the debug package on the connected Pixel 7 for visual comparison.

## 4. Material3 Tuning

- [x] 4.1 Hide the Material3 bottom-sheet handle while disabling sheet drag gestures that can stick on repeated upward drags.
- [x] 4.2 Keep initial task/habit title focus immediate so keyboard and sheet animations start together.
- [x] 4.3 Add a small shared top inset for bottom-sheet content.

## 5. Custom Sheet Follow-up

- [x] 5.1 Restore the custom bottom-sheet implementation after the Material3 wrapper experiment showed a keyboard jump.
- [x] 5.2 Move predictive-back handling into the custom dialog and disable default dialog back dismissal.
- [x] 5.3 Clamp bottom-sheet dragging to downward movement and show the drag handle because drag is supported.
- [x] 5.4 Re-run formatting, compile, OpenSpec validation, and Pixel 7 smoke verification.
