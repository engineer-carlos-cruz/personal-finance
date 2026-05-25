# JPA Relationship Mappings

Reference for mapping `FOREIGN KEY` constraints and associations between entities.
Load this file when the table definition contains one or more `FOREIGN KEY` clauses.

---

## Table of Contents

1. [Determining the Relationship Type](#1-determining-the-relationship-type)
2. [@ManyToOne (most common FK)](#2-manytoone)
3. [@OneToMany (inverse side)](#3-onetomany)
4. [@OneToOne](#4-onetoone)
5. [@ManyToMany](#5-manytomany)
6. [Fetch Type Guidelines](#6-fetch-type-guidelines)
7. [Cascade Guidelines](#7-cascade-guidelines)
8. [Orphan Removal](#8-orphan-removal)
9. [Bidirectional Sync Helpers](#9-bidirectional-sync-helpers)
10. [When the Referenced Entity Doesn't Exist Yet](#10-stub-pattern)

---

## 1. Determining the Relationship Type

Read the DDL and ask:

| Observation | Likely relationship |
|---|---|
| This table has a FK column to another table's PK | `@ManyToOne` on this side |
| Another table has a FK pointing to this table | `@OneToMany` on this side (if you want navigation) |
| FK column has a `UNIQUE` constraint | `@OneToOne` |
| A junction table has two FKs and almost no other columns | `@ManyToMany` via `@JoinTable` |

---

## 2. @ManyToOne

Use when: this table's FK column references the PK of another table (the most frequent pattern).

```java
// FK column: order_id BIGINT NOT NULL REFERENCES orders(id)
@ManyToOne(fetch = FetchType.LAZY, optional = false)   // optional = false mirrors NOT NULL
@JoinColumn(
    name = "order_id",          // exact FK column name
    nullable = false,
    foreignKey = @ForeignKey(name = "fk_order_item_order")  // match DDL constraint name
)
private Order order;
```

**Rules:**
- Always `fetch = FetchType.LAZY` unless there is a concrete, measured performance reason to use `EAGER`.
- Set `optional = false` when the FK column is `NOT NULL`.
- Always name the `@ForeignKey` to match the DDL, or use a predictable convention (`fk_{child_table}_{parent_table}`).

---

## 3. @OneToMany

Use when: you want a collection on the "one" side of a `@ManyToOne` relationship.
This side does **not** own the FK; the `@ManyToOne` side does.

```java
// On the Order entity, navigating to OrderItem
@OneToMany(
    mappedBy = "order",          // field name on the owning side (@ManyToOne)
    fetch = FetchType.LAZY,
    cascade = CascadeType.ALL,   // only if Order fully owns its items
    orphanRemoval = true         // only if items cannot exist without Order
)
@ToString.Exclude               // Lombok: avoid infinite loop
private List<OrderItem> items = new ArrayList<>();
```

**Rules:**
- `mappedBy` must match the **field name** (not the column name) on the owning entity.
- Never use `@JoinColumn` on the `@OneToMany` side — it belongs on `@ManyToOne`.
- Initialize the collection to `new ArrayList<>()` to avoid NPE.

---

## 4. @OneToOne

Use when: the FK column has a `UNIQUE` constraint, meaning each parent row maps to at most one child row.

### Owning side (has the FK column)

```java
// FK column: user_id BIGINT UNIQUE NOT NULL REFERENCES users(id)
@OneToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(
    name = "user_id",
    nullable = false,
    unique = true,
    foreignKey = @ForeignKey(name = "fk_profile_user")
)
private User user;
```

### Inverse side (no FK column, optional navigation)

```java
// On the User entity
@OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
@ToString.Exclude
private UserProfile profile;
```

---

## 5. @ManyToMany

Use when: a junction/association table exists with exactly two FK columns and little or no extra payload.

### Junction table: `student_course(student_id, course_id)`

```java
// On Student entity — owning side (choose one side arbitrarily)
@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
@JoinTable(
    name = "student_course",
    joinColumns = @JoinColumn(name = "student_id"),
    inverseJoinColumns = @JoinColumn(name = "course_id"),
    foreignKey = @ForeignKey(name = "fk_sc_student"),
    inverseForeignKey = @ForeignKey(name = "fk_sc_course")
)
@ToString.Exclude
private Set<Course> courses = new HashSet<>();

// On Course entity — inverse side
@ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
@ToString.Exclude
private Set<Student> students = new HashSet<>();
```

### Junction table with extra payload

If the junction table has additional columns (e.g., `enrolled_at`, `grade`), model it as a
**dedicated entity** with two `@ManyToOne` fields and a composite or surrogate PK.
See `composite-keys.md` for the `@EmbeddedId` pattern.

```java
@Entity
@Table(name = "student_course")
public class StudentCourse {

    @EmbeddedId
    private StudentCourseId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;
}
```

---

## 6. Fetch Type Guidelines

| Relationship | Default | Recommended | Rationale |
|---|---|---|---|
| `@ManyToOne` | `EAGER` | **`LAZY`** | Prevents N+1; join explicitly with JPQL when needed |
| `@OneToOne` (owning) | `EAGER` | **`LAZY`** | Same as above |
| `@OneToMany` | `LAZY` | `LAZY` (keep) | Already lazy by default |
| `@ManyToMany` | `LAZY` | `LAZY` (keep) | Already lazy by default |

Override to `EAGER` only after profiling confirms it is faster in your specific access pattern.

---

## 7. Cascade Guidelines

| Cascade type | Use when |
|---|---|
| `CascadeType.ALL` | Parent fully owns child lifecycle (e.g., `Order` → `OrderItem`) |
| `CascadeType.PERSIST, MERGE` | Parent and child are independent but co-saved sometimes (e.g., `Student` ↔ `Course`) |
| No cascade | Entities are managed independently (most `@ManyToOne` cases) |

**Never** use `CascadeType.REMOVE` on `@ManyToMany` — it will delete the referenced entity, not just the junction row.

---

## 8. Orphan Removal

Set `orphanRemoval = true` only when:

- The child cannot exist without the parent (e.g., `OrderItem` without `Order`).
- The relationship is `@OneToMany` or `@OneToOne`.
- You want deleting a child from the parent's collection to also delete the row.

```java
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrderItem> items = new ArrayList<>();
```

---

## 9. Bidirectional Sync Helpers

For bidirectional relationships, add sync methods to the owning entity to keep both sides consistent in memory:

```java
// On Order entity
public void addItem(OrderItem item) {
    items.add(item);
    item.setOrder(this);
}

public void removeItem(OrderItem item) {
    items.remove(item);
    item.setOrder(null);
}
```

---

## 10. Stub Pattern

When the referenced entity class does not exist yet in the project, generate a minimal stub so the entity compiles:

```java
// TODO: Replace with full entity once defined
@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Fields to be added
}
```

Add a comment on the field using the stub:

```java
// References Order entity (stub — full entity pending)
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "order_id", nullable = false)
private Order order;
```
