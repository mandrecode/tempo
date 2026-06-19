# Contributing to Tempo

Thanks for your interest in contributing! This file is a quick-start; **[`AGENTS.md`](AGENTS.md)
is the single source of truth** for conventions and workflow — please skim it before opening a PR.

## Prerequisites

- **JDK 21**.
- Android SDK with API **37** (compile/target); **minSdk 24**.

## Build & verify

```bash
./gradlew assembleDebug          # Build
./gradlew testDebugUnitTest      # Unit tests (CI uses this, not `test`)
./gradlew koverVerifyDebug       # Coverage: 80% line / 70% branch
./gradlew ktlintFormat           # Auto-fix formatting — run before every commit
./gradlew ktlintCheck            # Formatting check
./gradlew :app:detekt            # Static analysis
./gradlew lintDebug              # Android lint (fails on Missing/ExtraTranslation)
```

## Workflow

We use **OpenSpec** for substantive work (new features, behavior changes, non-trivial
fixes, cross-layer refactors). Run `openspec list` to see active changes and follow the
OpenSpec proposal → apply → archive flow. Trivial doc/formatting tweaks don't need a proposal.

1. **Branch** off `main` — never commit directly to `main`.
   - With an issue: `<type>/<id>-<slug>` (e.g. `feat/88-habit-history-graph`)
   - Without: `<type>/<slug>` (e.g. `fix/crash-on-empty-list`)
2. **Commit** using Conventional Commits:
   - With an issue: `<type>(#<id>): <description>`
   - Without: `<type>: <description>`
   - Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`
3. **Before committing:** run `./gradlew ktlintFormat`.
4. **Open a PR** against `main`. Title follows the commit format; the body summarizes the
   change and includes `Closes #<id>` when applicable.

## Architecture rules (summary)

- **Domain** is pure Kotlin — no `android.*`, Room, or Compose types; `kotlinx-datetime` only.
- **Data** is the only layer touching Room / preferences; map at boundaries with
  `toDomain()` / `toEntity()`.
- **UI** uses the `*Screen` / `*Content` split; `@Preview` lives in `src/debug/`; no
  hardcoded strings (use `stringResource`); `UiState` collections use
  `kotlinx.collections.immutable` types.

Details and the per-layer reference docs are in [`AGENTS.md`](AGENTS.md) and
[`docs/agents/`](docs/agents/).

## Tests, schemas & translations

- Add unit tests for use cases and ViewModels; UI-test `*Content` composables.
- If you change Room entities/migrations, run `./gradlew kspDebugKotlin` and commit the
  updated `app/schemas/` JSON.
- If you change `strings.xml`, add matching entries to every `values-<locale>/` folder
  (CI fails on `MissingTranslation` / `ExtraTranslation`).

## Reporting issues

Use the **Bug report** or **Feature request** templates when opening an issue.
