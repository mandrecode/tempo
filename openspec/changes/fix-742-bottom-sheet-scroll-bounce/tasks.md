## 1. Stop the scroll-up bounce

- [x] 1.1 Set `sheetGesturesEnabled = false` on the `ModalBottomSheet` inside `TempoModalBottomSheet` (covers the task and habit editor sheets), with an explanatory comment.
- [x] 1.2 Set `sheetGesturesEnabled = false` on the `ModalBottomSheet` inside `SortBottomSheet`, with an explanatory comment.
- [x] 1.3 Remove drag handles on affected sheets while gestures are disabled so affordance matches behavior.

## 2. Preserve dismissal

- [x] 2.1 Confirm scrim-tap dismissal still works with gestures disabled (scrim `onDismissRequest` is independent of `sheetGesturesEnabled`).
- [x] 2.2 Confirm system back and explicit Cancel/Save actions still dismiss the editor sheets.

## 3. Verification

- [x] 3.1 Run `./gradlew ktlintFormat`.
- [x] 3.2 Run `./gradlew :app:assembleDebug`.
- [x] 3.3 Run `./gradlew testDebugUnitTest`.
- [x] 3.4 Reproduce #742 on a device/emulator: open an existing habit/task and scroll up; confirm the sheet no longer bounces (slow-motion frame capture shows no sheet movement).
- [x] 3.5 Run `openspec validate fix-742-bottom-sheet-scroll-bounce --strict`.
