# AGENTS

## Project shape
- Single-module Gradle app (`settings.gradle` only sets `rootProject.name = 'personal-finance'`).
- Spring Boot entrypoint: `src/main/java/com/ccruz/personal_finance/PersonalFinanceApplication.java`.
- Java package path uses an underscore: `com.ccruz.personal_finance`.
- Current domain slice is `account` with controller/service/repository/entity under `src/main/java/com/ccruz/personal_finance/account`.

## Toolchain and runtime facts
- Requires Java toolchain 25 (`build.gradle` sets `JavaLanguageVersion.of(25)`).
- Use Gradle wrapper (`./gradlew`), not a system Gradle.
- Spring Boot version is `4.1.0`; Gradle wrapper is `9.4.1`.

## Profiles and database behavior
- Default profile is `dev` (`src/main/resources/application.yaml`), so app startup and unqualified tests hit local Postgres `personal_finance_dev`.
- `dev` profile: `ddl-auto: update`, Flyway enabled, SQL init `always`.
- `test` profile: local Postgres `personal_finance_test`, `ddl-auto: create-drop`, Flyway enabled; test DB creds are hardcoded in `application-test.yaml`.
- `prod` profile: `ddl-auto: validate`, SQL init `never`, DB config from `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` with local defaults.
- Flyway migrations live in `src/main/resources/db/migration` (currently `V1__create_accounts_table.sql`).

## Commands you will likely need
- Run app locally: `./gradlew bootRun`.
- Run tests with explicit test profile: `./gradlew test -Dspring.profiles.active=test`.
- Run one test class/method: `./gradlew test --tests 'com.ccruz.personal_finance.PersonalFinanceApplicationTests.contextLoads' -Dspring.profiles.active=test`.
- Build artifact: `./gradlew build`.

## Current testing caveat
- `PersonalFinanceApplicationTests` does not set `@ActiveProfiles("test")`; if you forget `-Dspring.profiles.active=test`, tests can accidentally use the `dev` datasource.
