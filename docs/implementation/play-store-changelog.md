# Play Store Changelog

Automated generation of user-friendly "What's New" text for Google Play Store
listings, based on the technical `CHANGELOG.md` maintained by release-please.

## How It Works

```
release-please (CHANGELOG.md) → generate-whatsnew.sh → distribution/whatsnew/
```

1. **release-please** creates a technical changelog in `CHANGELOG.md` from
   conventional commits.
2. **`scripts/generate-whatsnew.sh`** extracts the latest version's entries,
   strips technical details (commit hashes, PR links, issue references, scope
   prefixes). It also strips direct GitHub issue/PR URLs (including plain
   URLs and markdown links), then groups entries under user-friendly section
   headers:
   - `### Features` → `✨ New`
   - `### Bug Fixes` → `🐛 Fixes`
   - `### Performance Improvements` → `⚡ Performance`
   - `### ⚠ BREAKING CHANGES` → `⚠️ Breaking`

   If release-please lists the same cleaned entry under `BREAKING CHANGES`
   and any other section, the generated Play Store text keeps the breaking
   entry and omits the duplicate from non-breaking sections.

   Output is written to `distribution/whatsnew/whatsnew-en-US`.
3. **GitHub Actions workflows** pass `distribution/whatsnew/` to the
   `r0adkll/upload-google-play` action via `whatsNewDirectory`, so release
   notes are uploaded alongside the AAB. The publish workflow also fails if
   generated changelog files still contain GitHub issue/PR links.

## Directory Structure

```
distribution/
└── whatsnew/
    ├── whatsnew-en-US    # English (auto-generated, editable)
    └── whatsnew-es-ES    # Spanish (manual translation required)
```

Each file must stay under **500 characters** (Play Store limit).

## Usage

### Automatic (CI/CD)

Both `ci.yml` (build-release job) and `promote-to-production.yml` run the
script automatically before uploading to Google Play. No manual action is
needed for English release notes.

### Manual (Local)

```bash
# Generate for the latest version in CHANGELOG.md
./scripts/generate-whatsnew.sh

# Generate for a specific version
./scripts/generate-whatsnew.sh 0.1.7
```

After running, review `distribution/whatsnew/whatsnew-en-US` and translate
`distribution/whatsnew/whatsnew-es-ES` manually.

## Adding a New Language

1. Create a new file in `distribution/whatsnew/` named `whatsnew-<locale>`
   (e.g., `whatsnew-fr-FR` for French).
2. Write the translated release notes (≤ 500 characters).
3. The `upload-google-play` action picks up all files in the directory
   automatically.

## Customizing Release Notes

The auto-generated text is a good starting point but may need editing for
user-friendliness. Common adjustments:

- **Reword technical descriptions** into benefit-oriented language
  (e.g., "optimize task filtering" → "Tasks load faster")
- **Remove internal changes** that don't affect users
  (e.g., dependency updates, code style fixes)
- **Highlight key features** by moving them to the top of the list

Edit `distribution/whatsnew/whatsnew-en-US` directly after running the script,
or edit the file in the release-please PR before merging.

## Future Automation Ideas

When Play Store publishing is fully automated, consider:

- **AI-powered translation**: Use a translation API in CI to auto-generate
  `whatsnew-es-ES` from the English text.
- **AI rewriting**: Use an LLM step in CI to rewrite technical entries into
  user-friendly language automatically.
- **PR comment preview**: Add a workflow that posts the generated What's New
  text as a PR comment for review before release.
