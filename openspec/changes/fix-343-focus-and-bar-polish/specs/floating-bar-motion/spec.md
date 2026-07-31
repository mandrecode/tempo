## ADDED Requirements

### Requirement: The floating bar re-centres continuously

When a control joins or leaves the portrait floating bar because the active tab changed, the centred group SHALL move continuously to its new resting position. The group's width SHALL NOT change in a single frame at any point of the transition, including the frame on which a leaving control's node is finally disposed or an arriving control's node is first composed.

#### Scenario: Leaving a tab that owns extra controls

- **WHEN** the user moves from Tasks — whose bar carries the sort and clear-completed buttons and the add button — to Focus, which carries none of them
- **THEN** the leaving controls shrink to zero width and the group re-centres in one continuous motion
- **AND** the group does not settle and then shift again

#### Scenario: Arriving on a tab that owns extra controls

- **WHEN** the user moves from Focus to Tasks
- **THEN** the arriving controls grow from zero width and the group re-centres in one continuous motion
- **AND** the group does not jump on the frame the arriving controls are first composed

#### Scenario: The gap belongs to the control that can leave

- **WHEN** a control that can join or leave the bar is hidden
- **THEN** it contributes no width to the group, spacing included

#### Scenario: Spacing while every control is present

- **WHEN** every control of the active tab is fully shown
- **THEN** the gaps between adjacent controls are unchanged from before this fix
