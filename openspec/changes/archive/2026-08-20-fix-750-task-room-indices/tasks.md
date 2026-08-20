## 1. Entity Indices

- [x] 1.1 Add `@Index("categoryId")` and `@Index("parentTaskId")` to `TaskEntity` via the `@Entity(indices = [...])` parameter, following the `HabitChainMemberEntity` pattern.

## 2. Database Migration

- [x] 2.1 Bump the Room database `version` from 8 to 9 in `TempoDatabase`.
- [x] 2.2 Add `MIGRATION_8_9` that issues `CREATE INDEX IF NOT EXISTS` for `index_tasks_categoryId` and `index_tasks_parentTaskId`.
- [x] 2.3 Register `MIGRATION_8_9` in the `MIGRATIONS` array and the `addMigrations(...)` builder call.

## 3. Schema Export

- [x] 3.1 Run `./gradlew kspDebugKotlin` and commit the regenerated `app/schemas/9.json`.
- [x] 3.2 Confirm `9.json` lists the two task indices and no task foreign keys.

## 4. Verification

- [x] 4.1 Add an in-memory Room migration test for v8→v9 in `MigrationTest`.
- [x] 4.2 Run `openspec validate fix-750-task-room-indices`.
- [x] 4.3 Run `./gradlew ktlintFormat`, `./gradlew :app:detekt`, `./gradlew ktlintCheck`.
- [x] 4.4 Run `./gradlew testDebugUnitTest` and `./gradlew koverVerifyDebug`.
