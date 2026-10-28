# Play Store Listing Copy

Source of truth for the Play Store listing text (app title, short
description, full description). Unlike the Play "What's new" text, which
publish.yml generates from `CHANGELOG.md` at release time (see
`docs/implementation/play-store-changelog.md`), these files are **not** wired
into any CI workflow. Update the Play Console listing manually by copying
these files in, and keep both in sync when either changes.

## Files

- `title-<locale>.txt` — app name (≤ 30 characters)
- `short-description-<locale>.txt` — brief description (≤ 80 characters)
- `full-description-<locale>.txt` — full description (≤ 4000 characters)

Play Store's full description only supports `<b>`, `<i>`, `<u>` and line
breaks — no font colors — so emoji are used for visual "color" and section
scanning instead.

## Locales

- `en-US` — English (source)
- `es-ES` — Spanish (manual translation, kept in sync with `en-US`)
