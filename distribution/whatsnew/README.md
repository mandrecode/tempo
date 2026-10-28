# Play Store "What's new"

Release notes for Google Play, written by hand for each release in every
listing locale. `CHANGELOG.md` is written for developers from commit
subjects; these notes are written for the people using the app.

## Files

```
distribution/whatsnew/
└── <version>/          # e.g. 1.18.1, the version release-please is releasing
    ├── whatsnew-en-US  # English
    └── whatsnew-es-ES  # Spanish (Spain)
```

Each file is plain text, at most **500 characters** (Play's limit), with no
GitHub issue or PR links. Emoji section headers (`✨ New`, `🐛 Fixes`,
`⚡ Faster`) are optional; skip them when there's only a line or two.

## Flow

1. **Find the version.** The open release-please PR (`chore(main): release
   x.y.z`) names it. The 📝 Release Notes check on that PR fails until
   `distribution/whatsnew/x.y.z/` holds valid notes for every locale.
2. **Draft (optional).** Start from the changelog with internal entries
   (tooling, tests, refactors, "DX fixes", code-review follow-ups) filtered out:

   ```bash
   ./scripts/generate-whatsnew.sh --draft x.y.z
   ```

   This writes `x.y.z/whatsnew-en-US` only and never overwrites. Treat it as a
   list of what changed, not as copy: rewrite each line as what someone
   using Tempo will notice, and drop anything they won't.
3. **Write `whatsnew-es-ES`.** Translate the meaning, not the words (see
   [Writing](#writing)).
4. **Check and merge.** Run `./scripts/generate-whatsnew.sh --check x.y.z`,
   then open a normal PR to `main` (e.g. `docs: release notes for x.y.z`).
   Don't push to the release-please branch: release-please rewrites it.
   Once merged, release-please refreshes its PR and the check goes green.
5. **Publish.** When the release is created, `publish.yml` runs
   `generate-whatsnew.sh --check` before building and then
   `generate-whatsnew.sh <version>`, which copies the notes to
   `build/whatsnew/` for `r0adkll/upload-google-play` (`whatsNewDirectory`).

If more changes land before the release and release-please bumps the
version (say a `feat` turns 1.18.1 into 1.19.0), rename the directory and
add the new changes to both locales.

## What the checks enforce

`scripts/generate-whatsnew.sh` fails when, for a version, any locale's file:

- is missing or empty,
- is over 500 characters (counted as characters, not bytes),
- contains a GitHub issue or PR link,

or when `whatsnew-es-ES` is identical to `whatsnew-en-US`. CI runs
`--check-all` on every PR (all committed versions must stay valid) and
additionally `--check <version>` on the release-please PR. There is no
fallback that publishes changelog text: it would ship English under the
Spanish listing.

## Writing

Match the tone of `distribution/listing/`: short, warm, about what changes
for the person, not how it was built.

- **en-US:** plain English, sentence case, one change per line starting
  with `• `.
- **es-ES:** Spain's Spanish, informal *tú*. Use the app's own terms from
  `app/src/main/res/values-es/strings.xml` (Enfoque, Rutinas, Tareas,
  Cadenas de hábitos, Sesiones de enfoque, rachas…) so the notes name things
  the way the app does. Use «» quotes, as the listing does.

## Adding a locale

Add the locale to `LOCALES` in `scripts/generate-whatsnew.sh`, then a
`whatsnew-<locale>` file to the current release's directory. Play picks up
every `whatsnew-<locale>` file in the uploaded directory.
