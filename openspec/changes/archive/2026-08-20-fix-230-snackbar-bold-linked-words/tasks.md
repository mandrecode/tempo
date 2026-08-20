## 1. Shared snackbar infrastructure

- [x] 1.1 Add `SnackbarBoldSegment(@StringRes prefixResId: Int, word: String, @StringRes suffixResId: Int)` under `core/ui`.
- [x] 1.2 Add `TempoSnackbarVisuals` (implements `SnackbarVisuals`) under `core/ui/components`, carrying an `annotatedMessage: AnnotatedString` alongside the required `message`/`actionLabel`/`withDismissAction`/`duration`.
- [x] 1.3 Update `ExpressiveSnackbarHost.kt`'s `ExpressiveSnackbar` to render `(snackbarData.visuals as? TempoSnackbarVisuals)?.annotatedMessage ?: AnnotatedString(snackbarData.visuals.message)` instead of the plain `.message` string.
- [x] 1.4 Add debug-source previews (light/dark) for a bold-emphasized snackbar state, alongside the existing message-only/actionable previews.

## 2. String resources

- [x] 2.1 In `values/strings.xml`, replace `msg_category_exists` with `msg_category_exists_prefix`/`msg_category_exists_suffix`; same for `msg_category_added_success` and `msg_category_deleted_success`. Suffixes use ` ` for the leading space (Android trims literal leading/trailing whitespace from string resources — the initial trailing-space-on-prefix / leading-space-on-suffix approach silently collapsed on device).
- [x] 2.2 Make the matching edits in `values-es/strings.xml`, preserving parity (the "exists" suffix is just "." with no leading space, since the Spanish sentence structure puts the name right before the period).
- [x] 2.3 Delete the 3 old quote-wrapped resources from both locale files once nothing references them.

## 3. Tasks feature wiring

- [x] 3.1 Add `boldSegment: SnackbarBoldSegment? = null` to `TasksContract.UiEffect.ShowSnackbar` (made `messageResId` nullable with a mutual-exclusivity `init` check).
- [x] 3.2 Add `showBoldSnackbar(@StringRes prefixResId: Int, word: String, @StringRes suffixResId: Int, @StringRes actionResId: Int? = null, deletionToken: Long? = null)` to `TasksViewModel.kt` alongside the existing `showSnackbar`.
- [x] 3.3 Update the 4 call sites in `TasksViewModelCategoryAndPermissionActions.kt` (`msg_category_exists` x2, `msg_category_added_success`, `msg_category_deleted_success`) to call `showBoldSnackbar(...)` with the category name as the bold word.
- [x] 3.4 Update `TasksScreen.kt`'s `UiEffect.ShowSnackbar` handling via a `toAnnotatedMessage(context)` extension: when `boldSegment != null`, build the `AnnotatedString` (prefix.trimEnd() + explicit space + bold word + suffix); otherwise falls back to the existing `context.getString(messageResId, *formatArgs)` path. Always routes through `snackbarHostState.showSnackbar(visuals = TempoSnackbarVisuals(...))` now (both bold and plain messages), keeping one code path.

## 4. Routines parity check (no offenders expected, verify only)

- [x] 4.1 Confirmed no Routines/habits string resource uses quote-wrapped `%N$s` snackbar quoting.
- [x] 4.2 No current Routines offenders — left `RoutinesContract`/`RoutinesViewModel*`/`RoutinesScreen.kt` untouched. The shared infrastructure (`SnackbarBoldSegment`, `TempoSnackbarVisuals`, `ExpressiveSnackbarHost` render logic) is reusable if Routines needs it later.

## 5. Verification

- [x] 5.1 Run `./gradlew ktlintFormat` and `./gradlew :app:detekt`.
- [x] 5.2 Run `./gradlew testDebugUnitTest` and `./gradlew koverVerifyDebug`.
- [x] 5.3 Run `./gradlew lintDebug` (clean of `MissingTranslation`/`ExtraTranslation`).
- [x] 5.4 On a Pixel 10 AVD: triggered all 3 bold snackbars (add category success, duplicate-name error, delete category with Undo) — category name renders bold with correct spacing, no stray quote characters. Undo button is present and its dispatch logic (`UiEvent.UndoDeletion` → `restoreDeletedCategoryUseCase`) is unchanged and covered by existing passing unit tests.
- [x] 5.5 Run `openspec validate fix-230-snackbar-bold-linked-words`.
