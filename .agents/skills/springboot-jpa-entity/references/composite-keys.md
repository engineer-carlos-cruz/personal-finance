# Composite Primary Keys in JPA

Reference for tables with multi-column primary keys.
Load this file when the DDL defines a composite PK (`PRIMARY KEY (col1, col2, ...)`) or
when the user asks for `@EmbeddedId` / `@IdClass` patterns.

---

## Table of Contents

1. [Choosing @EmbeddedId vs @IdClass](#1-choosing-embeddedid-vs-idclass)
2. [@EmbeddedId (recommended)](#2-embeddedid)
3. [@IdClass (legacy / simple)](#3-idclass)
4. [@MapsId with @EmbeddedId (junction tables)](#4-mapsid-with-embeddedid)
5. [Checklist](#5-checklist)

---

## 1. Choosing @EmbeddedId vs @IdClass

| Criterion | `@EmbeddedId` | `@IdClass` |
|---|---|---|
| JPA standard compliance | ✅ Yes | ✅ Yes |
| OOP encapsulation | ✅ Strong — PK is a first-class object | ⚠️ Weaker — PK fields duplicated in entity |
| JPQL access | `WHERE e.id.col1 = :v` | `WHERE e.col1 = :v` |
| Works with `@MapsId` | ✅ Yes (required for junction entities) | ⚠️ More complex |
| Recommended default | ✅ **Prefer this** | Only for legacy / simple cases |

---

## 2. @EmbeddedId

### The Embeddable Key class

```java
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode          // Required — JPA uses equals/hashCode for identity checks
public class StudentCourseId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Column names must match the actual FK columns
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;
}
```

**Rules for the Embeddable:**
- Must implement `Serializable`.
- Must override `equals()` and `hashCode()` — use `@EqualsAndHashCode` from Lombok.
- No `@Id` annotation inside the embeddable.
- No relationships (`@ManyToOne`) inside the embeddable — those go on the entity.

### The Entity using @EmbeddedId

```java
@Entity
@Table(name = "student_course")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentCourse {

    @EmbeddedId
    private StudentCourseId id;

    // Relationships mapped back to the PK columns
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("studentId")                          // refers to field name inside StudentCourseId
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;

    // Extra payload columns
    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    @Column(name = "grade", precision = 4, scale = 2)
    private BigDecimal grade;
}
```

### Creating and persisting a junction entity

```java
// Build the composite key
StudentCourseId key = new StudentCourseId(student.getId(), course.getId());

// Build the entity
StudentCourse enrollment = StudentCourse.builder()
    .id(key)
    .student(student)
    .course(course)
    .enrolledAt(LocalDateTime.now())
    .build();

repository.save(enrollment);
```

### Finding by composite key

```java
// Spring Data JPA repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, StudentCourseId> {
    List<StudentCourse> findByStudent(Student student);
    List<StudentCourse> findByCourse(Course course);
}

// Usage
StudentCourseId key = new StudentCourseId(1L, 42L);
Optional<StudentCourse> enrollment = repository.findById(key);
```

---

## 3. @IdClass (Legacy / Simple)

Use only when integrating with a legacy schema or when the team strongly prefers it.

### The IdClass

```java
import java.io.Serializable;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class StudentCourseId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long studentId;   // must match field names in the entity exactly
    private Long courseId;
}
```

### The Entity

```java
@Entity
@Table(name = "student_course")
@IdClass(StudentCourseId.class)
@Getter @Setter @NoArgsConstructor
public class StudentCourse {

    @Id
    @Column(name = "student_id")
    private Long studentId;

    @Id
    @Column(name = "course_id")
    private Long courseId;

    // Relationships must use insertable = false, updatable = false
    // to avoid mapping conflicts with @Id columns
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private Course course;
}
```

---

## 4. @MapsId with @EmbeddedId (Junction Tables — Best Practice)

When a junction entity has both `@EmbeddedId` and `@ManyToOne` references to the same FK columns,
use `@MapsId` to avoid duplicate column mapping:

- `@MapsId("studentId")` tells JPA that the `student` relationship's FK also populates `id.studentId`.
- The `@JoinColumn` on the relationship still names the actual DB column.
- You do **not** add `@Column` inside the embeddable for those fields — JPA derives them from `@MapsId`.

This is the cleanest pattern and avoids the `insertable = false, updatable = false` workaround
required by `@IdClass`.

---

## 5. Checklist

Before finalizing an entity with a composite PK:

- [ ] The embeddable class implements `Serializable`.
- [ ] `equals()` and `hashCode()` are overridden in the embeddable (Lombok `@EqualsAndHashCode`).
- [ ] No `@Id` annotation inside `@Embeddable`.
- [ ] The entity uses `@EmbeddedId` (not `@Id` on multiple fields, unless `@IdClass`).
- [ ] Each FK relationship uses `@MapsId("fieldName")` matching the embeddable field name.
- [ ] The repository type parameter is `JpaRepository<Entity, EmbeddableKeyClass>`.
- [ ] The Spring Data JPA repository can find by the composite key using `findById(new KeyClass(...))`.
