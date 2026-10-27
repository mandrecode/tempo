## Why

Tasks and Settings lists are made of separate cards with an 8dp gap (or one card cut by dividers), which reads busier than the Material 3 Expressive connected groups the app already uses for its button groups. Settings rows are also much taller than Android's own Settings (about 87dp against 72dp). And the floating bar's selected tab is an icon like the others, so the bar never says where you are.

## What Changes

- Tasks under one header (or the whole list when it has none) form a connected run: round outer corners at the run's ends, tight 8dp corners where cards meet, a 3dp seam between them.
- Each Settings section becomes a connected group, one card per option, with no dividers; an option that only shows while its switch is on folds away with its seam.
- Settings rows are sized like Android Settings: at least 72dp tall, 24dp icons, 24dp group corners.
- On the bottom bar, the selected tab fills with the primary colour and widens to show its name; rails fill the selected item with the same colour.

## Capabilities

### New Capabilities
- `connected-groups`: connected card groups in Tasks and Settings, Android-sized settings rows, and the labelled selected tab.

## Impact

- `core/ui/components/CardGroupPosition.kt` (new), `features/tasks/presentation/TaskCardGroups.kt` (new)
- `TasksContent.kt`, `TaskCard.kt`, `SettingsComponents.kt` and every settings section, `TempoBottomNavigation.kt`
- Screenshot references for the task list; What's New entry.
