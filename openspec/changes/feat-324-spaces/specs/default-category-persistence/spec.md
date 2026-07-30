## MODIFIED Requirements

### Requirement: Default category is seeded once
The system SHALL create a default category with the current initial localized name for each space, only when that space's default category row is first seeded.

#### Scenario: Fresh database creates default category
- **WHEN** the app creates a new database and seeds the default space and its default category
- **THEN** the stored default category name uses the current `category_inbox` string value

#### Scenario: Creating a space seeds its own default category
- **WHEN** a user creates a new space
- **THEN** exactly one default category is seeded for that space, using the current `category_inbox` string value

#### Scenario: Migration seeds no additional default category
- **WHEN** an installation created before spaces is migrated into the seeded default space
- **THEN** the existing default category becomes that space's default category and no second default category is created

### Requirement: Default category identity is independent of display name
The system SHALL identify default category behavior using persisted identity/default metadata scoped to its space, rather than the category name text or a fixed sentinel identifier.

#### Scenario: Renamed category remains default
- **WHEN** the default category has a custom user-provided name
- **THEN** the category remains the default category according to its persisted default metadata

#### Scenario: Default category is resolved per space
- **WHEN** the system needs the default category while a space is active
- **THEN** it resolves the default category belonging to that space using persisted default metadata

#### Scenario: No fixed identifier is used
- **WHEN** the system resolves or persists a default category
- **THEN** it does not rely on a reserved or negative row identifier to do so

## ADDED Requirements

### Requirement: Every space has exactly one default category
The system SHALL ensure each space has exactly one default category at all times, and SHALL prevent that category from being deleted while its space exists.

#### Scenario: Deleting the default category is not offered
- **WHEN** a user views a space's default category
- **THEN** deleting it is not offered

#### Scenario: A space always resolves a default category
- **WHEN** a task is created without an explicit category while a space is active
- **THEN** it is assigned to that space's default category
