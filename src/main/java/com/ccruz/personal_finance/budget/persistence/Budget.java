package com.ccruz.personal_finance.budget.persistence;

import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategory;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "budgets", check = {
    @CheckConstraint(constraint = "state IN ('WITHIN', 'NEAR_LIMIT', 'FULLY_USED', 'OVER', 'NOT_STARTED')")
})
@SQLDelete(sql = "UPDATE budgets SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_budgets_expense_category"))
    private ExpenseCategory expenseCategory;

    @NotNull
    @Digits(integer = 19, fraction = 2)
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "initial_date", nullable = false)
    private LocalDate initialDate;

    @NotNull
    @Column(name = "final_date", nullable = false)
    private LocalDate finalDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'NOT_STARTED'")
    @Column(name = "state", nullable = false, length = 20)
    private BudgetState state;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
