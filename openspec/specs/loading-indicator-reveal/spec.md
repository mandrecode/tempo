# loading-indicator-reveal Specification

## Purpose
Defines when the shared full-size loading indicator becomes visible, so fast loads do not flash it.

## Requirements

### Requirement: Loading indicator waits before appearing
The shared loading indicator SHALL stay invisible for a grace period of 400 ms after it is first shown. If loading is still in progress after the grace period, it SHALL fade in. The indicator SHALL keep its layout space during the grace period, so surrounding content does not move when it appears.

#### Scenario: Fast load
- **WHEN** a screen's data finishes loading within the grace period
- **THEN** the loading indicator is never visible, and the content replaces the empty space

#### Scenario: Slow load
- **WHEN** a screen's data is still loading once the grace period has passed
- **THEN** the loading indicator fades in and stays visible until loading finishes

#### Scenario: Preview without delay
- **WHEN** the indicator is created with a reveal delay of zero
- **THEN** it is visible from the first frame
