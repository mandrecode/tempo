## Context

`ExpressiveSnackbarHost.kt`'s `ExpressiveSnackbar(snackbarData: SnackbarData)` renders `snackbarData.visuals.message` (plain `String`, from Material3's `SnackbarVisuals` interface) via a `Text(text = ...)` call. `TasksScreen.kt` and `RoutinesScreen.kt` both build that string with `context.getString(messageResId, *formatArgs)` from a `ShowSnackbar` UI effect (`TasksContract.kt` / `RoutinesContract.kt`, structurally identical but separate types), then call the stock `SnackbarHostState.showSnackbar(message: String, actionLabel: String?, duration: SnackbarDuration)` overload. Three Tasks string resources embed a linked category name via quote-wrapped `%1$s`; the fix must render that name bold instead, without breaking any of the ~20 other existing plain-message `showSnackbar` call sites across Tasks and Routines.

`DeleteCategoryConfirmDialog.kt` already solves the equivalent problem for a `Text` composable directly: `buildAnnotatedString { append(prefix); withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(word) }; append(suffix) }`, backed by `..._prefix`/`..._suffix` string resource pairs. The snackbar case is harder only because `SnackbarVisuals.message` is typed `String`, not `AnnotatedString`.

## Goals / Non-Goals

**Goals:**
- Render the linked word (category name) bold inside the snackbar message, replacing the `'%1$s'` quoting for the 3 known Tasks strings.
- Keep every existing plain-message `showSnackbar` call site (Tasks and Routines) source-compatible and visually unchanged.
- Make the bold-message capability reusable by Routines later without further plumbing changes, since both features share `ExpressiveSnackbarHost`.

**Non-Goals:**
- Not changing `BackupSection.kt`'s quote-wrapped strings — those render in a dialog list, not a snackbar.
- Not adding bold-word support for more than one linked word per message (none of the 3 offenders need it).
- Not changing the Undo/dismissal `SnackbarResult` handling, deletion-token plumbing, or snackbar duration/timing logic.

## Decisions

- **Add a custom `SnackbarVisuals` implementation (`TempoSnackbarVisuals`) carrying an `AnnotatedString`, used via Material3's `SnackbarHostState.showSnackbar(visuals: SnackbarVisuals): SnackbarResult` overload**, rather than a side-channel (e.g. a parallel map keyed by message text).
  - *Why:* Material3 already supports custom `SnackbarVisuals` as first-class input to `showSnackbar`; it composes cleanly with the existing `SnackbarResult`-based Undo/dismiss handling with no new state to keep in sync. A side-channel would require correlating snackbar instances by content, which is fragile with duplicate/rapid messages.
  - *Alternative considered:* Extend `SnackbarVisuals.message` itself to a rich-text-encoded string parsed at render time (e.g. markdown-ish `**word**`). Rejected — needlessly reinvents `AnnotatedString`, and risks the bold markers leaking into `.message` (used for the interface's default `equals`/accessibility string) or bleeding through if a category name itself contains `*`.
- **`ExpressiveSnackbar` renders `(snackbarData.visuals as? TempoSnackbarVisuals)?.annotatedMessage ?: AnnotatedString(snackbarData.visuals.message)`.** This makes the plain-`String` path (all existing callers) and the new bold path share one render call, with zero behavior change for existing callers.
- **Add a dedicated `showBoldSnackbar(...)` suspend function on each ViewModel (Tasks and Routines) alongside the existing `showSnackbar(...)`,** taking `@StringRes prefixResId, word: String, @StringRes suffixResId` instead of extending `showSnackbar`'s signature with more optional params.
  - *Why:* Keeps the common-case `showSnackbar(messageResId, formatArgs, ...)` signature and its ~20 call sites completely untouched; the bold case is opt-in and self-describing at the call site (`showBoldSnackbar(...)` vs. a `showSnackbar(...)` call with a new nullable param nobody else passes).
  - *Alternative considered:* Add a nullable `boldSegment: SnackbarBoldSegment? = null` param to the existing `showSnackbar`. Rejected as slightly noisier for the ~20 unaffected call sites' mental model, though source-compatible either way; a separate function reads more clearly at the 3 call sites that need it.
- **`ShowSnackbar` UI effect gains an optional `boldSegment: SnackbarBoldSegment?` field (default `null`)** rather than a second sealed-variant effect, keeping `TasksScreen.kt`/`RoutinesScreen.kt`'s existing `is UiEffect.ShowSnackbar ->` branch as the single handling site; it branches internally on whether `boldSegment` is present to decide whether to build an `AnnotatedString` or call `context.getString(messageResId, *formatArgs)` as today.
- **`SnackbarBoldSegment(@StringRes prefixResId: Int, word: String, @StringRes suffixResId: Int)`** lives in `core/ui` (shared by both Contracts) so Tasks and Routines use the identical shape; each feature's own `UiEffect.ShowSnackbar` still stays a separate type per existing convention (`TasksContract`/`RoutinesContract` don't share a base type today), just referencing the shared segment class.
- **String resources**: replace `msg_category_exists` → `msg_category_exists_prefix`/`_suffix` (same for `msg_category_added_success`, `msg_category_deleted_success`), following the exact wording/whitespace convention `delete_category_message_prefix`/`_suffix` already established (trailing space on prefix, trimmed at the append site; leading space/punctuation on suffix, trimmed at the append site). The old single-format resources are deleted (both locales) since nothing references them after the call-site updates.

## Risks / Trade-offs

- [`TempoSnackbarVisuals` must correctly implement all `SnackbarVisuals` members (`message`, `actionLabel`, `withDismissAction`, `duration`) or duration/dismiss-button behavior could silently regress for bold snackbars] → Mitigation: implement it as a straightforward data class delegating everything except `message`/the new `annotatedMessage`, and verify existing snackbar duration/action behavior (Undo flow) still works for the category-deleted bold snackbar specifically, since that's the one offender that also has an Undo action.
- [Prefix/suffix string restructuring changes translator-facing string keys] → Acceptable; matches the precedent already set by `delete_category_message_prefix`/`_suffix`, and en/es parity is updated in the same change per AGENTS.md.
- [Missed a 4th quote-wrapped snackbar resource somewhere] → Mitigated by the explicit full-tree grep already performed during scoping (only 3 offenders found, plus 3 unrelated `BackupSection.kt` dialog strings confirmed out of scope).
