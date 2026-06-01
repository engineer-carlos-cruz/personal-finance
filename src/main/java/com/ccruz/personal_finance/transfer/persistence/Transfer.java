package com.ccruz.personal_finance.transfer.persistence;

import com.ccruz.personal_finance.account.persistence.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "transfers",
    indexes = {
        @Index(name = "idx_transfers_source_account", columnList = "source_account_id"),
        @Index(name = "idx_transfers_target_account", columnList = "target_account_id")
    }
)
@Check(constraints = "source_account_id <> target_account_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "source_account_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_transfers_source_account")
    )
    private Account sourceAccount;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "target_account_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_transfers_target_account")
    )
    private Account targetAccount;

    @NotNull
    @Digits(integer = 17, fraction = 2)
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;
}
