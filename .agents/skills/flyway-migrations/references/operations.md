# Flyway Operations: Rollback, Repair, Baseline

## Rollback Philosophy

Flyway is **forward-only by design**. Rollback migrations are fragile and rarely safe.

Prefer:
1. **Forward-fix migration** — write a new migration that undoes the change
2. **Database backup restore** — for catastrophic failures affecting data integrity
3. **Flyway Teams undo scripts** — only if using the paid tier and the operation is reversible

Only provide rollback SQL when:
- The user explicitly requests it
- The operation is purely structural (no data loss risk)
- You include explicit warnings about irreversibility

---

## Forward-Fix Examples

### Undo an added column
```sql
-- V9__revert_add_status_column.sql
ALTER TABLE users DROP COLUMN status;
```

### Undo an added table
```sql
-- V9__drop_notifications_table.sql
DROP TABLE IF EXISTS notifications;
```

### Undo a data backfill (if original state is known)
```sql
-- V9__revert_status_backfill.sql
UPDATE users SET status = NULL WHERE status = 'ACTIVE';
-- ⚠️ Only safe if no new rows with status have been added
```

---

## Repair

Use `flyway repair` when:
- A migration failed mid-execution and left a broken checksum entry
- A migration file was accidentally modified after being applied
- The `flyway_schema_history` table has inconsistent state

```bash
flyway repair
```

After repair, always re-run `flyway validate` before the next migration.

> ⚠️ Never modify a migration file that has already been applied to production. Use repair only for failed/partial runs.

---

## Baseline

Use `flyway baseline` when:
- Introducing Flyway to an existing database that already has a schema
- The schema history table doesn't exist yet

```bash
flyway baseline -baselineVersion=1 -baselineDescription="existing_schema"
```

Then set in config:
```properties
flyway.baselineOnMigrate=true
```

> After baseline, all migrations with version ≤ baseline version are skipped. Start new migrations from the next version number.

---

## Out-of-Order Migrations

Default behavior rejects migrations applied out of version order.

If needed (e.g., parallel feature branches):
```properties
flyway.outOfOrder=true
```

> ⚠️ Enable only in development/staging. In production, enforce strict ordering.

---

## Validate on Migrate

Always enable in production:
```properties
flyway.validateOnMigrate=true
```

This detects checksum mismatches before migrations run, preventing silent corruption.

---

## Clean — Never in Production

```properties
# Always set this in production
flyway.cleanDisabled=true
```

`flyway clean` drops **all objects** in the configured schemas. It is useful for local dev and CI teardown only. It should be permanently disabled in any environment with real data.

---

## Schema History Table

Flyway tracks applied migrations in:
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

Fields of interest:
| Column | Meaning |
|--------|---------|
| `version` | Migration version number |
| `description` | Migration description |
| `checksum` | Hash of migration file content |
| `success` | Whether the migration completed |
| `installed_on` | Timestamp of application |

A `success = false` row means a partial/failed migration — run `flyway repair` after fixing the issue.
