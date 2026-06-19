## Context

This change addresses architecture debt in presentation code by enforcing immutable UI contracts and decomposing large files into focused units. The main risk is behavior drift during extraction-heavy refactors.

## Key Decisions

1. **Immutable contract-first migration**
   - Convert contract `UiState` collection fields first.
   - Then adapt producers (ViewModels) and consumers (screens/previews/tests) to persistent collections.
   - This gives compiler-guided coverage across all call sites.

2. **Behavior-preserving decomposition**
   - Keep public composable and ViewModel class signatures stable.
   - Move logic into package-local/internal helper files and section composables.
   - Keep orchestration entry points in original files to preserve navigation and DI wiring.

3. **Exception narrowing in routines flows**
   - Replace generic `catch (Exception)` with:
     - `catch (CancellationException) { throw e }`
     - targeted recoverable branches (`IOException`, `IllegalArgumentException`, `IllegalStateException`)
   - Preserve existing user feedback paths (`showSnackbar`) for recoverable errors.

4. **Null-safety in presentation**
   - Remove `!!` usages in screen dispatch and grouping logic.
   - Use explicit null checks / `?.let` and safe grouping transforms.

5. **Settings D3 architecture decision**
   - Keep Settings as a documented thin exception.
   - Add explicit promotion criteria to avoid architecture drift.

## Decomposition Strategy

- **Bottom sheets:** split into content + focused form/footer/periodicity/chain files.
- **Cards:** split chain/quit/metadata rendering helpers from shared item cards.
- **ViewModels:** split data-loading/grouping and action clusters into extension/helper files.

## Invariants

- No public API changes to `TaskBottomSheet`, `HabitBottomSheet`, `TasksViewModel`, `RoutinesViewModel`.
- Existing UI behavior, side effects, and snackbar semantics remain intact.
- `UiState` collections remain persistent end-to-end.
