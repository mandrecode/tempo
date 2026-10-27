# glass-material Specification

## Purpose
Define Tempo's glass material: an ambient backdrop, translucent panels, blurred floating chrome and modals, and the glass finish every component on them wears, all derived from the active colour scheme.

## Requirements

### Requirement: Pages keep the two-tone layout, with one gradient
Each top-level page (Tasks, Routines, Focus, Settings, Onboarding) SHALL paint, once at its root, an opaque backdrop: the scheme's `background` with one smooth gradient of the brand (primary) tone behind the header, fading out before the content. Layers inside the page SHALL be transparent over it. The content block and the docked editor pane SHALL be solid `surface`, with a faint, smooth wash of the secondary tone down from their top edge. Cards and other surfaces SHALL keep their own colours, opaque, so the list keeps its contrast.

#### Scenario: Dynamic colour
- **WHEN** dynamic colour is on and the wallpaper changes the scheme
- **THEN** the header gradient and the panel wash take the new scheme's tones

#### Scenario: Tab switch
- **WHEN** the user switches between Tasks and Routines
- **THEN** the crossfade shows no flash of the root surface between the two backdrops

### Requirement: Components wear the glass finish
Content components (cards, chips, toggle buttons, secondary buttons, neutral pills and rows, sheets and the content panel) SHALL be flat surfaces in their own colours with no outline, top light, sheen or shadow: the two-tone page sets them apart. The floating chrome (the bar, its buttons and the snackbar) SHALL not be outlined either: it stands out by a tone of its own. The rim SHALL follow a component's animated shape (a toggle rounding out when checked, a chip squaring under the finger). Solid colour fills (selected chips and toggles, filled and tonal buttons, coloured cards, pills and notices) SHALL stay clean, without the rim or sheen. Secondary buttons SHALL be filled with the cards' frost rather than outlined. The snackbar SHALL be chrome glass like the floating bar.

#### Scenario: Task card on a panel
- **WHEN** a task card sits on the Tasks content block
- **THEN** it reads as a flat surface in its own colour, with no outline, in both themes

#### Scenario: Selector toggles
- **WHEN** the user picks a priority in the task editor
- **THEN** the chosen option rounds out into a clean, solid colour, and no option is outlined

### Requirement: The selected tab shows its name
On the bottom navigation bar the selected tab SHALL be filled with the primary colour and widen to show its name beside the icon, easing between tabs as the selection moves; the others SHALL stay icons. Rails SHALL fill the selected item with the same colour.

#### Scenario: Switching tabs
- **WHEN** the user goes from Tasks to Routines
- **THEN** the Tasks pill shrinks back to an icon while the Routines one widens to show its name

#### Scenario: A focus session is running
- **WHEN** a session is running and the user is on Focus
- **THEN** Focus is the ordinary selected tab, like any other
- **WHEN** the user moves to another tab
- **THEN** the session pill takes the Focus slot and shows its countdown wherever the bar has room for it

### Requirement: Tab headers carry the brand
On phones, the Tasks, Routines and Focus headers SHALL show "Tempo" rather than the tab's name, since the selected tab in the bottom bar already names it. Where the tabs are unlabelled icons (the narrow rail), the header SHALL keep the section's name. Settings SHALL keep its own title.

#### Scenario: On the Tasks tab
- **WHEN** the user is on Tasks on a phone
- **THEN** the header reads "Tempo" and only the bottom bar says "Tasks"

### Requirement: Floating chrome is blurred glass
The floating navigation pill, the add and sort controls, the snackbar and the expanded rail's settings button SHALL stand out from the content panel in a tone of their own, with no outline, and SHALL blur the content beneath them, with its colour saturated back up, and draw their fill over it. The top bar's settings button, which has no content beneath it, SHALL instead use the add-category chip's light, translucent tint, so the header gradient shows through it. Role-coloured controls (add, an active sort, a selected settings button) SHALL keep their role colour as tinted glass. The blur SHALL follow the content as it changes (scrolling, switching tabs). It SHALL not cause frames to be drawn while the app is otherwise idle.

#### Scenario: List scrolls under the bar
- **WHEN** a task list scrolls beneath the floating bar on API 31+
- **THEN** the cards are visible through the bar, blurred, at their current position

#### Scenario: Idle
- **WHEN** nothing on screen changes
- **THEN** the app renders no frames

### Requirement: Modals blur the app behind them
Modal sheets and alert dialogs SHALL blur the app behind them, never switching the blur on or off at once: a sheet's blur and scrim SHALL follow how far it is open (growing as it slides in, easing off as it is dragged down, gone once it has slid away), and a dialog's blur SHALL ease in and out. The blur SHALL be drawn on the app's own root layer, so it shows on the frame the modal opens rather than when the app next redraws. It SHALL be heavy enough that the app behind reads as a wash of its colours, not recognisable shapes. While the blur shows, a modal sheet SHALL be nearly opaque, with a light rim and no shadow, so the form on it sits on a clean surface; the scrim or dim behind it SHALL be lighter.

#### Scenario: Opening the task editor
- **WHEN** the task editor sheet opens on API 31+ with window blurs enabled
- **THEN** the list behind it goes out of focus and the sheet is frosted

#### Scenario: Typing in a sheet
- **WHEN** the user types in the task editor and the sheet moves with the keyboard or a wrapping title
- **THEN** nothing from the app behind shows through or shifts under the form

#### Scenario: Opening a sheet over a still screen
- **WHEN** a sheet opens and nothing behind it moves
- **THEN** the app behind is blurred as the sheet arrives, without waiting for a scroll

### Requirement: Glass is the only look
The glass material SHALL be the app's one look, with no setting to turn it off, so every component is designed and checked once. Where blur is unavailable the material SHALL degrade as below rather than switch to a second design.

#### Scenario: Settings and onboarding
- **WHEN** the user opens Settings or onboarding's appearance page
- **THEN** there is no switch for the glass look

### Requirement: Glass degrades to an opaque material
Where backdrop blur is unavailable (below API 31, on low-RAM devices, in previews, or for chrome with no recorded backdrop), chrome SHALL use a denser, unblurred fill, and modals SHALL keep an opaque surface and the full scrim. Every surface SHALL remain legible in both themes.

#### Scenario: Android 11 device
- **WHEN** the app runs on API 30
- **THEN** the floating bar is a near-opaque surface, and sheets are opaque over the full scrim
