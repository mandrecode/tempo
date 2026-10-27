# Design

## Decisions

- **Reopen on the last-used tab, not always Focus.** The last route was already persisted on every tab change but never read at startup; reading it is a few lines and removes the need for a setting. Focus remains the first-launch tab (`TempoTab.DEFAULT`).
- **Ignore, don't migrate, the old preferences.** The `enabled_tabs` and `default_tab` keys stay on disk unread; migrating them would be code that runs once and is then dead.
- **Backup stays tolerant, stops writing.** The backup JSON is decoded with `ignoreUnknownKeys`, so old files import unchanged. New exports omit the fields; an older build importing one uses its defaults. A legacy `defaultTab` was considered for one release and rejected as a field we want gone.
- **Remove the single-tab layout.** It existed only for "one tab enabled"; with that state unreachable, the solo-action bar and the scroll signal that compacted it are dead code.
