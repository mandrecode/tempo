# Startup And First-Open Performance Audit

Issue: [#435](https://github.com/mandrecode/tempo/issues/435)

## Release Targets

Measure on a lower-end Android profile before public release. Record the device, API level, build variant, compilation mode, and whether the install is fresh or warm.

- Cold startup `timeToInitialDisplayMs`: target under 1,200 ms, investigate over 1,500 ms.
- Cold startup `timeToFullDisplayMs`: target under 2,000 ms if reported, investigate over 2,500 ms.
- First tab interaction p95 frame duration: target under 32 ms, investigate sustained jank over 50 ms.
- First-open visual stability: no blank white flash after splash, no blocked permission prompt before user intent, no visible tab jump.

## Current Findings

- `TempoApp.onCreate()` was enqueueing periodic reminder refresh work through WorkManager during process startup. This was not required for first draw and could initialize WorkManager early on lower-end devices.
- `MainActivity` was activity-owning both `TasksViewModel` and `RoutinesViewModel`. Both ViewModels start database-backed flows in `init`, so first-open could initialize inactive-tab data before the user sees or interacts with that tab.
- `MainViewModel` combines lightweight SharedPreferences-backed theme and navigation state. This is required for the first screen and remains in the launch path.
- Typography uses bundled static Google Sans Flex fonts. The font family is statically allocated and should be measured with startup benchmarks, but no speculative font change is included in this pass.
- Reminder notification channels are created by receivers/live-activity paths when notifications are shown, not during normal app startup.

## Remediations In This Change

- Periodic reminder refresh scheduling moved out of `Application.onCreate()`.
- Reminder refresh enqueue now runs after the first Compose frame from `MainActivity` and remains idempotent per process plus durable through WorkManager unique periodic work `KEEP` semantics.
- Route ViewModels are now obtained inside their `NavHost` destinations instead of owned by `MainActivity`.
- Notification reminder intents are parsed by `MainActivity` into pending route actions. The active destination consumes the pending action once its ViewModel exists, preserving task, habit, and habit-chain notification opens without eager ViewModel initialization.
- A dedicated `:benchmark` module measures cold startup and cold startup plus first tab interaction.

## Benchmark Commands

Use a representative device or emulator profile. Benchmarks target the debug package
`com.mandrecode.tempo.debug` and clear its app data before each iteration. Do not point local
benchmarks at the production package `com.mandrecode.tempo` on a personal device.
These are safe debug-package smoke benchmarks and use no explicit compilation mode so they do not
require ProfileInstaller in the target app.

```bash
./gradlew :benchmark:connectedCheck
```

For local smoke checks on the debug package without benchmark instrumentation, use manual startup timing:

```bash
./gradlew installDebug
adb shell am force-stop com.mandrecode.tempo.debug
adb shell am start -W -n com.mandrecode.tempo.debug/com.mandrecode.tempo.MainActivity
```

For release-like manual timing:

```bash
./gradlew installRelease
adb shell am force-stop com.mandrecode.tempo
adb shell am start -W -n com.mandrecode.tempo/com.mandrecode.tempo.MainActivity
```

## Profiling Workflow

1. Start with `:benchmark:connectedCheck` on a representative lower-end profile.
2. If startup exceeds the targets, capture a Perfetto trace around cold startup.
3. Inspect main-thread work before first frame for WorkManager, Room open/migration, SharedPreferences reads, font/resource loading, dynamic color, and ViewModel initialization.
4. Repeat with a fresh install and with persisted user data containing reminders, tasks, routines, and habit chains.
5. Record results in the release checklist or follow-up issue with benchmark output, trace links, and device details.

## Follow-Up Candidates

- Add Baseline Profile generation if release measurements show class loading/JIT cost remains a startup bottleneck.
- Add benchmark scenarios for notification cold start into tasks and routines if reminder notification opens become release-critical.
- Revisit font weights if traces show repeated font loading or text measurement dominates first frame.
