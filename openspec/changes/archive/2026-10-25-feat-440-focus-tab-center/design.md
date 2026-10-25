# Design

`TempoTab` is documented as the single source of display order, so the change is a reorder of its entries rather than a nav-only list that Settings and onboarding would disagree with. Nothing depends on `ordinal` for identity. Persistence, backup and route mapping use `preferenceValue` / `name`. The one `ordinal` use is the default-tab fallback in `TabPreferencesPolicy`.

That fallback used to be "first enabled tab in display order", which was Focus whenever it was enabled. With Routines first, the same rule would open the app on Routines after a user disabled their chosen default. The rule is now "home (`TempoTab.DEFAULT`) if enabled, otherwise the first enabled tab in display order". This keeps today's behaviour and decouples where the app opens from where tabs sit in the bar.

## Backup restore

`BackupSettingsDataSource.apply` used to set each tab on or off in display order. Because the repository refuses to turn off the last enabled tab, some transitions left an extra tab on: restoring Focus-only over Routines-only once Routines came first, and Tasks-only over Focus-only even before. Restore now enables every wanted tab before disabling the rest, which is correct for any order. `BackupSettingsDataSourceTest.apply switches a single enabled tab for another` checks every single-tab pair against the real `TabPreferencesPolicy` guard. Copilot raised this in review.
