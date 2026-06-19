<!--
Title format (Conventional Commits):
  <type>(#<id>): <description>   e.g. fix(#679): preserve overdue periodic tasks
  <type>: <description>          when there is no issue
Types: feat, fix, docs, style, refactor, perf, test, chore
-->

## Summary

<!-- What does this PR do and why? -->

## Related issue

Closes #<!-- id -->

## Changes

<!-- Bullet the notable changes. -->
-

## Checklist

- [ ] Branch and PR title follow the conventions in [`AGENTS.md`](../AGENTS.md)
- [ ] `./gradlew ktlintFormat` run; `./gradlew :app:detekt` clean (or baseline unchanged)
- [ ] `./gradlew testDebugUnitTest` passes; coverage thresholds met
- [ ] Room entities/migrations changed → `app/schemas/` regenerated and committed
- [ ] `strings.xml` changed → translations added for every `values-<locale>/`
- [ ] Substantive change → an OpenSpec change exists and validates (`openspec validate`)

## Screenshots / recordings

<!-- For UI changes, before/after. -->
