## Context

Tasks and Routines each collect a feature-specific `ShowSnackbar` effect and render it through the shared `ExpressiveSnackbarHost`. The effect currently contains only a message resource and formatting arguments, so screens cannot expose or report a snackbar action. Destructive flows delete from Room immediately, cancel or alter alarms through domain scheduler interfaces, and then emit an action-free success message.

Undo spans presentation, domain, data, and reminder infrastructure. A restored category must bring back its tasks; task deletion must include subtasks; habit deletion must restore chain membership; chain deletion can either preserve its habits or delete them; and every restored item must retain its primary key so references and AlarmManager request identities remain stable. Room mutations must be atomic, while Android alarm operations cannot participate in the database transaction.

## Goals / Non-Goals

**Goals:**

- Give all Tempo snackbars one Didi-inspired visual treatment using Material theme roles.
- Make successful data-deletion flows reversible for the lifetime of their Undo snackbar.
- Restore complete domain snapshots with stable identifiers and reconcile reminders safely.
- Keep contracts testable and preserve Clean Architecture boundaries.

**Non-Goals:**

- Persist undo history across process death or after the snackbar is dismissed.
- Remove confirmation dialogs or add multi-level undo/redo history.
- Undo reminder clearing, completion toggles, edits, or other non-deletion actions.
- Add a dependency or change the Room schema solely to support transient undo.

## Decisions

### 1. Use action-aware snackbar effects with deletion tokens

Each feature's `ShowSnackbar` effect will optionally carry a localized action resource and an opaque deletion token. The Screen resolves resources, calls `SnackbarHostState.showSnackbar`, and reports either `ActionPerformed` or `Dismissed` to the ViewModel with that token. The ViewModel keeps pending deletion snapshots in a private token-keyed collection and removes each entry after undo succeeds or its snackbar is dismissed.

This avoids lambdas in MVI effects and prevents a queued snackbar from undoing a newer deletion. A single `pendingDeletion` slot was rejected because independent deletions can occur while snackbar effects are queued. Persisting tokens and snapshots in Room was rejected because the recovery window is transient UI state and must disappear with the snackbar.

### 2. Return complete domain snapshots from atomic repository deletion operations

Feature-specific snapshot models will describe everything needed for restoration:

- task deletion: the requested task and its deleted descendants;
- category deletion: the category, all tasks in it, and the previously selected category context;
- completed-task deletion: every completed task removed from the selected category;
- habit deletion: the habit and its ordered memberships in affected chains;
- habit-chain deletion: the chain and membership order, plus deleted habits and their other memberships when the user chose to delete habits.

Repositories will expose transaction-backed delete-and-capture and restore operations returning domain models at their boundary. Room implementations will read the snapshot and perform the deletion in one `withTransaction` block, and restore parent records before dependent records in one separate transaction. Existing mapper extensions remain the entity/domain boundary.

Capturing state in the ViewModel before calling today's delete use cases was rejected because concurrent Flow/database changes could make capture and deletion inconsistent. Delaying the actual delete until snackbar timeout was rejected because it would require optimistic filtering across multiple reactive lists and would defer irreversible scheduler side effects unpredictably.

### 3. Preserve primary keys, ordering, relationships, and completion data

Restore operations will insert snapshots with their original non-zero IDs. Parent/category/chain records are restored before child or membership records, and ordered relationships retain their recorded positions. Snapshot restoration is idempotent: inserts use a deliberate conflict strategy or existence checks so retrying the same token cannot duplicate records or renumber relationships. A token is consumed only after the database restore completes successfully.

The deletion and restore transaction boundaries are strictly Room-only. No AlarmManager or other Android scheduler call runs inside a Room transaction.

### 4. Reconcile scheduler state after committed database mutations

Deletion use cases will retain their current cancellation behavior after the delete transaction commits. Undo use cases first restore Room data atomically, then schedule each restored future reminder through the existing domain scheduler interfaces. Scheduling is keyed by stable task, habit, and chain IDs and therefore MUST be safe to repeat. Past, absent, completed, or otherwise ineligible reminders are not scheduled; periodic behavior follows the same rules as normal create/update flows.

