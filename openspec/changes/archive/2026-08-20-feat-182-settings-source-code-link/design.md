## Context

Settings → About is a plain list of `SettingsItem` rows rendered by `SettingsContent.kt`. Two of them already leave the app: `review_app` opens the Play Store and `send_feedback` opens a Google Form. Both delegate to top-level helpers in `SettingsExternalActions.kt`, which build an `ACTION_VIEW` intent and catch `ActivityNotFoundException`. The feedback helper also surfaces a `no_browser_app` toast on failure; the review helper instead falls back from `market://` to an `https://` Play Store URL.

Issue #182 adds a third outbound entry — the project's GitHub repository — so the work is UI + intent only. Per the AGENTS.md "Settings feature scope (D3)" exception, Settings stays presentation-light: no repository, use case, or persistence is warranted for a constant URL.

## Goals / Non-Goals

**Goals:**
- Give users a one-tap path from Settings to `https://github.com/mandrecode/tempo`.
- Reuse the existing About row styling, icon language, and failure handling so the entry is indistinguishable in behavior from its neighbors.
- Keep the URL configurable at build time rather than embedded in Compose code.
- Keep all user-facing copy localized in `values/` and `values-es/`.

**Non-Goals:**
- Custom Tabs / WebView / in-app browsing.
- A licences, contributors, or open-source-attribution screen.
- Deep links to specific repo pages (issues, releases, discussions).
- Any change to the review or feedback entries.

## Decisions

- **Expose the URL as a `buildConfigField` (`SOURCE_CODE_URL`) in `app/build.gradle.kts`.**
  - Rationale: matches how `FEEDBACK_FORM_URL` is already handled, keeps the project-specific endpoint in one place, and lets a fork retarget it without touching Kotlin or resources.
  - Alternative considered: a `translatable="false"` string resource. Rejected because the sibling outbound URL already lives in `BuildConfig` and splitting the two conventions is worse than following the existing one.

- **Add `openSourceCode(context)` to `SettingsExternalActions.kt` and extract a shared `openExternalLink(context, url, failureLogMessage)` helper used by both it and `openFeedback`.**
  - Rationale: the two helpers would otherwise be byte-identical apart from the URL and log message; a single helper keeps the missing-browser toast behavior in one place.
  - Alternative considered: duplicating the try/catch. Rejected — duplicated intent + toast handling is exactly the kind of drift detekt and reviewers flag.
  - `openReview` is deliberately left alone: its two-step `market://` → `https://` fallback is not the same shape and folding it in would obscure it.

- **Place the entry last in the About section, after "Send Feedback", using `R.drawable.ic_code` and the `ic_chevron_right` trailing icon.**
  - Rationale: About is ordered from most to least commonly used; source code is the most niche. `ic_code` already exists in the drawable set (registered in `TempoIcon`) so no new asset is needed, and the neighboring rows all use `ic_chevron_right`.
  - Alternative considered: `ic_open_in_new` as the trailing icon (used by the Notifications/Language rows that jump to system settings). Rejected for consistency with the adjacent About rows.

- **Announce the feature by replacing `WhatsNewRegistry.latest` with id `settings-source-code-link`.**
  - Rationale: required by the AGENTS.md New Feature Checklist; only one entry is ever retained, so the previous `missed-reminder-catch-up` strings are rewritten in place rather than appended to.

- **Cover the entry with an instrumented Compose test rather than a unit test.**
  - Rationale: `SettingsContent*` and `*ExternalActions*` are both excluded from Kover, and the meaningful assertion — the row is present, scrollable-to, and clickable — is a Compose concern. The intent itself is a thin `startActivity` wrapper with no branching logic beyond the shared catch.

## Risks / Trade-offs

- [Risk] A device with no browser or handler for `https://` shows nothing on tap. → Mitigation: reuse the existing `no_browser_app` toast path already proven by `openFeedback`.
- [Risk] Extracting `openExternalLink` touches the working `openFeedback` path. → Mitigation: the extraction is behavior-preserving (same intent, same catch, same toast); `openFeedback` keeps building its own version-parameterized URL before delegating.
- [Risk] Hardcoding the upstream repo in `BuildConfig` is wrong for forks. → Trade-off accepted: it is a build-time constant a fork edits in one line, which is the same posture as the feedback form URL.
