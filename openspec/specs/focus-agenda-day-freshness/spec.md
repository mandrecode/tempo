# focus-agenda-day-freshness Specification

## Purpose
Keep the task-only Focus agenda aligned with the current local day while the screen stays open.
## Requirements
### Requirement: Single active Focus day observation
The system SHALL keep only one active Focus day observation after refreshing an open screen for a new local date.

#### Scenario: Task completion on the new day
- **WHEN** a user toggles task completion after the local date advances
- **THEN** the system renders the current day's task agenda without retaining updates from the previous day's observer
