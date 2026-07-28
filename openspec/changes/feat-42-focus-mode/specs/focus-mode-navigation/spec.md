# focus-mode-navigation Specification

## ADDED Requirements

### Requirement: Focus is a top-level navigation tab
The app SHALL expose Focus as a top-level destination alongside Routines and Tasks, rendered first
in the navigation bar and rail, with its own back stack and navigator section.

#### Scenario: Focus appears first among enabled tabs
- **WHEN** all three tabs are enabled and the navigation bar is rendered
- **THEN** the tab order is Focus, Routines, Tasks

#### Scenario: Focus keeps its own back stack
- **WHEN** the user opens a sheet in Focus, switches to Tasks, and returns to Focus
- **THEN** Focus is restored to the state it was left in, independently of the Tasks back stack

#### Scenario: Focus renders in every navigation tier
- **WHEN** the window is at the compact, medium, or expanded tier
- **THEN** Focus is present in the bottom bar, the compact rail, and the expanded rail with a label
  respectively

### Requirement: Tab enablement is defined per tab with at least one tab enabled
Tab enablement SHALL be modelled as a per-tab preference set rather than paired booleans, and the
"at least one tab must remain enabled" rule SHALL be enforced in a single place shared by Settings
and Onboarding.

#### Scenario: Focus is enabled by default
- **WHEN** an existing installation is updated, or a new installation completes onboarding without
  changing tab settings
- **THEN** the Focus tab is enabled

#### Scenario: Disabling the last enabled tab is rejected
- **WHEN** only one tab remains enabled and the user attempts to disable it
- **THEN** the tab remains enabled and its switch stays on

#### Scenario: Disabling the current default tab reassigns the default
- **WHEN** the user disables the tab currently set as the default
- **THEN** the default tab is reassigned to an enabled tab

### Requirement: Focus can be selected as the default tab
The default-tab picker SHALL offer every enabled tab, including Focus, and the app SHALL open on the
selected tab.

#### Scenario: Focus selected as default opens on launch
- **WHEN** the default tab is Focus, Focus is enabled, and onboarding is complete
- **THEN** launching the app opens the Focus tab

#### Scenario: Default tab points at a disabled tab
- **WHEN** the stored default tab is disabled
- **THEN** the app opens the first enabled tab instead of showing a disabled destination

#### Scenario: Picker is hidden when only one tab is enabled
- **WHEN** exactly one tab is enabled
- **THEN** the default-tab picker is not shown

### Requirement: Tab preferences survive backup and restore
Backup export and import SHALL carry per-tab enablement and the default tab, and imports of exports
written before Focus existed SHALL apply the Focus default rather than failing.

#### Scenario: Round trip preserves tab settings
- **WHEN** a backup is exported with Focus disabled and Tasks as the default, then imported
- **THEN** Focus is disabled and Tasks is the default after import

#### Scenario: Older export without Focus data
- **WHEN** a backup produced before this change is imported
- **THEN** the import succeeds and Focus is enabled with the previously stored default tab unchanged

### Requirement: Existing users are asked before their start tab changes
The app SHALL NOT change an existing user's default tab as a side effect of the update. The
what's-new sheet SHALL offer the change once, and declining or dismissing SHALL leave the default
unchanged.

#### Scenario: Update leaves the default tab alone
- **WHEN** a user whose default tab is Routines updates to the version introducing Focus
- **THEN** the app still opens on Routines

#### Scenario: Accepting the what's-new offer
- **WHEN** the user taps the start-tab action in the what's-new sheet
- **THEN** the default tab becomes Focus and the offer is not shown again

#### Scenario: Dismissing the what's-new sheet
- **WHEN** the user dismisses the sheet without taking the action
- **THEN** the default tab is unchanged and the offer is not shown again

### Requirement: Switching between top-level tabs remains an instant cut
Navigating between Focus, Routines and Tasks SHALL use no enter or exit transition, preserving the
behavior established for Routines and Tasks.

#### Scenario: Focus to Tasks switch
- **WHEN** the user taps Tasks while on Focus
- **THEN** the destination is swapped with no fade, slide, or scale animation
