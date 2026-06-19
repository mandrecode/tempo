## 1. Startup Remediation

- [x] 1.1 Move periodic reminder refresh enqueue out of `TempoApp.onCreate()`.
- [x] 1.2 Add an idempotent deferred scheduler that enqueues reminder refresh after initial activity UI work.
- [x] 1.3 Update `MainActivity` to stop owning inactive route ViewModels at startup.
- [x] 1.4 Add pending notification action handoff from activity intent parsing to route destinations.
- [x] 1.5 Ensure task, habit, and habit-chain notification opens still navigate and open the correct sheet.

## 2. Benchmark Coverage

- [x] 2.1 Add a dedicated Android benchmark module to the Gradle build.
- [x] 2.2 Add benchmark dependency aliases and plugin configuration.
- [x] 2.3 Add a cold startup benchmark for the app package.
- [x] 2.4 Add a first meaningful interaction benchmark with frame timing metrics.

## 3. Audit Documentation

- [x] 3.1 Document release startup targets and representative lower-end measurement setup.
- [x] 3.2 Document current audit findings and the remediations made in this change.
- [x] 3.3 Document benchmark and manual profiling commands.

## 4. Verification

- [x] 4.1 Run `openspec validate perf-435-audit-startup-and-first-open-performance`.
- [x] 4.2 Run `./gradlew ktlintFormat`.
- [x] 4.3 Run `./gradlew testDebugUnitTest`.
- [x] 4.4 Run `./gradlew assembleDebug`.
- [x] 4.5 Run benchmark module compile or connected benchmark command if an emulator/device is available.
