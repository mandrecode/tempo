## 1. Fix auto-save dirty-check baseline

- [x] 1.1 In `TaskBottomSheetContent.kt`, change the `onSnapshot` auto-save condition to compare against `lastDispatchedSnapshot ?: taskSnapshot` instead of `taskSnapshot`
- [x] 1.2 In `TaskBottomSheetContent.kt`, change the `onSheetDismissRequest` save condition to compare against `lastDispatchedSnapshot ?: taskSnapshot` instead of `taskSnapshot`
- [x] 1.3 Verify `hasUnsavedChanges` still compares against the original `taskSnapshot` (unchanged) so the discard-prompt semantics are unaffected

## 2. Animate the category count badge

- [x] 2.1 In `CategoryChipRow.kt` `CategoryItem`, wrap the count badge in `AnimatedContent`/`AnimatedVisibility` using fade + `SizeTransform`, following the `subtaskBadge` pattern in `TaskCardMetadata.kt`
- [x] 2.2 Confirm the badge animates in on first appearance (count 0 → >0), out on disappearance (count >0 → 0), and crossfades on value changes (e.g. 2 → 3)

## 3. Verification

- [x] 3.1 Manually verify: create/open a task, move it to category B, move it back to category A, confirm the task's category persists as A after leaving the sheet (both via debounce and via immediate dismiss)
- [x] 3.2 Run `./gradlew ktlintFormat` and `./gradlew ktlintCheck`
- [x] 3.3 Run `./gradlew testDebugUnitTest`
- [x] 3.4 Run `./gradlew :app:detekt`
- [x] 3.5 Run `openspec validate fix-167-category-move-back-stuck --strict`