If an alarm operation fails after data restoration, the restored data remains authoritative and the ViewModel shows an error or permission message. Existing startup/permission synchronization provides a later repair path. Rolling the database back after a scheduler failure was rejected because Android scheduler side effects are not transactional and rollback could produce a worse split-brain state.

### 5. Keep snackbar visuals shared and token-driven

`ExpressiveSnackbarHost` remains the only snackbar renderer. It will use a pill-like rounded surface, theme-derived container/content/action roles, compact horizontal spacing, and a visually distinct action affordance that meets the 48dp touch-target requirement. Tasks and Routines retain their existing bottom clearance above the floating navigation. A debug-source preview will cover message-only and Undo-action states in light and dark themes.

No Didi-specific colors or hardcoded user-facing strings will be introduced. The shared host accepts Material `SnackbarData`, so informational, error, and Undo snackbars use the same structure without duplicating feature UI.

### 6. Match Didi's snackbar component exactly

The visual reference is Didi's `DidiSnackbar` implementation in the sibling `Git/didi` checkout. Tempo's shared renderer will mirror that component's Compose structure and values: a custom `Surface` with 24dp horizontal and 8dp vertical outer padding, 560dp maximum width, 28dp corners, and a 1dp `outlineVariant` border. Its row uses 24dp horizontal and 16dp vertical content padding with 16dp spacing. The message uses `titleMedium`; the action uses a filled `primaryContainer` surface, `labelLarge` bold text, 24dp by 16dp padding, and Didi's spring-driven pressed-corner transition from 20dp to 12dp.

The package name, active Tempo theme, and elevation are adapted. The initial parity pass used 3dp shadow and tonal elevation instead of Didi's 6dp. Decision 7 supersedes the elevation, outline emphasis, and action animation values after in-app review; Didi's layout and content geometry remain the structural reference. Using the active theme keeps Tempo's brand and dynamic colors rather than importing Didi-specific color constants.

### 7. Integrate the Didi structure with Tempo's flatter surface language

After visual review on the Pixel 7, retain Didi's layout, typography, outer geometry, spacing, and filled action while using Tempo's established surface hierarchy. The snackbar uses `surfaceContainerHigh` directly with zero tonal elevation, a restrained 2dp shadow, and Tempo's subtle 1dp outline at 10% alpha. This keeps the transient surface distinguishable above content without making it read like a dialog.

The action uses Tempo's shared `rememberPressableButtonAnimation`, giving it the same 24dp resting and 12dp pressed corners and 150ms tween as other Tempo buttons. Reusing this utility aligns motion and shape behavior across the app and removes a snackbar-specific animation implementation.

## Risks / Trade-offs

- [Large category or bulk-completed snapshots consume memory during the snackbar window] → Keep only active token snapshots, discard on dismissal, and store domain data rather than serialized duplicates.
- [Process death after deletion removes the opportunity to undo] → Treat this as an explicit transient-undo limitation; the snackbar and its action also disappear on process death.
- [A restore conflicts with a newly created record using an old ID] → Preserve auto-generated key monotonicity and make restore conflict handling explicit and tested; report failure instead of overwriting unrelated data.
- [Scheduler reconciliation partially fails] → Keep restored Room data authoritative, make scheduling idempotent, surface the failure, and rely on normal reminder synchronization for repair.
- [Deleting habits with a chain affects memberships in other chains] → Capture all affected memberships before deletion and restore them in dependency order.

## Migration Plan

1. Add snapshot repository operations and domain undo use cases without changing existing schema definitions.
2. Route destructive ViewModel actions through snapshot-producing deletion operations and tokenized snackbar effects.
3. Add the shared snackbar action styling and Screen result handling.
4. Verify schema generation produces no change, then run unit, Room integration, Compose UI, localization lint, formatting, and static-analysis checks.

Rollback consists of reverting the feature code; no stored-data migration is required.

## Open Questions

None. The issue's "destructive actions" scope is interpreted as permanent data deletions, not reversible field changes or reminder clearing.
