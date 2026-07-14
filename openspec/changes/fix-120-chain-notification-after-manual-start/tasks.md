## 1. Chain Reminder Delivery

- [x] 1.1 Load chain member habits when a scheduled chain alarm is received and gate notification posting on occurrence-date progress.
- [x] 1.2 Keep chain recurrence rollover and next-alarm scheduling independent from notification suppression.

## 2. Regression Coverage

- [x] 2.1 Add unit tests for unstarted, partially started, and other-date chain completion histories.
- [x] 2.2 Run the focused receiver tests and the full debug unit-test suite.

## 3. Quality Gates

- [x] 3.1 Validate the OpenSpec change and run `ktlintFormat`, `ktlintCheck`, and `:app:detekt`.
