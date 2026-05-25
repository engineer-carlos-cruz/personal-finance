---
name: springboot-jpa-entity
description: >
  Use this skill whenever the user is working on a Spring Boot project and needs to generate
  a JPA entity class from a database table definition. Triggers include: any mention of
  "JPA entity", "generate entity", "table to entity", "Spring Boot entity", "Hibernate entity",
  "entity class from table", "DDL to entity", "SQL table to Java", or when the user provides
  a CREATE TABLE statement and wants a corresponding Java class. Also use when the user asks
  to add JPA annotations, constraints, or relationships to an existing entity. Always use this
  skill when a SQL table definition (DDL) is present in the context — either in the conversation,
  uploaded as a file, or placed in the resources folder — and the user wants a Java entity.
---

# Spring Boot JPA Entity Generator

## Overview

This skill generates production-grade JPA entity classes from SQL table definitions (DDL).
It reads a table definition from `src/main/resources/` (or wherever the user provides it),
then produces a fully annotated Java entity with constraints, indexes, relationships, and
Lombok integration.

## Input: Locating the Table Definition

Before generating anything, **find the table definition**. Check in this order:

1. Directly in the conversation (the user pasted a `CREATE TABLE` statement).
2. An uploaded file — run `view /mnt/user-data/uploads/` to list files, then read the relevant one.
3. The project's resources folder — look for `.sql` files under `src/main/resources/`:
   ```bash
   find src/main/resources -name "*.sql" | head -20
   ```
4. Ask the user to provide the table definition if it cannot be found.

**Always read the full DDL before writing a single line of Java.** Do not guess column types
or constraints — derive everything from what is declared in the table.

---

## Workflow

```
1. Read table definition  →  2. Map to Java  →  3. Handle relationships  →  4. Write entity  →  5. Validate
```

### Step 1 — Parse the Table Definition

Extract from the DDL:

| DDL element | What to derive |
|---|---|
| Table name | Entity class name (PascalCase), `@Table(name = "...")` |
| Column name | Field name (camelCase), `@Column(name = "...")` |
| Data type | Java type — see **Type Mapping** below |
| `NOT NULL` | `nullable = false` in `@Column` |
| `DEFAULT` | Document in Javadoc; use `@ColumnDefault` if Hibernate-specific default needed |
| `UNIQUE` | `unique = true` in `@Column` or `@UniqueConstraint` in `@Table` |
| `PRIMARY KEY` | `@Id` + `@GeneratedValue` strategy |
| `FOREIGN KEY` | Relationship annotation — see `references/relationships.md` |
| `CHECK` constraint | `@Check(constraints = "...")` (Hibernate) or Bean Validation |
| `INDEX` | `@Index` in `@Table(indexes = {...})` |
| Column length | `length` in `@Column` for `VARCHAR`/`CHAR` |
| Precision/scale | `precision` and `scale` in `@Column` for `DECIMAL`/`NUMERIC` |

### Step 2 — Type Mapping

Use this canonical mapping. For anything not listed, apply reasonable judgement and note it.

| SQL type | Java type | JPA/Annotation notes |
|---|---|---|
| `BIGINT` | `Long` | — |
| `INT` / `INTEGER` | `Integer` | — |
| `SMALLINT` | `Short` | — |
| `TINYINT` | `Byte` / `Boolean` | Boolean if used as flag |
| `BOOLEAN` / `BIT(1)` | `Boolean` | — |
| `DECIMAL(p,s)` / `NUMERIC(p,s)` | `BigDecimal` | `precision`, `scale` in `@Column` |
| `FLOAT` / `REAL` | `Float` | — |
| `DOUBLE` | `Double` | — |
| `CHAR(n)` | `String` | `columnDefinition = "CHAR(n)"` |
| `VARCHAR(n)` | `String` | `length = n` |
| `TEXT` / `LONGTEXT` | `String` | `@Lob` or `columnDefinition = "TEXT"` |
| `DATE` | `LocalDate` | — |
| `TIME` | `LocalTime` | — |
| `DATETIME` / `TIMESTAMP` | `LocalDateTime` | add `updatable = false` if `created_at` |
| `TIMESTAMP WITH TIME ZONE` | `OffsetDateTime` | — |
| `BLOB` / `BYTEA` | `byte[]` | `@Lob` |
| `UUID` | `UUID` | `@GeneratedValue(generator = "UUID")` if PK |
| `JSON` / `JSONB` | `String` or custom converter | `columnDefinition = "jsonb"` for PG |
| `ENUM(...)` | Java `enum` | `@Enumerated(EnumType.STRING)` preferred |

