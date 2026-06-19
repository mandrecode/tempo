## Why

GitHub issue [#435](https://github.com/mandrecode/tempo/issues/435) calls for a targeted startup and first-open performance audit before public release. Tempo currently does release-sensitive work during process/activity startup, so the first impression on lower-end devices needs measurable coverage and small remediations before 1.0.

## What Changes

- Add repeatable benchmark coverage for cold startup and first meaningful tab interaction.
- Document measurable release targets, profiling workflow, and audited startup risks.
- Defer reminder refresh scheduling so app process startup does not initialize WorkManager before the first UI frame.
- Avoid eagerly creating both task and routine route ViewModels during `MainActivity` startup.
- Preserve notification deep-link behavior while handing pending reminder opens to the route ViewModel once that destination exists.

Non-goals:

- Do not redesign onboarding or first-run education flows.
- Do not change reminder scheduling semantics, notification receiver behavior, or recurrence calculations.
- Do not optimize speculative UI/rendering costs unless the audit identifies them as launch-blocking.
- Do not require the benchmark module to run on every PR in CI; it should be available for targeted release-readiness checks.

## Capabilities

### New Capabilities

- `startup-performance`: Defines startup/first-open performance targets, benchmark coverage, and launch initialization constraints.

### Modified Capabilities

- None. No existing OpenSpec specs are present.

## Impact

- `TempoApp` startup initialization and reminder refresh scheduling.
- `MainActivity` notification intent handling and route ViewModel ownership.
- `core/ui/navigation/Navigation.kt` route composition and pending reminder open handoff.
- New benchmark Gradle module and benchmark dependencies.
- Documentation under `docs/performance/` for audit findings and measurement workflow.
