## Why

Several snackbar messages quote a linked entity name (a category, in the three known offenders) by wrapping it in literal apostrophes, e.g. "Category 'Groceries' added successfully." Issue #230 asks for the linked word to be rendered **bold** instead — matching how `DeleteCategoryConfirmDialog` already emphasizes a category name inside a confirmation dialog with an `AnnotatedString` bold span. Material3's `SnackbarVisuals.message` is a plain `String`, so today's snackbar plumbing (`ShowSnackbar` UI effect → `context.getString(messageResId, *formatArgs)` → `SnackbarHostState.showSnackbar(message: String, ...)`) has no way to carry a bold span through to the shared `ExpressiveSnackbarHost` renderer.

## What Changes

- Add a reusable way to show a snackbar with one bold-emphasized word (prefix text + bold word + suffix text) instead of a single quote-wrapped format string, reusing the same prefix/suffix string-resource split pattern `DeleteCategoryConfirmDialog` already uses.
- Introduce a custom `SnackbarVisuals` implementation carrying an `AnnotatedString` so `ExpressiveSnackbarHost`'s renderer can display bold spans; falls back to plain text for all existing (non-bold) snackbar calls, which are unaffected.
- Restructure the 3 known quote-wrapped string resources (`msg_category_exists`, `msg_category_added_success`, `msg_category_deleted_success`) into prefix/suffix pairs in `values/strings.xml` and `values-es/strings.xml`, and update their 4 call sites in `TasksViewModelCategoryAndPermissionActions.kt` to use the new bold-snackbar path.
- Swept the rest of the app (Routines/habits snackbar call sites, and a full `strings.xml` grep for quote-wrapped `%N$s` resources) — no other snackbar currently uses this quoting pattern, so no other call sites change, but the new infrastructure is shared/available to Routines since both features render through the same `ExpressiveSnackbarHost`.
- **Not in scope / explicitly excluded**: `BackupSection.kt`'s quote-wrapped strings (`backup_conflict_entry`, `backup_issue_unknown_category`, `backup_issue_unknown_parent_task`) are rendered directly as dialog list text, not through a snackbar — left unchanged.

## Capabilities

### New Capabilities
(none)

### Modified Capabilities
- `expressive-snackbar-feedback`: add a requirement that a snackbar can bold-emphasize one linked entity name within its message, alongside the existing message-only/actionable requirements which are unaffected.

## Impact

- `app/src/main/res/values/strings.xml`, `app/src/main/res/values-es/strings.xml` — replace 3 quote-wrapped resources with prefix/suffix pairs.
- `app/src/main/java/com/mandrecode/tempo/core/ui/components/ExpressiveSnackbarHost.kt` — render an `AnnotatedString` when present instead of always using the plain `message` string.
- New shared type(s) under `core/ui` for the bold-capable snackbar visuals/message representation.
- `app/src/main/java/com/mandrecode/tempo/features/tasks/presentation/TasksContract.kt`, `TasksViewModel.kt`, `TasksViewModelCategoryAndPermissionActions.kt`, `TasksScreen.kt` — thread the new bold-message option through the existing `ShowSnackbar` effect.
- No changes needed to `RoutinesContract.kt`/`RoutinesViewModel*.kt`/`RoutinesScreen.kt` call sites (no current offenders there), but `RoutinesScreen.kt`'s snackbar-building path is touched only if it shares the new visuals-rendering helper — verify during implementation.
- No Room schema, migration, or public API changes.

Closes #230
