# Writing Flyway Migrations

## Output Checklist

Every generated migration must include:
- [ ] Correct filename (`V{n}__{description}.sql`)
- [ ] SQL comment with filename at top
- [ ] Comments explaining non-obvious operations
- [ ] Deployment considerations note
- [ ] Rollback guidance (or explicit "forward-fix only" note)

When relevant, also include:
- Pre-deployment validation queries
- Post-deployment verification queries
- Index strategy justification
- Batching recommendation for large tables
- Zero-downtime alternative (link to [production.md](production.md))

---

## Templates

### Create Table

```sql
-- V1__create_users_table.sql

CREATE TABLE users (
    id          BIGSERIAL       PRIMARY KEY,
    email       VARCHAR(255)    NOT NULL UNIQUE,
    full_name   VARCHAR(255)    NOT NULL,
    status      VARCHAR(50)     NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
```

### Add Column (safe 3-step pattern)

```sql
-- V2__add_status_to_users.sql

-- Step 1: add nullable first to avoid full table rewrite
ALTER TABLE users ADD COLUMN status VARCHAR(50);

-- Step 2: backfill existing rows
UPDATE users
SET status = 'ACTIVE'
WHERE status IS NULL;

-- Step 3: enforce NOT NULL after backfill
ALTER TABLE users ALTER COLUMN status SET NOT NULL;
```

> ⚠️ On large tables, Step 2 should be batched. See [production.md](production.md).

### Add Index

```sql
-- V3__add_orders_created_at_index.sql

-- NOTE: On PostgreSQL, prefer CONCURRENTLY for large tables (cannot run inside a transaction)
CREATE INDEX idx_orders_created_at ON orders(created_at);
```

### Add Foreign Key

```sql
-- V4__add_orders_user_fk.sql

ALTER TABLE orders
ADD CONSTRAINT fk_orders_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE RESTRICT;
```

### Rename Column (transition strategy)

```sql
-- V5__add_customer_name_alias.sql

-- Phase 1: add new column and sync data (app must write to both)
ALTER TABLE customers ADD COLUMN customer_name VARCHAR(255);

UPDATE customers
SET customer_name = full_name
WHERE customer_name IS NULL;
```

```sql
-- V6__drop_old_full_name_column.sql
-- Run only after app is fully migrated to customer_name

ALTER TABLE customers DROP COLUMN full_name;
```

### Seed / Reference Data

```sql
-- V7__seed_roles.sql

INSERT INTO roles (name, description)
VALUES
    ('ADMIN',  'Full system access'),
    ('USER',   'Standard access'),
    ('VIEWER', 'Read-only access')
ON CONFLICT (name) DO NOTHING;
```

### Create View

```sql
-- R__active_users_view.sql
-- Repeatable migration — re-runs if content changes

CREATE OR REPLACE VIEW active_users AS
SELECT id, email, full_name
FROM users
WHERE status = 'ACTIVE';
```

---

## Design Rules

**Prefer:**
- Atomic, single-concern migrations
- Deterministic SQL (no random, no NOW() in data migrations)
- Environment-independent SQL (no hardcoded env values)
- Conditional inserts (`ON CONFLICT DO NOTHING`, `WHERE NOT EXISTS`)

**Avoid:**
- Multiple unrelated changes in one file
- ORM-generated SQL without manual review
- Implicit assumptions about existing data distribution
- Dropping columns in the same migration that adds the replacement
