# Production-Safe Migrations & Zero-Downtime

## Expand-and-Contract Pattern

The safest way to evolve a schema without downtime. Three phases across multiple deployments:

```
Phase 1 — Expand:   Add new structure, keep old. App writes to both.
Phase 2 — Migrate:  Backfill data. Deploy app reading from new structure.
Phase 3 — Contract: Remove old structure once app no longer uses it.
```

Never combine Phase 1 and Phase 3 in the same migration or release.

---

## Batching Large Data Updates

Never update millions of rows in a single statement — it locks the table and may time out.

```sql
-- V8__backfill_user_status.sql
-- Batch size: 10,000 rows per iteration

DO $$
DECLARE
    batch_size INT := 10000;
    updated    INT;
BEGIN
    LOOP
        UPDATE users
        SET status = 'ACTIVE'
        WHERE status IS NULL
        LIMIT batch_size;

        GET DIAGNOSTICS updated = ROW_COUNT;
        EXIT WHEN updated = 0;

        PERFORM pg_sleep(0.1); -- brief pause between batches
    END LOOP;
END $$;
```

> Adjust batch size based on row width and index contention. Test on staging first.

---

## PostgreSQL: Online Index Creation

```sql
-- Must run OUTSIDE a Flyway transaction
-- Use flyway:nonTransactional annotation or separate migration

CREATE INDEX CONCURRENTLY idx_users_email ON users(email);
```

In Spring Boot / Flyway config, mark the migration file:
```sql
-- flyway:nonTransactional
CREATE INDEX CONCURRENTLY idx_users_email ON users(email);
```

Or configure per-migration in Java:
```java
@Override
public boolean canExecuteInTransaction() { return false; }
```

---

## Engine-Specific Lock Warnings

| Operation | PostgreSQL | MySQL / MariaDB | SQL Server |
|-----------|-----------|-----------------|------------|
| ADD COLUMN (nullable) | ✅ No rewrite | ✅ Instant (InnoDB) | ✅ Metadata only |
| ADD COLUMN NOT NULL + DEFAULT | ⚠️ Rewrite (< PG11) / ✅ (PG11+) | ⚠️ Rewrite | ✅ Metadata only |
| CREATE INDEX | 🔴 Table lock | ⚠️ Metadata lock | ⚠️ Online option |
| CREATE INDEX CONCURRENTLY | ✅ No lock | ❌ Not supported | N/A |
| DROP COLUMN | ✅ Metadata only | ⚠️ Rewrite (older) | ✅ Metadata only |
| RENAME COLUMN | ✅ Metadata only | ✅ (MySQL 8+) | ⚠️ Workaround |

Always verify behavior for your specific engine version.

---

## Deployment Sequencing for Zero-Downtime

```
1. Deploy schema migration (backward-compatible — new columns nullable, old columns kept)
2. Deploy application update (reads new schema, writes to both old and new if needed)
3. Verify application stability
4. Deploy cleanup migration (drop deprecated columns/tables)
```

Never deploy app and breaking schema change at the same time.

---

## Pre/Post Verification Queries

Always include verification after critical migrations:

```sql
-- Pre-migration: capture baseline
SELECT COUNT(*) FROM users WHERE status IS NULL;

-- Post-migration: verify backfill
SELECT COUNT(*) FROM users WHERE status IS NULL; -- expect 0
SELECT COUNT(*), status FROM users GROUP BY status;
```

---

## When to Warn Explicitly

Always add a comment and warn the user when a migration:
- Locks a table for more than a few seconds
- Triggers a full table rewrite
- Blocks writes during deployment
- Affects replication lag
- Involves irreversible data deletion
