## 1. Generator update

- [x] 1.1 Track cleaned `### ⚠ BREAKING CHANGES` entries while building release notes.
- [x] 1.2 Skip matching entries in every non-breaking generated section.
- [x] 1.3 Keep first-letter capitalization portable on macOS/BSD tooling.
- [x] 1.4 Remove the existing duplicate breaking entry from tracked Play Store release notes.

## 2. Verification

- [x] 2.1 Run `openspec validate fix-800-release-notes-breaking-duplicates --strict`.
- [x] 2.2 Run `./scripts/generate-whatsnew.sh 0.8.0` and confirm the breaking entry is not duplicated under `✨ NEW`.
