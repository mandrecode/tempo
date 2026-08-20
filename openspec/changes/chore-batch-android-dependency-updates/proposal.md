## Why

The two open Dependabot dependency PRs update Kotlin- and Google-owned artifacts independently, despite their compatibility constraints. Each PR consequently fails CI before it can be merged.

## What Changes

- Update the Kotlin, Kotlin Symbol Processing, Hilt, immutable collections, and Kover dependencies as one tested unit.
- Select Hilt and KSP versions compatible with the updated Kotlin metadata format.
- Add any direct compile dependency required by the updated Hilt code generation.
- Verify the combined update against the repository's Android build and quality gates.

## Capabilities

### New Capabilities

- `android-build-dependency-compatibility`: The application build uses mutually compatible Kotlin, KSP, and Hilt toolchain artifacts.

### Modified Capabilities

None.

## Impact

- `gradle/libs.versions.toml`
- Gradle dependency resolution and generated Hilt component sources
- CI build, lint, unit, screenshot, static-analysis, and instrumented-test workflows
