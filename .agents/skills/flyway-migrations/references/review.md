# Reviewing Flyway Migrations

## Review Checklist

Run through each item when reviewing an existing migration.

### Naming & Ordering
- [ ] Follows `V{n}__{description}.sql` convention
- [ ] Version numbers are sequential and non-conflicting
- [ ] Description is meaningful and snake_case
- [ ] File has never been renamed or modified after application

### Safety
- [ ] No DROP without prior deprecation migration
- [ ] No destructive column renames in a single step
- [ ] No unsafe data type changes (e.g., VARCHAR → INT without validation)
- [ ] No full-table rewrites on large tables without zero-downtime strategy
- [ ] No blanket UPDATE without a WHERE clause

### Correctness
- [ ] SQL is deterministic (no non-deterministic functions in data migrations)
- [ ] Constraints have explicit names (not auto-generated)
- [ ] Foreign keys have explicit ON DELETE / ON UPDATE behavior
- [ ] Indexes are named consistently (`idx_{table}_{column}`)

### Locking & Performance
- [ ] No long-running operations inside a transaction on large tables
- [ ] Indexes on large tables use CONCURRENTLY where supported
- [ ] Batch updates used for large data migrations
- [ ] No implicit lock escalation risk

### Configuration Risk
- [ ] No `flyway.cleanDisabled=false` in production context
- [ ] No `spring.jpa.hibernate.ddl-auto=update` in production context
- [ ] `validateOnMigrate=true` is set

---

## Common Anti-Patterns

### ❌ Unsafe: Immediate column drop
```sql
-- Dangerous: drops column before app stops using it
ALTER TABLE users DROP COLUMN legacy_field;
```
**Fix:** Use expand-and-contract. Deprecate first, drop in a later release.

---

### ❌ Unsafe: Blanket UPDATE without condition
```sql
UPDATE orders SET status = 'PENDING';
```
**Fix:**
```sql
UPDATE orders SET status = 'PENDING' WHERE status IS NULL;
```

---

### ❌ Unsafe: Adding NOT NULL without backfill
```sql
ALTER TABLE users ADD COLUMN verified BOOLEAN NOT NULL DEFAULT FALSE;
```
**Issue:** On some engines this triggers a full table rewrite.  
**Fix:** Add nullable → backfill → add constraint (3-step pattern in [writing.md](writing.md)).

---

### ❌ Unsafe: Renaming column in one step
```sql
ALTER TABLE customers RENAME COLUMN full_name TO customer_name;
```
**Issue:** Breaks existing app code immediately.  
**Fix:** Add new column → sync → migrate app → drop old column across multiple releases.

---

### ❌ Risk: Index without CONCURRENTLY on PostgreSQL
```sql
CREATE INDEX idx_orders_user_id ON orders(user_id);
```
**Issue:** Locks table during index creation.  
**Fix:**
```sql
-- Must be outside a transaction block
CREATE INDEX CONCURRENTLY idx_orders_user_id ON orders(user_id);
```
Note: Flyway wraps migrations in transactions by default — use `@NonTransactional` annotation or a separate migration file for CONCURRENTLY indexes on PostgreSQL.

---

## Severity Levels

When reporting review findings, use:

| Severity | Meaning |
|----------|---------|
| 🔴 BLOCKER | Will cause data loss, downtime, or deployment failure |
| 🟠 HIGH | Likely to cause production issues under load |
| 🟡 MEDIUM | Bad practice, may cause problems at scale |
| 🔵 LOW | Style or convention issue, low risk |
