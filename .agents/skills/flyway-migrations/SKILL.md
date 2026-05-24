---
name: flyway-migrations
description: >
  Use this skill for any task involving Flyway database migrations — creating,
  reviewing, validating, or fixing them. Trigger when the user mentions: flyway,
  migration, schema change, alter table, add column, create table, database
  versioning, DDL, data migration, seed data, Spring Boot Flyway, CI/CD schema,
  rollback migration, or anything related to evolving a database schema safely.
  Always use this skill when a migration file needs to be written or reviewed,
  even if the request seems simple.
license: MIT
---

# flyway-migrations

## Quick Reference

| Task | Read |
|------|------|
| Create or write a migration | [writing.md](references/writing.md) |
| Review an existing migration | [review.md](references/review.md) |
| Zero-downtime / production safety | [production.md](references/production.md) |
| Rollback, repair, baseline | [operations.md](references/operations.md) |
| Spring Boot or CI/CD setup | [integration.md](references/integration.md) |

---

## Naming Conventions (always apply)

```
V1__initial_schema.sql
V2__create_users_table.sql
V3__add_email_index.sql
R__refresh_reporting_views.sql
```

Rules:
- Uppercase `V` for versioned, `R` for repeatable
- Double underscore `__` between version and description
- `snake_case` descriptions, deterministic and descriptive
- Never rename or modify an already-applied migration

---

## Core Principles (always apply)

- Forward-only migrations — treat applied migrations as immutable
- Additive changes first: create → backfill → migrate app → drop later
- Small, isolated, reviewable migrations — one concern per file
- Production safety over convenience — warn about locks, rewrites, downtime
- Explicit SQL over ORM-generated changes

---

## Clarify Before Generating

Ask if any of these are unknown:
- Database engine (PostgreSQL, MySQL, MariaDB, SQL Server, Oracle)
- Rollback expectations
- Deployment strategy (blue/green, rolling, direct)
- Backward compatibility requirements
- Estimated table size (impacts index and ALTER strategies)
