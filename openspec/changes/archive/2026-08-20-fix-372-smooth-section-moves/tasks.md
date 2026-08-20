## 1. The mechanism

- [x] 1.1 In `ViewportHold.kt`, replace the `LaunchedEffect` + `scrollToItem` restore with a
      `SideEffect` + `requestScrollToItem`, comparing the section keys against the previous ones by
      hand and returning early when nothing crossed.
- [x] 1.2 Move the recorded position and the previous section keys off snapshot state onto a plain
      remembered holder, and drop the now-unneeded `rememberUpdatedState`.
- [x] 1.3 Change the private overload's parameter from `scrollToItem: suspend (Int, Int) -> Unit` to
      the non-suspend request, and point both public overloads at
      `LazyListState`/`LazyGridState.requestScrollToItem`.
- [x] 1.4 Rewrite the KDoc so it says what the mechanism now does — the position is requested for
      the next measure and the anchor key is forgotten, rather than restored after the fact — and
      keep the call-site contract (`holdViewport()` immediately before the event; section keys that
      change by value and only on a crossing) intact.

## 2. Room above the first card

- [x] 2.1 Take the Tasks list's top content padding from 8dp to 16dp, matching its own sides and
      Routines, and say in a comment why the top needs what the sides need — the 28dp corner curve.
- [x] 2.2 Confirm on the emulator that the first card clears the seam, and that
      `validateDebugScreenshotTest` stays green (no reference covers the Tasks list).

## 3. Checks

- [x] 3.1 `./gradlew :app:compileDebugKotlin` and `./gradlew :app:compileDebugAndroidTestKotlin`.
- [x] 3.2 `./gradlew ktlintFormat` then `./gradlew ktlintCheck` and `./gradlew :app:detekt`.
- [x] 3.3 Run `ViewportHoldTest` and `TasksViewportHoldTest` on the emulator, unchanged, and confirm
      the run reports the number of tests expected rather than a stale APK's. 2 + 20 tests, both
      green on Pixel_10.
- [x] 3.4 `./gradlew testDebugUnitTest`. Also `validateDebugScreenshotTest`, green.

## 4. Verify on a device

- [x] 4.1 On the emulator, with enough tasks to fill the list, record checking rows off in the Tasks
      screen and dating rows in the plan sheet, from the top of the list and from part-way down.
- [x] 4.2 Extract frames and compare against the same interaction before the change: no frame at the
      chased position, and the moved card's travel spread across frames rather than landing in one.
      Before: two states, ~180ms parked at the chased position (the dated card still at the top of
      the viewport, the list having followed it into the planned section) and then a snap back.
      After: the card slides down through the list across the whole change, and the chased position
      is never drawn.
- [x] 4.3 Confirm the view still holds — the next row arrives under the finger — so the fix has not
      cost the behaviour #371 established. Same end state on both builds, in the plan sheet and in
      the Tasks list.
