# Spec Delta

## Purpose

Defines when Tasks and Routines confirm that reminders are active again after notification or exact-alarm permission returns.

## ADDED Requirements

### Requirement: Recovery message answers the screen's own request
Tasks and Routines SHALL show "All set! Reminders are active again." only after that screen has asked for reminder permission, either after a save whose reminder could not be scheduled for lack of permission, or through the revoked-permission dialog shown for existing reminders. Once permission is granted, each request SHALL be answered at most once.

#### Scenario: Permission granted elsewhere
- **WHEN** reminder permission becomes granted while Routines (or Tasks) has not asked for it, for example from the habit sheet's own permission flow, from another tab, or with no reminders affected
- **THEN** that screen records the new permission state without showing the message

#### Scenario: Revoked dialog then settings
- **WHEN** a screen showed the revoked-permission dialog because existing reminders lost permission, and the user grants the permission and returns
- **THEN** that screen shows the message once, no matter how many times it resumes

#### Scenario: Request after save
- **WHEN** saving a reminder triggers a permission request and the user grants it
- **THEN** the screen shows the message once, even though both the request flow and the resume check observe the grant

#### Scenario: Reminders cleared instead
- **WHEN** the user clears every reminder from the revoked-permission dialog and later grants the permission
- **THEN** no message is shown