### Step 3 — Relationships

If the table has `FOREIGN KEY` constraints, determine the relationship type from context:

- A FK column in this table to another table's PK → `@ManyToOne` (most common) or `@OneToOne`.
- A junction / association table (two+ FKs, no other payload) → `@ManyToMany` (use `@JoinTable`).
- If the other entity is not yet defined, use a stub comment and `@ManyToOne(fetch = FetchType.LAZY)`.

For detailed relationship patterns and bidirectional mapping, read `references/relationships.md`.

### Step 4 — Write the Entity

Apply this class template. Adapt as needed:

```java
package com.example.domain;            // adjust to project package

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "table_name",
    indexes = {
        @Index(name = "idx_table_column", columnList = "column_name")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_table_column", columnNames = {"column_name"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"lazyField"})  // exclude lazy associations
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EntityName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // or SEQUENCE / UUID
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @NotNull
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    // ... remaining fields
}
```

**Mandatory annotations checklist:**

- [ ] `@Entity` on the class
- [ ] `@Table(name = "exact_table_name")` — always explicit
- [ ] `@Id` on the primary key field
- [ ] `@GeneratedValue` with the correct strategy (see **PK Strategies** below)
- [ ] `@Column(name = "exact_column_name")` on every field — never rely on naming convention
- [ ] `nullable = false` for every `NOT NULL` column
- [ ] Bean Validation (`@NotNull`, `@Size`, `@Min`, `@Max`, `@Pattern`) mirroring DB constraints
- [ ] Lombok annotations (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- [ ] `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` with `@EqualsAndHashCode.Include` on `@Id`

### Step 5 — Validate the Output

Before presenting the entity, mentally walk through this checklist:

- [ ] Every column in the DDL has a corresponding field (no omissions).
- [ ] No field uses a type that doesn't match the DDL column (e.g., `int` instead of `Long` for a `BIGINT`).
- [ ] All `NOT NULL` columns have `nullable = false` **and** a Bean Validation annotation.
- [ ] Foreign keys have a relationship annotation; raw `Long` FK columns are avoided.
- [ ] Unique constraints appear either in `@Column(unique = true)` (single column) or `@UniqueConstraint` (composite).
- [ ] Indexes from the DDL are represented in `@Table(indexes = {...})`.
- [ ] Enum columns use `@Enumerated(EnumType.STRING)` unless there is an explicit reason to use `ORDINAL`.
- [ ] Date/time fields use `java.time.*` types (not `java.util.Date` or `java.sql.*`).
- [ ] The class compiles without errors (no missing imports, no Lombok conflicts).

---

## PK Strategies

| Scenario | Strategy | Code |
|---|---|---|
| Auto-increment integer | `IDENTITY` | `@GeneratedValue(strategy = GenerationType.IDENTITY)` |
| Sequence (PostgreSQL, Oracle) | `SEQUENCE` | `@SequenceGenerator` + `SEQUENCE` strategy |
| UUID generated by DB | Custom generator | `@GeneratedValue(generator = "UUID")` + `@GenericGenerator` |
| UUID generated by app | Assign in `@PrePersist` | `if (id == null) id = UUID.randomUUID();` |
| Composite PK | `@EmbeddedId` | See `references/composite-keys.md` |

---

## Project Dependencies

Ensure the project's `pom.xml` (or `build.gradle`) includes:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
</dependency>
```

If the project uses **Spring Boot 2.x** (javax.* namespace), replace `jakarta.persistence.*`
with `javax.persistence.*` and `jakarta.validation.*` with `javax.validation.*`.

---

## Output Format

Deliver:

1. **The entity Java file** — ready to copy into the project, with a header comment indicating
   the expected package path (e.g., `// src/main/java/com/example/domain/OrderItem.java`).
2. **A short summary table** listing every field, its Java type, and the constraints applied.
3. **Notes** on any assumptions made (e.g., relationship fetch type, enum values inferred, etc.).
4. **Optionally**, a matching Spring Data JPA repository interface stub.

---

## Reference Files

Load these on demand — do not load them unless the situation requires it:

| File | Load when... |
|---|---|
| `references/relationships.md` | The table has one or more `FOREIGN KEY` constraints |
| `references/composite-keys.md` | The table has a multi-column primary key or an `@IdClass` / `@EmbeddedId` scenario |
| `references/advanced-mappings.md` | Inheritance (`@Inheritance`), JSON columns, custom converters, soft-delete (`@SQLDelete`/`@Where`), or auditing (`@CreatedDate`, `@LastModifiedDate`) |
