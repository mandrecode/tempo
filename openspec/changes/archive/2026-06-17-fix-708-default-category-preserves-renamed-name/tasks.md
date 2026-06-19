## 1. Database Behavior

- [x] 1.1 Remove startup/open-time renaming of the existing default category row.
- [x] 1.2 Keep fresh database seeding of the default category with the current localized initial name.
- [x] 1.3 Update callback comments to describe create-only seeding and user-owned persistence.

## 2. Regression Coverage

- [x] 2.1 Add focused regression coverage that opening the database does not rename an existing default category.
- [x] 2.2 Ensure the regression covers a renamed default category remaining marked as default.

## 3. Verification

- [x] 3.1 Run `openspec validate fix-708-default-category-preserves-renamed-name`.
- [x] 3.2 Run `./gradlew testDebugUnitTest`.
