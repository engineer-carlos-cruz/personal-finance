# Advanced JPA Mappings

Reference for non-standard entity patterns.
Load this file when the table or user request involves any of:
- Inheritance hierarchies (`@Inheritance`)
- Auditing fields (`created_at`, `updated_at`, `created_by`)
- Soft delete (`deleted_at`, `is_deleted`)
- JSON / JSONB columns
- Custom type converters (`@Converter`)
- Enum mapping
- Generated / computed columns
- Optimistic locking (`@Version`)

---

## Table of Contents

1. [Auditing (@CreatedDate, @LastModifiedDate)](#1-auditing)
2. [Soft Delete (@SQLDelete + @Where)](#2-soft-delete)
3. [Optimistic Locking (@Version)](#3-optimistic-locking)
4. [JSON Columns](#4-json-columns)
5. [Custom AttributeConverter](#5-custom-attributeconverter)
6. [Enum Mapping](#6-enum-mapping)
7. [Inheritance Strategies](#7-inheritance-strategies)
8. [Generated / Computed Columns](#8-generated--computed-columns)

---

## 1. Auditing

Use Spring Data's `@EnableJpaAuditing` (on a `@Configuration` class) + the `Auditable` base class or `@EntityListeners`.

### Enable auditing in config

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {}
```

### Auditable base class (shared across entities)

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class AuditableEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
```

### Entity extending the base class

```java
@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor
public class Product extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false, length = 255)
    private String name;
}
```

If `@CreatedBy` / `@LastModifiedBy` are needed, implement `AuditorAware<String>`:

```java
@Bean
public AuditorAware<String> auditorProvider() {
    return () -> Optional.ofNullable(SecurityContextHolder.getContext())
        .map(ctx -> ctx.getAuthentication())
        .map(auth -> auth.getName());
}
```

---

## 2. Soft Delete

Soft-delete marks a row as deleted instead of removing it.

```java
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")           // Hibernate 5/6; automatically filters queries
// For Hibernate 6.3+:  @FilterDef / @Filter or @SoftDelete annotation
@Getter @Setter @NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // other fields ...
}
```

**Caveats:**
- `@Where` is a Hibernate extension, not standard JPA.
- Hibernate 6.3+ introduced `@SoftDelete` — prefer it for new projects:
  ```java
  @SoftDelete(columnName = "deleted_at", strategy = SoftDeleteType.TIMESTAMP)
  ```
- Native queries bypass `@Where`; add `AND deleted_at IS NULL` manually.
- Cascade deletes will also trigger `@SQLDelete`, so children are soft-deleted too.

---

## 3. Optimistic Locking

Prevents lost-update conflicts when multiple transactions read and write the same row.

```java
@Version
@Column(name = "version", nullable = false)
private Long version;           // or Integer; JPA increments it on every UPDATE
```

- JPA throws `OptimisticLockException` (or Spring's `ObjectOptimisticLockingFailureException`)
  if the `version` value has changed between read and write.
- Add a `version` column to the DDL: `version BIGINT NOT NULL DEFAULT 0`.
- Expose the field via getter but not setter (set via `@Builder.Default`).

---

## 4. JSON Columns

### PostgreSQL JSONB / MySQL JSON

Option A — store as `String` (simple, no typing):

```java
@Column(name = "metadata", columnDefinition = "jsonb")
private String metadata;    // caller serializes/deserializes manually
```

Option B — store as typed object with a converter (recommended):

```java
@Convert(converter = JsonMetadataConverter.class)
@Column(name = "metadata", columnDefinition = "jsonb")
private Map<String, Object> metadata;
```

See section 5 for the converter implementation.

Option C — Hibernate's `@JdbcTypeCode(SqlTypes.JSON)` (Hibernate 6+):

```java
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "metadata")
private Map<String, Object> metadata;
```

---

## 5. Custom AttributeConverter

Use `@Converter` when you need to transform a value between Java and the database representation.

### Example: Map<String, Object> ↔ JSON string

```java
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonMetadataConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialize metadata to JSON", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return MAPPER.readValue(dbData, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not deserialize metadata from JSON", e);
        }
    }
}
```

Apply it:

```java
@Convert(converter = JsonMetadataConverter.class)
@Column(name = "metadata", columnDefinition = "TEXT")
private Map<String, Object> metadata;
```

Or register globally with `@Converter(autoApply = true)` so it activates for all `Map<String, Object>` fields automatically.

---

## 6. Enum Mapping

### Basic mapping (recommended: STRING)

```java
// DDL: status VARCHAR(20) NOT NULL
public enum OrderStatus { PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED }

@Enumerated(EnumType.STRING)            // stores "PENDING", not 0
@Column(name = "status", nullable = false, length = 20)
private OrderStatus status;
```

**Never use `EnumType.ORDINAL`** — reordering the enum values silently corrupts data.

### DB ENUM type (MySQL / PostgreSQL)

```java
// PostgreSQL: status order_status NOT NULL
@Column(name = "status", columnDefinition = "order_status")
@Enumerated(EnumType.STRING)
private OrderStatus status;
```

### Converter for custom DB values

If the DB stores `'A'`, `'C'`, `'S'` but your enum is `ACTIVE`, `CLOSED`, `SUSPENDED`:

```java
public enum AccountStatus {
    ACTIVE("A"), CLOSED("C"), SUSPENDED("S");

    private final String code;
    AccountStatus(String code) { this.code = code; }
    public String getCode() { return code; }

    public static AccountStatus fromCode(String code) {
        return Arrays.stream(values())
            .filter(s -> s.code.equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown code: " + code));
    }
}

@Converter(autoApply = true)
public class AccountStatusConverter implements AttributeConverter<AccountStatus, String> {
    public String convertToDatabaseColumn(AccountStatus attr) { return attr == null ? null : attr.getCode(); }
    public AccountStatus convertToEntityAttribute(String db) { return db == null ? null : AccountStatus.fromCode(db); }
}
```

---

## 7. Inheritance Strategies

Use when multiple entities share columns and a discriminator.

| Strategy | DDL layout | When to use |
|---|---|---|
| `SINGLE_TABLE` | All subclasses in one table | Simple, best performance, nullable columns acceptable |
| `JOINED` | Parent table + one table per subclass | Clean schema, more JOINs |
| `TABLE_PER_CLASS` | Complete table per subclass | Avoid — poor polymorphic query performance |

### SINGLE_TABLE example

```java
// DDL: vehicle(id, type CHAR(1), make, model, max_payload, max_passengers)
@Entity
@Table(name = "vehicle")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.CHAR)
@Getter @Setter @NoArgsConstructor
public abstract class Vehicle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "make") private String make;
    @Column(name = "model") private String model;
}

@Entity
@DiscriminatorValue("T")
public class Truck extends Vehicle {
    @Column(name = "max_payload") private BigDecimal maxPayload;
}

@Entity
@DiscriminatorValue("B")
public class Bus extends Vehicle {
    @Column(name = "max_passengers") private Integer maxPassengers;
}
```

### JOINED example

```java
@Entity
@Table(name = "vehicle")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Vehicle { /* shared fields */ }

@Entity
@Table(name = "truck")
@PrimaryKeyJoinColumn(name = "vehicle_id")
public class Truck extends Vehicle { /* truck-specific fields */ }
```

---

## 8. Generated / Computed Columns

### Database-generated columns (read-only in JPA)

```java
// DDL: full_name VARCHAR(200) GENERATED ALWAYS AS (CONCAT(first_name, ' ', last_name))
@Column(name = "full_name", insertable = false, updatable = false)
private String fullName;
```

### @GeneratedValue with sequence (PostgreSQL)

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
@SequenceGenerator(
    name = "product_seq",
    sequenceName = "product_id_seq",   // must match DDL: CREATE SEQUENCE product_id_seq
    allocationSize = 50                 // batch allocation — tune to your insert rate
)
@Column(name = "id", updatable = false, nullable = false)
private Long id;
```

### UUID primary key

```java
// Strategy 1: DB generates UUID (PostgreSQL gen_random_uuid())
@Id
@GeneratedValue(strategy = GenerationType.UUID)   // JPA 3.1 / Hibernate 6.2+
@Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
private UUID id;

// Strategy 2: Application generates UUID before insert
@Id
@Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
private UUID id;

@PrePersist
private void initId() {
    if (this.id == null) this.id = UUID.randomUUID();
}
```
