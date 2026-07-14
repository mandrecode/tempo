## 1. Transactional deletion snapshots

- [x] 1.1 Add pure domain snapshot models for task, category, completed-task, habit, and habit-chain deletions, including dependent records, ordered memberships, and pre-deletion reminder values.
- [x] 1.2 Extend task/category repository contracts and Room implementations with atomic delete-and-capture and idempotent restore operations for individual tasks, categories, and completed tasks.
- [x] 1.3 Extend habit/chain repository contracts and Room implementations with atomic delete-and-capture and idempotent restore operations for habits and both habit-chain deletion choices.
- [x] 1.4 Add DAO and Room integration tests proving transaction rollback, stable-ID restoration, task hierarchy/order, completion history, and chain membership preservation.

## 2. Domain deletion and undo orchestration

- [x] 2.1 Update task deletion use cases to return snapshots only after committed deletion and keep scheduler cancellation outside Room transactions.
- [x] 2.2 Add task undo use cases that restore snapshots atomically and idempotently, then reconcile eligible reminders through task scheduler interfaces.
- [x] 2.3 Update habit and habit-chain deletion use cases to return complete snapshots and perform scheduler changes only after committed deletion.
- [x] 2.4 Add routine undo use cases that restore habits, chains, histories, memberships, and prior reminder values before idempotent reminder reconciliation.
- [x] 2.5 Add domain unit tests for every deletion/undo variant, retry/idempotency behavior, ineligible reminders, and scheduler failure after successful data restoration.

## 3. Tokenized MVI undo flow

- [x] 3.1 Extend Tasks and Routines snackbar effects with optional localized actions and opaque deletion tokens, plus events for action-performed and dismissed results.
- [x] 3.2 Keep token-keyed pending snapshots privately in each ViewModel, consume the correct snapshot on Undo, and discard it when its snackbar is dismissed.
- [x] 3.3 Route individual task, category, completed-task, habit, and both habit-chain deletion variants through snapshot-producing operations and emit localized Undo snackbars only on success.
- [x] 3.4 Handle restore and reminder-reconciliation failures with non-actionable localized feedback while retaining retry-safe state until the token is consumed or dismissed.
- [x] 3.5 Add ViewModel tests for each deletion action, correct token routing with queued deletions, dismissal cleanup, successful undo, and failure feedback.

## 4. Shared expressive snackbar UI

- [x] 4.1 Restyle `ExpressiveSnackbarHost` with the Didi-inspired pill shape, theme color roles, spacing, typography, and accessible action treatment.
- [x] 4.2 Update Tasks and Routines screens to resolve localized action labels, pass snackbar duration/action data, and forward `SnackbarResult` with the matching token while preserving floating-navigation clearance.
- [x] 4.3 Add English and Spanish Undo and result strings, and update destructive confirmation copy that currently states deletion cannot be undone.
- [x] 4.4 Add debug-source light/dark previews for message-only and Undo-action snackbar states.
- [x] 4.5 Add Compose UI tests for snackbar presentation, action semantics/touch target, and action-versus-dismiss result forwarding.

## 5. Verification

- [x] 5.1 Run `openspec validate feat-48-snackbar-undo`.
- [x] 5.2 Run `./gradlew kspDebugKotlin` and verify `app/schemas/` remains unchanged.
- [x] 5.3 Run `./gradlew testDebugUnitTest` and relevant Room/Compose instrumented tests on the default AVD.
- [x] 5.4 Run `./gradlew lintDebug`, `./gradlew ktlintFormat`, `./gradlew ktlintCheck`, and `./gradlew :app:detekt`.
- [x] 5.5 Manually smoke-test each deletion/Undo flow, queued deletion snackbars, timeout behavior, and future-reminder restoration on the connected Pixel 7 when available.

## 6. Pixel-equivalent Didi snackbar refinement

- [x] 6.1 Replace the stock Material snackbar composition with a Didi-equivalent custom surface, layout, typography, border, filled action, and pressed-corner animation, using Tempo-calibrated elevation.
- [x] 6.2 Update previews and Compose UI assertions for the Didi-equivalent actionable and message-only states without changing Undo behavior or accessibility.
- [x] 6.3 Run formatting, static analysis, unit/UI checks, and compare the resulting snackbar on the connected Pixel 7 against the Didi reference.

## 7. Tempo visual integration

- [x] 7.1 Soften the snackbar outline and elevation to match Tempo's flat tonal surfaces.
- [x] 7.2 Replace the Didi-specific action shape animation with Tempo's shared pressable-button animation.
- [x] 7.3 Run formatting, static analysis, focused UI tests, and inspect the result on the connected Pixel 7.
