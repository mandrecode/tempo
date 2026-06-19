# testing-coverage-uplift Specification

## Purpose
Define the baseline for trustworthy automated test quality by ensuring unit-testable logic is counted by coverage, repository behavior is validated against real Room wiring, and time/scheduler tests assert deterministic behavior.
## Requirements
### Requirement: Coverage filters include unit-testable logic
Kover verification SHALL include mapper logic and reminder scheduler orchestration logic that can be exercised with unit tests, while excluding only Android framework entry points that require instrumented/runtime environment support.

#### Scenario: Mapper and scheduler logic are counted by Kover
- **WHEN** Kover filters are evaluated for debug unit-test verification
- **THEN** mapper packages and reminder scheduler implementation orchestration are not excluded by blanket patterns

#### Scenario: Android entry points remain excluded
- **WHEN** Kover filters are evaluated
- **THEN** receivers, workers, and permissions/framework-only entry points remain excluded

### Requirement: Repository implementations are verified with real Room
The repository implementations for habits, habit chains, tasks, and categories SHALL have integration tests that run against an in-memory `TempoDatabase` instead of DAO mocks.

#### Scenario: Repository tests use in-memory TempoDatabase
- **WHEN** repository integration tests execute
- **THEN** each targeted repository reads/writes through real DAOs backed by an in-memory Room database

#### Scenario: Repository behavior assertions cover data round-trips
- **WHEN** repository methods perform insert/update/delete/query operations
- **THEN** assertions validate persisted output and mapped domain values from the database

### Requirement: Time-based unit tests are deterministic
Time-based tests SHALL use fixed-time fixtures rather than live wall-clock values to avoid midnight/time-zone flakiness.

#### Scenario: HabitChain utility tests use fixed createdDate values
- **WHEN** `HabitChainUtilTest` constructs habit and chain fixtures
- **THEN** fixture timestamps are deterministic and independent from current time

#### Scenario: Task reminder date utility tests use fixed now reference
- **WHEN** `TaskReminderDateUtilTest` validates advance-to-future behavior
- **THEN** assertions compare against a fixed `now` reference passed into utility calls where supported

### Requirement: Scheduler tests verify behavior, not fixture shape
Reminder scheduler tests SHALL verify schedule/cancel interactions with scheduler collaborators instead of asserting only static fixture properties.

#### Scenario: Habit scheduler schedule and cancel calls are verified
- **WHEN** schedule/cancel APIs are invoked with habits or chains
- **THEN** tests verify the collaborator receives the corresponding scheduling/cancellation calls

#### Scenario: Task scheduler schedule and cancel calls are verified
- **WHEN** task reminder scheduling/cancellation is invoked
- **THEN** tests verify alarm scheduling/cancellation behavior contracts via explicit interaction assertions
