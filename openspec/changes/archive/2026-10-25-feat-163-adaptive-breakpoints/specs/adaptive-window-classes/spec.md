# Spec Delta

## Purpose

Defines how window width maps to the app's layout tiers and how the layout moves between them.

## ADDED Requirements

### Requirement: Official width classes decide every layout tier
The app SHALL classify window width with the Material 3 / WindowManager width classes (Compact < 600 dp ≤ Medium < 840 dp ≤ Expanded < 1200 dp ≤ Large < 1600 dp ≤ Extra-large). The floating rail SHALL replace the bottom bar from Medium up, and editors SHALL dock beside content from Large up, both derived from the same classification.

#### Scenario: Large desktop window
- **WHEN** the window is 1280 dp wide
- **THEN** navigation is a floating rail and editors dock beside the content

#### Scenario: Extra-large display
- **WHEN** the window is 1920 dp wide
- **THEN** it behaves as Large

#### Scenario: Boundary
- **WHEN** the window is 1199 dp wide
- **THEN** editors open as bottom sheets, and the rail is shown

### Requirement: Layout changes glide
When the rail clearance of screen content changes, because the window crosses a breakpoint or an editor docks beside the list, the content SHALL move to its new position with an animation, not in a single frame.

#### Scenario: Window resized across Medium
- **WHEN** a desktop window is widened from 500 dp to 700 dp
- **THEN** the rail appears and the content slides to clear it
