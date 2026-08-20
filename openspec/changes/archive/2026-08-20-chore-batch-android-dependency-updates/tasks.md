## 1. Resolve compatible dependency set

- [x] 1.1 Identify a Hilt release compatible with Kotlin 2.4.10 and its required generated-source dependencies. (Hilt 2.60.1 supports Kotlin metadata 2.3.21, not Kotlin 2.4; keep Kotlin 2.3.21.)
- [x] 1.2 Update the compatible KSP, Hilt, immutable collections, Kover, and required annotation dependencies together in the version catalog.

## 2. Verify the build

- [x] 2.1 Run OpenSpec validation and JVM build, unit, lint, screenshot, schema, and static-analysis checks. (OpenSpec validation, assemble, unit, lint, KSP schema, detekt, and ktlint pass; local screenshot rendering differs from committed Focus references while the same suite passes on GitHub Actions for `main`.)
- [x] 2.2 Run automated instrumented tests on the reusable Android virtual device, or record the environment-specific blocker. (269 `:app` tests pass on Pixel 10 AVD; macrobenchmarks correctly reject emulator execution.)

## 3. Prepare the review branch

- [x] 3.1 Create a dedicated chore branch containing only the combined dependency update and its OpenSpec record.
- [x] 3.2 Review the diff and record the two obsolete Dependabot PRs it replaces. (Replaces Dependabot PRs #94 and #206.)
