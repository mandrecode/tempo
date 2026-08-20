## 1. Focus day refresh

- [x] 1.1 Ensure FocusViewModel replaces its dated agenda observer when a habit or chain toggle detects that the local day has advanced.
- [x] 1.2 Toggle the requested habit completion using the refreshed current day.

## 2. Regression coverage

- [x] 2.1 Extend the Focus ViewModel test harness with a controllable clock and date-specific agenda flows.
- [x] 2.2 Add tests for single-habit and chain toggles after midnight, including removal of the previous day observer.

## 3. Verification

- [x] 3.1 Run OpenSpec validation for `fix-378-habit-session-midnight`.
- [x] 3.2 Run focused unit tests, formatting, ktlint, and detekt.
