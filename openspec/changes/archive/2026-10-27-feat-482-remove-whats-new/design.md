# Design

## Decisions

- **Remove, don't hide.** A feature flag would keep the registry, the preference and the checklist step alive for a sheet nobody sees. The code is small and recoverable from history if the idea returns.
- **Remove the plumbing that only served it.** The sheet had to avoid showing over onboarding and before the splash handed over; those two signals had no other reader, so they go too.
- **Leave stored data alone.** The old preference file is not migrated or deleted: nothing reads it and removing it would need a code path that runs once and is then dead.
