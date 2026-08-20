## 1. Build metadata

- [x] 1.1 Add Gradle resolution and fallback for the source commit SHA without changing version name/code.
- [x] 1.2 Extend `AppVersionInfo` and `AppVersionProviderImpl` with the abbreviated commit identifier.

## 2. Presentation and feedback

- [x] 2.1 Format the combined version/build string in Settings and pass it to feedback URL construction.
- [x] 2.2 Preserve the existing localized version label and add focused tests for metadata formatting and Settings behavior.

## 3. Verification

- [ ] 3.1 Run OpenSpec validation, formatting, unit tests, ktlint, and detekt; resolve any failures.
