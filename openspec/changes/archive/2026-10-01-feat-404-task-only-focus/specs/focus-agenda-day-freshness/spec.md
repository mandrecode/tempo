## REMOVED Requirements

### Requirement: Habit completion uses the current Focus day
**Reason**: Habits and chains no longer appear in Focus.
**Migration**: Complete routines from Routines; Focus offers task interactions only.

## MODIFIED Requirements

### Requirement: Single active Focus day observation
The system SHALL keep only one active Focus day observation after refreshing an open screen for a new local date.

#### Scenario: Task completion on the new day
- **WHEN** a user toggles task completion after the local date advances
- **THEN** the system renders the current day's task agenda without retaining updates from the previous day's observer
