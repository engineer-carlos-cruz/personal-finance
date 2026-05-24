# Flyway Integration: Spring Boot & CI/CD

## Spring Boot Configuration

### Recommended production setup

```yaml
# application.yml
spring:
  flyway:
    enabled: true
    validate-on-migrate: true
    baseline-on-migrate: false
    clean-disabled: true
    locations: classpath:db/migration
    schemas: public

  jpa:
    hibernate:
      ddl-auto: validate  # Never use 'update' in production
```

### Why `ddl-auto=validate`

| Setting | Behavior | Production-safe? |
|---------|----------|-----------------|
| `validate` | Validates schema matches entities, no changes | ✅ Yes |
| `none` | Does nothing | ✅ Yes (with Flyway) |
| `update` | Auto-alters schema to match entities | ❌ No |
| `create` | Drops and recreates schema on startup | ❌ No |
| `create-drop` | Creates on start, drops on stop | ❌ No |

### Migration file location

```
src/
└── main/
    └── resources/
        └── db/
            └── migration/
                ├── V1__initial_schema.sql
                ├── V2__add_users_table.sql
                └── R__refresh_views.sql
```

### Multiple datasources

When using multiple datasources, configure Flyway explicitly per datasource:

```java
@Bean
public Flyway flyway(DataSource dataSource) {
    return Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .validateOnMigrate(true)
        .cleanDisabled(true)
        .load();
}
```

Disable Spring Boot's auto-configuration:
```properties
spring.flyway.enabled=false
```

---

## CI/CD Integration

### Pipeline sequencing

```
1. Run unit/integration tests (with test DB)
2. Apply migrations to staging
3. Run smoke/integration tests on staging
4. Apply migrations to production (before app deployment)
5. Deploy new application version
6. Run post-deployment verification queries
```

> Schema migration always goes before app deployment when changes are backward-compatible.  
> App rollback must also be backward-compatible with the new schema.

### Validation step in pipeline

```bash
# Validate migrations without applying them
flyway validate \
  -url=jdbc:postgresql://staging-db:5432/mydb \
  -user=$DB_USER \
  -password=$DB_PASSWORD
```

### Detect schema drift

```bash
# Compare schema against expected state
flyway info \
  -url=jdbc:postgresql://prod-db:5432/mydb \
  -user=$DB_USER \
  -password=$DB_PASSWORD
```

Look for any migration with `Pending` status in production that shouldn't be there.

### Docker / containerized environments

```dockerfile
# Run migrations as a separate init container or pre-deploy job
flyway \
  -url=jdbc:postgresql://${DB_HOST}:5432/${DB_NAME} \
  -user=${DB_USER} \
  -password=${DB_PASSWORD} \
  migrate
```

Prefer running Flyway as a **separate job** before the application starts, not at application startup, in orchestrated environments (Kubernetes, ECS).

---

## Environment-Specific Configuration

Use Flyway placeholders for environment-specific values:

```sql
-- Migration using placeholder
CREATE SCHEMA IF NOT EXISTS ${app_schema};
```

```properties
flyway.placeholders.app_schema=myapp
```

> Avoid hardcoding environment-specific schema names, roles, or tablespaces directly in migration files.

---

## Test Database Strategy

For integration tests, use an isolated test database per run:

```yaml
# application-test.yml
spring:
  flyway:
    clean-disabled: false  # OK for test environments only
    baseline-on-migrate: false
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
```

Or use Testcontainers for production-parity testing:

```java
@Container
static PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb");
```
