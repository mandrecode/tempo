# Spec Delta

## ADDED Requirements

### Requirement: Pages sit on a theme-derived ambient backdrop
Each top-level page (Tasks, Routines, Focus, Settings, Onboarding) SHALL paint, once at its root, an opaque backdrop made of the scheme's `background` and soft glows of its primary, tertiary and secondary container colours. Layers inside the page SHALL be transparent over it. The content block and the docked editor pane SHALL be slightly translucent over the backdrop, with a light rim along their top edge. Content (cards, text, inputs) SHALL stay opaque.

#### Scenario: Dynamic colour
- **WHEN** dynamic colour is on and the wallpaper changes the scheme
- **THEN** the glows take the new scheme's container colours

#### Scenario: Tab switch
- **WHEN** the user switches between Tasks and Routines
- **THEN** the crossfade shows no flash of the root surface between the two backdrops

### Requirement: Floating chrome is blurred glass
The floating navigation pill, the add, sort and settings controls, and the expanded rail's settings button SHALL blur the content beneath them and draw a fill, a sheen and a light rim over it. Role-coloured controls (add, an active sort, a selected settings button) SHALL keep their role colour as tinted glass. The blur SHALL follow the content as it changes (scrolling, switching tabs). It SHALL not cause frames to be drawn while the app is otherwise idle.

#### Scenario: List scrolls under the bar
- **WHEN** a task list scrolls beneath the floating bar on API 31+
- **THEN** the cards are visible through the bar, blurred, at their current position

#### Scenario: Idle
- **WHEN** nothing on screen changes
- **THEN** the app renders no frames

### Requirement: Modals blur the app behind them
Modal sheets and alert dialogs SHALL blur the app behind their window while cross-window blur is available, easing the blur in as they open and out as a sheet dismisses. While the blur shows, a modal sheet SHALL be translucent with a light rim and no shadow, and the scrim or dim behind it SHALL be lighter.

#### Scenario: Opening the task editor
- **WHEN** the task editor sheet opens on API 31+ with window blurs enabled
- **THEN** the list behind it goes out of focus and the sheet is frosted

#### Scenario: Battery saver turns blurs off while open
- **WHEN** the system disables window blurs while a sheet is open
- **THEN** the blur goes away and the sheet returns to an opaque surface with the full scrim

### Requirement: Glass degrades to an opaque material
Where backdrop blur is unavailable (below API 31, on low-RAM devices, in previews, or for chrome with no recorded backdrop), chrome SHALL use a denser, unblurred fill, and modals SHALL keep an opaque surface and the full scrim. Every surface SHALL remain legible in both themes.

#### Scenario: Android 11 device
- **WHEN** the app runs on API 30
- **THEN** the floating bar is a near-opaque surface with its rim, and sheets look as they did before glass
