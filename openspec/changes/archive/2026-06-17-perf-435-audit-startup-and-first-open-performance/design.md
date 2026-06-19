## Context

Tempo starts through `TempoApp` and `MainActivity`. `TempoApp.onCreate()` currently enqueues the periodic reminder refresh through WorkManager, which can initialize WorkManager before the first activity frame. `MainActivity` also owns both task and routine route ViewModels even though only one tab is visible at launch, and both ViewModels start database-backed flows in `init`.

The app already disables WorkManager's default initializer so Hilt can provide worker configuration. Any reminder refresh change must preserve that configuration and keep the existing `ExistingPeriodicWorkPolicy.KEEP` semantics. Notification reminder launches must continue to open the correct task, habit, or habit chain after startup.

## Goals / Non-Goals

**Goals:**

- Make startup and first-open performance measurable through a targeted benchmark module.
- Move non-UI reminder refresh scheduling out of process startup and after first UI work.
- Avoid creating inactive tab ViewModels and their database flows during first-open.
- Preserve notification deep links for task, habit, and habit-chain reminders.
- Document release targets, audit findings, and manual profiling workflow for lower-end devices.

**Non-Goals:**

- Change reminder recurrence rules, alarm receiver behavior, notification channel creation, or Room schema.
- Add startup onboarding or alter first-run permission education.
- Add benchmark execution to required CI checks in this change.
- Replace SharedPreferences/DataStore or perform broad architecture cleanup.

## Decisions

1. Defer WorkManager enqueue from `Application.onCreate()` to first activity startup after first frame.

   Rationale: The periodic refresh is maintenance work and is already idempotent through unique periodic work. It does not need to block process startup or the first UI frame. Running it after first frame keeps reminder refresh available while reducing cold-start risk.

   Alternative considered: Keep WorkManager enqueue in `TempoApp` and only document the risk. Rejected because this is a clear, low-risk source of early startup work.

2. Keep reminder refresh scheduling idempotent and process-local.

   Rationale: `ExistingPeriodicWorkPolicy.KEEP` is the durable idempotency boundary. A small process-local guard prevents repeated enqueue attempts from multiple activity recreations without changing persisted WorkManager behavior.

   Alternative considered: Persist a flag in SharedPreferences. Rejected because WorkManager already owns durable work existence, and extra persisted state could go stale.

3. Let navigation destinations own their ViewModels.

   Rationale: Route ViewModels start feature data flows in `init`; creating both from `MainActivity` defeats lazy route loading. Keeping ViewModel creation inside each destination aligns startup work with the active UI.

   Alternative considered: Inject lazy ViewModel providers into `MainActivity`. Rejected because Compose/Hilt route ownership is simpler and avoids activity-level lifecycle ambiguity.

4. Represent notification opens as pending route actions.

   Rationale: Notification intent parsing belongs in `MainActivity`, but opening the actual bottom sheet requires the route ViewModel. A pending action can navigate to the correct route and be consumed by that destination once its ViewModel exists.

   Alternative considered: Keep eager ViewModels solely for notification launches. Rejected because it preserves the first-open cost this change is meant to remove.

5. Add a separate benchmark module.

   Rationale: Startup and first-interaction measurements need instrumented benchmark APIs, isolation from app code, and release-like build behavior. A dedicated module is standard and keeps benchmark dependencies out of runtime app code.

   Alternative considered: Shell-only `adb shell am start -W` checks. Rejected as the only automated option because they do not capture frame timing or integrate with AndroidX benchmark reporting.

## Risks / Trade-offs

- Deferred reminder refresh may run slightly later after app launch -> The work is periodic maintenance and still enqueued once per process with durable `KEEP` semantics.
- Pending notification actions could be dropped if route composition changes -> Model actions explicitly and consume them from destination effects tied to route triggers.
- Benchmark module adds Gradle complexity -> Keep it isolated, avoid CI requirement changes, and document the command separately.
- Baseline numbers vary by emulator/host -> Define targets as release gates to measure on representative lower-end profiles and document environment with each run.
