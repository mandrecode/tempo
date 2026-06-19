## MODIFIED Requirements

### Requirement: Play Store changelog generation

Generated Play Store release notes MUST remove technical changelog noise while preserving user-visible release-impact sections.

#### Scenario: Breaking entries are not repeated in other sections

- **GIVEN** a `CHANGELOG.md` version block contains the same cleaned entry under `### ⚠ BREAKING CHANGES` and another section such as `### Features` or `### Bug Fixes`
- **WHEN** `scripts/generate-whatsnew.sh` generates Play Store release notes for that version
- **THEN** the entry appears in the generated breaking section
- **AND** the matching entry is omitted from every non-breaking section
