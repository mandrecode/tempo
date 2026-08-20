## ADDED Requirements

### Requirement: Indexed Task Category and Parent Columns
The `tasks` table SHALL declare database indices on the `categoryId` and `parentTaskId` columns so that the queries filtering on those columns are backed by indices rather than full table scans.

#### Scenario: Task entity declares indices
- **WHEN** the Room schema for the `tasks` table is generated
- **THEN** it contains an index on `categoryId` and an index on `parentTaskId`

#### Scenario: Exported schema reflects task indices
- **WHEN** the version 9 schema is exported to `app/schemas`
- **THEN** the `tasks` table entry lists indices `index_tasks_categoryId` and `index_tasks_parentTaskId`

### Requirement: Task Index Migration Preserves Existing Installs
Tempo SHALL provide a migration from database version 8 to version 9 that creates the task indices on existing installs without altering task data.

#### Scenario: Upgrading from version 8 to version 9
- **WHEN** a database created at version 8 is migrated to version 9
- **THEN** the migration completes validation and the `tasks` table gains the `categoryId` and `parentTaskId` indices

#### Scenario: Task rows are preserved across the migration
- **WHEN** a version 8 database containing task rows is migrated to version 9
- **THEN** the existing task rows remain unchanged after the migration

### Requirement: Indices-Only Change Without Foreign-Key Cascade
The task indexing change SHALL NOT introduce foreign-key constraints or cascade-delete behavior on the `tasks` table.

#### Scenario: No foreign keys added to tasks
- **WHEN** the version 9 schema for the `tasks` table is generated
- **THEN** the table declares no foreign keys on `categoryId` or `parentTaskId`
