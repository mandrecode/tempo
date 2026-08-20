## Context

PR #94 independently upgrades Hilt 2.59.2 to 2.60 and KSP 2.3.8 to 2.3.9; Hilt-generated sources then lack Error Prone annotations on the compile classpath. PR #206 independently upgrades Kotlin 2.3.21 to 2.4.10, but Hilt 2.60 supports metadata only through 2.3. Both failures surface as the same Hilt Java compilation task in CI.

## Goals / Non-Goals

**Goals:**

- Upgrade the pending dependency groups together, using mutually compatible toolchain versions.
- Declare the generated-source compile dependency required by the selected Hilt release.
- Verify the result with the repository's required Gradle tasks.

**Non-Goals:**

- Altering application behavior, application source code, or test baselines.
- Updating unrelated dependency families.

## Decisions

- Use a single version-catalog change and branch rather than merging either Dependabot PR. This preserves Kotlin/Hilt/KSP compatibility as one reviewable unit.
- Resolve the Hilt/Kotlin compatibility based on the selected Hilt release's published metadata support, then retain KSP's Kotlin-compatible version. Keeping Hilt 2.60 is rejected because CI establishes that it cannot consume Kotlin 2.4 metadata.
- Add Error Prone annotations as a direct compile dependency when its classes appear in Hilt-generated Java. Relying on a transitive dependency is rejected because the existing PR demonstrates it is absent from the compilation classpath.

## Risks / Trade-offs

- [Newer annotation processors can expose source or Gradle compatibility issues] → Run build, unit, lint, screenshot, static analysis, and schema verification before publishing.
- [Instrumented tests require an emulator and take longer] → Run against the project's reusable Pixel 10 AVD after local JVM checks.

## Migration Plan

1. Update the compatible dependency set in the version catalog.
2. Run the verification suite and investigate any failure.
3. Publish one replacement PR after all checks pass; the obsolete Dependabot PRs can then be closed.
