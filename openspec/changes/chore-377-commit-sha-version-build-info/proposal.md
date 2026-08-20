## Why

Tempo’s settings and feedback context identify a build only by its semantic version, which is insufficient for diagnosing a specific release build or development snapshot. Issue [#377](https://github.com/mandrecode/tempo/issues/377) requests adding the source commit SHA to the version build information, using the format illustrated by `1.2.3 (au4b6i)`.

## What Changes

- Embed the checked-out Git commit identifier in the Android build configuration.
- Expose the identifier through the existing application version provider.
- Display the short commit identifier alongside the version name in Settings and in feedback links.
- Use an empty fallback when the Git commit cannot be resolved, such as a source archive build, and omit the optional suffix from display.
- Do not change the app’s semantic version or version code.

## Capabilities

### New Capabilities

- `version-build-info`: Exposes and presents the semantic version together with the source commit identifier.

### Modified Capabilities

- None.

## Impact

- Gradle build configuration and generated `BuildConfig` fields.
- `AppVersionInfo`, its implementation, Settings presentation state, and feedback URL construction.
- Unit/instrumented tests covering version metadata and the displayed format.
- No new dependencies, persistence changes, migrations, or runtime scheduling changes.
