## ADDED Requirements

### Requirement: Deferred Maintenance Startup Work
Tempo SHALL avoid starting non-UI maintenance work from `Application.onCreate()` when that work is not required to draw the first frame.

#### Scenario: App process cold starts
- **WHEN** the Tempo process is created for normal app launch
- **THEN** periodic reminder refresh scheduling is not enqueued from `Application.onCreate()`

#### Scenario: First activity reaches initial UI
- **WHEN** the first activity has reached initial UI work after launch
- **THEN** Tempo enqueues the periodic reminder refresh with the existing unique-work `KEEP` semantics

### Requirement: Lazy First-Open Feature Initialization
Tempo SHALL initialize feature ViewModels for task and routine tabs only when their destination is active or needed for a pending notification action.

#### Scenario: User opens default routines tab
- **WHEN** the routines tab is the launch destination
- **THEN** task feature data flows are not started solely because `MainActivity` was created

#### Scenario: User opens default tasks tab
- **WHEN** the tasks tab is the launch destination
- **THEN** routine feature data flows are not started solely because `MainActivity` was created

### Requirement: Notification Deep Links Preserve Target Opens
Tempo SHALL preserve reminder notification deep-link behavior while deferring route ViewModel creation.

#### Scenario: Task reminder notification launches app
- **WHEN** the app receives an intent containing a task reminder id
- **THEN** Tempo navigates to tasks and opens the matching task after the tasks ViewModel is available

#### Scenario: Habit reminder notification launches app
- **WHEN** the app receives an intent containing a habit reminder id
- **THEN** Tempo navigates to routines and opens the matching habit after the routines ViewModel is available

#### Scenario: Habit chain reminder notification launches app
- **WHEN** the app receives an intent containing a habit chain reminder id
- **THEN** Tempo navigates to routines and opens the matching habit chain after the routines ViewModel is available

### Requirement: Startup Benchmark Coverage
Tempo SHALL provide benchmark coverage that can measure cold startup and first meaningful tab interaction on a representative device or emulator.

#### Scenario: Benchmark module is executed
- **WHEN** the startup benchmark tests run on a connected device
- **THEN** they measure cold startup of the app package and report AndroidX benchmark metrics

#### Scenario: First interaction benchmark is executed
- **WHEN** the first interaction benchmark runs on a connected device
- **THEN** it opens the app and performs a representative first tab interaction with frame timing metrics

### Requirement: Release Performance Audit Documentation
Tempo SHALL document startup performance targets, measurement workflow, and launch-risk findings for release readiness.

#### Scenario: Developer prepares release-readiness measurements
- **WHEN** a developer reads the startup performance audit document
- **THEN** they can identify target metrics, recommended devices/emulators, benchmark commands, and current remediations
