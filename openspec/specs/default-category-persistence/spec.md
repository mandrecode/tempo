# default-category-persistence Specification

## Purpose
Define how the default ("Inbox") category is seeded and how its name persists, so the default category is created exactly once and a user's rename is never silently overwritten by the localized default name on later launches or locale changes.
## Requirements
### Requirement: Default category is seeded once
The system SHALL create the default category with the current initial localized name only when the default category row is first seeded.

#### Scenario: Fresh database creates default category
- **WHEN** the app creates a new database and seeds the default category
- **THEN** the stored default category name uses the current `category_inbox` string value

### Requirement: Default category name is user-owned after creation
The system SHALL persist user edits to the default category name and SHALL NOT overwrite that name during later app starts or database opens.

#### Scenario: Renamed default category survives restart
- **WHEN** a user renames the default category and the app database is opened again
- **THEN** the stored default category name remains the user-provided name

#### Scenario: Startup does not re-localize existing default category
- **WHEN** an existing default category row is opened after creation
- **THEN** the system does not replace its name with the current localized initial name

### Requirement: Default category identity is independent of display name
The system SHALL identify default category behavior using persisted identity/default metadata rather than the category name text.

#### Scenario: Renamed category remains default
- **WHEN** the default category has a custom user-provided name
- **THEN** the category remains the default category according to its persisted default metadata

