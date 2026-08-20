## Why

GitHub issue [#800](https://github.com/mandrecode/tempo/issues/800) reports that Play Store release notes often repeat release-please `BREAKING CHANGES` entries under their original `Features` or `Bug Fixes` sections. That makes generated release notes noisier and can waste the strict Play Store character budget.

## What Changes

- Deduplicate generated Play Store release-note entries that also appear in `### ⚠ BREAKING CHANGES`.
- Keep the breaking entry under the breaking section so the most important release impact remains prominent.
- Keep non-breaking section grouping and existing cleanup behavior unchanged.

Non-goals:

- Do not change release-please changelog generation.
- Do not remove breaking-change entries from `CHANGELOG.md`.
- Do not add translation automation or change Play Store publishing workflows.

## Capabilities

### Modified Capabilities

- `play-store-changelog`: generated `distribution/whatsnew` text no longer repeats breaking-change entries in feature, fix, or other non-breaking sections.

## Impact

- `scripts/generate-whatsnew.sh` duplicate filtering.
- Local release-note generation output for versions with breaking changes.
