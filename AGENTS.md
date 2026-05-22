# AGENTS

## Project shape
- Single-module Gradle Spring Boot app (`settings.gradle` only sets `rootProject.name`).
- Main entrypoint: `src/main/java/com/ccruz/personal_finance/PersonalFinanceApplication.java`.
- Package path uses underscore: `com.ccruz.personal_finance` (not `personal-finance`).

## Toolchain and runtime facts
- Requires Java toolchain 25 (`build.gradle` sets `JavaLanguageVersion.of(25)`).
- Use Gradle wrapper (`./gradlew`), not a system Gradle.
- Spring Boot version is `4.0.6`; Gradle wrapper is `9.4.1`.

## Profiles and database behavior
- Default active profile is `dev` (`application.yaml`), so app startup and unqualified tests target `personal_finance_dev` on local Postgres.
- `dev` profile uses `ddl-auto: update` and always runs SQL init.
- `test` profile uses Postgres `personal_finance_test` with `ddl-auto: create-drop`; activate it explicitly when running tests.
- `prod` profile keeps `ddl-auto: validate` and reads DB settings from `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

## Commands you will likely need
- Run app locally: `./gradlew bootRun`.
- Run tests with explicit test profile: `./gradlew test -Dspring.profiles.active=test`.
- Run one test class/method: `./gradlew test --tests 'com.ccruz.personal_finance.PersonalFinanceApplicationTests.contextLoads' -Dspring.profiles.active=test`.
- Build artifact: `./gradlew build`.

## Current testing caveat
- `PersonalFinanceApplicationTests` does not set `@ActiveProfiles("test")`; if you forget `-Dspring.profiles.active=test`, tests can accidentally use the `dev` datasource.
