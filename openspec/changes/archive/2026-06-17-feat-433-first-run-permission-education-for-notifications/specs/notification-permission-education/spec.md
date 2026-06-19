## ADDED Requirements

### Requirement: Reminder permission education precedes notification prompt
The system SHALL explain notification reminders in product terms before launching the Android notification permission prompt when notification permission is missing and a user tries to create a reminder.

This education SHALL be contextual to each reminder attempt while notification permission remains missing, not persisted as one-time app onboarding.

#### Scenario: Missing notification permission before creating reminder
- **WHEN** a user selects a reminder action and Android notification permission is not granted
- **THEN** Tempo presents an education message explaining that reminders notify users at selected task and routine times before requesting the system permission

#### Scenario: Notification permission already granted
- **WHEN** a user selects a reminder action and Android notification permission is already granted
- **THEN** Tempo skips notification education and proceeds to any remaining reminder permission checks

#### Scenario: Notification permission becomes granted while education is visible
- **WHEN** the education message is visible and the app resumes after notification permission became granted elsewhere
- **THEN** Tempo closes the education message and proceeds to any remaining reminder permission checks

### Requirement: Users can continue or defer without pressure
The system SHALL provide explicit actions to continue to the Android permission prompt or defer reminder permission education without opening the system prompt.

#### Scenario: User continues from education
- **WHEN** the user chooses to enable reminders from the education message
- **THEN** Tempo launches the Android notification permission prompt

#### Scenario: User defers from education
- **WHEN** the user chooses not now or dismisses the education message
- **THEN** Tempo closes the permission flow without opening the Android notification permission prompt and without opening the reminder picker

### Requirement: Denied and settings flows remain explicit
The system SHALL handle denied notification permission and permanently disabled notification permission with clear next steps.

#### Scenario: User denies notification permission
- **WHEN** the Android notification permission prompt returns denied and notification permission can still be requested later
- **THEN** Tempo explains that reminders need notifications to alert the user and allows the user to dismiss the flow

#### Scenario: User permanently disables notification permission
- **WHEN** notification permission is denied in a state that requires app settings to change it
- **THEN** Tempo explains that reminders need notifications and offers a path to the app notification settings screen

### Requirement: Reminder re-enable path remains available from settings
The system SHALL retain a path from Tempo settings to Android notification settings so users can re-enable reminders after changing their mind.

#### Scenario: User opens notification settings from Tempo settings
- **WHEN** the user activates the notifications item in Tempo settings
- **THEN** Tempo opens the app notification settings screen when supported, or the app details settings fallback otherwise
